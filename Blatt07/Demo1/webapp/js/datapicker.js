var requestobj = getXMLHttpRequest();

function getXMLHttpRequest() {
    // XMLHttpRequest for Firefox, Opera, Safari
    if (window.XMLHttpRequest) {
        return new XMLHttpRequest();
    }
    if (window.ActveObject) { // Internet Explorer
        try { // for IE new
            return new ActiveXObject("Msxml2.XMLHTTP")
        }
        catch (e)  {  // for IE old
            try {
                return new ActiveXObject("Microsoft.XMLHTTP")
            }
            catch (e)  {
                alert("Your browser does not support AJAX!");
                return null;
            }
        }
    }
    return null;
} 

function AsyncResult() {
    if (requestobj.readyState==4) {
        if (requestobj.status==200) {
            var data = requestobj.responseXML.getElementsByTagName('address')[0];
            var cField = document.getElementById('namefield');
            var browserName = navigator.appName;
            if (browserName == "Microsoft Internet Explorer")
                // cField.defaultValue = data.firstChild.text
                cField.defaultValue = data.childNodes[1].textContent
            else 
                cField.defaultValue = data.childNodes[1].firstChild.wholeText;
            cField = document.getElementById('cityfield');
            if (browserName == "Microsoft Internet Explorer")
                // cField.defaultValue = data.lastChild.textContent
                cField.defaultValue = data.childNodes[9].textContent
            else
                cField.defaultValue = data.childNodes[9].firstChild.wholeText;
        }
    }
}

function AsyncJsonResult() {
    if (requestobj.readyState==4) {
        if (requestobj.status==200) {
            var jsonObj = JSON.parse(requestobj.responseText);
            document.getElementById('namefield').defaultValue = jsonObj.addresses[0].name;
            document.getElementById('cityfield').defaultValue = jsonObj.addresses[0].city;
        }
    }
}

function GetData(url) {
    if (requestobj) {
        requestobj.open('GET', url, true);
        requestobj.onreadystatechange=AsyncResult;
        requestobj.send(null);
    }
}

function GetJsonData(url) {
    if (requestobj) {
        requestobj.open('GET', url, true);
        requestobj.onreadystatechange=AsyncJsonResult;
        requestobj.send(null);
    }
}