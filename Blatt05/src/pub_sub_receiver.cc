/* Pub-Sub-Server
 * Getestet unter Ubuntu 20.04 64 Bit / g++ 9.3
 * @hje
 */

#include <ctime>
#include <fstream>
#include <iostream>
#include <memory>
#include <string>

#include <grpcpp/ext/proto_server_reflection_plugin.h>
#include <grpcpp/grpcpp.h>
#include <grpcpp/health_check_service_interface.h>

// Dieses Include wird generiert.
#include "pub_sub_deliv.grpc.pb.h"

// Notwendige gRPC Klassen.
using grpc::Server;
using grpc::ServerBuilder;
using grpc::ServerContext;
using grpc::Status;

// Diese Klassen sind in den .proto Dateien definiert.
using pubsub::EmptyMessage;
using pubsub::Message;
using pubsub::PubSubDelivService;
using pubsub::PubSubParam;

// Implementierung des Service
class PubSubDelivServiceImpl final : public PubSubDelivService::Service {
    Status deliver(ServerContext *context, const Message *request, EmptyMessage *response) override {

        // TODO: Zeitstempel erzeugen und zusammen mit Nachricht ausgeben.
        std::time_t t = std::time(0);
        std::tm *now = std::localtime(&t);

        std::cout
            << "["
            << (now->tm_year + 1900) << '-'
            << (now->tm_mon + 1) << '-'
            << (now->tm_mday) << ' '
            << (now->tm_hour) << ':'
            << (now->tm_min) << ':'
            << (now->tm_sec)
            << "] "
            << request->message()
            << std::endl;

        return Status::OK;
    }
};

void RunServer() {
    // Server auf dem lokalen Host starten.
    std::string server_address("0.0.0.0:44441");
    PubSubDelivServiceImpl service;

    grpc::EnableDefaultHealthCheckService(true);
    grpc::reflection::InitProtoReflectionServerBuilderPlugin();
    ServerBuilder builder;
    // Server starten ohne Authentifizierung
    builder.AddListeningPort(server_address, grpc::InsecureServerCredentials());
    // Registrierung als synchroner Dienst
    builder.RegisterService(&service);
    // Server starten
    std::unique_ptr<Server> server(builder.BuildAndStart());
    std::cout << "Server gestartet auf " << server_address << std::endl;

    // Warten auf das Ende Servers. Das muss durch einen anderen Thread
    // ausgeloest werden.  Alternativ kann der ganze Prozess beendet werden.
    server->Wait();
}

int main(int argc, char **argv) {
    // Server starten
    RunServer();
    return 0;
}