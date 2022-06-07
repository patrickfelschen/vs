globGetMethod = 0; /* 0: html; 1: xyz */

function setGetMethod (val) {
    globGetMethod = val;
} 

function $(id) {
    return document.getElementById(id);
}

function getXMLHttpRequest() {
    // XMLHttpRequest for Firefox, Opera, Safari
    if (window.XMLHttpRequest) {
        return new XMLHttpRequest();
    }
    if (window.ActveObject) { // Internet Explorer
        try { // for IE new
            return new ActiveXObject("Msxml2.XMLHTTP");
        }
        catch (e)  {  // for IE old
            try {
                return new ActiveXObject("Microsoft.XMLHTTP");
            }
            catch (e)  {
                alert("Your browser does not support AJAX!");
                return null;
            }
        }
    }    
    return null;
} 

function getHttpRequest(url) {
    if (globGetMethod == 0)
        getHtmlHttpRequest(url);
    else
        /* xyz = JSON oder XML .... */
        getxyzHttpRequest(url);
}

function getHtmlHttpRequest(url) {
    var xmlhttp = getXMLHttpRequest(); 
    xmlhttp.open("GET", url, true);
    xmlhttp.onreadystatechange = function() {
        if(xmlhttp.readyState != 4) {
            $('posters').innerHTML = 'Seite wird geladen ...';
        }
        if(xmlhttp.readyState == 4 && xmlhttp.status == 200) {
            $('posters').innerHTML = xmlhttp.responseText;
        }
        $('timestamp').innerHTML = new Date().toString();
    };
    xmlhttp.send(null);
}

// https://stackoverflow.com/questions/24468459/sending-a-json-to-server-and-retrieving-a-json-in-return-without-jquery
function getxyzHttpRequest(url) {
    // TO BE IMPLEMENTED!!!
}

function postHttpRequest(url) {
    // TO BE IMPLEMENTED!!!
    let name = document.getElementById("contents").value;

    let xhr = new XMLHttpRequest();
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
            let json = JSON.parse(xhr.responseText);
            console.log(json.name);
        }
    };
    let data = JSON.stringify({
        "name": name
    });
    xhr.send(data);
}

function putHttpRequest(url, id) {
    // TO BE IMPLEMENTED!!!
}

function deleteHttpRequest(url, id) {
    // TO BE IMPLEMENTED!!!
}