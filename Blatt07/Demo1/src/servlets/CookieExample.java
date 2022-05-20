package servlets;

import de.hsos.vs.web.util.HtmlTemplateLoader;
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
 *
 * @author H.-J. Eikerling
 */
@WebServlet(name = "CookieExample", urlPatterns = {"/CookieExample"})
public class CookieExample extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        // User-Name aus Formular lesen und Cookie erzeugen
        String uname = req.getParameterValues("user")[0];
        ServletContext ctxt = getServletContext();
        ctxt.log("Name des Users aus Formular = " + uname);
        res.setContentType("text/html");
        Cookie c = new Cookie("user", uname);
        c.setMaxAge(400); // in Sekunden = 5 min 40 s!
        // nach dieser Zeit ist der Cookie invalide
        // Bereich, in dem Cookie gilt.
        c.setPath("/");
        c.setComment("Personalisierter Gruss");
        res.addCookie(c);

        // Mapping initialisieren und Template mit Formular aufrufen.
        HashMap<String, String> mappings = new HashMap();
        mappings.put("@@username@@", uname);

        // Servlet verwendet folgenden Template.
        String template = "templates/CookieNotifier.html";
        // Response generieren
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
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
     *
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
     *
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
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
