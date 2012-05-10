/*
 * OpenRemote, the Home of the Digital Home. Copyright 2012, OpenRemote Inc.
 *
 * See the contributors.txt file in the distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3.0 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU General Public License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF site:
 * http://www.fsf.org.
 *
 * @Author: Melroy van den Berg
 * @email: melroy.van.den.berg@tass.nl
 * @license: Feel free to use it, but keep this credits please!					
 */

//SETTING UP OUR POPUP
//0 means disabled; 1 means enabled;
var popupStatus = 0;
var focusPopUp = false;

window.onload=function() 
{
  // get tab container
  var container = document.getElementById("tabContainer");
    
  // set current tab
  var navitem = container.querySelector(".tabs ul li");
  //store which tab we are on
  var ident = navitem.id.split("_")[1];
  navitem.parentNode.setAttribute("data-current",ident);
  //set current tab with class of activetabheader
  navitem.setAttribute("class","tabActiveHeader");

  //hide two tab contents we don't need
  var pages = container.querySelectorAll(".tabpage");
  for (var i = 1; i < pages.length; i++) {
 		pages[i].style.display="none";
  }
  
  //this adds click event to tabs
  var tabs = container.querySelectorAll(".tabs ul li");
  for (var i = 0; i < tabs.length; i++) {
    tabs[i].onclick=displayPage;
	}
}

$(document).ready(function() 
{
	// Ajax forms
	showUserManagementWarningMessage(); 	
	
  $('.statusSubmit').click(function(){
  	clearMessage();
  	showUpdateIndicator();
  });
  
  $('.statusForm').ajaxForm(function(result) {  
  	clearMessage();	
		statusFormResult(result);
  });
  
  $('.deleteForm').ajaxForm(function(result) {
  	clearMessage();
  	if (result == 'OK') {
			message("Device was successfully deleted.");
			delayedRefreshPage(500);
		} else if (result == 'UNAUTHORIZED') {
			centerPopup();
			showLogin();
		} else {
			error("Device deletion was unsuccessfully: " + result);
		}
  }); 

 $('#statusFormPin').ajaxForm(function(result) { 
 		disablePopup(); 
  	clearMessage();	
		statusFormResult(result);
		changeButtonToStatusSubmit(result);
  });   

  $('#saveSettings').ajaxForm(function(result) {
  	clearMessage();
  	if (result == 'OK') {
			message("Settings are successfully saved. Reloading...");
			delayedRefreshPage(800);
		} else if (result == 'UNAUTHORIZED') {
			centerPopup();
			showLogin();
		} else if(result == 'OK_REBOOT') {
			//centering with css
			centerPopup();	
							
			//load loading popup
			showLoading();
		} else {
			error("There was a problem with saving the settings: " + result);
		}
  }); 
    
  $('#caForm').ajaxForm(function(result) {
  	clearMessage();
  	if (result == 'OK') {
			message("CA successfully created. <br/><b><font color='#FF4500'>Do NOT forget to restart your Tomcat server manually to apply the changes.</font></b>");
		} else if (result == 'UNAUTHORIZED') {
			centerPopup();
			showLogin();
		} else {
			error("CA creation was unsuccessfully: " + result);
		}
  });
    
  // Logout button
	$('#logOut').click(function() {
		clearMessage();
		$.get("admin.htm?method=logOut",
			function(msg){
				if (msg == 'OK') {
					window.location = "./login";
				} else {
					error("Log-out failed.");
				}
			}
		 );
	});	
	
	// Pop-up
	//Click the button event!
	$(".button").live('click', function() {		
		// get the client ID from the HTML attribute of the button clicked
		var element = arguments[0] || window.event;
			
		// Ensure that the element is a button otherwise ignore
		if(element.target.type == 'button')
		{
			var valueArray = element.target.getAttribute("id").split("-");
			var clientID = valueArray[1];		
			changeValueOfClientIDPin(clientID);
				
			//centering with css
			centerPopup();	
							
			//load popup
			loadPopup();
		}
	});
	
	//Click the x event!
	$("#popupPinClose").click(function(){
		disablePopup();
	});
	
	//Click out event!
	$("#backgroundPopup").click(function(){
		if(!focusPopUp)
		{
			disablePopup();
		}
	});
	
	//Press Escape event!
	$(document).keypress(function(e){
		if(!focusPopUp)
		{
			if(e.keyCode==27 && popupStatus==1){
				disablePopup();
			}
		}
	});
});

