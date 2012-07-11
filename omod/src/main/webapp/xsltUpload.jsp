<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<style>
.xforms_info{
	margin-top: 5px;
	margin-bottom: 5px;
	border: 1px dashed lightgrey;
	padding: 5px 5px 5px 18px;
	background-color: lightyellow;
}
</style>

<h2><spring:message code="xforms.uploadXslt" /></h2>

<c:choose>
<c:when test="${isOnePointNineAndAbove == true}">
	<div class="xforms_info">
		<img src="<openmrs:contextPath />/images/info.gif" /> 
		<spring:message code="xforms.xsltMessageInfo" />
	</div>
</c:when>
<c:otherwise>
<form method="post" enctype="multipart/form-data">
	<table>
		<tr>
			<td><spring:message code="xforms.selectXsltFile" /></td>
			<td><input type="file" name="xsltFile" /></td>
		</tr>
	</table>
	<input type="submit" value='<spring:message code="general.submit" />' />
</form>
</c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/template/footer.jsp"%>
