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

    @Override
    public ChatProxy subscribeUser(String username, ClientProxy handle) throws RemoteException {
        ChatProxyImpl chatProxyImpl = new ChatProxyImpl(this, username);
        ChatProxy chatProxy = (ChatProxy) UnicastRemoteObject.exportObject(chatProxyImpl, 0);

        chatProxyList.put(username, handle);

        System.out.println("[SUBSCRIBE] " + username + " subscribed");

        return chatProxy;
    }

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

    public static void main(String[] args) throws RemoteException, AlreadyBoundException {
        Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);;
        ChatServerImpl chatServer = new ChatServerImpl();

        registry.rebind("ChatServer", chatServer);

        System.out.println("Server started");
    }

    public Map<String, ClientProxy> getActiveChats() {
        return this.chatProxyList;
    }
}
