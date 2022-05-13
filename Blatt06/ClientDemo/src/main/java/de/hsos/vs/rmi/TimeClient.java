/*
 * Client for TimeServer.
 */
package main.java.de.hsos.vs.rmi;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;

public class TimeClient {

    public static void main(String[] args) throws MalformedURLException {
        String refURL = "rmi://localhost:1099/TimeServer";
        try {
            // Zwei Moeglichkeiten, auf die Registry zuzugreifen:
            // 1. Option: Referenz auf Registry bestimmen und darauf lookup()
            // durchführen.
            // Registry registry = LocateRegistry.getRegistry();
            // TimeServer timer = (TimeServer) registry.lookup("TimeServer");
            // 2. Option: Per URL direkt auf registriertes Interface zugreifen.
            TimeServerInterf stub = (TimeServerInterf) Naming.lookup (refURL);
            System.out.println ("Timer Stub: " + stub);
            long tm = stub.getTime(); 
            System.out.println("TimeServer: " + tm + " (" + new Date (tm) + ")");
        } catch (RemoteException e) {
            System.err.println ("RemoteException: while accessing '" + refURL +   
                    "'.\nCheck that server & registry are running.");
        } catch (NotBoundException e) {
            System.err.println ("RemoteException: while accessing '" + refURL +   
                    "'.\nRemote object not found. Start server.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
