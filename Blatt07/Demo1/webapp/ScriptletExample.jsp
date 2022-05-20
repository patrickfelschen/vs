<%-- 
    Document   : ScriptletExample.jsp
    Created on : 27.05.2015, 14:55:30
    Author     : hje
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <head>
        <title>Einfache Java Server Page</title>
    </head>
    <body style="font-family:arial;">
        <h1>Einfache Java Server Page</h1>
        <%@ page language="java" import="java.util.Date" %>
        <h2>Berechnung von Quadraten per JSP</h2>
        <%! int i = 0; int z = 0; private int accessCount = 0;%>
        <ol>
            <% for (int i = 1; i <= 10; i++) {
                    z = i * i;
            %>
            <li><%= z%></li><%}%>
        </ol>
        <h2>Weitere Scriptlets</h2>
        <%  out.println("Zugriff von Rechner: " + request.getRemoteHost());%><p>
        Diese Seite wurde am <%= new Date()%><br> zum
        <Font color="F00000"><%= ++accessCount%></Font>. mal besucht.
    </body>
</html>
