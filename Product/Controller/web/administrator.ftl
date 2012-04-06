<HTML>
<HEAD>
<TITLE>OpenRemote Administrator Panel</TITLE>
<link href="image/OpenRemote_Logo16x16.png" rel="shortcut icon"/>
<link href="image/OpenRemote_Logo16x16.png" type="image/png" rel="icon"/>
<META http-equiv=Content-Type content="text/html; charset=UTF-8">
<link type="text/css" href="css/index.css" rel="stylesheet" />
<link type="text/css" href="css/admin.css" rel="stylesheet" />
<script type="text/javascript" src="jslib/jquery-1.3.1.min.js"></script>
<script type="text/javascript" src="jslib/jquery.form-2.24.js"></script>
<script type="text/javascript" src="js/index.js"></script>
<script type="text/javascript" src="js/admin.js"></script>
</HEAD>
<BODY style="TABLE-LAYOUT: fixed; WORD-BREAK: break-all" topMargin=10
	marginwidth="10" marginheight="10">
<#list configurations as configuration>
	<#if configuration.configuration_name == 'pin_check'>
		<#assign isPinCheck=configuration.configuration_value>
	</#if>
</#list>
<TABLE height="95%" cellSpacing=0 cellPadding=0 width="100%"
	align=center border=0>
	<TBODY>
		<TR vAlign="center" align="middle">
			<TD>
			<TABLE cellSpacing=0 cellPadding=0 width=768 bgColor=#ffffff border=0>
				<TBODY>
					<TR>
						<TD width=20 background="image/rbox_1.gif" height=20></TD>
						<TD width=205 background="image/rbox_2.gif" height=20></TD>
						<TD width=56><IMG height=20 src="image/rbox_2.gif" width=56></TD>
						<TD width=205 background="image/rbox_2.gif"></TD>
						<TD width=56><IMG height=20 src="image/rbox_2.gif" width=56></TD>
						<TD width=205 background="image/rbox_2.gif"></TD>
						<TD width=20 background="image/rbox_3.gif" height=20></TD>
					</TR>
					<TR>
						<TD align=left background="image/rbox_4.gif" rowSpan=2></TD>
						<TD style="border-bottom: 1px solid #ccc; vertical-align: middle;" colSpan=5 height=50>
						<a href="http://www.openremote.org/"><img alt=""
							src="image/global.logo.gif" align="middle"></a><span class="heading">Administrator Panel</span></TD>
						<TD align=left background="image/rbox_6.gif" rowSpan=2></TD>
					</TR>
					<TR>
						<TD align=left colSpan=5 height=180>
							<div class="top">
								<div class="left"><a href="index.html"><img src="image/back.png" alt="Back" border=0 /> Back</a></div>
								<div class="right"><a id="logOut" href="javascript:void(0);"><img src="image/logout.gif" alt="Log-out" /> Log-out</a></div>
							</div>		
							<#if errorMessage?has_content>
								<#assign info_messsage_line='<p id="errMsg" class="errMsg" /><p id="activeErrMsg" class="activeErrMsg">${errorMessage}</p><p id="msg" class="msg" />'>							
						  <#else>
								<#assign info_messsage_line='<p id="errMsg" class="errMsg" /><p id="msg" class="msg" />'>
							</#if>
						  <div id="tabContainer">
						    <div class="tabs">
						      <ul>
						        <li id="tabHeader_1">User Management</li>
						        <li id="tabHeader_2">Configuration</li>
						      </ul>
						    </div>
						    <div class="tabscontent">
						    	${info_messsage_line}					    	
						      <div class="tabpage" id="tabpage_1">        
						    		<p class="welcome">User Management</p>	
										<p><i>User table:</i><br /></p>
										<TABLE cellSpacing=1 cellPadding=2 width=700 align="center" bgColor=#ffffff border=0>
										<TBODY>								
											<#if clients?exists>
											<TR>
												<th align="left">Device name</th><th align="left">E-mail</th><#if isPinCheck == 'false'><th align="left">Pin</th></#if><th align="left">Status</th><th align="left">Group</th>
											</TR>
											<#list clients as client>
											<TR>
												<TD>${client.client_device_name}</TD><TD><#if client.client_email?has_content>${client.client_email}<#else><i>No e-mail</i></#if></TD><#if isPinCheck == 'false'><TD><span id="pincode-${client.client_id}">${client.client_pincode}</span></TD></#if>
												<TD>
													<form class="statusForm" action="admin.htm?method=changeUserStatus" method="post">
													<input type="hidden" name="client_id" value="${client.client_id}" />
													<#if client.client_active>
															<input type="hidden" id="action-${client.client_id}" name="action" value="deny" />
															<input type="submit" id="submit-${client.client_id}" class="statusSubmit accept_button" value="" />													
													<#else>														
														<input type="hidden" id="action-${client.client_id}" name="action" value="accept" />
														<#if isPinCheck == 'false'>														
															<input type="submit" id="submit-${client.client_id}" class="statusSubmit deny_button" value="" />
														<#else>														
															<input type="button" id="submit-${client.client_id}" class="statusSubmit button deny_button" value="" />
														</#if>
													</#if>
													</form>
												</TD>
												<TD>							
													<select name="group">
														<option value="">No Group</option>											
														<option<#if client.client_group_id == 1> selected</#if> value="admin">Administrator</option>
														<option<#if client.client_group_id == 2> selected</#if> value="parent">Parent</option>
														<option<#if client.client_group_id == 3> selected</#if> value="child">Childeren</option>
													</select>
												</TD>
											</TR>
											</#list>
											<#else>
											<TR>
												<TD><b>No users</b></TD>
											</TR>
											</#if>
										</TBODY>
										</TABLE>			
										<p><div style="text-align: center;"><a href="javascript:refreshPage();"><img src="image/refresh.png" alt="Refresh" align="middle"/></a></div></p>
						      </div>
									<div class="tabpage" id="tabpage_2">
						        <p class="welcome">Configuration</p>
						        <form id="saveSettings" action="admin.htm?method=saveSettings" method="post">
						        	<table cellpadding="4" border="0">
												<#list configurations as configuration>
						           		<tr>
							        			<td align="left">
							        				<b>${configuration.configuration_name?replace("_", " ")?capitalize}:</b> <#if configuration.configuration_information?has_content><span class="info"><img src="image/info_icon.png" alt=""/><span>${configuration.configuration_information}</span></span></#if>
							        			</td>
							        			<td>
							        			<#if configuration.configuration_type == 'boolean'>
							        				<#if configuration.configuration_value == 'true'>
							        					<#assign checked=' checked'>
							        				<#else>
							        					<#assign checked=''>
							        				</#if>					        				
							        				<input type="checkbox" name="${configuration.configuration_name}" value="true"${checked} />
							        				<input type="hidden" name="${configuration.configuration_name}" value="false" />		
														<#else>
															<input type="text" size="35" name="${configuration.configuration_name}" value="${configuration.configuration_value}" />
														</#if>
														</td>
													</tr>												
						      			</#list>
												<tr>
													<td colspan="2" align="right">
														<input type="submit" value="Save settings"/>
													</td>
												</tr>
											</table>
										</form>
										<br/><br/>
										<hr noshade size="1">
										<br/>
										<form id="caForm" action="admin.htm?method=setupCA" method="post">
											<b>Reset all devices:</b> <br/><input type="submit" value="Reset devices" onClick="return confirm('You are about remove all devices, meaning that all currently accepted devices will be invalid.\nAre you sure you want to continue?\n\nClick OK to continue or Cancel to abort.');"/>
										</form>
						      </div>				      
						    </div>
						  </div>
						  <#if isPinCheck == 'true'>
					  	<div id="popupContact">
								<a id="popupContactClose">x</a>
								<h1>Enter the pin</h1>							
								<p id="contactArea">
									Please enter the pin shown on the device you want to accept.<br/><br/>									
									<form class="statusFormPin" action="admin.htm?method=changeUserStatus" method="post">
										<input type="hidden" name="client_id" value="" id="pin_client_id" />
										<input type="hidden" name="action" value="accept" />
										<center>
											<b>Pin:</b> <input type="text" name="pin" value="" size="6" /><br/>	<br/>									
											<input type="submit" name="submit" value="Check" />
										</center>
									</form>
								</p>
							</div>	
							</#if>						
							<p><a href="index.html"><img src="image/back.png" alt="Back" border=0 /> Back</a></p>
						</TD>
					</TR>
					<TR>
						<TD align=left background="image/rbox_7.gif" height=20></TD>
						<TD align=left background="image/rbox_8.gif" colSpan=5 height=20></TD>
						<TD align=left background="image/rbox_9.gif" height=20></TD>
					</TR> 							
				</TBODY>
			</TABLE>
			<span class="copyright">Copyright &copy; 2008-2012 OpenRemote.
			Licensed under Affero General Public License.</span> <span id="version" class="version">Version:</span></TD>
			</TD>
		</TR>
	</TBODY>
</TABLE>
<#if isPinCheck == 'true'>
<div id="backgroundPopup"></div>
</#if>
</BODY>
</HTML>
