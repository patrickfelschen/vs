/* Pub-Sub-Server
 * Getestet unter Ubuntu 20.04 64 Bit / g++ 9.3
 */

#include <fstream>
#include <iostream>
#include <map>
#include <memory>
#include <string>

#include <grpcpp/ext/proto_server_reflection_plugin.h>
#include <grpcpp/grpcpp.h>
#include <grpcpp/health_check_service_interface.h>

// Diese Includes werden generiert.
#include "pub_sub.grpc.pb.h"
#include "pub_sub_config.h"
#include "pub_sub_deliv.grpc.pb.h"

#include "hashing.cpp"

// Notwendige gRPC Klassen.
using grpc::Server;
using grpc::ServerBuilder;
using grpc::ServerContext;

using grpc::Channel;
using grpc::ClientContext;
using grpc::Status;

// Diese Klassen sind in den .proto Dateien definiert.
using pubsub::EmptyMessage;
using pubsub::Message;
using pubsub::PubSubParam;
using pubsub::SessionId;
using pubsub::UserName;

using pubsub::PubSubDelivService;
using pubsub::PubSubService;
using pubsub::ReturnCode;
using pubsub::SubscriberAddress;
using pubsub::Topic;

// Implementierung des Service
class PubSubServiceImpl final : public PubSubService::Service {
    // TODO: Channel topic und Subscribers für diesen Server merken
    std::string topic;
    std::set<std::string> subscribers;
    std::map<std::string, std::string> user_hashes;
    std::map<std::string, std::string> sessions;

    void readUserHashes() {
        std::ifstream hashfile("hashes.txt");

        if (!hashfile.is_open()) {
            std::cout << "Cannot open file" << std::endl;
            return;
        }

        std::string line;
        char user[512];
        char hash[512];
        while (std::getline(hashfile, line)) {
            sscanf(line.c_str(), "%s %s", user, hash);
            user_hashes.insert({user, hash});
        }

        hashfile.close();
    }

    bool checkSession(const PubSubParam *request, ReturnCode *response) {
        std::string hash_string = request->hash_string();
        std::string session_id = std::to_string(request->sid().id());

        std::map<std::string, std::string>::iterator it_session;
        // User zur Session suchen
        it_session = sessions.find(session_id);
        if (it_session == sessions.end()) {
            response->set_value(ReturnCode::SESSION_INVALID);
            return false;
        }
        std::string user = it_session->second;

        std::map<std::string, std::string>::iterator it_user;
        // User-Passwort-Hash zu User suchen
        it_user = user_hashes.find(user);
        if (it_user == user_hashes.end()) {
            response->set_value(ReturnCode::NO_HASH_FOR_SESSION);
            return false;
        }

        std::string data;

        if (request->has_opttopic()) {
            data = request->opttopic().topic();
        } else if (request->has_optmessage()) {
            data = request->optmessage().message();
        } else if (request->has_optaddress()) {
            data = stringify(request->optaddress());
        }

        // Gefundenen Hash mit session_id hashen
        std::string validate_hash = sha256(session_id + data + it_user->second);

        std::cout << "[CHECK_SESSION]: req:" << hash_string << std::endl;
        std::cout << "[CHECK_SESSION]: val:" << validate_hash << std::endl;

        // Hashes vergleichen
        if (hash_string.compare(validate_hash) != 0) {
            response->set_value(ReturnCode::WRONG_HASH_FOR_SESSION);
            return false;
        }

        response->set_value(ReturnCode::OK);
        return true;
    }

    static std::string stringify(const SubscriberAddress &adr) {
        std::string s = adr.ip_address() + ":";
        s += std::to_string(adr.port());
        return s;
    }

    Status get_session(ServerContext *context, const UserName *request, SessionId *response) override {
        std::clock_t session_id = std::clock();
        std::cout << "[GET_SESSEION] name: " << request->name() << std::endl;
        std::cout << "[GET_SESSEION] id: " << session_id << std::endl;

        sessions.insert({std::to_string(session_id), request->name()});
        response->set_id(session_id);

        return Status::OK;
    }

    Status validate(ServerContext *context, const PubSubParam *request, ReturnCode *response) override {
        checkSession(request, response);
        return Status::OK;
    }

    Status invalidate(ServerContext *context, const SessionId *request, ReturnCode *response) override {
        std::string session_id = std::to_string(request->id());
        std::map<std::string, std::string>::iterator it;

        // User zur Session suchen
        it = sessions.find(session_id);
        if (it == sessions.end()) {
            response->set_value(ReturnCode::SESSION_INVALID);
            return Status::OK;
        }

        sessions.erase(session_id);
        return Status::OK;
    }

