package de.hsos.vs.Client;

import de.hsos.vs.Client.ClientProxy;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ClientProxyServletImpl implements ClientProxy {

  private final List<String> messages;

  public ClientProxyServletImpl() {
    this.messages = new ArrayList<>();
  }

  public void setMessages(List<String> messages) {
    if(messages == null) return;
    this.messages.addAll(messages);
  }

  public List<String> getMessages() {
    return messages;
  }

  @Override
  public void receiveMessage(String username, String message) throws RemoteException {
    messages.add(username + ": " + message);
  }
}
