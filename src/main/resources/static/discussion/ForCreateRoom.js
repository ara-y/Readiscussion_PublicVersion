/**
 * 
 */
 
 
 
 var saveCheckBox = document.getElementById('hasPassword');
 document.getElementById('password').style.visibility="hidden"
 
 function openSetPassword(){
	
	let displayPassword = document.getElementById('password');
	
	console.log(saveCheckBox.checked)
	if(saveCheckBox.checked){
		displayPassword.style.visibility = "visible";
		displayPassword.value = "";		
		
	}else{
		displayPassword.style.visibility = "hidden";
		displayPassword.value = "none";
	}
}

 saveCheckBox.addEventListener('change', openSetPassword);
 