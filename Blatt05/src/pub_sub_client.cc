/* Pub-Sub-Client
 * Implementiert eine interaktive Shell, in die Kommandos eingegeben werden können.
 * Getestet unter Ubuntu 20.04 64 Bit / g++ 9.3
 * @hje
 */

#include <iostream>
#include <memory>
#include <sstream>
#include <string>

#include <arpa/inet.h>
#include <net/if.h>
#include <netinet/in.h>
#include <sys/ioctl.h>
#include <sys/socket.h>

#include <grpcpp/grpcpp.h>
#include <stdio.h>

// Diese Includes werden generiert.
#include "pub_sub.grpc.pb.h"
#include "pub_sub_common.grpc.pb.h"
#include "pub_sub_config.h"

#include <unistd.h>

#include "hashing.cpp"

// Notwendige gRPC Klassen im Client.
using grpc::Channel;
using grpc::ClientContext;
using grpc::Status;

// Diese Klassen sind in den .proto Dateien definiert.
using pubsub::EmptyMessage;
using pubsub::Message;
using pubsub::PubSubParam;
using pubsub::PubSubService;
using pubsub::ReturnCode;
using pubsub::SessionId;
using pubsub::SubscriberAddress;
using pubsub::Topic;
using pubsub::UserName;

/**** Dies muss editiert werden! ****/
char receiverExecFile[] = RECEIVER_EXEC_FILE;

// Client Session ID
SessionId currentSid;
std::string H_User_Pwd;
bool loggedIn = false;

/* TODO: noch notwendig? */
void trim(std::string &s) {
    /* erstes '\n' durch '\0' ersetzen */
    for (int i = 0; i < s.length(); i++) {
        if (s[i] == '\n') {
            s[i] = '\0';
            break;
        }
    }
}

// Argumente für Aufruf: der Client kann mit --target aufgerufen werden.
class Args {
public:
    std::string target;

    Args(int argc, char **argv) {
        target = PUBSUB_SERVER_IP;
        target += ":";
        target += std::to_string(PUBSUB_SERVER_PORT);

        // Endpunkt des Aufrufs ueber --target eingestellt?
        std::string arg_str("--target");
        for (int i = 1; i < argc; i++) {
            std::string arg_val = argv[i];
            size_t start_pos = arg_val.find(arg_str);
            if (start_pos != std::string::npos) {
                start_pos += arg_str.size();
                if (arg_val[start_pos] == '=') {
                    target = arg_val.substr(start_pos + 1);
                } else {
                    std::cout << "Error: set server address via --target=" << std::endl;
                    std::cout << target << " will be used instead." << std::endl;
                }
            }
        }
    }
};

static std::string get_receiver_ip() {
    // Hier wird eine statisch konfigurierte Adresse zurueck gegeben.
    // Diese koennte auch dynamisch ermittelt werden.
    // Dann aber: alle Adapter und die dafuer vorgesehenen IP Adresse durchgehen;
    // eine davon auswaehlen. Das funktioniert aber auch nur im lokalen Netz.
    // Was ist wenn NAT verwendet wird? Oder Proxies?
    int fd;
    struct ifreq ifr;

    fd = socket(AF_INET, SOCK_DGRAM, 0);
    ifr.ifr_addr.sa_family = AF_INET;

    strncpy(ifr.ifr_name, "enp0s25", IFNAMSIZ - 1);

    ioctl(fd, SIOCGIFADDR, &ifr);

    close(fd);

    return inet_ntoa(((struct sockaddr_in *)&ifr.ifr_addr)->sin_addr);
}

class PubSubClient {
private:
    void print_prompt(const Args &args) {
        std::cout << "Pub / sub server is: " << args.target << std::endl;
    }

    void print_help() {
        std::cout << "Client usage: \n";
        std::cout << "     'quit' to exit; \n";
        std::cout << "     'set_topic' to set new topic; \n";
        std::cout << "     'subscribe' subscribe to server & register / start receiver; \n";
        std::cout << "     'unsubscribe' from this server & terminate receiver; \n";
        std::cout << "     'logout' to log user out; \n";
    }

