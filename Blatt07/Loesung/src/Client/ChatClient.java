package Client;

import Connection.ChatProxy;
import Server.ChatServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class ChatClient {
    private boolean loggedIn;
    private String currentUser;
    private Registry registry;
    private ChatServer chatServer;
    private ChatProxy chatProxy;

    /**
     * Im Konstruktor wird die Registry mit dem Standardport gesucht
     * Anschliessend wird der Server unter dem Namen "ChatServer" aus der Registry ausgelesen
     */
    ChatClient() {
        try {
            this.registry = LocateRegistry.getRegistry(12345);
            this.chatServer = (ChatServer) registry.lookup("ChatServer");
            this.chatProxy = null;
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *  Bei gültigem Input wird ein neuer Client-Proxy erstellt, welcher im Chat-Proxy als Kommunikationsschnittstelle für den Nachrichtenaustausch dient (Nachrichten ausgeben).
     *  Über das zurückgelieferte ChatProxy Element aus der subscribeUser() Funktion des Servers, wird eine Kommunikation zwischen Server und Client ermöglicht (Nachrichten verteilen).
     * @param input Nutzername
     * @return Anmeldestatus
     */
    private boolean subscribe(String input) {
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
            System.out.println("<unsubscribe> zum Abmelden - Texteingabe zum Nachricht abschicken");
            this.loggedIn = true;
            this.currentUser = input;
            return true;
        }
    }

    /**
     * Die Server-Funktion unsubscribeUser() wird ausgeführt, wodurch der Nutzer aus der Liste der aktiven Verbindungen ausgetragen wird.
     */
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

    /**
     * Ist der Nutzer angemeldet, lassen sich über den zuvor im Login erstellten ChatProxy Nachrichten versenden.
     * @param message Zu versendende Nachricht
     */
    private void sendMessage(String message) {
        if(!loggedIn) {
            System.out.println("Sie sind nicht angemeldet");
        }else {
            try {
                chatProxy.sendMessage(message);
            } catch (RemoteException e) {
                System.out.println("Nachricht konnte nicht zugestellt werden.");
            }
        }
    }

    /**
     * Übersicht der Verfuegbaren Befehle.
     */
    private void printHelp() {
        System.out.println("- Moegliche Befehle: ");
        System.out.println("- subscribe");
        System.out.println("- unsubscribe");
        System.out.println("- sendmessage");
        System.out.println("- exit");

    }
    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Bitte Namen zum Anmelden angeben: ");
        String username = scanner.nextLine();

        if(chatClient.subscribe(username)) {
            while(true){
                String userInput = scanner.nextLine();

                if(userInput.equals("exit") || userInput.equals("unsubscribe")) {
                    chatClient.unsubscribe();

                    if(userInput.equals("exit")) {
                        System.exit(0);
                    }
                }
                else if(userInput.equals("help")) {
                    chatClient.printHelp();
                }
                else if(userInput.equals("subscribe")) {
                    System.out.println("Bitte Namen zum Anmelden angeben: ");

                    chatClient.subscribe(scanner.nextLine());
                }
                else {
                    chatClient.sendMessage(userInput);
                }
            }
        }
    }
}