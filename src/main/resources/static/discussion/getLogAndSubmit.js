
let page = 0;


const contents = document.getElementById("upperLimitLog");

const getLog = document.getElementById("getLog");
 
 
  

const loadContent = async () => {
	
	//pageableで取得したテキストを画面に残す
	
	console.count();
	
	
	
	const logSize = document.getElementById("currentLogSize").value;
	
	console.log("切り分け:ログの移動はできる。")
	
	/*
	for(let i = logSize; 0 < i; i--){
	let addContributor = document.getElementById('contributor' + i).value;
	let addMessageLog = document.getElementById('MessageLog' + i).value;
	
	contents.insertAdjacentHTML('beforebegin',
		'<table>'+
			'<tr>'+
				'<th>'+ addContributor +'</th>'+
				'<th>'+ addMessageLog +'</th>'+
			'</tr>'+
		'</table>'
	);

	}
	*/
	
	
	//pagebleの次のページを取得
	try{
	let csrfToken = document.getElementById("_csrf").value;
	
	const token = csrfToken.replace("XSRF-TOKEN=", "");
	const header = "X-CSRF-TOKEN";
	let logUrl = document.getElementById('url').value;
	
	//ページの設定
	
	const pageLimit = document.getElementById("pageLimit").value;
	
	page++;
	let requestPageNumber = '?page=' + page;
	logUrl = logUrl + requestPageNumber;
	
	
	const XHR = new XMLHttpRequest();
	XHR.open('GET', logUrl);
	XHR.setRequestHeader(header, token);
	XHR.responseType = 'json';
	XHR.onload = function(){
		let chatLogs = XHR.response;
		
			
	//　新規ログを取得	
		if(chatLogs[0] !== null && chatLogs.length != 0 && page !== pageLimit){
		
		
			// 仮 新規ログに格納されている値を履歴へ移動
		for(let i = logSize; 0 < i; i--){
	let addContributor = document.getElementById('contributor' + i).value;
	let addMessageLog = document.getElementById('MessageLog' + i).value;
	
	contents.insertAdjacentHTML('beforebegin',
		'<table>'+
			'<tr>'+
				'<th>'+ addContributor +'</th>'+
				'<th>'+ addMessageLog +'</th>'+
			'</tr>'+
		'</table>'
	);
		console.log("履歴に表示されるメッセージは" + addMessageLog)
		}
		
		
		for(let i = 0; i <= chatLogs.length-1; i++){
		
			const nowLow = i + 1;
			console.log( i + "番目は" + chatLogs[i].message);
			
			document.getElementById('contributor' + nowLow).value = chatLogs[i].id;
			document.getElementById('MessageLog' + nowLow).value = chatLogs[i].message;
			
			console.log("取得したメッセージは" + chatLogs[i].message)
			
			
	

		}
	
		
				//ページ最後のログが従来のログより少ない時にも対応する
				if(page === pageLimit　-1 ){
					for(let i = chatLogs.length; 0 < i; i--){
							let addContributor = document.getElementById('contributor' + i).value;
							let addMessageLog = document.getElementById('MessageLog' + i).value;
							
							contents.insertAdjacentHTML('beforebegin',
								'<table>'+
									'<tr>'+
										'<th>'+ addContributor +'</th>'+
										'<th>'+ addMessageLog +'</th>'+
									'</tr>'+
								'</table>'
							);
							
							console.log("問題のメッセージは" + addMessageLog)
						}
				
			}
	
			
		}else{
		
		//　履歴が３未満の時の処理
		if(page === 1){
					for(let i = logSize; 0 < i; i--){
							let addContributor = document.getElementById('contributor' + i).value;
							let addMessageLog = document.getElementById('MessageLog' + i).value;
							
							contents.insertAdjacentHTML('beforebegin',
								'<table>'+
									'<tr>'+
										'<th>'+ addContributor +'</th>'+
										'<th>'+ addMessageLog +'</th>'+
									'</tr>'+
								'</table>'
							);
							
							console.log("問題のメッセージは" + addMessageLog)
						}
				}
			
		
		
		
		
		
			contents.insertAdjacentHTML('afterend',
		'<p>'+
			'全ての履歴が読み込まれました。'
			 +
		'</p>'
			);
			
		document.getElementById("upperLimitLog").remove();
		document.getElementById("getLog").disabled = true;
		
		
	
		}
		
		
	}
	
	//XHR.onerror
	XHR.send();
	
	console.log("切り分け：XHR送信しました")
	
	
	}catch{
		document.open()
			document.write("<h1>エラーが発生しました</h1>")
			document.close()
	}
	
	
	console.log("切り分け:無限スクロールの動作は正常。")
	
	
	
	//loadContentの中身
}

getLog.onclick = function(){loadContent();}






//２重投稿防止
document.getElementById("send").onclick = function(){
	document.getElementById("send").disabled = true;
	setTimeout(function(){
							document.getElementById("send").disabled = false;
						},3000);
}





