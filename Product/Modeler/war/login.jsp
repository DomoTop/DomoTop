<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<jsp:useBean id="now" class="java.util.Date" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta content="openremote, boss, iphone, android, knx, insteon, x10, infrared, z-wave, zigbee, opensource, gpl, iknx, lirc" name="KEYWORDS"/>
<link href="image/OpenRemote.Logo.16x16.png" rel="shortcut icon"/>
<link href="image/OpenRemote.Logo.16x16.png" type="image/png" rel="icon"/>
<title>Login - OpenRemote Boss 2.0 Online</title>
<style type="text/css">
    body{
        line-height:100%;
        background-color:#D2D1D0;
        font-family:'Lucida Grande',Geneva,Verdana,Arial,sans-serif;
    }
	.form_label{
	    padding-right:10px;
	}
	.center-form{
	   margin:auto;
	   font-size:11px;
	   color:#4D4D4D;
	   width:500px;
	}
	a img{
        border:none;
    }
    .login_submit{
        width:70px;
        margin-left: 80px;
    }
    .copyright{
        text-align: center;
    }
    p.title {
    	text-align: center; 
    	font-weight: bold; 
    	font-size: 13px;
    }
    p.fail{
        color:red;
        text-align: center;
    }
    p.pass{
        color:green;
        text-align: center;
    }
    .register_btn{
    	margin-left: 40px;
    }
    div.inner-boundary {
		border:1px solid #A6A6A6;
	}
	div.inner-border  {
		border:1px solid #FEFEFE;
		background-color:#E0E0E0;	
		padding:20px;
	}
	p.input {
		text-align: right;
		width: 240px;
	}
	a, a:hover {
		color:#225E8A;
		text-decoration:none;
	}
	.incorrect {
		color:red;
		text-align: left;
	}
	.forget_btn {
	   margin-left: 10px;
	}
</style>
</head>
<body>

<div class="center-form">
	<form method="POST" action="j_security_check">
		<div class="inner-boundary">
		  <div class="inner-border">
            <a href="http://www.openremote.org" ><img src="image/global.logo.png" /></a>
            <p class="title">Login to OpenRemote Boss 2.0 Online</p>
            
            <c:if test="${isActivated ne null and isActivated}">
                <p class="pass"><b>${username}</b> has been activated, please login.</p>
            </c:if>
            <c:if test="${isActivated ne null and not isActivated}">
                <p class="fail">Invalid activation credentials, activation failed.</p>
            </c:if>
            <c:if test="${needActivation ne null}">
                <p class="pass">We have sent an activation email to <b>${email}</b>,
                 please follow the instructions in the email to complete your registration.</p>
            </c:if>
            <c:if test="${isAccepted ne null and isAccepted}">
                <p class="pass">You have accepted the invitation, please login.</p>
            </c:if>
            <c:if test="${needActivation eq null}">
	            <div style="padding-left:110px">
		            <p class="input"><b class="form_label">Username</b>
		            <input id="username" style="width:150px" type="text" name="j_username" value="${username}"></p>
		            <p class="input"><b class="form_label">Password</b>
		            <input id="password" style="width:150px" type="password" name="j_password" value=""></p>
	                <c:if test="${param.fail ne null }">
		                <p class="incorrect">The username or password you entered is incorrect.</p>
	                </c:if>
	                <p>
	                	<input id="rememberme" type="checkbox" name="_spring_security_remember_me"><label for="rememberme">Remember Me</label>
	                	<a class="register_btn" href="register.jsp">Create a New Account</a>
	                </p> 
		            <div>
                     <input class="login_submit" type="submit" value="Login">
                     <a class="forget_btn" href="forget.jsp">Forget password?</a>
                  </div>
	            </div>
            </c:if>                    
	        <p class="copyright">Copyright &copy; 2008-<fmt:formatDate value="${now}"pattern="yyyy" />  
          <a href="http://www.openremote.org">OpenRemote</a>.</p>
		  </div>
		</div>
	</form>
</div>
</body>
</html>