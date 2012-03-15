<HTML>
<HEAD>
<TITLE>OpenRemote User Management</TITLE>
<link href="image/OpenRemote_Logo16x16.png" rel="shortcut icon"/>
<link href="image/OpenRemote_Logo16x16.png" type="image/png" rel="icon"/>
<META http-equiv=Content-Type content="text/html; charset=UTF-8">
<link type="text/css" href="css/index.css" rel="stylesheet" />
<script type="text/javascript" src="jslib/jquery-1.3.1.min.js"></script>
<script type="text/javascript" src="jslib/jquery.form-2.24.js"></script>
<script type="text/javascript" src="js/index.js"></script>
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
							<p class="welcome">OpenRemote User Management</p>
							<p>Select the user that you want to give or remove access by clicking on the icon below the activated column.<br /></p>
							<p><i>Users:</i><br /></p>

							<TABLE cellSpacing=0 cellPadding=0 width=600 align="center" bgColor=#ffffff border=0>
							<TBODY>
								<TR>
									<th align="left">Username</th><th align="left">E-mail</th><th align="left">Pin Code</th><th align="left">Activated</th><th align="left">Group</th>
								</TR>
								<#list clients as client>
								<TR>
									<TD>${client.name}</TD><TD>${client.email}</TD><TD>${client.pinCode}</TD>
									<TD>
										<#if client.active>											
									  	<a href="#deny"><img src="image/accept.gif" alt="Accepted" border=0 /></a>
										<#else>
											<a href="#accept"><img src="image/denied.gif" alt="Denied" border=0 /></a>
										</#if>
									</TD>
									<TD>		
										${client.groupName}							
										<select name="group">
											<option value="">No group</option>
											<option value="admin">Administrator</option>
											<option value="parent">Parent</option>
											<option value="child">Childeren</option>
										</select>
									</TD>
								</TR>
								</#list>
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
