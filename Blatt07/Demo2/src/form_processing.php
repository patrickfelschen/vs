<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Verarbeitung von Formularen</title>
    </head>
    <body style="font-family:arial;">
        <h2>Registration erfolgreich</h2>
        Hallo
        <?php
        if  ( $_POST['age'] < 16 ) {
            echo $_POST['firstname'];
        } elseif ( $_POST['gender'] == "female") {
            echo " Frau " . $_POST['lastname'];
        } else {
            echo " Herr " . $_POST['lastname'];
        }
        ?>,
        sie sind jetzt registriert.
    </body>
</html>