globGetMethod = 0; /* 0: html; 1: xyz */

function setGetMethod(val) {
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
        } catch (e) {  // for IE old
            try {
                return new ActiveXObject("Microsoft.XMLHTTP");
            } catch (e) {
                alert("Your browser does not support AJAX!");
                return null;
            }
        }
    }
    return null;
}

function getHttpRequest(url) {
    if (globGetMethod === 0)
        getHtmlHttpRequest(url);
    else
        /* xyz = JSON oder XML .... */
        getxyzHttpRequest(url);
}

let waiting = false;

function getHtmlHttpRequest(url) {
    if (!waiting) {
        waiting = true;
        let xmlhttp = getXMLHttpRequest();
        xmlhttp.onreadystatechange = function () {
            // if (xmlhttp.readyState !== 4) {
            //     $('posters').innerHTML = 'Seite wird geladen ...';
            // }

            if (xmlhttp.readyState === 4 && xmlhttp.status === 200) {
                waiting = false;
                let postersElement = document.getElementById("posters");
                if(postersElement != null) {
                    //postersElement.innerHTML = xmlhttp.responseText;
                    console.log(xmlhttp.responseText);
                    let json = JSON.parse(xmlhttp.responseText);
                    console.log(json);

                    let table = "<table>";

                    for(let i = 0; i < json.length; i++) {
                        if(!json[i]["owner"]) {
                            table += "<tr><td>" + json[i]["id"] +"<input type='text' size='100' minlength='100' maxlength='100' value='" + json[i]["text"] + "' readonly/></td></tr>"
                        }
                        else {
                            table += "<tr><td>" + json[i]["id"] +"<input type='text' size='100' minlength='100' maxlength='100' value='" + json[i]["text"] + "'/></td></tr>"
                        }
                    }
                    table += "</table>";
                    postersElement.innerHTML = table;


                }
                $('timestamp').innerHTML = new Date().toString();
            }
        };
        xmlhttp.open("GET", url, true);
        xmlhttp.send();
    }
}


function getxyzHttpRequest(url) {
    // TO BE IMPLEMENTED!!!
}

// https://stackoverflow.com/a/24468752

function postHttpRequest(url) {
    // TO BE IMPLEMENTED!!!
    let name = document.getElementById("contents").value;
    let xhr = getXMLHttpRequest();
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    let data = JSON.stringify({
        "name": name
    });
    xhr.send(data);
}

function putHttpRequest(url, id) {
    // TO BE IMPLEMENTED!!!
    let name = document.getElementById("input_field_" + id).value;
    let xhr = getXMLHttpRequest();
    xhr.open("PUT", url, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    let data = JSON.stringify({
        "id": id,
        "name": name
    });
    xhr.send(data);
}

function deleteHttpRequest(url, id) {
    // TO BE IMPLEMENTED!!!
    let name = document.getElementById("input_field_" + id).value;
    let xhr = getXMLHttpRequest();
    xhr.open("DELETE", url, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    let data = JSON.stringify({
        "id": id
    });
    xhr.send(data);
}