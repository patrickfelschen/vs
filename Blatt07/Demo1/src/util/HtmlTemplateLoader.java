/*
 * Laden von Html-Templates. 
 * Sehr einfache, aber kompakte Loesung, die den Einsatz
 * einer Template-Engine vermeidet.
 */

package util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * In den Html-Templates koennen
 * @@tags@@ per folgender Methode durch Werte ersetzt werden.
 * 
 * @author heikerli
 */
public class HtmlTemplateLoader {

    
    /* Unix directory separator = default */
    private static String getSeparator() {
        String os = System.getProperties().getProperty("os.name");
        if (os.contains("Windows")) {
            return ("\\");
        }
        return "/"; 
    }
    
    /**
     * Bestimmt den absolutenl lokalen Pfad zur Web-Applikation.
     * Relativ dazu soll die zu ladende Datei (= Template) angegeben werden.
     * Es wird angenommen, dass der Template im Web-Projekt enthalten. 
     * Dies vermeidet das Laden der Templates über eine Http-Verbindung, 
     * was auch moeglich aber ineffizient waere.
     *
     * @param contextPath Pfad des Root-Kontextes der Web-Applikation.
     * @param servletPath Absoluter Pfad zum Servlet-Verzeichnis.
     * @return Absoluter Pfad zum Root-Kontext im Dateisystem.
     */
    private static String getWebAppSourcePath(String contextPath, String servletPath, String sep) {
        String path = "";
        StringTokenizer st = new StringTokenizer(servletPath, sep);
        /* Sucht Kontext-Namen im Servlet Pfad */
        /* Annahme: Java-Klassenname und Kontext stimmen überein */
        while (st.hasMoreTokens()) {
            String dir = st.nextToken();
            path += dir + sep; 
            if (dir.equals(contextPath)) {
                break;
            }
        }
        //path += sep;
        return path;
    }  
        
    /**
     * Die Mappings der Tags sind in der HashMap zu hinterlegen.
     */
    private static void loadTemplate(String fullPath, HashMap<String, String> mappings,
            PrintWriter out) throws IOException {

        try {
            FileInputStream fstream = new FileInputStream(fullPath);
            if (fstream.available() == 0) {
                throw new IOException();
            }
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (strLine.contains("@@")) {
                    Iterator it = mappings.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        String tag = (String) pair.getKey();
                        String val = (String) pair.getValue();
                        strLine = strLine.replaceAll(tag, val);
                    }
                }
                out.println(strLine);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            throw (e);
        } catch (Exception e) {
            System.out.println("Unknown error: " + e.getMessage());
        } finally {
        }
    }

    /**
     * Internes Laden eines Templates ueber das lokale Dateisystem.
     */
    public static void loadTemplateInternal(HttpServlet servlet, HttpServletRequest request,
            String fName, HashMap<String, String> mappings, PrintWriter out)
            throws IOException {
        
        String contextPath = request.getContextPath().replace("/", "");
        System.out.println("Context Path: " + contextPath);
        
        String sep = getSeparator();
        String servletPath = servlet.getServletContext().getRealPath(sep);
        System.out.println("Servlet Path: " + servletPath);

        String spath = HtmlTemplateLoader.getWebAppSourcePath(contextPath, servletPath, getSeparator());
        System.out.println("Path: " + spath);

        String fullPath = spath + fName;
        System.out.println("Fullpath: " + fullPath);

        HtmlTemplateLoader.loadTemplate(fullPath, mappings, out);
    }
    
    /**
     * Externes Laden eines Templates via Web-Server.
     * Vereinfacht den Einsatz in Web-Formularen.
     */
    public static void loadTemplateExternal(HttpServlet servlet, HttpServletRequest request,            
            String fName, HashMap<String, String> mappings, PrintWriter out)
            throws IOException {
        
        StringBuffer servURL = request.getRequestURL();
        String context = servlet.getServletContext().getContextPath();
        context = context.replace ("/", "");
        // System.out.println("Context Path: " + context);
        
        /* Base URL bestimmen. Dabei Schema korrigieren. */
        String baseURL = getWebAppSourcePath (context, servURL.toString(), "/");
        baseURL = baseURL.replace ("http:/", "http://");
        baseURL = baseURL.replace ("https:/", "https://");
        
        String templateURL = baseURL + "/" + fName;
        InputStream in = new URL(templateURL).openStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        while ((strLine = br.readLine()) != null) {
            if (strLine.contains("@@")) {
                Iterator it = mappings.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    String tag = (String) pair.getKey();
                    String val = (String) pair.getValue();
                    strLine = strLine.replaceAll(tag, val);
                }
            }
            out.println(strLine);
        }
        System.out.println("Template " + templateURL + " loaded.");
        
    }   

    /**
     * Fehler: Template kann nicht gefunden werden.
     * Das koennte man auch durch einen Template realisieren.
     * Der muesste dann aber in jedem Fall zur Verfuegung stehen und 
     * selbst nicht zugegriffen werden koennen.
     */
    public static void showError(PrintWriter out, HttpServletRequest request, String fName) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>FileTemplateLoader: Error</title>");
        out.println("</head>");
        out.println("<body style=\"font-family:arial;\">");
        out.println("<h1>Error in FileTemplateLoader at "
                + request.getContextPath() + "</h1>");
        out.println("Template file '" + fName + "' not found.<br>");
        out.println("</body>");
        out.println("</html>");
    }
}
