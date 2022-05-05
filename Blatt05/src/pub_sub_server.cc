/* Pub-Sub-Server  
 * Getestet unter Ubuntu 20.04 64 Bit / g++ 9.3
 */

#include <iostream>
#include <memory>
#include <string>
#include <fstream>
#include <map>

#include <grpcpp/grpcpp.h>
#include <grpcpp/health_check_service_interface.h>
#include <grpcpp/ext/proto_server_reflection_plugin.h>

// Diese Includes werden generiert.
#include "pub_sub.grpc.pb.h"
#include "pub_sub_deliv.grpc.pb.h"
#include "pub_sub_config.h"

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
using pubsub::UserName;
using pubsub::SessionId;

using pubsub::PubSubDelivService;
using pubsub::PubSubService;
using pubsub::ReturnCode;
using pubsub::SubscriberAddress;
using pubsub::Topic;

// Implementierung des Service
class PubSubServiceImpl final : public PubSubService::Service
{
  // TODO: Channel topic und Subscribers für diesen Server merken
  std::string topic;
  std::set<std::string> subscribers;

  static std::string stringify(const SubscriberAddress &adr)
  {
    std::string s = adr.ip_address() + ":";
    s += std::to_string (adr.port());
    return s;
  }

  Status get_session(ClientContext* context, const UserName& request, SessionId* response)
  {
    
  }

  Status validate(ClientContext* context, const PubSubParam& request, ReturnCode* response)
  {

  }

  Status invalidate(ClientContext* context, const SessionId& request, ReturnCode* response)
  {

  }

  Status subscribe(ServerContext *context, const PubSubParam *request, ReturnCode *reply) override
  {
    std::string receiver = stringify(*request);
    // TODO: Client registrieren und Info ausgeben

    if(subscribers.find(receiver) == subscribers.end()){
      std::cout << "[SUBSCRIBE] " << receiver << std::endl;
      subscribers.insert(receiver);
      reply->set_value(ReturnCode::OK);
    }else{
      reply->set_value(ReturnCode::CLIENT_ALREADY_REGISTERED);
    }
 
    return Status::OK;
  }

  Status unsubscribe(ServerContext *context, const PubSubParam *request, ReturnCode *reply) override
  {
    std::string receiver = stringify(*request);
    // TODO: Client austragen und Info ausgeben

    if(subscribers.find(receiver) != subscribers.end()){
      std::cout << "[UNSUBSCRIBE] " << receiver << std::endl;
      subscribers.erase(receiver);
      reply->set_value(ReturnCode::OK);
    }else{
      reply->set_value(ReturnCode::CANNOT_UNREGISTER);
    }
 
    return Status::OK;
  }

  void handle_status(const std::string operation, Status &status)
  {
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

  Status publish(ServerContext *context, const PubSubParam *request, ReturnCode *reply) override
  {
    // TODO: Nachricht an alle Subscriber verteilen
    for (std::string subscriber: subscribers) {
      std::shared_ptr<Channel> channel = grpc::CreateChannel(subscriber, grpc::InsecureChannelCredentials());
      stub_ = PubSubDelivService::NewStub(channel);
    
      ClientContext srv_context;
      Message srv_request;
      EmptyMessage srv_reply;

      srv_request.set_message("<" + topic + "> " + request->message());

      std::cout << "[PUBLISH] \"" << request->message() << "\" to " << subscriber << std::endl;

      Status status = stub_->deliver(&srv_context, srv_request, &srv_reply);
      handle_status(subscriber, status);
    }

    reply->set_value(ReturnCode::OK);
    return Status::OK; 
  }

  Status set_topic(ServerContext *context, const PubSubParam *request, ReturnCode *reply) override
  {
    // TODO: Topic setzen und Info ausgeben
    if(request->passcode() == PASSCODE){
      topic = request->topic();
      reply->set_value(ReturnCode::OK);
      std::cout << "[SET_TOPIC] " << topic << std::endl;
    }else{
      std::cout << "[SET_TOPIC] Fehlgeschlagen" << std::endl;
      reply->set_value(ReturnCode::CANNOT_SET_TOPIC);
    }
    return Status::OK;
  }

public:
  PubSubServiceImpl()
  {
    // TODO: Topic initialisieren
    topic = "no topic set";
  }

private:
    std::unique_ptr<PubSubDelivService::Stub> stub_;
};

void RunServer()
{
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

int main(int argc, char **argv)
{
  // Server starten
  RunServer();
  return 0;
}