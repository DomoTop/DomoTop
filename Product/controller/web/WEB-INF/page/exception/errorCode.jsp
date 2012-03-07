
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
    import="org.openremote.controller.exception.ButtonCommandException" %>
<%
	ButtonCommandException e = (ButtonCommandException)request.getAttribute("exception");
	response.setStatus(e.getErrorCode());
%>
Exception(<%=e.getErrorCode()%>): <%=e.getMessage()%>