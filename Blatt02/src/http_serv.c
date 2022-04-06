/* Mehrstufiger TCP Echo-Server fuer mehrere Clients  
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
#include <time.h>
#include <dirent.h>
#include <sys/stat.h>

//#define SRV_PORT 8998
#define MAX_SOCK 10
#define MAXLINE 512

// Vorwaertsdeklarationen 
void read_socket_request(int);
void write_header(int sockfd);
void get_request(int sockfd, char *url);
void write_index(int sockfd, char *url);
void err_abort(char *str);

char date_buf[32];
char *doc_root;

int main(int argc, char *argv[]) {

    // Deskriptoren, Adresslaenge, Prozess-ID
    int sockfd;
    int newsockfd;
    int pid;
    socklen_t alen;
    int reuse = 1;
    // Socket Adressen
    struct sockaddr_in cli_addr;
    struct sockaddr_in srv_addr;

    // Root Ordner
    doc_root = argv[1];
    // Port
    char *srv_port = argv[2];

    // TCP-Socket erzeugen
    if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        err_abort((char *) "Kann Stream-Socket nicht oeffnen!");
    }
    // Nur zum Test: Socketoption zum sofortigen Freigeben der Sockets
    if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &reuse, sizeof(reuse)) < 0) {
        err_abort((char *) "Kann Socketoption nicht setzen!");
    }
    // Binden der lokalen Adresse damit Clients uns erreichen
    memset((void *) &srv_addr, '\0', sizeof(srv_addr));
    srv_addr.sin_family = AF_INET;
    srv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    srv_addr.sin_port = htons(atoi(srv_port));
    if (bind(sockfd, (struct sockaddr *) &srv_addr, sizeof(srv_addr)) < 0) {
        err_abort((char *) "Kann  lokale  Adresse  nicht  binden,  laeuft  fremder Server?");
    }
    // Warteschlange fuer TCP-Socket einrichten
    listen(sockfd, 5);
    printf((char *) "TCP HTTP-Server: bereit ...\n");

    for (;;) {
        alen = sizeof(cli_addr);

        // Verbindung aufbauen
        newsockfd = accept(sockfd, (struct sockaddr *) &cli_addr, &alen);
        if (newsockfd < 0) {
            err_abort((char *) "Fehler beim Verbindungsaufbau!");
        }

        // fuer jede Verbindung einen Kindprozess erzeugen
        if ((pid = fork()) < 0) {
            err_abort((char *) "Fehler beim Erzeugen eines Kindprozesses!");
        } else if (pid == 0) {
            close(sockfd);
            read_socket_request(newsockfd);
            _exit(0);
        }
        close(newsockfd);
    }
}

/* read_socket_request: Lesen von Daten vom Socket und Zuruecksenden an Client */
void read_socket_request(int sockfd) {
    size_t n;
    char in[MAXLINE];
    memset((void *) in, '\0', MAXLINE);

    // Daten vom Socket lesen
    n = read(sockfd, in, MAXLINE);
    if (n == 0) {
        return;
    } else if (n < 0) {
        err_abort((char *) "Fehler beim Lesen des Sockets!");
    }
    printf("%zu byte vom Socket gelesen.\n", n);
    //printf("%s\n", in);

    write_header(sockfd);

    char uri[255];
    sscanf(in, "GET %255s HTTP/", uri);
    get_request(sockfd, uri);
}

void get_request(int sockfd, char *uri) {
    char url[255];
    sprintf(url, "%s%s", doc_root, uri);

    struct stat buf;
    lstat(url, &buf);
    S_ISDIR(buf.st_mode);

    if(S_ISDIR(buf.st_mode)){
        write_index(sockfd, uri);
    }
}

void write_bytes(int sockfd, char* bytes){
    write(sockfd, bytes, strlen(bytes));
}

void write_header(int sockfd){
    write_bytes(sockfd, "HTTP/1.1 200 OK\r\n");
    write_bytes(sockfd, "Content-Language: de\r\n");
    write_bytes(sockfd, "Content-Type: text/html; charset=iso-8859-1\r\n\r\n");
}

void write_index(int sockfd, char *uri){
    char url[255];
    sprintf(url, "%s%s", doc_root, uri);
    printf("%s\n", url);

    DIR* dir = opendir(url);
    struct dirent* dirent;
    write_bytes(sockfd, "<html>");
    write_bytes(sockfd, "<head><title>Index</title></head>");
    write_bytes(sockfd, "<body>");
    write_bytes(sockfd, "<h1>Index</h1>");
    if(dir){
        while((dirent = readdir(dir)) != NULL){
            write_bytes(sockfd, "<a href=\"");
            write_bytes(sockfd, dirent->d_name);
            write_bytes(sockfd, "\">");
            write_bytes(sockfd, dirent->d_name);
            write_bytes(sockfd, "</a><br>");
        }
        closedir(dir);
    }
    write_bytes(sockfd, "</body>");
    write_bytes(sockfd, "</html>");
}

/* Ausgabe von Fehlermeldungen */
void err_abort(char *str) {
    fprintf(stderr, " TCP HTTP-Server: %s\n", str);
    fflush(stdout);
    fflush(stderr);
    _exit(1);
} 
