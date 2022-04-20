/* Iterativer UDP Echo-Server  
 * Basiert auf Stevens: Unix Network Programming  
 * getestet unter Ubuntu 20.04 64 Bit
 */

#include <cstdio>
#include <cstring>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <map>
#include <ctime>

#define SRV_PORT 8998
#define MAXLINE 2048

// Vorwaertsdeklarationen 
void handle_request(int);

void err_abort(char *str);

int main(int argc, char *argv[]) {
    // Deskriptor
    int sockfd;
    // Socket Adresse
    struct sockaddr_in srv_addr;
    // UDP-Socket erzeugen
    if ((sockfd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        err_abort((char *) "Kann Datagram-Socket nicht oeffnen!");
    }
    // Binden der lokalen Adresse damit Clients uns erreichen
    memset((void *) &srv_addr, '\0', sizeof(srv_addr));
    srv_addr.sin_family = AF_INET;
    srv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    srv_addr.sin_port = htons(SRV_PORT);
    if (bind(sockfd, (struct sockaddr *) &srv_addr, sizeof(srv_addr)) < 0) {
        err_abort((char *) "Kann  lokale  Adresse  nicht  binden,  laeuft  fremder Server?");
    }
    printf("UDP Server: bereit ...\n");
    handle_request(sockfd);
}

struct Session {
    int fd;
    long chunk_size;
};

std::map<long, Session> sessions;

/* handle_request: Lesen von Daten vom Socket und an den Client zuruecksenden
 * HSOSSTP_INITX;<chunk size>;<filename>
 * HSOSSTP_SIDXX;<session key>
 * HSOSSTP_GETXX;<session key>;<chunk no>
 * HSOSSTP_DATAX;<chunk no>;<actual chunk size>;<data>
 * HSOSSTP_ERROR;FNF
 * HSOSSTP_ERROR;CNF
 * HSOSSTP_ERROR;NOS
*/
void handle_request(int sockfd) {
    socklen_t alen;
    struct sockaddr_in cli_addr;

    char req[MAXLINE];

    for (;;) {
        alen = sizeof(cli_addr);
        recvfrom(sockfd, req, MAXLINE, 0, (struct sockaddr *) &cli_addr, &alen);

        char *type = strtok(req, ";");

        if (strcmp(type, "HSOSSTP_INITX") == 0) {
            char *chunk_size_str = strtok(nullptr, ";");
            char *file_name_str = strtok(nullptr, ";");

            long chunk_size = strtol(chunk_size_str, nullptr, 10);
            long new_session_key = std::time(nullptr);

            printf("REQUEST: %s\n", type);
            printf("chunk_size: %ld\nfile_name: %s\n", chunk_size, file_name_str);

            FILE *file = fopen(file_name_str, "rb");
            if (!file) {
                const char *fnf_response = "HSOSSTP_ERROR;FNF";
                sendto(sockfd,fnf_response,strlen(fnf_response),0,(struct sockaddr *) &cli_addr,alen);
                printf("RESPONSE: %s\n", fnf_response);
                continue;
            }

            Session newSession;
            newSession.fd = fileno(file);
            newSession.chunk_size = chunk_size;
            sessions[new_session_key] = newSession;

            const char res_type[] = "HSOSSTP_SIDXX;";
            std::string res = std::string(res_type) + std::to_string(new_session_key);

            sendto(sockfd, res.data(), strlen(res.data()), 0, (struct sockaddr *) &cli_addr, alen);

            printf("RESPONSE: HSOSSTP_SIDXX\n");
            printf("new_session_key: %ld\n", new_session_key);
        }

        if (strcmp(type, "HSOSSTP_GETXX") == 0) {
            char *session_key_str = strtok(nullptr, ";");
            char *chunk_no_str = strtok(nullptr, " ");

            long session_key = strtol(session_key_str, nullptr, 10);
            long chunk_no = strtol(chunk_no_str, nullptr, 10);

            printf("REQUEST: %s\n", type);
            printf("session_key: %ld\nchunk_no: %ld\n", session_key, chunk_no);

            if (sessions.find(session_key) == sessions.end()) {
                const char *nos_response = "HSOSSTP_ERROR;NOS";
                sendto(sockfd, nos_response, strlen(nos_response), 0, (struct sockaddr *) &cli_addr, alen);
                printf("RESPONSE: %s\n\n", nos_response);
                continue;
            }

            Session session = sessions.at(session_key);

            char data[session.chunk_size];
            long a_chunk_size = 0;

            if ((a_chunk_size = read(session.fd, data, session.chunk_size)) > 0) {
                const char res_type[] = "HSOSSTP_DATAX;";
                std::string res_header = std::string(res_type) + std::to_string(chunk_no) + ";" + std::to_string(a_chunk_size) + ";";

                char result[res_header.size() + a_chunk_size];

                for(int i = 0; i < res_header.size(); i++){
                    result[i] = res_header.at(i);
                }

                for(int i = 0; i <= a_chunk_size; i++){
                    result[i + res_header.size()] = data[i];
                }

                sendto(sockfd, result, sizeof(result), 0, (struct sockaddr *) &cli_addr,alen);
                printf("RESPONSE: HSOSSTP_DATAX\n");
                printf("a_chunk_size: %ld\n\n", a_chunk_size);
                //printf("%s\n\n", result);
            }
        }
    }
}

/* Ausgabe von Fehlermeldungen */
void err_abort(char *str) {
    fprintf(stderr, " UDP Echo-Server: %s\n", str);
    fflush(stdout);
    fflush(stderr);
    _exit(1);
} 
