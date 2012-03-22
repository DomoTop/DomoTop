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
<TABLE height="95%" cellSpacing=0 cellPadding=0 width="100%"
	align=center border=0>
	<TBODY>
		<TR vAlign="center" align="middle">
			<TD>
			<TABLE cellSpacing=0 cellPadding=0 width=668 bgColor=#ffffff border=0>
				<TBODY>
					<TR>
						<TD width=20 background="image/rbox_1.gif" height=20></TD>
						<TD width=178 background="image/rbox_2.gif" height=20></TD>
						<TD width=56><IMG height=20 src="image/rbox_2.gif" width=56></TD>
						<TD width=170 background="image/rbox_2.gif"></TD>
						<TD width=56><IMG height=20 src="image/rbox_2.gif" width=56></TD>
						<TD width=178 background="image/rbox_2.gif"></TD>
						<TD width=20 background="image/rbox_3.gif" height=20></TD>
					</TR>
					<TR>
						<TD align=left background="image/rbox_4.gif" rowSpan=2></TD>
						<TD style="border-bottom: 1px solid #ccc" colSpan=5 height=50>
						<a href="http://www.openremote.org/"><img alt=""
							src="image/global.logo.gif"></a></TD>
						<TD align=left background="image/rbox_6.gif" rowSpan=2></TD>
					</TR>
					<TR>
						<TD align=left colSpan=5 height=150>
							<p><a href="index.html"><img src="image/back.png" alt="Back" border=0 /> Back</a></p>
							<p class="welcome">OpenRemote Administrator Panel</p>
							<p id="errMsg" class="errMsg" />
							<#if errorMessage?has_content>
								<p class="activeErrMsg">${errorMessage}</p>
							</#if>
							<p id="msg" class="msg" />
							<p><i>User Management:</i><br /></p>
							<TABLE cellSpacing=0 cellPadding=0 width=600 align="center" bgColor=#ffffff border=0>
							<TBODY>								
								<#if clients?exists>
								<TR>
									<th align="left">Username</th><th align="left">E-mail</th><th align="left">Pin Code</th><th align="left">Status</th><th align="left">Group</th>
								</TR>
								<#list clients as client>
								<TR>
									<TD>${client.client_device_name}</TD><TD>${client.client_email}</TD><TD><span id="pincode${client.client_id}">${client.client_pincode}</span></TD>
									<TD>
										<form class="statusForm" action="admin.htm?method=changeUserStatus" method="post">
										<input type="hidden" name="clientid" value="${client.client_id}" />
										<input type="hidden" name="clientfile" value="${client.client_file_name}" />
										<#if client.active>
											<input type="hidden" id="action${client.client_id}" name="action" value="deny" />
											<input type="submit" id="submit${client.client_id}" class="statusSubmit" value="" style="background: #fff url('image/accept.gif') no-repeat center top;" />
										<#else>
											<input type="hidden" id="action${client.client_id}" name="action" value="accept" />
											<input type="submit" id="submit${client.client_id}" class="statusSubmit" value="" style="background: #fff url('image/denied.gif') no-repeat center top;" />
										</#if>
										</form>
									</TD>
									<TD>							
										<select name="group">
											<option value="">No Group</option>											
											<option<#if client.client_group_id == '1'> selected</#if> value="admin">Administrator</option>
											<option<#if client.client_group_id == '2'> selected</#if> value="parent">Parent</option>
											<option<#if client.client_group_id == '3'> selected</#if> value="child">Childeren</option>
										</select>
									</TD>
								</TR>
								</#list>
								<#else>
								<TR>
									<TD><i>No users</i></TD>
								</TR>
								</#if>
							</TBODY>
							</TABLE>							
							<br />
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

</BODY>
</HTML>
