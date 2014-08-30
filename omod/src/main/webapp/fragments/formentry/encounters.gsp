<%
    ui.includeCss("uicommons", "datatables/dataTables.css")
    ui.includeJavascript("uicommons", "datatables/jquery.dataTables.min.js")
%>

<script type="text/javascript">

	<% if (encounters.size() > 0) { %>
		jq(document).ready(function() {
			var oTable = jq("#encounterTableParent > #encounterTable").dataTable({
				"bPaginate": false,
				"bAutoWidth": false,
				"bSort": false
			});
		});
	<% } %>
	
</script>

<div>

    <div id="encounterTableParent">

        <table id="encounterTable" width="100%" border="1" cellspacing="0" cellpadding="2">
            <thead>
                <tr>
                    <th>${ ui.message("Encounter.form") }</th>
                    <th>${ ui.message("ActiveLists.date") }</th>
                    <th>${ ui.message("general.type") }</th>
                    <th>${ ui.message("Encounter.provider") }</th>
                    <th>${ ui.message("Encounter.location") }</th>
                    <th>${ ui.message("Encounter.enterer") }</th>
                </tr>
            </thead>
            <tbody>
				<% if (encounters.size() == 0) { %>
                    <tr>
                        <td colspan="3">${ ui.message("emr.none") }</td>
                    </tr>
                <% } %>
                
                <% encounters.each { encounter -> %>
                    <tr>
                        <td> <a href=/${ ui.contextPath() }/${formToEditUrlMap[encounter.form]}?encounterId=${encounter.encounterId}&patientId=${patient.patientId}&refappui=true>${encounter.form.name}</a> </td>
                        <td> ${ ui.formatDatetimePretty(encounter.encounterDatetime) }</td>
                        <td> ${encounter.encounterType.name} </td>
                        <td> 
                        	<% encounter.providersByRoles.each { key, value -> %>
			                	${value.name}
			                <% } %>	 
                        </td>
                        <td> ${encounter.location} </td>
                        <td> ${encounter.creator.personName} </td>
                    </tr>
                <% } %>
                
            </tbody>
        </table>
    </div>
    
</div>