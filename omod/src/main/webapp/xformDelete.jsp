<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="${promptText}" /> ${formName}?</h2>

<form method="post" enctype="multipart/form-data">
	<table>
		<tr>
			<td><input type="submit" name="no" value='<spring:message code="xforms.no" />' /> </td>
			<td><input type="submit" name="yes" value='<spring:message code="xforms.yes" />' /> </td>
		</tr>
	</table>
	
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
