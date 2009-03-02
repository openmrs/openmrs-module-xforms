<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<style type="text/css">
	body {
		font-size: 12px;
	}
</style>

<openmrs:htmlInclude file="/moduleResources/xforms/formrunner/org.purc.purcforms.FormRunner.nocache.js"/>

<div id="purcformrunner"><div>

<div id="formId" style="visibility:hidden;">${formId}</div>
<div id="patientId" style="visibility:hidden;">${patientId}</div>

<div id="dateTimeSubmitFormat" style="visibility:hidden;">${dateTimeSubmitFormat}</div>
<div id="dateTimeDisplayFormat" style="visibility:hidden;">${dateTimeDisplayFormat}</div>

<div id="entityIdName" style="visibility:hidden;">patientId</div>
<div id="formIdName" style="visibility:hidden;">formId</div>
    
<div id="entityFormDefDownloadUrlSuffix" style="visibility:hidden;">moduleServlet/xforms/xformDownload?target=xformentry&contentType=xml&</div>
<div id="formDataUploadUrlSuffix" style="visibility:hidden;">module/xforms/xformDataUpload.form</div>
<div id="afterSubmitUrlSuffix" style="visibility:hidden;">patientDashboard.form?</div>
<div id="externalSourceUrlSuffix" style="visibility:hidden;">moduleServlet/xforms/widgetValueDownload?</div>

<div id="defaultFontFamily" style="visibility:hidden;">${defaultFontFamily}</div>

<div id="appendEntityIdAfterSubmit" style="visibility:hidden;">1</div>