<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Forms" otherwise="/login.htm" redirect="/admin/forms/formSchemaDesign.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:htmlInclude file="/dwr/engine.js"/>
<openmrs:htmlInclude file="/dwr/util.js"/>
<openmrs:htmlInclude file="/dwr/interface/DWRXformsService.js"/>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />
<openmrs:htmlInclude file="/moduleResources/xforms/scripts/medicalHistoryFields.js"/>

<style>
	.required {
		color: red;
	}
	a.delete {
		background: url(${pageContext.request.contextPath}/images/delete.gif) no-repeat center center;
		text-decoration: none;
		padding-left: 2px;
		cursor: pointer;
	}
	.disabled, .disabled * {
		color: gray;
		background-color: whitesmoke;
		}
		.disabled #formFieldTitle {
			background-color: whitesmoke;
		}
	div.dojoTree {
		overflow: hidden;
	}
	#editFormField {
		position: absolute;
		left: -1000px;
		top: 0px;
		background-color: white;
		z-index: 20;
		width: 500px;
		border: 2px solid lightgreen;
		padding: 1px;
		}
		#editFormField.disabled {
			border-color: gray;
		}
	
	#formFieldTitle {
		background-color: lightgreen;
		width: 100%;
	}
	#fieldWarning, #fieldWarningIframe {
		position: absolute;
		margin-left: 5%;
		margin-top: 7%;
		width: 90%;
		padding: 3px;
		}
		#fieldWarning {
			color: firebrick;
			border: 2px solid firebrick;
			text-align: center;
			z-index: 999;
			background-color: white;
		}
		#fieldWarningIframe {
			padding: 2px;
			z-index: 998;
			height: 50px;
		}
	span.fieldConceptHit {
		color: gray;
	}
	span.treeNodeRow div.dojoTree div.dojoTreeNode {
		display: inline;
	}
	.openmrsSearchTable tr td div {
		overflow: hidden;
	}
	#fieldSearchDiv {
		position: fixed;
		z-index: 10;
		background-color: white;
	}
	
</style>

<script type="text/javascript">
	var formPublished = false;
	var formId = 0;
</script>

<h2>
	<spring:message code="xforms.medicalHistoryFields" />
</h2>

<br/>

<div dojoType="TreeBasicController" widgetId="treeController" DNDController="create"></div>
<div dojoType="TreeSelector" widgetId="searchTreeSelector"></div>
<div dojoType="TreeSelector" widgetId="treeSelector"></div>
<div dojoType="TreeContextMenu" toggle="explode" contextMenuForWindow="false" widgetId="treeContextMenu">
	<div dojoType="TreeMenuItem" treeActions="edit" caption="Edit Field" widgetId="treeContextMenuEdit"></div>
	<div dojoType="TreeMenuItem" treeActions="addChild" caption="Create" widgetId="treeContextMenuCreate"></div>
	<div dojoType="TreeMenuItem" treeActions="remove" caption="Remove" widgetId="treeContextMenuRemove"></div>
</div>

<div id="loadingTreeMessage">
	<br/>
	&nbsp;
	<spring:message code="general.loading" />
</div>

<table width="99%">
	<tr>
		<td valign="top">
			<div dojoType="Tree" menu="treeContextMenu" strictFolders="false" DNDMode="between" toggler="fade" widgetId="tree" DNDAcceptTypes="<c:if test="${form.published != true}">*</c:if>" controller="treeController" selector="treeSelector">
			</div>
		</td>
		<td valign="top" style="padding-left: 5px;" id="fieldSearch" width="40%">
			<div id="fieldSearchDiv">
				<div dojoType="ConceptSearch" widgetId="fieldSearch" searchLabel='<spring:message code="Concept.find" />' showHeaderRow="false" ></div>
			</div>
		</td>
	</tr>
</table>

<div id="editFormField">
	<div id="formFieldTitle"><spring:message code="FormField.edit"/>:</div>
	
	<form xonsubmit="save(selectedNode)" style="padding: 0px; margin: 0px; overflow: auto">
		<table id="field" width="100%">
			<input type="hidden" id="fieldId" value=""/>
			<tr>
				<td valign="top">
					<spring:message code="Field.name"/>
				</td>
				<td>
					<input type="text" id="fieldName" size="60" />
				</td>
			</tr>
			<tr>
				<td valign="top">
					<spring:message code="xforms.tabIndex"/>
				</td>
				<td>
					<input type="text" id="tabIndex" size="10" /> <input type="hidden" id="isNew" />
				</td>
			</tr>
		</table>
	
		<input type="submit" id="saveFormField" onclick="return save(selectedNode);" value="<spring:message code="general.save"/>" />
		<input type="button" id="cancelFormField" onclick="cancelClicked()" value="<spring:message code="general.cancel"/>" />
	</form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>