function ajaxUpdateCompleted(data) {
    if (data.status === 'success') { 
        updatePrompt();
    }
}

var currentPrompt = -1;
var currentPromptDisplayed = -1;

function showPrompt(x){
    currentPrompt = x;
    updatePrompt();
}

function updatePrompt(){
	if(currentPromptDisplayed === currentPrompt)
		return;
        
	currentPromptDisplayed = currentPrompt;
        
        let user_prompt = document.getElementById("user_prompt");
        let password_prompt = document.getElementById("password_prompt-p");
        let password_list_prompt = document.getElementById("password_prompt-ul");
	
	switch(currentPrompt){
		case 0:
			user_prompt.style.display = "block";
			password_prompt.style.display = "none";
			password_list_prompt.style.display = "none";
			break;
		case 1:
			user_prompt.style.display = "none";
			password_prompt.style.display = "block";
			password_list_prompt.style.display = "block";
			break;
		default:
			user_prompt.style.display = "none";
			password_prompt.style.display = "none";
			password_list_prompt.style.display = "none";
			
			break;
	}
	
}