    void print_auth_help() {
        std::cout << "Client usage: \n";
        std::cout << "     'quit' to exit; \n";
        std::cout << "     'login' to login into user; \n";
    }

    static std::string stringify(const SubscriberAddress &adr) {
        std::string s = adr.ip_address() + ":";
        s += std::to_string(adr.port());
        return s;
    }

    static std::string stringify(pubsub::ReturnCode_Values value) {
        /* TODO: Hier sollte eine passende Status-Ausgabe generiert werden! */
        switch (value) {
        case ReturnCode::OK:
            return "OK";
            break;
        case ReturnCode::CANNOT_REGISTER:
            return "CANNOT_REGISTER";
            break;
        case ReturnCode::CLIENT_ALREADY_REGISTERED:
            return "CLIENT_ALREADY_REGISTERED";
            break;
        case ReturnCode::CANNOT_UNREGISTER:
            return "CANNOT_UNREGISTER";
            break;
        case ReturnCode::CANNOT_SET_TOPIC:
            return "CANNOT_SET_TOPIC";
            break;
        case ReturnCode::NO_HASH_FOR_SESSION:
            return "NO_HASH_FOR_SESSION";
            break;
        case ReturnCode::WRONG_HASH_FOR_SESSION:
            return "WRONG_HASH_FOR_SESSION";
            break;
        case ReturnCode::USER_ALREADY_LOGGED_IN:
            return "USER_ALREADY_LOGGED_IN";
            break;
        case ReturnCode::SESSION_INVALID:
            return "SESSION_INVALID";
            break;
        case ReturnCode::UNKNOWN_ERROR:
            return "UNKNOWN_ERROR";
            break;
        }

        return "UNKNOWN";
    }

    void handle_status(const std::string operation, Status &status, ReturnCode &reply) {
        // Status auswerten
        if (status.ok()) {
            std::cout << operation << " -> " << stringify(reply.value()) << std::endl;
        } else {
            std::cout << "RPC error: "
                      << status.error_code()
                      << " (" << status.error_message()
                      << ")" << std::endl;
        }
    }

public:
    PubSubClient(std::shared_ptr<Channel> channel) : stub_(PubSubService::NewStub(channel)) {
    }

