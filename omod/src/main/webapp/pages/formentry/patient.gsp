<%
    ui.decorateWith("appui", "standardEmrPage")
    
    ui.includeCss("uicommons", "datatables/dataTables.css")
    ui.includeJavascript("uicommons", "datatables/jquery.dataTables.min.js")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("xforms.app.formentry.title") }", link: "${ ui.pageLink("coreapps", "findpatient/findPatient", [app: "xforms.formentry"]) }" },
        { label: "${ ui.format(patient.familyName) }, ${ ui.format(patient.givenName) }" , link: '${ui.pageLink("coreapps", "patientdashboard/patientDashboard", [patientId: patient.patientId])}'},
    ];
</script>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient ]) }

<script type="text/javascript">

	<% if (formToEntryUrlMap.size() > 0) { %>
		jq(document).ready(function() {
			var oTable = jq("#formEntryTableParent > #formEntryTable").dataTable({
				"bPaginate": false,
				"bAutoWidth": false,
				"aaSorting": [[0, 'asc']],
				"aoColumns":
					[
						{ "iDataSort": 1 },
						null
					]
			});
		});
	<% } %>
	
</script>

<div>

    <h1>${ ui.message("xforms.app.formentry.selectForm") }</h1>

    <div id="formEntryTableParent">

        <table id="formEntryTable">
            <thead>
                <tr>
                    <th>${ ui.message("general.name") }</th>
                    <th>${ ui.message("Form.version") }</th>
                </tr>
            </thead>
            <tbody>
                <% if (formToEntryUrlMap.size() == 0) { %>
                    <tr>
                        <td colspan="3">${ ui.message("emr.none") }</td>
                    </tr>
                <% } %>
                <% formToEntryUrlMap.each { entry -> %>
                    <tr>
                    	<% if (entry.value.formEntryUrl.contains("?")) { %>
                    		<td> <a href="/${ ui.contextPath() }/${entry.value.formEntryUrl}&formId=${entry.key.formId}&patientId=${patient.patientId}&refappui=true">${entry.key.name}</a> </td>
                    	<% } else { %>
                        	<td> <a href="/${ ui.contextPath() }/${entry.value.formEntryUrl}?formId=${entry.key.formId}&patientId=${patient.patientId}&refappui=true">${entry.key.name}</a> </td>
                        <% } %>
                        
                        <td> ${entry.key.version} </td>
                    </tr>
                <% } %>
            </tbody>
        </table>
    </div>
</div>