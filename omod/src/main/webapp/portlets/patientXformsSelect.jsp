<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:hasPrivilege privilege="View Encounters">
	<openmrs:portlet url="patientEncounters" id="patientDashboardEncounters" patientId="${patient.patientId}" parameters="num=10|hideHeader=true|title=Encounter.last.encounters|showPagination=true" />
	<br/>
</openmrs:hasPrivilege>

<openmrs:hasPrivilege privilege="Form Entry">		
	<script type="text/javascript">	
		function downloadForm(formId) {
			if ( formId == '' ) {
				return false;
			}
			else { 
				url = '${pageContext.request.contextPath}/module/xforms/formEntry.form?formId=' + formId + '&patientId=${patient.patientId}';
			}
			window.location = url;
		}
	</script>

	<div id="selectForm">
		<form id="selectFormForm" method="get" action="<%= request.getContextPath() %>/module/xforms/formEntry.form?patientId=${patient.patientId}&formId=${form.formId}">			
			Add an encounter: 			 
			<select id="formSelect" name="formId">
				<option value="" selected></option>
				<c:forEach items="${forms}" var="form">
					<c:if test="${form.retired == false}">
						<option value="${form.formId}">
							${form.name} (v.${form.version})
							<c:if test="${form.published == false}"><i>(<spring:message code="formentry.unpublished"/>)</i></c:if>
						</option>
					</c:if>
				</c:forEach>
			</select>
			<input type="hidden" name="target" value="xformentry"/>
			<input type="hidden" name="patientId" value="${patient.patientId}"/>
			<input type="submit" value="Add"/>
		</form>
	</div>
	
</openmrs:hasPrivilege>
		