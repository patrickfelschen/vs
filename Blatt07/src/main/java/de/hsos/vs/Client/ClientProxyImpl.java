package de.hsos.vs.Client;

import java.rmi.RemoteException;

public class ClientProxyImpl implements ClientProxy{
    /**
     * Eine empfangene Nachricht wird auf der Kommandozeile mit dem ensprechenden Nutzernamen als Präfix ausgegeben
     * @param username Zugehöriger Absender zur Nachricht
     * @param message Empfangene Nachricht
     * @throws RemoteException
     */
    @Override
    public void receiveMessage(String username, String message) throws RemoteException {
        System.out.println(username + ": " + message);
    }
}
