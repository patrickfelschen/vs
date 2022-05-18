package Connection;

import Server.ChatServerImpl;

import java.rmi.RemoteException;
import java.util.HashMap;

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
        new HashMap<>(chatServer.getActiveChats()).forEach((username, clientProxy) -> {
            try {
                if(!this.username.equals(username)) {
                    clientProxy.receiveMessage(this.username, message);
                }
            } catch (RemoteException e) {
                try {
                    chatServer.unsubscribeUser(username);
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
                System.out.println(this.username + " konnte nicht erreicht werden.");
            }
        });
    }
}
