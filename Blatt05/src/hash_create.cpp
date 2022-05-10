#include <iostream>
#include <memory>
#include <sstream>
#include <stdio.h>
#include <string>
#include <unistd.h>

#include "hashing.cpp"

int main() {

    std::string name;
    std::string pwd;
    do {
        std::cout << "name: ";
        getline(std::cin, name);
        // trim(name);

        std::cout << "pwd: ";
        getline(std::cin, pwd);
        // trim(pwd);

        if (name.length() == 0 || pwd.length() == 0) {
            std::cout << "Fehler." <<std::endl;
            continue;
        }

        std::string hash_user_pwd = sha256(name + ";" + pwd);

        FILE *hash_file = fopen("hashes.txt", "a");
        fprintf(hash_file, "%s %s\n", name.data(), hash_user_pwd.data());
        fclose(hash_file);

        std::cout << "Erstellt." << std::endl;

    } while (1);

    return 0;
}