// on click of one of tabs
function displayPage() 
{
	// clear msg & err div boxes
	clearMessage();
	
  var current = this.parentNode.getAttribute("data-current");
  //remove class of activetabheader and hide old contents
  document.getElementById("tabHeader_" + current).removeAttribute("class");
  document.getElementById("tabpage_" + current).style.display="none";

  var ident = this.id.split("_")[1];
 	if(ident == 1) // first tab
 	{
 		showUserManagementWarningMessage();
 	}
 	else
 	{
		hideUserManagementWarningMessage();
	}
   
  //add class of activetabheader to new active tab and show contents
  this.setAttribute("class","tabActiveHeader");
  document.getElementById("tabpage_" + ident).style.display="block";
  this.parentNode.setAttribute("data-current",ident);	
}

// Changes the background image of the submit element
function changeBackgroundByID(id, image)
{
	document.getElementById("submit-" + id).style.backgroundImage = "url(" + image + ")";
}

// Changes the value of the action input element
function changeValueById(id, value)
{
	document.getElementById("action-" + id).value = value;
}

// Changes the value of the pincode span element
function changePincodeById(id, value)
{
	document.getElementById("pincode-" + id).innerHTML = value;
}

// Changes the value of the 
function changeValueOfClientIDPin(value)
{
	document.getElementById("pin_client_id").value = value;
}

// Change the type of the button/submit element
function changeButtonToSubmitType(id, value)
{	
	document.getElementById("submit-" + id).type = value;
}

// Change the class of the button/submit element
function changeElementClass(id, value)
{	
	document.getElementById("submit-" + id).className = value;
}

// Changes the value of the 
function cleanPin()
{
	document.getElementById("pin").value = "";
}

function refreshPage()
{
	location.reload(true);
}

function delayedRefreshPage(delay)
{
	setInterval("location.reload()", delay);
}

function showUserManagementWarningMessage()
{
	$('#activeUserManagementWarnMsg').show();
}

function hideUserManagementWarningMessage()
{	
	$('#activeUserManagementWarnMsg').hide();
}

function statusFormResult(result)
{
	var resultArray = result.split("-");
	var resultString = resultArray[0];
	var resultID = resultArray[1];
	var resultAction = resultArray[2];
	var resultPinCode = 0;  
	var resultPinCheck = "true";
	
	if(resultArray.length >= 4)
	{
		resultPinCode = resultArray[3];
		resultPinCheck = resultArray[4];
	}
	
	if (resultString == 'OK') {
		message("User status changed successfully.");
		
		if(resultAction == 'accept')
		{
			changeValueById(resultID, "deny");
			changeBackgroundByID(resultID, "image/accept.png");
		}
		else if(resultAction == 'deny')
		{
			changeValueById(resultID, "accept");
			changeBackgroundByID(resultID, "image/deny.png");
			if(resultPinCheck == "false")
			{		
				changePincodeById(resultID, resultPinCode);
			}
			else
			{
				changeButtonToStatusSubmit(result);
			}
		}
	} else if (resultString == 'UNAUTHORIZED') {
			centerPopup();
			showLogin();
	} else {
		error("User status is unsuccessfully: " + result);
	}
}

function changeButtonToStatusSubmit(result)
{
	var resultArray = result.split("-");
	var resultID = resultArray[1];	
	var resultAction = resultArray[2];
	
	if(resultAction == 'accept')
	{
		changeButtonToSubmitType(resultID, "submit");
		changeElementClass(resultID, "statusSubmit accept_button");
	}
	else if(resultAction == 'deny')
	{
		changeButtonToSubmitType(resultID, "button");
		changeElementClass(resultID, "statusSubmit button deny_button");
	}
	else if(resultAction == 'UNAUTHORIZED')
	{
		centerPopup();
		showLogin();
	}
	else
	{
		error("Action is not recognized: " + result);
	}
		
}

