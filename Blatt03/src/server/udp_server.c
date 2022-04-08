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

#define SRV_PORT 8998 
#define MAXLINE 512 

// Vorwaertsdeklarationen 
void dg_echo (int);  
void err_abort (char *str); 

// Explizite Deklaration zur Vermeidung von Warnungen 
// void *memset (void *s, int c, size_t n); 

int main (int argc, char *argv[]) { 
	// Deskriptor 
	int sockfd; 
	// Socket Adresse 
	struct sockaddr_in srv_addr; 

	// UDP-Socket erzeugen 
	if ((sockfd=socket(AF_INET, SOCK_DGRAM, 0)) < 0) { 
		err_abort((char*) "Kann Datagram-Socket nicht oeffnen!"); 
	} 

	// Binden der lokalen Adresse damit Clients uns erreichen 
	memset ((void *)&srv_addr, '\0', sizeof(srv_addr)); 
	srv_addr.sin_family = AF_INET; 
	srv_addr.sin_addr.s_addr = htonl(INADDR_ANY); 
	srv_addr.sin_port = htons(SRV_PORT); 
	if (bind (sockfd, (struct sockaddr *)&srv_addr, 
		sizeof(srv_addr)) < 0 ) { 
			err_abort((char*) "Kann  lokale  Adresse  nicht  binden,  laeuft  fremder Server?"); 
	} 
	printf ("UDP Echo-Server: bereit ...\n"); 
	dg_echo(sockfd); 
}  

/* dg_echo: Lesen von Daten vom Socket und an den Client zuruecksenden */ 
void dg_echo (int sockfd) { 
	socklen_t alen;
	int n; 
	char in[MAXLINE], out[MAXLINE+6]; 
	struct sockaddr_in cli_addr; 

	for(;;) { 
		alen = sizeof(cli_addr); 
		memset((void *)&in,'\0',sizeof(in)); 

		// Daten vom Socket lesen 
		n = recvfrom(sockfd,  in,  MAXLINE,  0, (struct sockaddr *)& cli_addr, &alen); 
		if( n<0 ) { 
			err_abort ((char*) "Fehler beim Lesen des Sockets!");   
		} 
		printf ("%s\n", in);
		sprintf (out,"Echo: %s",in); 

		// Daten schreiben 
		if (sendto(sockfd,  out,  n+6,  0,  (struct  sockaddr *) &cli_addr, alen) != n + 6) { 
			err_abort((char*) "Fehler beim Schreiben des Sockets!"); 
		} 
	} 
} 

/* Ausgabe von Fehlermeldungen */ 
void err_abort(char *str){ 
	fprintf(stderr," UDP Echo-Server: %s\n",str); 
	fflush(stdout); 
	fflush(stderr); 
	_exit(1); 
} 
