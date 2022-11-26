/**
 * 
 */
 
 
 var stompClient = null;
 
 	var socket = new SockJS('/conversation'); //JavaScriptでwebsocketのようなオブジェクトを使用できるようにする
    stompClient = Stomp.over(socket); //STOMP（軽量型のメッセージングプロトコル）を使用できるようにする。
 
	  var error_callback = function(error){
	   	console.log(error);
	   	console.log("実行されない");
	   	stompClient.reconnect_delay = 3000;
	   };
	 
 	
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe(location.pathname, function (message) {　//メッセージを/topic/sendMessageに送信する
            showMessage(JSON.parse(message.body).content); //受け取ったメッセージを段落ごと追加する
        }); 
    } , error_callback);




function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}

function sendMessage() {
    stompClient.send("/app/room/"+document.getElementById("id").textContent , {}, JSON.stringify({'message': $("#sendMessage").val(),
    																							  'roomId': document.getElementById("id").textContent
    }));
}

function showMessage(message) {
    $("#message").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
 
    $( "#send" ).click(function() { sendMessage(); });
});


window.addEventListener("beforeunload", function(){
	
	disconnect();
	//別のメッセージマッピングで送信する
	stompClient.send("/app/updateReadTime", {} , JSON.stringify({"roomURL":document.getElementById("id").textContent}));
	
	console.log("送信しました")
});





