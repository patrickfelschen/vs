package Connection;

import Client.ClientProxy;
import Server.ChatServerImpl;

import java.rmi.RemoteException;

public class ChatProxyImpl implements ChatProxy {
    private ChatServerImpl chatServer;
    private String username;

    /**
     * @param chatServer Referenz auf den aktiven ChatServer
     * @param username Zu gesendeter Nachricht gehoerender Nutzer
     */
    public ChatProxyImpl(ChatServerImpl chatServer, String username) {
        this.chatServer = chatServer;
        this.username = username;
    }

    /**
     * Gesendete Nachricht wird an alle aktiven Clients per receiveMessage() Funktion weitergeleitet.
     * @param message Gesenedete Nachricht.
     * @throws RemoteException
     */
    @Override
    public void sendMessage(String message) throws RemoteException {
        for(ClientProxy proxy : chatServer.getActiveChats().values()) {
            proxy.receiveMessage(this.username, message);
        }
    }
}
