# Makefile zur Generierung des Client-/Server-System
# fuer das Pub-Sub-System. 
# @hje

CXX = g++
CPPFLAGS += `pkg-config --cflags protobuf grpc`
CXXFLAGS += -g -std=c++11

LDFLAGS += -L/usr/local/lib `pkg-config --libs protobuf grpc++`\
           -pthread\
           -Wl,--no-as-needed -lgrpc++_reflection -Wl,--as-needed\
           -ldl

PROTOC = protoc
GRPC_CPP_PLUGIN = grpc_cpp_plugin
GRPC_CPP_PLUGIN_PATH ?= `which $(GRPC_CPP_PLUGIN)`

PROTOS_PATH = ./protos

vpath %.proto $(PROTOS_PATH)

all: pub_sub_server pub_sub_receiver pub_sub_client 

pub_sub_server: pub_sub_common.pb.o pub_sub_common.grpc.pb.o pub_sub.pb.o pub_sub.grpc.pb.o pub_sub_deliv.pb.o pub_sub_deliv.grpc.pb.o pub_sub_server.o 
	$(CXX) $^ $(LDFLAGS) -o $@

pub_sub_receiver: pub_sub_common.pb.o pub_sub_common.grpc.pb.o pub_sub_deliv.pb.o pub_sub_deliv.grpc.pb.o pub_sub_receiver.o 
	$(CXX) $^ $(LDFLAGS) -o $@

pub_sub_client: pub_sub_common.pb.o pub_sub_common.grpc.pb.o pub_sub.pb.o pub_sub.grpc.pb.o pub_sub_client.o 
	$(CXX) $^ $(LDFLAGS) -o $@

%.grpc.pb.cc: %.proto
	$(PROTOC) -I $(PROTOS_PATH) --grpc_out=. --plugin=protoc-gen-grpc=$(GRPC_CPP_PLUGIN_PATH) $<

%.pb.cc: %.proto
	$(PROTOC) -I $(PROTOS_PATH) --cpp_out=. $<

clean:
	rm -f *.o *.pb.cc *.pb.h pub_sub_client pub_sub_server pub_sub_receiver