//loading popup with jQuery
function loadPopup()
{
	//loads popup only if it is disabled
	if(popupStatus==0)
	{
		// Reset the pin of the input field
		cleanPin();
			
		$("#backgroundPopup").css({
			"opacity": "0.7"
		});
		$("#backgroundPopup").fadeIn("slow");
		$("#popupPin").fadeIn("slow");
		$("#pin").focus();
		popupStatus = 1;
	}
}

//disabling popup with jQuery
function disablePopup()
{
	//disables popup only if it is enabled
	if(popupStatus==1){
		$("#backgroundPopup").fadeOut("slow");
		$("#popupPin").fadeOut("slow");
		popupStatus = 0;
	}
}

// show loading spinner
function showLoading()
{
	//loads popup only if it is disabled
	if(popupStatus==0){
		$("#backgroundPopup").css({
			"opacity": "0.7"
		});
		$("#backgroundPopup").fadeIn("slow");
		$("#popupLoading").fadeIn("slow");
		popupStatus = 1;
		focusPopUp = true;
		
		// wait until restart is complete
		setTimeout("waitLoading()", 6000);
	}
}

// hide loading spinner
function hideLoading()
{
	//disables popup only if it is enabled
	if(popupStatus==1){
		$("#backgroundPopup").fadeOut("slow");
		$("#popupLoading").fadeOut("slow");
		popupStatus = 0;
		focusPopUp = false;
		delayedRefreshPage(500);
	}
}

// show login pop-up
function showLogin()
{
	//loads popup only if it is disabled
	if(popupStatus==0){
		$("#backgroundPopup").css({
			"opacity": "0.7"
		});
		$("#backgroundPopup").fadeIn("slow");
		$("#popupLogin").fadeIn("slow");
		popupStatus = 1;
		focusPopUp = true;
	}
}

// Wait until page can be access again
function waitLoading()
{
	$.ajax({
	      type: "GET",
	      url: "administrator",
	      complete: function(response) {
		      if(response.status == 200) {	
		      		if(response.responseText.length > 0)
		      		{		        	
		          	hideLoading();
		          }
		          else
		          {
		          	waitLoading();
		          }
		      } else if(response.status == 404) {
		          waitLoading();
		      }
		  }
	});
		
}

//centering popup
function centerPopup()
{
	//request data for centering
	var windowWidth = document.documentElement.clientWidth;
	var windowHeight = document.documentElement.clientHeight;
	var popupHeight = $("#popupPin").height();
	var popupWidth = $("#popupPin").width();
	
	var loadingHeight = $("#popupLoading").height();
	var loadingWidth = $("#popupLoading").width();
	
	var loginHeight = $("#popupLogin").height();
	var loginWidth = $("#popupLogin").width();
	
	//centering
	$("#popupPin").css({
		"position": "absolute",
		"top": windowHeight/2-popupHeight/2,
		"left": windowWidth/2-popupWidth/2
	});

	$("#popupLoading").css({
		"position": "absolute",
		"top": windowHeight/2-loadingHeight/2,
		"left": windowWidth/2-loadingWidth/2
	});
	
	$("#popupLogin").css({
		"position": "absolute",
		"top": windowHeight/2-loginHeight/2,
		"left": windowWidth/2-loginWidth/2
	});	
	
	//only need force for IE6	
	$("#backgroundPopup").css({
		"height": windowHeight
	});	
}

function onChangeGroup(form)
{				
	$.ajax({
    type: "POST",
    url: "admin.htm?method=updateGroup",	      
		data: $(form).serialize(),
    complete: function(response) {
			clearMessage();
	  	if (response.responseText == 'OK') {
				message("Group of device is successfully updated.");
			} else if (response.responseText == 'UNAUTHORIZED') {
				centerPopup();
				showLogin();				
			} else {
				error("There was a problem updating the group of the device: " + response.responseText);
			}    
  	}
	});						
	return false;
}

