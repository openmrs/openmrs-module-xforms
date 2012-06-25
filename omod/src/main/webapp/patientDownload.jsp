<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="xforms.downloadPatients" /></h2>

<form method="post" enctype="multipart/form-data">
	<table>
		<tr>
			<td><spring:message code="xforms.selectCohort" /></td>
			<td>
				<select name="cohortId" id="cohortId">
		
				<c:forEach var="cohort" items="${cohorts}">
			  		<option value="${cohort.cohortId}">${cohort.name}</option>
				</c:forEach>
			
				</select>
			</td>
		</tr>
		<tr>
			<td><input type="submit" name='downloadPatients' value='<spring:message code="general.download" />' /></td>
			<td><input type="submit" name='setCohort' value='<spring:message code="xforms.setPatientDownloadCohort" />' /></td>			
		</tr>
		<tr>
			<td><spring:message code="xforms.selectPatientXform" /></td>
			<td><input type="file" name="patientXformFile" /></td>
		</tr>
		<tr>
			<td><input type="submit" name='downloadPatientXform' value='<spring:message code="xforms.downloadPatientXform" />' /></td>
			<td><input type="submit" name='uploadPatientXform' value='<spring:message code="xforms.uploadPatientXform" />' /></td>
			</tr>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
