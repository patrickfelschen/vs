package Server;

import Connection.ChatProxyImpl;
import Client.ClientProxy;
import Connection.ChatProxy;


import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class ChatServerImpl extends UnicastRemoteObject implements ChatServer {
    private Map<String, ClientProxy> chatProxyList = new HashMap<>();

    protected ChatServerImpl() throws RemoteException {
    }

    /**
     *
     * @param username Nutzername
     * @param handle Zum Nutzer gehörende Verbindungsschnittstelle für den Datenempfang.
     * @return Verbindungspunkt über den Nachrichten an den zugehörigen Nutzer verteilt werden können.
     * @throws RemoteException
     */
    @Override
    public ChatProxy subscribeUser(String username, ClientProxy handle) throws RemoteException {
        ChatProxyImpl chatProxyImpl = new ChatProxyImpl(this, username);
        ChatProxy chatProxy = (ChatProxy) UnicastRemoteObject.exportObject(chatProxyImpl, 0);

        chatProxyList.put(username, handle);

        System.out.println("[SUBSCRIBE] " + username + " subscribed");

        return chatProxy;
    }

    /**
     * Der übergebene Nutzer wird aus der Liste der aktiven Verbindungen gelöscht.
     * @param username Nutzername, welcher abgemeldet werden soll.
     * @return Status des Abmeldens.
     * @throws RemoteException
     */
    @Override
    public boolean unsubscribeUser(String username) throws RemoteException {
        ClientProxy removedObject;
        removedObject = chatProxyList.remove(username);

        boolean status = removedObject != null;

        if(status) {
            System.out.println("[UNSUBSCRIBE] " + username + " unsubscribed");
        }

        return status;
    }

    /**
     * Erstellen einer Registry mit Standardport.
     * Erstellen eines neuen ChatServerImpl Elements, welches der Registry unter dem Namen "ChatServer" zugewiesen wird.
     * @param args
     * @throws RemoteException
     */
    public static void main(String[] args) throws RemoteException {
        Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);;
        ChatServerImpl chatServer = new ChatServerImpl();

        registry.rebind("ChatServer", chatServer);

        System.out.println("Server started");
    }

    /**
     * Liefert die Liste der aktiven Verbindungen zurück.
     * Wird im ChatProxy genutzt, um Nachrichten an alle Clients verteilen zu koennen.
     * @return Liste der aktiven Verbindungen.
     */
    public Map<String, ClientProxy> getActiveChats() {
        return this.chatProxyList;
    }
}
