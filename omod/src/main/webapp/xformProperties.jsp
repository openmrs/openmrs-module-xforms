<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<br/>
<a href="<openmrs:contextPath />/admin"><spring:message code="general.back"/></a>
<br/>

<openmrs:portlet url="globalProperties" parameters="propertyPrefix=xforms" />

<%@ include file="/WEB-INF/template/footer.jsp"%>