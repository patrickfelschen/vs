package servlets;

import de.hsos.vs.web.util.HtmlTemplateLoader;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Eine beliebige Datei (txt, html, xml,...) wird gelade und die als Parameter
 * üebergebenen Tags werden durch die entsprechenden Werte ersetzt.
 * Tags/Werte-Paare werden z.B. mit ?filename=UserTemplate.html&
 *
 * @@username@@=heikerli&
 * @@location@@=Osnabrueck uebergeben.
 * 
 * @author H.-J. Eikerling
 */
@WebServlet(name = "FileTemplateLoader", urlPatterns = {"/FileTemplateLoader"})
public class FileTemplateLoader extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String fName = "";
        HashMap<String, String> mappings = new HashMap();

        Enumeration en = request.getParameterNames();
        while (en.hasMoreElements()) {
            String paramName = (String) en.nextElement();
            String value = request.getParameter(paramName);
            if (paramName.equals("filename")) {
                fName = value;
            }
            mappings.put(paramName, value);
            System.out.println(paramName + " = " + value);
        }
        
        PrintWriter out = response.getWriter();
        try {
            // HtmlTemplateLoader.loadTemplateInternal(this, request, fName, mappings, out);
            // Man koennte auch folgendes tun:
            HtmlTemplateLoader.loadTemplateExternal(this, request, 
                    "templates/UserTemplate.html", mappings, out);
            // Der Dateiname in der Schablone wird dann ignoriert.
            // Dies ist anzuwenden, wenn das Servlet nicht im lokalen Projekt läuft, sondern
            // dem Server als .war deployed wird. Dann sind die Templates nicht via Source-Verzeichnis
            // zugreifbar.
        } catch (Exception e) {
            HtmlTemplateLoader.showError(out, request, fName);
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
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
     * Handles the HTTP
     * <code>POST</code> method.
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
