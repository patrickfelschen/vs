<%-- 
    Document   : CalculatorWithBean
    Created on : 01.06.2014, 09:03:48
    Author     : hje
--%>
<html>
    <head>
        <title>Parameterauswertung per Java Bean</title>
    </head>
    <jsp:useBean id="calc" class="de.hsos.vs.web.beans.CalcBean" scope="session"/>
    <body style="font-family:arial;">
        <h1>Parameterauswertung per Java Bean</h1>
        <jsp:setProperty name="calc" property="*"/>
        <form method="POST" action=" CalculatorWithBean.jsp">
            <input type="text" size="2" name="op1"> +
            <input type="text" size="2" name="op2">
            <input type="submit" value="Eingabe">
        </form>

        <%  if (request.getMethod().equals("POST")) {
                out.println("<hr>Das Ergebnis: "
                        + calc.getOp1() + " + " + calc.getOp2() 
                        + " = <b>" + calc.getSumme() + "</b>");
            }
        %>
    </body>
</html>

