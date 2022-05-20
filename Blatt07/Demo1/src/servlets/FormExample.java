package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author H.-J. Eikerling
 */
@WebServlet(name = "FormExample", urlPatterns = {"/FormExample"})
public class FormExample extends HttpServlet {

    String tips[] = {"Deutschland", "Brasilien", "Frankreich"};
    int nrTip[] = new int[tips.length];
    String sports[] = {"Fussball", "Nordic Walking", "anderer"};
    int nrSport[] = new int[sports.length];

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        /* Parameter auswerten; ggf. Umleitung auf Fehlerseite */
        String name = req.getParameterValues("name")[0];
        String ft = req.getParameter("tip");
        String[] sport = req.getParameterValues("sport");
        if (name.equals("") || (ft == null) || (sport == null)) {
            res.sendRedirect("ErrorPage.html");
            return;
        }

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        res.setContentType("text/html");        
        out.println("<html><head><title>Danke, " + name + "!</title></head>");
        out.println("<body style=\"font-family:arial;\">\n");
        out.println("<h2>Danke f&uuml;r das Votum, " + name + "!</h2>");
        for (int i = 0; i < tips.length; i++) {
            if (ft.equals(tips[i])) {
                nrTip[i]++;
                break;
            }
        }
        if (sport != null) {
            for (int i = 0; i < sport.length; i++) {
                try {
                    int k = (new Integer(sport[i])).intValue();
                    nrSport[k]++;
                } catch (NumberFormatException ex) {
                    /* Kann bei Versenden über das HTML-Formular nie auftreten */
                    throw new ServletException("uups: " + ex);
                }
            }
        }
        /* Ausgabe der Statistik */
        out.println("<h3>Bisherige Tips:</h3>");
        for (int i = 0; i < tips.length; i++) {
            out.println("<b>" + tips[i] + "</b>:" + nrTip[i] + "<br>");
        }
        out.println("<h3>Statistik zu Sportarten:</h3>");
        for (int i = 0; i < sports.length; i++) {
            out.println("<b>" + sports[i] + "</b>:" + nrSport[i] + "<br>");
        }

        out.println("</body></html>");
        out.close();
    }    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

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
