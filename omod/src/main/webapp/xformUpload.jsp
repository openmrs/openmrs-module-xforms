<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="xforms.uploadXform" /></h2>

<form method="post" enctype="multipart/form-data">
	<table>
		<tr>
			<td><spring:message code="xforms.selectXformFile" /></td>
			<td><input type="file" name="xformFile" /></td>
		</tr>
	</table>
	<input type="submit" value='<spring:message code="general.submit" />' />
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
