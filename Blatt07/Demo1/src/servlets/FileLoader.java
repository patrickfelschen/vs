package servlets;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author H.-J. Eikerling
 */
@WebServlet(asyncSupported = true, name = "FileLoader", urlPatterns = {"/FileLoader"})
public class FileLoader extends HttpServlet {

    static final int DEFAULT_MILSECONDS_DELAY = 2000;
    
    static private String getExtension(String fullPath) {
        int dot = fullPath.lastIndexOf('.');
        return fullPath.substring(dot + 1);
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AsyncContext ac = request.startAsync();
        String path = getServletContext().getRealPath("/");
        System.err.print ("getServletContext().getRealPath(\"/\"): " + path);
        String fullPath = path + request.getParameter("filename");
        if (getExtension(fullPath).equals("xml"))
           response.setContentType("text/xml;charset=UTF-8");
        else 
           response.setContentType("text/html;charset=UTF-8");
        System.err.println("Accessing file '" + fullPath + "'");
        PrintWriter out = response.getWriter();
        
        String delayString = request.getParameter("delay");
        Integer i = DEFAULT_MILSECONDS_DELAY;
        if (delayString != null) 
               i = Integer.parseInt(delayString);
        try {
            Thread.sleep((int) i);
            
            FileInputStream fstream = new FileInputStream(fullPath);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                out.println(strLine);
            }
        } catch (Exception e) {
            // catch exception if any
            System.out.println("Error: " + e.getMessage());
        } finally {
            out.close();
        }
        ac.complete();
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
