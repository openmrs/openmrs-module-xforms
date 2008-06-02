<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="xforms.xformProperties" /></h2>

<form method="post" enctype="multipart/form-data">
	<table>
		<tr>
			<td><spring:message code="xforms.publish" /></td>
			<td><input type="checkbox" id="publish" name="publish" <c:if test="${publish}"> checked=true </c:if> /></td>
		</tr>
	</table>
	<input type="submit" value='<spring:message code="general.submit" />' />
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