    Status subscribe(ServerContext *context, const PubSubParam *request, ReturnCode *response) override {
        if (!checkSession(request, response)) {
            return Status::OK;
        }

        std::string receiver = stringify(request->optaddress());
        // TODO: Client registrieren und Info ausgeben

        if (subscribers.find(receiver) == subscribers.end()) {
            std::cout << "[SUBSCRIBE] " << receiver << std::endl;
            subscribers.insert(receiver);
            response->set_value(ReturnCode::OK);
        } else {
            response->set_value(ReturnCode::CLIENT_ALREADY_REGISTERED);
        }

        return Status::OK;
    }

    Status unsubscribe(ServerContext *context, const PubSubParam *request, ReturnCode *response) override {
        if (!checkSession(request, response)) {
            return Status::OK;
        }

        std::string receiver = stringify(request->optaddress());
        // TODO: Client austragen und Info ausgeben

        if (subscribers.find(receiver) != subscribers.end()) {
            std::cout << "[UNSUBSCRIBE] " << receiver << std::endl;
            subscribers.erase(receiver);
            response->set_value(ReturnCode::OK);
        } else {
            response->set_value(ReturnCode::CANNOT_UNREGISTER);
        }

        return Status::OK;
    }

    void handle_status(const std::string operation, Status &status) {
        // Status auswerten -> deliver() gibt keinen Status zurück,k deshalb nur RPC Fehler melden.
        if (!status.ok()) {
            std::cout
                << "[RPC error: "
                << status.error_code()
                << " (" << status.error_message()
                << ")] "
                << operation
                << std::endl;
        }
    }

    Status publish(ServerContext *context, const PubSubParam *request, ReturnCode *response) override {
        if (!checkSession(request, response)) {
            return Status::OK;
        }

        // TODO: Nachricht an alle Subscriber verteilen
        for (std::string subscriber : subscribers) {
            std::shared_ptr<Channel> channel = grpc::CreateChannel(subscriber, grpc::InsecureChannelCredentials());
            stub_ = PubSubDelivService::NewStub(channel);

            ClientContext srv_context;
            Message srv_request_message;
            EmptyMessage srv_response;

            srv_request_message.set_message("<" + topic + "> " + request->optmessage().message());

            std::cout << "[PUBLISH] \"" << request->optmessage().message() << "\" to " << subscriber << std::endl;

            Status status = stub_->deliver(&srv_context, srv_request_message, &srv_response);
            handle_status(subscriber, status);
        }

        response->set_value(ReturnCode::OK);
        return Status::OK;
    }

    Status set_topic(ServerContext *context, const PubSubParam *request, ReturnCode *response) override {
        if (!checkSession(request, response)) {
            return Status::OK;
        }

        // TODO: Topic setzen und Info ausgeben
        if (request->opttopic().passcode() == PASSCODE) {
            topic = request->opttopic().topic();
            response->set_value(ReturnCode::OK);
            std::cout << "[SET_TOPIC] " << topic << std::endl;
        } else {
            std::cout << "[SET_TOPIC] Fehlgeschlagen" << std::endl;
            response->set_value(ReturnCode::CANNOT_SET_TOPIC);
        }
        return Status::OK;
    }

public:
    PubSubServiceImpl() {
        // TODO: Topic initialisieren
        topic = "no topic set";
        readUserHashes();
    }

private:
    std::unique_ptr<PubSubDelivService::Stub> stub_;
};

void RunServer() {
    // Server auf dem lokalen Host starten.
    // std::string server_address(PUBSUB_SERVER_IP);
    std::string server_address("0.0.0.0"); // muss der lokale Rechner sein
    server_address += ":";
    server_address += std::to_string(PUBSUB_SERVER_PORT); // Port könnte umkonfiguriert werden

    PubSubServiceImpl service;

    grpc::EnableDefaultHealthCheckService(true);
    grpc::reflection::InitProtoReflectionServerBuilderPlugin();
    ServerBuilder builder;
    // Server starten ohne Authentifizierung
    builder.AddListeningPort(server_address, grpc::InsecureServerCredentials());
    // Registrierung als synchroner Dienst
    builder.RegisterService(&service);
    // Server starten
    std::unique_ptr<Server> server(builder.BuildAndStart());
    std::cout << "[ Server launched on " << server_address << " ]" << std::endl;

    // Warten auf das Ende Servers. Das muss durch einen anderen Thread
    // ausgeloest werden.  Alternativ kann der ganze Prozess beendet werden.
    server->Wait();
}

int main(int argc, char **argv) {
    // Server starten
    RunServer();
    return 0;
}