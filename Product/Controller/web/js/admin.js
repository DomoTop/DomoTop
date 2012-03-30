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

$(document).ready(function() {

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
  
  $('#caForm').ajaxForm(function(result) {
  	if (result == 'OK') {
			message("CA successfully created.");
		} else {
			error("CA creation was unsuccessfully: " + result);
		}
  });   
  
});

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
