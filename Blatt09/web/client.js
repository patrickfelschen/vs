var connection = new WebSocket('ws://127.0.0.1:4447');

connection.onopen = function () {
    console.log('Connected!');
    //connection.send('Ping'); // Send the message 'Ping' to the server
};

// Log errors
connection.onerror = function (error) {
    console.log('WebSocket Error ' + error);
};

// Log messages from the server
connection.onmessage = function (e) {
    console.log('Server: ' + e.data);
};


function addPoster() {
    const name = document.getElementById("contents").value;
    let data = JSON.stringify({
        "method": "ADD",
        "data": {
            "id": 1,
            "name": name
        }
    });
    connection.send(data);
    document.getElementById("contents").value = "";
}