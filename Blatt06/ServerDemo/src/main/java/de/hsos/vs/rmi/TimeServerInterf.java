/*
 * Java interface of Time server.
 */

package main.java.de.hsos.vs.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TimeServerInterf extends Remote {
    public long getTime() throws RemoteException;
}

