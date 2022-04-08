cmake_minimum_required(VERSION 3.16.3)
project(vs)

set(CMAKE_CXX_STANDARD 23)

add_executable(Blatt01 Blatt01/src/daytime.c)

add_executable(Blatt02 Blatt02/src/http_serv.c)

add_executable(Blatt03_Server Blatt03/src/server/udp_server.c)

add_executable(Blatt03_Client Blatt03/src/client/udp_client.c)
