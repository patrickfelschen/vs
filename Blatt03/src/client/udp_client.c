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

#define SRV_PORT 8998 
#define MAXLINE 512 

// Vorwaertsdeklarationen 
void dg_client(int, struct sockaddr*, int); 
void err_abort(char *str); 

int main(int argc, char *argv[]) { 
	// Deskriptor 
	int sockfd; 
	// Socket Adresse 
	struct sockaddr_in srv_addr, cli_addr; 

	// Argumente testen 
	if ( argc != 2 ) { 
		err_abort((char*) "IP Adresse des Servers fehlt!"); 
	} 
	// UDP Socket erzeugen 
	if ((sockfd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) { 
		err_abort((char*) "Kann Datagram-Socket nicht oeffnen!"); 
	} 
	// lokale Adresse binden 
	memset ((void *)&cli_addr, '\0', sizeof(cli_addr)); 
	cli_addr.sin_family = AF_INET; 
	cli_addr.sin_addr.s_addr = htonl(INADDR_ANY); 
	if (bind(sockfd, (struct sockaddr *)&cli_addr, sizeof(cli_addr))<0){ 
		err_abort((char*) "Fehler beim Binden der lokalen Adresse!"); 
	} 
	// Adress Struktur fuer Server aufbauen 
	memset ((void *)&srv_addr, '\0', sizeof(srv_addr)); 
	srv_addr.sin_family = AF_INET; 
	srv_addr.sin_addr.s_addr = inet_addr(argv[1]); 
	srv_addr.sin_port = htons(SRV_PORT); 
	printf ("UDP Echo-Client: bereit...\n"); 
	// Daten zum Server senden 
	dg_client (sockfd,(struct sockaddr*) &srv_addr, sizeof (srv_addr)); 

	close (sockfd); 
	_exit (0); 
} 

/*  dg_client:  Daten  von  der  Standardeingabe  lesen,  zum  Server  senden,  auf 
das Echo warten und dieses ausgeben */ 
void dg_client(int sockfd, struct sockaddr *srv_addr, int srv_len){ 
	int n; 
	char out[MAXLINE], in[MAXLINE+6]; 

	// Lesen bis zum Ende der Eingabe 
	while (fgets(out,MAXLINE,stdin) != NULL) { 
		n = strlen(out); 
		out[n-1] = '\0'; 
		// Zeile zum Server senden 
		if (sendto (sockfd, out, n, 0, srv_addr, srv_len) != n) { 
			err_abort((char*) "Fehler beim Schreiben des Sockets!"); 
		} 
		// Echo vom Server lesen 
		n = recvfrom (sockfd, in, MAXLINE, 0, (struct sockaddr*) NULL, (socklen_t*) NULL); 
		if (n < 0) { 
			err_abort ((char*) "Fehler beim Lesen des Sockets!"); 
		}  
		// Echo ausgeben 
		printf ("%s\n", in); 
	} 
} 

/* Ausgabe von Fehlermeldungen */ 
void err_abort (char *str){ 
	fprintf (stderr," UDP Echo-Client: %s\n",str); 
	fflush (stdout); 
	fflush (stderr); 
	_exit (1); 
} 
