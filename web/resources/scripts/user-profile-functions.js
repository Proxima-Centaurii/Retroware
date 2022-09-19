var account_modal = document.getElementById("account-settings-modal");
var profile_picture_modal = document.getElementById("profile-picture-modal");

var account_settings_button = document.getElementById("account-settings-button");
var profile_edit_button = document.getElementById("edit-profile-picture");

var close_account_settings = document.getElementById("close-account-modal");
var close_profile_edit = document.getElementById("close-profile-modal");


account_settings_button.onclick = function(){
    account_modal.style.display = "block";
};
profile_edit_button.onclick = function(){
    profile_picture_modal.style.display = "block";
};


close_account_settings.onclick = function(){
    account_modal.style.display = "none";
};
close_profile_edit.onclick = function(){
    profile_picture_modal.style.display = "none";
};


window.onclick = function(event){
    if(event.target === account_modal){
        account_modal.style.display = "none";
    }
    else if(event.target === profile_picture_modal){
        profile_picture_modal.style.display = "none";
    }
};