package servlets;

import util.HtmlTemplateLoader;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Empfaenger eines Cookies.
 * 
 * @author H.-J. Eikerling
 */
@WebServlet(name = "CookieRecExample", urlPatterns = {"/CookieRecExample"})
public class CookieRecExample extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        
        // Cookie auswerten
        Cookie[] cs = req.getCookies();
        String user = "Unknown";
        for (int i = 0; i < cs.length; i++) {
            Cookie c = cs[i];
            if (c.getName().equals("user")) {
                user = c.getValue();
                break;
            }
        }
        ServletContext ctxt = this.getServletContext();
        ctxt.log("Cookie erhalten = " + user);
        
        // Mapping initialisieren und Template mit Formular aufrufen.
        HashMap<String, String> mappings = new HashMap();
        mappings.put("@@username@@", user);
        mappings.put ("@@cookie_session_id@@", req.getHeader("Cookie"));
        
        // Response generieren
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        
        // Servlet verwendet folgenden Template.
        String template = "templates/CookieReceiver.html";
        try {
            HtmlTemplateLoader.loadTemplateExternal(this, req, template, mappings, out);
        } catch (IOException e) {
            HtmlTemplateLoader.showError(out, req, template);
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
