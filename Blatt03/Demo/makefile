# Makefile fuer UDP Client / Server
# 14/Feb/2021, H.-J. Eikerling

SRVEXE=./udp_srv
CLTEXE=./udp_clt
CHECKEXE=./check_host

# Auskommentieren, wenn keine Debug-Ausgaben erwuenscht.
#FLAGS=-DDEBUG

CC=gcc
GCC=gcc

all: $(SRVEXE) $(CLTEXE) $(CHECKEXE)

$(CHECKEXE): check_host.o 
	$(GCC) -o $(CHECKEXE) check_host.c

$(SRVEXE): udp_server.o
	$(GCC) -o $(SRVEXE) udp_server.c

$(CLTEXE): udp_client.o
	$(GCC) -o $(CLTEXE) udp_client.c
	
clean:
	$(RM) *.o $(CLTEXE) $(SRVEXE) $(CHECKEXE)

