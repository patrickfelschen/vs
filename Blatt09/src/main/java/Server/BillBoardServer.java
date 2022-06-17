package Server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BillBoardServer extends WebSocketServer {
    private static final int TCP_PORT = 4447;
    private final Set<WebSocket> webSockets;

    private final List<BillBoardEntry> billBoardEntries;

    public BillBoardServer() {
        super(new InetSocketAddress(TCP_PORT));
        webSockets = new HashSet<>();
        billBoardEntries = new ArrayList<>();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        webSockets.add(webSocket);
        System.out.println("New connection from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        webSockets.remove(webSocket);
        System.out.println("Closed connection to " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println("Message from client: " + s);

        JSONObject jsonObject = new JSONObject(s);
        String method = jsonObject.getString("method");
        String caller_ip = webSocket.getRemoteSocketAddress().getAddress().getHostAddress();

        if (method.equals("ADD")) {
            System.out.println("ADD");
            JSONObject data = jsonObject.getJSONObject("data");
            BillBoardEntry newEntry = new BillBoardEntry(data.getInt("id"), data.getString("name"), caller_ip);
            billBoardEntries.add(newEntry);

            for (WebSocket socket : webSockets) {
                JSONObject response = new JSONObject();
                response.put("method", "RESPONSE");
                response.put("data", newEntry.toJsonObject());
                response.put("status", 200);
                socket.send(response.toString(4));
            }
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        if (webSocket != null) {
            webSockets.remove(webSocket);
        }
        System.out.println("ERROR from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onStart() {

    }
}
