import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ChatServerImpl implements ChatServer {

    @Override
    public ChatProxy subscribeUser(String username, ClientProxy handle) throws RemoteException {
        return null;
    }

    @Override
    public boolean unsubscribeUser(String username) throws RemoteException {
        return false;
    }

    public static void main(String[] args) throws RemoteException {
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
            ChatServerImpl chatSrv = new ChatServerImpl();
            ChatServer stub = (ChatServer) UnicastRemoteObject.exportObject(chatSrv,0);
            registry.rebind("ChatServer", stub);
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
        System.err.println ("ChatServerImpl ready ...");

    }
}
