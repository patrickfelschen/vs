/* UDP Echo-Client   
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
#include <stdlib.h>

#define SRV_PORT 8998
#define MAXLINE 512

// Vorwaertsdeklarationen
void handle_answer(int sockfd, struct sockaddr *srv_addr, int srv_len);
void err_abort(char *str);

long chunk_size;
char *file_name;

int main(int argc, char *argv[]) {
    // Deskriptor
    int sockfd;
    // Socket Adresse
    struct sockaddr_in srv_addr, cli_addr;
    // Parameter einlesen
    chunk_size = strtol(argv[2], NULL, 10);
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


/* handle_answer: Lesen von Daten vom Socket und an den Client zuruecksenden
 *
 * HSOSSTP_INITX;<chunk size>;<filename>
 * HSOSSTP_SIDXX;<session key>
 * HSOSSTP_GETXX;<session key>;<chunk no>
 * HSOSSTP_DATAX;<chunk no>;<actual chunk size>;<data>
*/
void handle_answer(int sockfd, struct sockaddr *srv_addr, int srv_len) {
    char in[MAXLINE];
    unsigned long n;

    long sesssion_key;
    int chunk_no = 0;
    int actual_chunk_size = 0;
    char data[chunk_size];

    char init[MAXLINE];
    sprintf(init, "HSOSSTP_INITX;%li;%255s", chunk_size, file_name);

    // Zeile zum Server senden
    sendto(sockfd, init, strlen(init), 0, srv_addr, srv_len);

    for(;;) {
        memset((void *) &in, '\0', sizeof(in));

        n = recvfrom(sockfd,in, MAXLINE, 0, (struct sockaddr* ) NULL, NULL);

        if (n < 0) {
            err_abort((char *) "Fehler beim Lesen des Sockets!");
        }

        // Format: HSOSSTP_SIDXX;<session key>
        if(sscanf(in, "HSOSSTP_SIDXX;%li", &sesssion_key) > 0){
            printf("Session-Key: %li\n", sesssion_key);

            char get[MAXLINE];
            sprintf(get, "HSOSSTP_GETXX;%li;%i", sesssion_key, chunk_no);
            sendto(sockfd, get, strlen(get), 0, srv_addr, srv_len);
        }

        // Format: HSOSSTP_DATAX;<chunk no>;<actual chunk size>;<data>
        if(sscanf(in, "HSOSSTP_DATAX;%i;%i;%255s", &chunk_no, &actual_chunk_size, &data[0]) > 0){
            printf("Chunk no: %i\n", chunk_no);
            printf("Actual Chunk Size: %i\n", actual_chunk_size);

            char* d = strrchr(in, ';');
            printf("Data: %s\n", d);

            if(actual_chunk_size < chunk_size){
                return;
            }

            char get[MAXLINE];
            sprintf(get, "HSOSSTP_GETXX;%li;%i", sesssion_key, chunk_no+1);
            sendto(sockfd, get, strlen(get), 0, srv_addr, srv_len);
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
