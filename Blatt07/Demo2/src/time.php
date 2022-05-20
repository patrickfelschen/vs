<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Einfache PHP Seite</title>
    </head>
    <body style="font-family:arial;">
        <h2>Aktuelle Zeit:</h2>  
            <?php
            $datum_uhrzeit = date("d.m.Y, H:i:s", time());
            echo "Heute ist der " . $datum_uhrzeit . " Uhr";
            ?>
    </body>
</html>
