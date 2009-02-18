<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/xforms/formrunner/org.purc.purcforms.FormRunner.nocache.js"/>

<div id="purcformrunner"><div>

<div id="formId" style="visibility:hidden;">${formId}</div>
<div id="patientId" style="visibility:hidden;">${patientId}</div>

<div id="dateTimeFormat" style="visibility:hidden;">${dateTimeFormat}</div>

<div id="entityIdName" style="visibility:hidden;">patientId</div>
<div id="formIdName" style="visibility:hidden;">formId</div>
    
<div id="entityFormDefDownloadUrlSuffix" style="visibility:hidden;">moduleServlet/xforms/xformDownload?target=xformentry&contentType=xml&</div>
<div id="formDataUploadUrlSuffix" style="visibility:hidden;">module/xforms/xformDataUpload.form</div>
<div id="afterSubmitUrlSuffix" style="visibility:hidden;">patientDashboard.form?</div>