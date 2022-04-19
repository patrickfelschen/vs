/* Iterativer UDP Echo-Server  
 * Basiert auf Stevens: Unix Network Programming  
 * getestet unter Ubuntu 20.04 64 Bit
 */

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <map>
#include <ctime>

#define SRV_PORT 8998
#define MAXLINE 512

// Vorwaertsdeklarationen 
void handle_request(int);

void err_abort(char *str);

typedef struct session {
    long session_key;
    int fd;
    int chunk_size;
    int chunk_no;
} Session;

std::map<long, Session> sessions;

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

/* handle_request: Lesen von Daten vom Socket und an den Client zuruecksenden
 *
 * HSOSSTP_INITX;<chunk size>;<filename>
 * HSOSSTP_SIDXX;<session key>
 * HSOSSTP_GETXX;<session key>;<chunk no>
 * HSOSSTP_DATAX;<chunk no>;<actual chunk size>;<data>
*/
void handle_request(int sockfd) {
    socklen_t alen;
    struct sockaddr_in cli_addr;
    char in[MAXLINE];
    unsigned long n;

    int chunk_size;
    char file_name[256];
    long session_key;
    int chunk_no;

    for (;;) {
        alen = sizeof(cli_addr);
        memset((void *) &in, '\0', sizeof(in));
        // Daten vom Socket lesen
        n = recvfrom(sockfd, in, MAXLINE, 0, (struct sockaddr *) &cli_addr, &alen);
        if (n < 0) {
            err_abort((char *) "Fehler beim Lesen des Sockets!");
        }
        // Format: HSOSSTP_INITX;<chunk size>;<filename>
        if (sscanf(in, "HSOSSTP_INITX;%i;%255s", &chunk_size, &file_name[0]) > 0) {
            printf("Chunk-Size: %i\n", chunk_size);
            printf("Filename: %s\n", file_name);

            long new_session_key = std::time(nullptr);
            FILE *file = fopen(file_name, "r");
            if (!file) {
                const char *fnf_response = "HSOSSTP_ERROR;FNF";
                sendto(sockfd, fnf_response, strlen(fnf_response), 0, (struct sockaddr *) &cli_addr, alen);
                return;
            }
            Session newSession = {new_session_key, fileno(file), chunk_size, chunk_no};
            sessions[new_session_key] = newSession;
            char init_response[MAXLINE];
            sprintf(init_response, "HSOSSTP_SIDXX;%li", new_session_key);
            sendto(sockfd, init_response, sizeof(init_response), 0, (struct sockaddr *) &cli_addr, alen);
        }

        // Format: HSOSSTP_GETXX;<session key>;<chunk no>
        if (sscanf(in, "HSOSSTP_GETXX;%li;%i", &session_key, &chunk_no) > 0) {
            printf("Session-Key: %li\n", session_key);
            printf("Chunk no: %i\n", chunk_no);

            if (sessions.find(session_key) == sessions.end()) {
                const char *nos_response = "HSOSSTP_ERROR;NOS";
                sendto(sockfd, nos_response, strlen(nos_response), 0, (struct sockaddr *) &cli_addr, alen);
                return;
            }

            Session currSession = sessions.at(session_key);

            char data_buf[currSession.chunk_size];
            size_t actual_chunk_size = 0;

            //lseek(currSession.fd, currSession.chunk_size * currSession.chunk_no, 0);
            if ((actual_chunk_size = read(currSession.fd, data_buf, currSession.chunk_size)) > 0) {
                char data_header[MAXLINE];
                sprintf(data_header, "HSOSSTP_DATAX;%i;%li;", chunk_no, actual_chunk_size);

                unsigned long response_size = strlen(data_header) + actual_chunk_size;
                char data_response[response_size];

                sprintf(data_response, "%s%s", data_header, data_buf);

                //printf("%s\n", data_response);

                sendto(sockfd, data_response, response_size, 0, (struct sockaddr *) &cli_addr,
                       alen);
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
