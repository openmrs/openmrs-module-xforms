<%@ include file="/WEB-INF/template/include.jsp" %>

<html>
  <head>
    <title>OpenMRS Form Designer</title>
    <openmrs:htmlInclude file="/moduleResources/xforms/formdesigner/org.purc.purcforms.FormDesigner.nocache.js"/>
  </head>
  <body>
    <div id="purcformsdesigner"><div>
    <div id="title" style="visibility:hidden;">OpenMRS Form Designer</div>
    <div id="rubberBand"></div>
    
    <div id="formId" style="visibility:hidden;">${formId}</div>
    
    <div id="dateTimeSubmitFormat" style="visibility:hidden;">${dateTimeSubmitFormat}</div>
    <div id="dateTimeDisplayFormat" style="visibility:hidden;">${dateTimeDisplayFormat}</div>
    
    <div id="entityIdName" style="visibility:hidden;">patientId</div>
    <div id="formIdName" style="visibility:hidden;">formId</div>
    
    <div id="formDefDownloadUrlSuffix" style="visibility:hidden;">moduleServlet/xforms/xformDownload?target=xform&contentType=xml&</div>
    <div id="formDefUploadUrlSuffix" style="visibility:hidden;">module/xforms/xformUpload.form?target=xform&contentType=xml&</div>
    <div id="formDefRefreshUrlSuffix" style="visibility:hidden;">moduleServlet/xforms/xformDownload?target=xformrefresh&contentType=xml&</div>
   
    <div id="defaultFontFamily" style="visibility:hidden;">${defaultFontFamily}</div>
   
  </body>
</html>

