<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Verarbeitung von Formularen (Php)</title>
    </head>
    <body style="font-family:arial;">
        <h2>Verarbeitung von Formularen mit PHP</h2>
        <form name="form1" action="form_processing.php" method="post">
            <table border="0" cellpadding="0" cellspacing="4">
                <tr>
                    <td align="left"> Vorname:</td>
                    <td><input type="text" size="30" name="firstname"></td>
                </tr>
                <tr>
                    <td align="left"> Nachname: </td>
                    <td><input type="text" size="30" name="lastname"></td>
                <tr>
                    <td align="left"> Alter:</td>
                    <td><input type="text" size="3" name="age"></td>
                </tr>
            </table>   
            <br>Geschlecht:
            <input type="radio" name="gender" value="male"> männlich
            <input type="radio" name="gender" value="female"> weiblich<br><br>
            <input type="submit" value="Absenden"> <input type="reset">
        </form>
    </body>
</html>