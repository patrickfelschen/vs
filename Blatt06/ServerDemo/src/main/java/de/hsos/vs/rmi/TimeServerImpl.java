/*
 * Implementation of TimeServer.
 */
package main.java.de.hsos.vs.rmi;

import java.rmi.RemoteException;
//import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;

public class TimeServerImpl implements TimeServerInterf {
    TimeServerImpl() throws RemoteException {
        // Ctor emittiert ggf. RemoteException
        System.out.println ("TimeServerImpl (v3) created...");
    }

    public long getTime() throws RemoteException {
        return System.currentTimeMillis();
    }

    // Singleton-Objekt: wichtig um die Objekt-Referenz zu erhalten!
    // Siehe (**)
    static TimeServerImpl srv = null;

    public static void main(String[] args) {
        // Man kann entweder eine lokale Registry erzeugen ...
        // LocateRegistry.createRegistry (Registry.REGISTRY_PORT);
        // ... oder verwendet die Standard-RMI Registry als externer
        // Prozess auf dem lokalen Host unter Port 1099.
        // In diesem Fall wird mit getRegistry() auf diese zugegriffen.
        // Bei Verwendung der externen Registry kann es zu einer ClassNotException kommen,
        // wenn rmiregistry keinen Zugriff auf die Stub-Klasse TimeServer hat.
        // 'rmiregistry 1099' sollte also im Verzeichnis build/classes aufgerufen
        // werden (alternativ kann auch der Classpath angepasst werden).
        //
        // Die Konfiguration von Policies wird in der Datei
        // timer.policy gezeigt.


        Registry registry = null;
        try {
            registry = LocateRegistry.createRegistry (Registry.REGISTRY_PORT);
            // registry = LocateRegistry.getRegistry();
        } catch (RemoteException rex) {
            System.err.println ("RemoteException occured: " +  rex);
        }
        if (registry == null) {
            System.err.println ("Cannot find registry");
            System.exit(0);
        }

        try {
            // Objekt anlegen und exportieren des Server-Objektes
            srv = new TimeServerImpl();
            System.out.println ("Time Server: " + srv);
            TimeServerInterf stub = (TimeServerInterf) UnicastRemoteObject.exportObject(srv, 0);
            // Stub registrieren
            registry.rebind ("TimeServer", stub);
            // Es gibt auch eine statische Methode, die verwendet werden kann.
            // Naming.rebind ("TimeServer", new TimeServerImpl());
            System.out.println ("TimeServerImpl registered as 'TimeServer' ...");
        } catch (java.rmi.ConnectException cex) {
            System.err.println ("ConnectException while accessing registry (port = " + Registry.REGISTRY_PORT + ")");
            System.err.println ("Exiting. Run 'rmiregistry " + Registry.REGISTRY_PORT + "' and restart.");
            System.exit(0);
        } catch (java.rmi.ServerException rex) {
            System.err.println ("ServerException during server registration (registry port = " + Registry.REGISTRY_PORT + ")");
            System.err.println ("Registry needs access to interface classes folder. Exiting. ");
            System.exit(0);
        } catch (Exception ex) {
            System.err.println ("Exception during server registration (registry port = " + Registry.REGISTRY_PORT + "). Exiting.");
            ex.printStackTrace();
            System.exit(0);
        }
        System.err.println ("TimeServerImpl ready for requests ...");
        // (**) Endlosschleife: notwendig, wenn man bei einer per createRegistry() erzeugten
        // lokalen RMI Registry und lokal deklarierten Server-Objekt arbeiten will.
        // Ansonsten verwirft der Garbage Collector das Server-Objekt, womit keine Anfragen mehr
        // möglich sind!
        // while (true) {}
    }
}
