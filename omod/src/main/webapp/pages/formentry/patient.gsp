<%
    ui.decorateWith("appui", "standardEmrPage")
    
    def tabs = [
		[ id: "forms", label: ui.message("xforms.app.formentry.forms"), provider: "xforms", fragment: "formentry/forms" ],
		[ id: "encounters", label: ui.message("xforms.app.formentry.encounters"), provider: "xforms", fragment: "formentry/encounters" ]
	]
	
	tabs = tabs.flatten()
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("xforms.app.formentry.title") }", link: "${ ui.pageLink("coreapps", "findpatient/findPatient", [app: "xforms.formentry"]) }" },
        { label: "${ ui.format(patient.familyName) }, ${ ui.format(patient.givenName) }" , link: '${ui.pageLink("coreapps", "patientdashboard/patientDashboard", [patientId: patient.patientId])}'},
    ];
    
    
    if ('${returnUrl}') {
    	breadcrumbs = [
	        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
	        { label: "${ ui.format(patient.familyName) }, ${ ui.format(patient.givenName) }" , link: '${ui.escapeJs(returnUrl)}'},
	        { label: "${ ui.message("xforms.app.formentry.title") }", link: "${ ui.pageLink("coreapps", "findpatient/findPatient", [app: "xforms.formentry"]) }" }
    	];
    }
    
    jq(function(){
		jq(".tabs").tabs();
	});
</script>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient ]) }

<div class="tabs" xmlns="http://www.w3.org/1999/html">
	<ul>
		<% tabs.each { %>
			<li>
				<a href="#${ it.id }">
					${ it.label }
				</a>
			</li>
		<% } %>
	</ul>
	
	<% tabs.each { %>
		<div id="${it.id}">
			${ ui.includeFragment(it.provider, it.fragment, [ patient: patient ]) }
		</div>
	<% } %>
</div>