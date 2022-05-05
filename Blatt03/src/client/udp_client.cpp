/* UDP Echo-Client   
 * Basiert auf Stevens: Unix Network Programming  
 * getestet unter Ubuntu 20.04 64 Bit 
 */

#include <cstdio>
#include <cstring>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <cstdlib>
#include <string>

#define SRV_PORT 8998
#define MAXLINE 2048

// Vorwaertsdeklarationen
void handle_answer(int sockfd, struct sockaddr *srv_addr, int srv_len);

void err_abort(char *str);

long chunk_size;
char* file_name;

int main(int argc, char *argv[]) {
    // Deskriptor
    int sockfd;
    // Socket Adresse
    struct sockaddr_in srv_addr, cli_addr;
    // Parameter einlesen
    chunk_size = strtol(argv[2], nullptr, 10);
    file_name = argv[3];

    printf("Server-IP: %s\n", argv[1]);
    printf("Chunk-Size: %li\n", chunk_size);
    printf("Filename: %s\n", file_name);

    // UDP Socket erzeugen
    if ((sockfd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        err_abort((char *) "Kann Datagram-Socket nicht oeffnen!");
    }
    // lokale Adresse binden
    memset((void *) &cli_addr, '\0', sizeof(cli_addr));
    cli_addr.sin_family = AF_INET;
    cli_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    if (bind(sockfd, (struct sockaddr *) &cli_addr, sizeof(cli_addr)) < 0) {
        err_abort((char *) "Fehler beim Binden der lokalen Adresse!");
    }
    // Adress Struktur fuer Server aufbauen
    memset((void *) &srv_addr, '\0', sizeof(srv_addr));
    srv_addr.sin_family = AF_INET;
    srv_addr.sin_addr.s_addr = inet_addr(argv[1]);
    srv_addr.sin_port = htons(SRV_PORT);
    printf("UDP Client: bereit...\n");
    // Daten zum Server senden
    handle_answer(sockfd, (struct sockaddr *) &srv_addr, sizeof(srv_addr));

    close(sockfd);
    _exit(0);
}

/*
 * HSOSSTP_INITX;<chunk size>;<filename>
 * HSOSSTP_SIDXX;<session key>
 * HSOSSTP_GETXX;<session key>;<chunk no>
 * HSOSSTP_DATAX;<chunk no>;<actual chunk size>;<data>
 * HSOSSTP_ERROR;FNF
 * HSOSSTP_ERROR;CNF
 * HSOSSTP_ERROR;NOS
*/
void handle_answer(int sockfd, struct sockaddr *srv_addr, int srv_len) {
    char res[MAXLINE];

    const char init_req_type[] = "HSOSSTP_INITX;";
    std::string init_req = std::string(init_req_type) + std::to_string(chunk_size) + ";" + file_name + ";";
    sendto(sockfd, init_req.data(), strlen(init_req.data()), 0, srv_addr, srv_len);

    FILE *file = fopen(file_name, "wb");

    long session_key = 0;
    long chunk_no = 0;

    for (;;) {
        recvfrom(sockfd, res, MAXLINE, 0, (struct sockaddr *) nullptr, nullptr);

        char *type = strtok(res, ";");

        if (strcmp(type, "HSOSSTP_SIDXX") == 0) {
            char *session_key_str = strtok(nullptr, ";");

            session_key = strtol(session_key_str, nullptr, 10);

            printf("RESPONSE: %s\n", type);
            printf("session_key: %ld\n", session_key);

            const char req_type[] = "HSOSSTP_GETXX;";
            std::string req = std::string(req_type) + session_key_str + ";" + std::to_string(chunk_no);

            sendto(sockfd, req.data(), strlen(req.data()), 0, srv_addr, srv_len);
            printf("REQUEST: %s\n\n", req_type);
        }

        if (strcmp(type, "HSOSSTP_DATAX") == 0) {
            char *chunk_no_str = strtok(nullptr, ";");
            char *a_chunk_size_str = strtok(nullptr, ";");
            char *data_str = strtok(nullptr, "");

            chunk_no = strtol(chunk_no_str, nullptr, 10);
            long a_chunk_size = strtol(a_chunk_size_str, nullptr, 10);

            printf("RESPONSE: %s\n", type);
            printf("chunk_no: %ld\na_chunk_size: %ld\n\n", chunk_no, a_chunk_size);

            write(fileno(file), data_str, a_chunk_size);

            if (a_chunk_size < chunk_size) {
                fclose(file);
                return;
            }

            const char req_type[] = "HSOSSTP_GETXX;";
            std::string req = std::string(req_type) + std::to_string(session_key) + ";" + std::to_string(chunk_no + 1);

            sendto(sockfd, req.data(), strlen(req.data()), 0, srv_addr, srv_len);
            printf("REQUEST: %s\n", req_type);
            //printf("%s\n\n", req.data());
        }

        if (strcmp(type, "HSOSSTP_ERROR") == 0) {
            char *error_str = strtok(nullptr, ";");
            if(strcmp(error_str, "FNF") == 0){
                printf("ERROR: Datei wurde nicht gefunden.\n");
            }
            if(strcmp(error_str, "NOS") == 0){
                printf("ERROR: Session existiert nicht.\n");
            }
        }
    }
}

/* Ausgabe von Fehlermeldungen */
void err_abort(char *str) {
    fprintf(stderr, " UDP Client: %s\n", str);
    fflush(stdout);
    fflush(stderr);
    _exit(1);
} 
