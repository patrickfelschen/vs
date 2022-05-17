package Client;

import Connection.ChatProxy;
import Server.ChatServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Main {
    private boolean loggedIn;
    private String currentUser;
    private Registry registry;
    private ChatServer chatServer;
    private ChatProxy chatProxy;

    Main() {
        try {
            this.registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
            this.chatServer = (ChatServer) registry.lookup("ChatServer");
            this.chatProxy = null;
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean readLogin(String input) {
        ClientProxyImpl clientProxy = new ClientProxyImpl();
        try {
            ClientProxy handle = (ClientProxy) UnicastRemoteObject.exportObject(clientProxy, 0);
            this.chatProxy = chatServer.subscribeUser(input, handle);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        if(loggedIn){
            System.out.println("Sie sind bereits angemeldet");
            return false;
        }
        if(chatProxy == null) {
            System.out.println("Anmelden fehlgeschlagen");
            return false;
        }else {
            System.out.println("Anmelden erfolgreich");
            System.out.println("<unsubscribe> zum Abmelden - Texteingabe zum Nachricht abschicken");
            this.loggedIn = true;
            this.currentUser = input;
            return true;
        }
    }
    private void unsubscribe() {
        if(!loggedIn) {
            System.out.println("Sie sind nicht angemeldet");
        }

        boolean status = false;
        try {
            status = chatServer.unsubscribeUser(this.currentUser);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        if(!status) {
            System.out.println("Abmelden fehlgeschlagen");
        }else {
            System.out.println("Abmelden erfolgreich");
            System.out.println("<subscribe> zum erneuten Anmelden");
            loggedIn = false;
        }
    }

    private void sendMessage(String message) {
        if(!loggedIn) {
            System.out.println("Sie sind nicht angemeldet");
        }else {
            try {
                chatProxy.sendMessage(message);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void printHelp() {
        System.out.println("- Moegliche Befehle: ");
        System.out.println("- subscribe");
        System.out.println("- unsubscribe");
        System.out.println("- sendmessage");
        System.out.println("- exit");

    }
    public static void main(String[] args) {
        Main main = new Main();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Bitte Namen zum Anmelden angeben: ");
        String username = scanner.nextLine();

        if(main.readLogin(username)) {
            while(true){
                String userInput = scanner.nextLine();

                if(userInput.equals("exit") || userInput.equals("unsubscribe")) {
                    main.unsubscribe();

                    if(userInput.equals("exit")) {
                        System.exit(0);
                    }
                }
                else if(userInput.equals("help")) {
                    main.printHelp();
                }
                else if(userInput.equals("subscribe")) {
                    System.out.println("Bitte Namen zum Anmelden angeben: ");

                    main.readLogin(scanner.nextLine());
                }
                else {
                    main.sendMessage(userInput);
                }
            }
        }
    }
}