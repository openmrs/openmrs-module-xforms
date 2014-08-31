<%
    ui.includeCss("uicommons", "datatables/dataTables.css")
    ui.includeJavascript("uicommons", "datatables/jquery.dataTables.min.js")
%>

<script type="text/javascript">

	<% if (formToEntryUrlMap.size() > 0) { %>
		jq(document).ready(function() {
			var oTable = jq("#formEntryTableParent > #formEntryTable").dataTable({
				"bPaginate": false,
				"bAutoWidth": false,
				"bSort": true,
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

    <div id="formEntryTableParent">

        <table id="formEntryTable" width="100%" border="1" cellspacing="0" cellpadding="2">
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
                    		<td> <a href="/${ ui.contextPath() }/${entry.value.formEntryUrl}&formId=${entry.key.formId}&patientId=${patient.patientId}&refappui=true&returnUrl=${returnUrl}">${entry.key.name}</a> </td>
                    	<% } else { %>
                        	<td> <a href="/${ ui.contextPath() }/${entry.value.formEntryUrl}?formId=${entry.key.formId}&patientId=${patient.patientId}&refappui=true&returnUrl=${returnUrl}">${entry.key.name}</a> </td>
                        <% } %>
                        
                        <td> ${entry.key.version} </td>
                    </tr>
                <% } %>
            </tbody>
        </table>
    </div>
    
</div>