    void run_shell(const Args &args) {
        /* PID der Receiver Console */
        int rec_pid = -1;

        print_prompt(args);

        std::string cmd;
        do {
            if (loggedIn) {
                print_help();
            } else {
                print_auth_help();
            }

            std::cout << "> ";
            // Eingabezeile lesen
            getline(std::cin, cmd);
            // std::cin >> cmd;
            if (cmd.length() == 0)
                continue;

            trim(cmd);

            if (cmd.compare("login") == 0) {
                std::string user;
                std::cout << "enter username> ";
                // std::cin >> topic;
                getline(std::cin, user);
                trim(user);

                std::string pwd;
                std::cout << "enter password> ";
                // std::cin >> topic;
                getline(std::cin, pwd);
                trim(pwd);

                /* TODO: Hier den Request verschicken und Ergebnis auswerten! */
                // Session ID holen
                UserName request;
                SessionId reply;
                // Kontext kann die barbeitung der RPCs beeinflusst werden. Wird nicht genutzt.
                ClientContext context;
                // TODO: UserName fuer Server vorbereiten ...
                request.set_name(user);
                // TODO: RPC abschicken ...
                stub_->get_session(&context, request, &reply);
                // Status / Reply behandeln
                currentSid.set_id(reply.id());


                // Validieren
                PubSubParam validate_request;
                SessionId *sessionId = validate_request.mutable_sid();
                ReturnCode validate_response;
                ClientContext validate_context;

                sessionId->set_id(currentSid.id());

                std::string nonce = std::to_string(currentSid.id());

                // H(user;pwd)
                H_User_Pwd = sha256(user + ";" + pwd);
                // H(nonce;"";H(user;pwd))
                std::string H_Nonce_H_User_Pwd = sha256(nonce + H_User_Pwd);
                // nonce;"";H(nonce;"";H(user;pwd))
                validate_request.set_hash_string(H_Nonce_H_User_Pwd);

                Status status = stub_->validate(&validate_context, validate_request, &validate_response);

                loggedIn = validate_response.value() == ReturnCode::OK;

                this->handle_status("login()", status, validate_response);
            } else if (cmd.compare("set_topic") == 0) {
                std::string topic;
                std::cout << "enter topic> ";
                // std::cin >> topic;
                getline(std::cin, topic);
                trim(topic);
                // Passcode einlesen, damit topic gesetzt werden darf.
                std::string passcode;
                std::cout << "enter passcode> ";
                getline(std::cin, passcode);
                trim(passcode);

                /* TODO: Hier den Request verschicken und Ergebnis auswerten! */

                // Platzhalter fuer Request, Kontext & Reply.
                // Muss hier lokal definiert werden,
                // da es sonst Probleme mit der Speicherfreigabe gibt.
                PubSubParam request;
                Topic *optTopic = request.mutable_opttopic();
                SessionId *optSessionId = request.mutable_sid();
                ReturnCode reply;
                // Kontext kann die barbeitung der RPCs beeinflusst werden. Wird nicht genutzt.
                ClientContext context;

                // TODO: Topic fuer Server vorbereiten ...
                optTopic->set_topic(topic);
                optTopic->set_passcode(passcode);

                optSessionId->set_id(currentSid.id());

                std::string H_Nonce_Topic_H_User_Pwd = sha256(std::to_string(currentSid.id()) + topic + H_User_Pwd);

                request.set_hash_string(H_Nonce_Topic_H_User_Pwd);

                // TODO: RPC abschicken ...
                Status status = stub_->set_topic(&context, request, &reply);

                // Status / Reply behandeln
                this->handle_status("set_topic()", status, reply);
            } else if (cmd.compare("subscribe") == 0) {
                /* Ueberpruefen, ob Binary des Receivers existiert */
                if (access(receiverExecFile, X_OK) != -1) {
                    /* Receiver starten */
                    if ((rec_pid = fork()) < 0) {
                        std::cerr << "Cannot create process for receiver!\n";
                    } else if (rec_pid == 0) {
                        /* Der Shell-Aufruf */
                        /* xterm -fa 'Monospace' -fs 12 -T Receiver -e ...pub_sub_deliv */
                        /* kann nicht 1:1 uebertragen werden. Bei Aufruf via exec() */
                        /* verhaelt sich das Terminal anders. */
                        /* Alternative: Aufruf von xterm ueber ein Shell-Skript. */
                        /* Allerdings haette man dann 2 Kind-Prozesse. */
                        execl("/usr/bin/xterm", "Receiver", "-fs", "14", receiverExecFile, (char *)NULL);
                        /* -fs 14 wird leider ignoriert! */
                        exit(0); /* Kind beenden */
                    }

                    /* TODO: Hier den Request verschicken und Ergebnis auswerten! */
                    /* Platzhalter wie oben lokal erstellen ... */
                    PubSubParam request;
                    SubscriberAddress *optAddress = request.mutable_optaddress();
                    SessionId *optSessionId = request.mutable_sid();
                    ReturnCode reply;
                    ClientContext context;

                    // TODO: Receiver Adresse setzen ...
                    optAddress->set_ip_address(get_receiver_ip());
                    optAddress->set_port(PUBSUB_RECEIVER_PORT);

                    optSessionId->set_id(currentSid.id());

                    std::string H_Nonce_Address_H_User_Pwd = sha256(std::to_string(currentSid.id()) + stringify(*optAddress) + H_User_Pwd);
                    request.set_hash_string(H_Nonce_Address_H_User_Pwd);

                    // TODO: RPC abschicken ...
                    Status status = stub_->subscribe(&context, request, &reply);

                    // TODO: Status / Reply behandeln ...
                    this->handle_status("subscribe()", status, reply);
                } else {
                    std::cerr << "Cannot find message receiver executable!\n";
                    std::cerr << "Press <return> to continue";
                    char c = getc(stdin);
                    continue;
                }
            } else if (
                (cmd.compare("quit") == 0) ||
                (cmd.compare("unsubscribe") == 0) ||
                (cmd.compare("logout") == 0)) {
                /* Receiver console beenden */
                if (rec_pid > 0) {
                    if (kill(rec_pid, SIGTERM) != 0)
                        std::cerr << "Cannot terminate message receiver!\n";
                    else
                        rec_pid = -1;
                }
                /* Bei quit muss ebenfalls ein unsubscribe() gemacht werden. */

                /* TODO: Hier den Request verschicken und Ergebnis auswerten! */
                /* Platzhalter wie oben lokal erstellen ... */
                PubSubParam request;
                SubscriberAddress *optAddress = request.mutable_optaddress();
                SessionId *optSessionId = request.mutable_sid();
                ReturnCode reply;
                ClientContext context;

                // TODO: Receiver Adresse setzen ...
                optAddress->set_ip_address(get_receiver_ip());
                optAddress->set_port(PUBSUB_RECEIVER_PORT);

                optSessionId->set_id(currentSid.id());

                std::string H_Nonce_Address_H_User_Pwd = sha256(std::to_string(currentSid.id()) + stringify(*optAddress) + H_User_Pwd);
                request.set_hash_string(H_Nonce_Address_H_User_Pwd);

                // TODO: RPC abschicken ...
                Status status = stub_->unsubscribe(&context, request, &reply);

                // TODO: Status / Reply behandeln ...
                this->handle_status("unsubscribe()", status, reply);

                // nur bei Logout
                if (cmd.compare("logout") == 0) {
                    ReturnCode response;
                    ClientContext context;
                    SessionId sessionId;

                    sessionId.set_id(currentSid.id());

                    // TODO: RPC abschicken ...
                    Status status = stub_->invalidate(&context, sessionId, &response);

                    loggedIn = !(response.value() == ReturnCode::OK);

                    // Status / Reply behandeln
                    this->handle_status("logout()", status, response);
                }

                /* Shell beenden nur bei quit */
                if (cmd.compare("quit") == 0) {
                    break; /* Shell beenden */
                }
            } else /* kein Kommando -> publish() aufrufen */
            {
                /* TODO: Hier den Request verschicken und Ergebnis auswerten! */
                /* Platzhalter wie oben lokal erstellen ... */
                ClientContext context;
                PubSubParam request;
                Message *optMessage = request.mutable_optmessage();
                SessionId *optSessionId = request.mutable_sid();
                ReturnCode reply;

                // TODO: Message setzen ...
                optMessage->set_message(cmd);
                optSessionId->set_id(currentSid.id());

                std::string H_Nonce_Message_H_User_Pwd = sha256(std::to_string(currentSid.id()) + optMessage->message() + H_User_Pwd);
                request.set_hash_string(H_Nonce_Message_H_User_Pwd);

                // TODO: RPC abschicken ...
                Status status = stub_->publish(&context, request, &reply);

                // TODO: Status / Reply behandeln ...
                this->handle_status("publish()", status, reply);
            }
        } while (1);
    }

private:
    std::unique_ptr<PubSubService::Stub> stub_;
};

int main(int argc, char **argv) {
    // Einlesen der Argumente. Der Endpunkt des Aufrufs
    // kann über die Option --target eingestellt werden.
    Args args(argc, argv);

    PubSubClient client(grpc::CreateChannel(
        args.target, grpc::InsecureChannelCredentials()));

    client.run_shell(args);

    return 0;
}
