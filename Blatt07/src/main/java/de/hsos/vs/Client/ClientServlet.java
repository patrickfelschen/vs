package de.hsos.vs.Client;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import de.hsos.vs.Connection.ChatProxy;
import de.hsos.vs.Server.ChatServer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "clientServlet", value = "/client-servlet")
public class ClientServlet extends HttpServlet {
  private Registry registry;
  private ChatServer chatServer;
  private HttpSession session;
  private String username;
  private boolean loggedIn;

  enum SubscribeStatus { SUCCESS, EXISTS, SERVERNOTFOUND, ERROR }
  enum UnsubscribeStatus{ SUCCESS, SERVERNOTFOUND, ERROR }
  enum SendMessageStatus{ SUCCESS, ERROR }

  @Override
  public void init() throws ServletException {
    try{
      registry = LocateRegistry.getRegistry(12345);
      chatServer = (ChatServer) registry.lookup("ChatServer");
    } catch (RemoteException | NotBoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    this.loggedIn = req.getSession(false).getAttribute("username") != null;
    this.handleRequest(req, res);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    this.loggedIn = req.getSession(false).getAttribute("username") != null;
    this.handleRequest(req, res);
  }

  private void handleRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
    System.out.println("request from: "+req.getRemoteAddr());
    res.setContentType("text/html");

    PrintWriter out = res.getWriter();
    out.println("<html><body>");

    SubscribeStatus subscribeStatus;
    session = req.getSession(true);

    ClientProxyServletImpl clientProxy = (ClientProxyServletImpl) session.getAttribute("ClientProxy");

    if(req.getParameter("subscribe") != null) {
      username = req.getParameter("username");
      session.setAttribute("username", username);
      subscribeStatus = subscribe();
    }else{
      clientProxy.setMessages((ArrayList<String>) session.getAttribute("messages"));
      subscribeStatus = SubscribeStatus.SUCCESS;
    }

    if(req.getParameter("send") != null) {
      String message = req.getParameter("message");
      if (message != null && message.trim().length() > 0) {

        SendMessageStatus messageStatus = sendMessage(message);

        switch (messageStatus) {
          case SUCCESS: {
            break;
          }
          case ERROR: {
            out.println("<script>alert(\"Nachricht konnte nicht zugestellt werden.)</script>\"");
            break;
          }
        }
      }
    }

    if(req.getParameter("close") != null){
      UnsubscribeStatus unsubscribeStatus = unsubscribe();
      switch (unsubscribeStatus){
        case SUCCESS:{
          session.removeAttribute("username");
          session.invalidate();
          res.sendRedirect(".");
          break;
        }
        case SERVERNOTFOUND:{
          out.println("<h1>Server nicht gefunden!</h1>");
          out.println("<a href=\".\">Zurück zur Anmeldung</a>");
          break;
        }
        case ERROR: {
          out.println("<h1>Abmelden fehlgeschlagen!</h1>");
          out.println("<a href=\".\">Zurück zur Anmeldung</a>");
          break;
        }
      }
      return;
    }

    switch (subscribeStatus){
      case SUCCESS:{
        out.println("<meta http-equiv=\"refresh\" content=\"10\">");
        out.println("<h1>Login als User: " + session.getAttribute("username") + "</h1>");
        out.println("<form method=\"POST\">");
        out.println("<label for=\"message\">Nachricht:</label>");
        out.println("<input type=\"text\" name=\"message\" id=\"message\">");
        out.println("<button type=\"submit\" name=\"send\">Senden</button>");
        out.println("<br><textarea name=\"chatoutput\" cols=\"50\" rows=\"10\" readonly>");
        if(clientProxy != null) {
          for (String m : clientProxy.getMessages()) {
            out.println(m);
          }
        }
        out.println("</textarea>");
        out.println("<br><button type=\"submit\" name=\"close\">Beenden</button>");
        out.println("</form>");
        break;
      }
      case EXISTS:{
        out.println("<h1>Session bereits vorhanden!</h1>");
        out.println("<a href=\".\">Zurück zur Anmeldung</a>");
        break;
      }
      case SERVERNOTFOUND:{
        out.println("<h1>Server nicht gefunden!</h1>");
        out.println("<a href=\".\">Zurück zur Anmeldung</a>");
        break;
      }
      case ERROR:{
        out.println("<h1>Keine Session vorhanden!</h1>");
        out.println("<a href=\".\">Zurück zur Anmeldung</a>");
        break;
      }
    }

    out.println("</body></html>");
    out.close();
  }

  private SubscribeStatus subscribe() {
    if(loggedIn){
      return SubscribeStatus.EXISTS;
    }

    ClientProxyServletImpl clientProxy = new ClientProxyServletImpl();

    try {
      ClientProxy handle = (ClientProxy) UnicastRemoteObject.exportObject(clientProxy, 0);
      ChatProxy chatProxy = chatServer.subscribeUser(username, handle);
      this.session.setAttribute("ChatProxy", chatProxy);
      this.session.setAttribute("ClientProxy", clientProxy);
    } catch (RemoteException e) {
      return SubscribeStatus.SERVERNOTFOUND;
    }

    if(session.getAttribute("ChatProxy") == null) {
      return SubscribeStatus.ERROR;
    } else {
      return SubscribeStatus.SUCCESS;
    }
  }

  private UnsubscribeStatus unsubscribe() {
    if(!loggedIn) {
      return UnsubscribeStatus.ERROR;
    }

    boolean status = false;

    try {
      status = chatServer.unsubscribeUser(username);
    } catch (RemoteException e) {
      return UnsubscribeStatus.SERVERNOTFOUND;
    }

    if(!status) {
      return UnsubscribeStatus.ERROR;
    }else {
      return UnsubscribeStatus.SUCCESS;
    }
  }

  private SendMessageStatus sendMessage(String message) {
    if(!loggedIn) {
      return SendMessageStatus.ERROR;
    }

    try {
      ChatProxy chatProxy = (ChatProxy) session.getAttribute("ChatProxy");
      chatProxy.sendMessage(message);
    } catch (RemoteException e) {
      return SendMessageStatus.ERROR;
    }

    return SendMessageStatus.SUCCESS;
  }

  @Override
  public void destroy() {
    session.removeAttribute("username");
    session.removeAttribute("ChatProxy");
    session.removeAttribute("messages");
    session.invalidate();
  }
}