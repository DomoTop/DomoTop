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
 */

window.onload=function() {
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

$(document).ready(function() {
	showErrorMessage();
 	 	
  $('.statusSubmit').click(function(){
  	clearMessage();
  	showUpdateIndicator();
  });
  $('.statusForm').ajaxForm(function(result) {  	
  	var resultArray = result.split("-");
  	var resultString = resultArray[0];
  	var resultID = resultArray[1];
  	var resultAction = resultArray[2];
  	var resultPinCode = 0;  
  	if(resultArray.length >= 4)
  	{
  		resultPinCode = resultArray[3];
  	}
  	
  	if (resultString == 'OK') {
			message("User status changed successfully.");
			
			if(resultAction == 'accept')
			{
				changeValueById(resultID, "deny");
				changeBackgroundByID(resultID, "image/accept.gif");
			}
			else if(resultAction == 'deny')
			{
				changeValueById(resultID, "accept");
				changeBackgroundByID(resultID, "image/denied.gif");		
				changePincodeById(resultID, resultPinCode);	
			}
		} else {
			error("User status is unsuccessfully: " + result);
		}
  });   

  $('#saveSettings').ajaxForm(function(result) {
  	if (result == 'OK') {
			message("Settings are successfully saved.");
		} else {
			error("There was a problem with saving the settings: " + result);
		}
  }); 
    
  $('#caForm').ajaxForm(function(result) {
  	if (result == 'OK') {
			message("CA successfully created.");
		} else {
			error("CA creation was unsuccessfully: " + result);
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
 		showErrorMessage();
 	}
 	else
 	{
		hideErrorMessage();
	}
   
  //add class of activetabheader to new active tab and show contents
  this.setAttribute("class","tabActiveHeader");
  document.getElementById("tabpage_" + ident).style.display="block";
  this.parentNode.setAttribute("data-current",ident);
}


// Changes the background image of the submit element
function changeBackgroundByID(id, image)
{
	document.getElementById("submit" + id).style.backgroundImage = "url(" + image + ")";
}

// Changes the value of the action input element
function changeValueById(id, value)
{
	document.getElementById("action" + id).value = value;
}

// Changes the value of the pincode span element
function changePincodeById(id, value)
{
	document.getElementById("pincode" + id).innerHTML = value;
}

function refreshPage()
{
	location.reload(true);
}

function showErrorMessage()
{
	$('#activeErrMsg').show();
}

function hideErrorMessage()
{	
	$('#activeErrMsg').hide();
}
		
