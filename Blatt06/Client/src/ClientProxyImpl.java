import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientProxyImpl implements ClientProxy{
    @Override
    public void receiveMessage(String username, String message) throws RemoteException {

    }

    public static void main(String[] args) {
        Registry registry = null;

        try {
            registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
        } catch (RemoteException rex) {
            System.err.println ("RemoteException occured: " +  rex);
        }
        if (registry == null) {
            System.err.println ("Cannot find registry");
            System.exit(0);
        }
    }
}
