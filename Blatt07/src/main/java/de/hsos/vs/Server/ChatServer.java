package de.hsos.vs.Server;

import de.hsos.vs.Client.*;
import de.hsos.vs.Connection.*;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatServer extends Remote {
    public ChatProxy subscribeUser (String username, ClientProxy handle) throws RemoteException;
    public boolean unsubscribeUser(String username) throws RemoteException;
}
