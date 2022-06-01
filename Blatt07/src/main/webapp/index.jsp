<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
            <title>Blatt07</title>
    </head>
    <body>
        <form method="POST" action="client-servlet">
            <h1>Chat-System Anmeldung</h1>
            <label for="username">Name:</label>
            <input type="text" name="username" id="username">
            <input type="hidden" name="subscibe">
            <button>Anmelden</button>
        </form>
    </body>
</html>