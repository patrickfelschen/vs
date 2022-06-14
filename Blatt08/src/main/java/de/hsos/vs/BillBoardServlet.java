package de.hsos.vs;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementierung des BillBoard-Servers.
 * In dieser Version unterstützt er asynchrone Aufrufe.
 * Damit wird die Implementierung von Long Polling möglich:
 * Anfragen (HTTP GET) werden nicht sofort wie bei zyklischem
 * Polling beantwortet sondern verbleiben so lange im System,
 * bis eine Änderung an den Client gemeldet werden kann.
 *
 * @author heikerli
 */
@WebServlet(asyncSupported = true, urlPatterns = {"/BillBoardServer"})
public class BillBoardServlet extends HttpServlet {
  // private final BillBoardHtmlAdapter bb = new BillBoardHtmlAdapter("BillBoardServer");
  private final BillBoardJsonAdapter bbJson = new BillBoardJsonAdapter("BillBoardServer");
  private final List<AsyncContext> contexts = new LinkedList<>();

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request  servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException      if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    /* Ausgabe des gesamten Boards */
    AsyncContext asyncContext = request.startAsync(request, response);
    asyncContext.setTimeout(10 * 60 * 1000);
    contexts.add(asyncContext);
    // https://www.oracle.com/webfolder/technetwork/tutorials/obe/java/async-servlet/async-servlets.html
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request  servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException      if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String caller_ip = request.getRemoteAddr();

    List<AsyncContext> asyncContexts = new ArrayList<>(this.contexts);
    this.contexts.clear();

    JSONObject data = getJSONBody(request.getReader());
    String name = data.getString("name");

    bbJson.createEntry(name, caller_ip);

    completeContext(asyncContexts);
  }

  private void completeContext(List<AsyncContext> asyncContexts) {
    for (AsyncContext asyncContext : asyncContexts) {
      try (PrintWriter writer = asyncContext.getResponse().getWriter()) {
        String table = bbJson.readEntries(asyncContext.getRequest().getRemoteAddr());
        writer.println(table);
        writer.flush();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        asyncContext.complete();
      }
    }
  }

  /**
   * Handles the HTTP <code>DELETE</code> method.
   *
   * @param request  servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException      if an I/O error occurs
   */
  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // TODO implementation of doDelete()!
    List<AsyncContext> asyncContexts = new ArrayList<>(this.contexts);
    this.contexts.clear();

    String caller_ip = request.getRemoteAddr();
    JSONObject data = getJSONBody(request.getReader());
    int id = data.getInt("id");
    bbJson.deleteEntry(id, caller_ip);

    completeContext(asyncContexts);
  }

  /**
   * Handles the HTTP <code>PUT</code> method.
   *
   * @param request  servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException      if an I/O error occurs
   */
  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String caller_ip = request.getRemoteAddr();
    // TODO implementation of doPut()!
    List<AsyncContext> asyncContexts = new ArrayList<>(this.contexts);
    this.contexts.clear();

    JSONObject data = getJSONBody(request.getReader());
    int id = data.getInt("id");
    String name = data.getString("name");
    bbJson.updateEntry(id, name, caller_ip);

    completeContext(asyncContexts);
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "BillBoard Servlet";
  }// </editor-fold>

  private JSONObject getJSONBody(BufferedReader reader) throws IOException {
    StringBuilder jb = new StringBuilder();
    String line = null;
    try {
      while ((line = reader.readLine()) != null)
        jb.append(line);
    } catch (Exception e) { /*report an error*/ }

    try {
      JSONObject jsonObject = new JSONObject(jb.toString());
      System.out.println(jsonObject);
      return jsonObject;
    } catch (JSONException e) {
      throw new IOException("Error parsing JSON request string");
    }
  }

}
