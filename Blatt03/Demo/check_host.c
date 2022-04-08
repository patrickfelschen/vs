/* Beispiel check_host: Benutzung von gethostbyname()
 * getestet unter Ubuntu 16.04 64 Bit 
 */ 
#include <stdio.h>
#include <unistd.h>
#include <sys/types.h>
#include <netdb.h>
#include <sys/socket.h>
#include <netinet/in.h> 
#include <arpa/inet.h>
int main(int argc, char *argv[]) {
	char *ptr;
	struct hostent *hptr;   /* fuer gethostbyname */
	struct in_addr *aptr;   /* fuer IP Adressen */
	while(--argc > 0 ) { /* Schleife uber alle Argumente */
		/* naechstes Argument, z.B. lbst-npca-1.lbst.ecs.fh-osnabrueck.de */
		ptr = *++argv;
		/* gethostbyname aufrufen */
		if( (hptr = gethostbyname(ptr)) == NULL ) {
			fprintf(stderr, "gethostbyname fails for %s\n", ptr);
			/* naechstes Argument */
			continue;
		}
		printf("official name of %s is %s\n", ptr, hptr->h_name);
		/* alle Aliasnamen ausgeben */
		while( (ptr = *(hptr->h_aliases)) != NULL ) {
			printf("    alias: %s\n", ptr);
			hptr->h_aliases++;
		}
		/* Adresstyp usw. ausgeben */
		printf("  addr.type = %d, addr.length=%d\n",
			hptr->h_addrtype, hptr->h_length);
		switch( hptr->h_addrtype ) {
		case AF_INET:
			/* Adressen ausgeben, also z.B. 131.173.110.1 */
			while((aptr=\
				(struct in_addr *)*(hptr->h_addr_list))\
				!= NULL ) {
					printf("IP addr: %s\n",inet_ntoa(*aptr));
					hptr->h_addr_list++;
			}
			break;
		default:
			printf("Unknown address type\n");
			break;
		}   /* of while ptr */
	}   /* of while -argc */
}

