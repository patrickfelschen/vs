<%-- 
    Document   : CalcExample.jsp
    Created on : 01.06.2014, 09:15:14
    Author     : hje
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <head>
        <title>Parameterauswertung per JSP</title>
    </head>
    <body style="font-family:arial;">
        <h1>Parameterauswertung per JSP</h1>
        <!-- Definition von globalen Informationen fuer die Seite -->
        <%@ page language="java" %>
        <!-- Deklaration der Variablen -->
        <%! int x = 0;
            int y = 0;
            int z = 0;%>
        <!-- 2. Scriptlet - Java code -->
        <%  try {
                x = Integer.parseInt(request.getParameter("op1"));
                y = Integer.parseInt(request.getParameter("op2"));
                z = x + y;
                out.println("<h2>Ergebnis</h2>\nDas Ergebnis lautet: " + z);
            } catch (Exception e) {
                out.println("<h2>Fehler</h2>\nAufruf mit fehlerhaften Werten.");
            }
        %>
    </body>
</html>
