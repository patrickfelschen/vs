import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatServer extends Remote {
    public void sendMessage(String message) throws RemoteException;
}
