package servlets;

import util.HtmlTemplateLoader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 *
 * @author H.-J. Eikerling
 */
@WebServlet(name = "SessionEndExample", urlPatterns = {"/SessionEndExample"})
public class SessionEndExample extends HttpServlet {

    int de_count;
    int br_count;
    int fr_count;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        
        // Mapping fuer Template anlegen.
        HashMap<String, String> mappings = new HashMap();
        String template = "templates/SessionError.html";
        HttpSession session = req.getSession(false);
        res.setContentType("text/html");
        if (session != null) {
            // Formular konfigurieren
            template = "templates/FormResult.html";
            String name = (String) session.getAttribute("name");
            String tip = req.getParameterValues("tip")[0];
            if ("Frankreich".equals(tip)) fr_count++;
            else if ("Deutschland".equals(tip)) de_count++;
            else if ("Brasilien".equals(tip)) br_count++;
 
            // Ausgabe erzeugen
            mappings.put("@@username@@", name);
            mappings.put("@@de_count@@", String.valueOf(de_count));
            mappings.put("@@fr_count@@", String.valueOf(fr_count));
            mappings.put("@@br_count@@", String.valueOf(br_count));
            session.invalidate();
        }
        try {
            // Template laden
            HtmlTemplateLoader.loadTemplateExternal(this, req, template, mappings, out);
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
