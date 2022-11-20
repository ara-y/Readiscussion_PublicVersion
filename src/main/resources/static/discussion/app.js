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
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe(location.pathname, function (message) {　//メッセージを/topic/greetingに送信する '/topic/greetings'
            showMessage(JSON.parse(message.body).content); //受け取ったメッセージを段落ごと追加する
        }); //greetings →　message
    } , error_callback);
    
  
   
   //stompClient.heartbeat.outgoing = 200; 
    //stompClient.heartbeat.incoming = 200; 
  

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
   $("#message").html(""); //送信内容をリセットする。
}


function connect() {
    var socket = new SockJS('/conversation'); //JavaScriptでwebsocketのようなオブジェクトを使用できるようにする
    stompClient = Stomp.over(socket); //STOMP（軽量型のメッセージングプロトコル）を使用できるようにする。
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe(location.pathname, function (message) {　//メッセージを/topic/greetingに送信する '/topic/greetings'
            showMessage(JSON.parse(message.body).content); //受け取ったメッセージを段落ごと追加する
        });
    });
    
     // stompClient.reconnect_delay = 300;
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendMessage() {
    stompClient.send("/app/room/"+document.getElementById("id").textContent , {}, JSON.stringify({'message': $("#sendMessage").val(),
    																							  'roomId': document.getElementById("id").textContent
    })); //name → sendMessage→content
}

function showMessage(message) {
    $("#message").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
 //  $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendMessage(); });
});


window.addEventListener("beforeunload", function(){
	
	
	//別のメッセージマッピングで送信する
	stompClient.send("/app/updateReadTime", {} , JSON.stringify({"roomURL":document.getElementById("id").textContent}));
	
	console.log("送信しました")
});





