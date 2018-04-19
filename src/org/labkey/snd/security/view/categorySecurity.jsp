<%@ page import="org.labkey.snd.SNDController" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>

<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>

<labkey:panel title="Category Security">

    <labkey:form id="categorySecurityForm" action="<%=h(buildURL(SNDController.CategorySecurityAction.class))%>" method="POST">

    </labkey:form>


</labkey:panel>