<%@page contentType="text/html" pageEncoding="UTF-8"%>
<jsp:useBean id="applicant" class="edu.jsu.mcis.cs425.project2.BeanApplicant" scope="session" />
<jsp:setProperty name="applicant" property="username" value="<%= request.getRemoteUser() %>" />
<% applicant.setUserInfo(); %>
<!DOCTYPE html>
<html>
    
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Applicant Welcome Page</title>
    </head>
    
    <body>
        <h1>Hello, <jsp:getProperty name="applicant" property="username" />!</h1>
        <a href="<%= request.getContextPath() %>/jobreport" target="_self">Your Report</a>
        <a href="<%= request.getContextPath() %>/main/applicant_jobs.jsp" target="_self">Your Jobs</a>
        <a href="<%= request.getContextPath() %>/main/applicant_skills.jsp">Your Skills</a>
        <a href="<%= request.getContextPath() %>/public/logout.jsp" target="_self">Log Out</a>
    </body>
    
</html>