package servlets;

import de.hsos.vs.web.util.HtmlTemplateLoader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Initiierung einer Session.
 * 
 * @author H.-J. Eikerling
 */
@WebServlet(name = "SessionExample", urlPatterns = {"/SessionExample"})
public class SessionExample extends HttpServlet {

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

        String name = req.getParameterValues("name")[0];
        // Neue Session anlegen
        HttpSession session = req.getSession(true);
        // Objekt ‚name‘ in Session ablegen
        session.setAttribute("name", name);

        // Mapping initialisieren und Template mit Formular aufrufen.
        HashMap<String, String> mappings = new HashMap();
        mappings.put("@@username@@", name);
        
        // Servlet verwendet folgenden Template.
        String template = "templates/FormInput.html";
        try {
            HtmlTemplateLoader.loadTemplateExternal(this, req, template, mappings, out);
        } catch (Exception e) {
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
