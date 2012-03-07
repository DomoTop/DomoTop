/*
 * OpenRemote, the Home of the Digital Home. Copyright 2008, OpenRemote Inc.
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
	var checkedMode = $("input[name='mode']:checked").val();
	if (checkedMode == 'offline') {
		showOffline();
	} else {
		showOnline();
	}
	
	$('#online').click(function(){
		showOnline();
	});
	$('#offline').click( function() {
		showOffline();
	});
    $('#uploadForm').ajaxForm(function(result) {
    	if (result == 'OK') {
			message("Upload complete!");
		} else if (result == 'disabled') {
			error("Upload is disabled");
		} else {
			error("Upload failed! " + result);
		}
    }); 
    $('#syncForm').ajaxForm(function(result) {
    	if (result == 'OK') {
			message("Sync complete!");
    	} else if (result == 'forbidden') {
			error("The username or password you entered is incorrect.");
    	} else if (result == 'n/a') {
    		error("Can't connect to Beehive.");
    	} else if (result == 'missing') {
    		error("openremote.zip not found in account, please edit UI and save.");
		} else {
			error("Sync failed! " + result);
		}
    }); 
    $('#syncSubmit').click(function(){
    	clearMessage();
    	showUpdateIndicator();
    });
	$('#uploadSubmit').click(function(){
		clearMessage();
		showUpdateIndicator();
		var zipPath = $('#zip').val();
		if(zipPath == ''){
			error("Please select a zip first");
			return false;
		}else if(!/.+?\.zip/.test(zipPath)){
			error("Only zip is allowed");
			return false;
		}
	});
	$('#refresh').click(function() {
		clearMessage();
		showRefreshIndicator();
		$.get("config.htm?method=refreshController",
			function(msg){
				if (msg == 'OK') {
					message("Finished reloading configuration.");
				} else if (msg == 'latest') {
					message("The cache is already up to date.");
				} else {
					error("Failed to reload configuration and clear cache! " + msg);
				}
			}
		 );
	});
	$("#version").append(getVersionLabel());
});
function showOnline() {
	$('#online-cont').show();
	$('#offline-cont').hide();
	clearMessage();
}
function showOffline() {
	$('#offline-cont').show();
	$('#online-cont').hide();
	clearMessage();
}
function message(msg){
	hideUpdateIndicator();
	hideRefreshIndicator();
	$('#errMsg').text("").hide();
	if (msg == '') {
		$('#msg').hide().text(msg);
	} else {
		$('#msg').hide().show().text(msg);
	}
}

function error(msg){
	hideUpdateIndicator();
	hideRefreshIndicator();
	$('#msg').text("").hide();
	if (msg == '') {
		$('#errMsg').hide().text(msg);
	} else {
		$('#errMsg').hide().show().text(msg);
	}
}

function clearMessage() {
	message("");
	error("");
}

function showUpdateIndicator() {
	$('#update_indicator').show();
}
function hideUpdateIndicator() {
	$('#update_indicator').hide();
}

function showRefreshIndicator() {
	$('#refresh_indicator').show();
}

function hideRefreshIndicator() {
	$('#refresh_indicator').hide();
}

function getVersionLabel(){
	var headUrl = "$HeadURL$";
	var revision = "$Revision$";
	var result = "Untagged";
    var verStr = "";
    var tagStart = -1;
    if ((tagStart = headUrl.indexOf("tags")) >= 0) {
       var tagsEnd = headUrl.indexOf("/", tagStart + 5);
       if (tagsEnd >= 0) {
          verStr = headUrl.substring(tagStart + 5, tagsEnd);
       }
    } else if ((tagStart = headUrl.indexOf("branches")) >= 0) {
       var tagsEnd = headUrl.indexOf("/", tagStart + 9);
       if (tagsEnd >= 0) {
          verStr = headUrl.substring(tagStart + 9, tagsEnd);
          verStr = "Branch: " + verStr;
       }
    } else if (revision != null) {
       tagStart = revision.indexOf("$Revision:");
       var tagsEnd = revision.indexOf("$", tagStart + 10);
       if (tagsEnd >= 0) {
          verStr = " r" + $.trim(revision.substring(tagStart + 10, tagsEnd));
       }
    }
    if (verStr.length != 0) {
       result = verStr.replace('_', '.');
    }
    return result;
}