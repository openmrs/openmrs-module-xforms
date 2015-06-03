<%
    ui.decorateWith("appui", "standardEmrPage")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("xforms.app.formentry.title") }", link: "${ ui.pageLink("coreapps", "findpatient/findPatient", [app: "xforms.formentry"]) }" },
        { label: "${ ui.format(patient.familyName) }, ${ ui.format(patient.givenName) }" , link: '${ui.pageLink("xforms", "formentry/patient", [patientId: patient.patientId])}'},
        { label: "${ ui.escapeJs(ui.format(formName)) }" }
    ];
    
    if ('${returnUrl}') {
	    breadcrumbs = [
	        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
	        { label: "${ ui.format(patient.familyName) }, ${ ui.format(patient.givenName) }" , link: '${ui.pageLink("xforms", "formentry/patient", [patientId: patient.patientId])}'},
	        { label: "${ ui.message("xforms.app.formentry.title") }", link: "${ ui.pageLink("coreapps", "findpatient/findPatient", [app: "xforms.formentry"]) }" },
	        { label: "${ ui.escapeJs(ui.format(formName)) }" }
	    ];
	 }
	 
	 if ('${returnUrl}') {
	    var returnPage = '${returnPage}';
	    var returnModule = '${returnModule}';
	    breadcrumbs = [
	        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
	        { label: "${ ui.format(patient.familyName) }, ${ ui.format(patient.givenName) }" , link: '${ui.pageLink(returnModule, returnPage, [patientId: patient.patientId])}'},
	        { label: "${ ui.escapeJs(ui.format(formName)) }" }
	    ];
	 }
</script>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient ]) }

<link rel="stylesheet" href="/${contextPath}/scripts/jquery-ui/css/green/jquery-ui.custom.css" type="text/css" />

<script type="text/javascript" src='/${contextPath}/openmrs.js'></script>
<script type="text/javascript" src='/${contextPath}/scripts/openmrsmessages.js'></script>
<script type="text/javascript" src='/${contextPath}/dwr/engine.js'></script>
<script type="text/javascript" src='/${contextPath}/dwr/util.js'></script>
<script type="text/javascript" src='/${contextPath}/dwr/interface/DWRXformsService.js'></script>

<script type="text/javascript" src='/${contextPath}/scripts/calendar/calendar.js'></script>
<script type="text/javascript" src='/${contextPath}/scripts/timepicker/timepicker.js'></script>

<script type="text/javascript" src='/${contextPath}/dwr/interface/DWRConceptService.js'></script>
<script type="text/javascript" src='/${contextPath}/dwr/interface/DWRPersonService.js'></script>
<script type="text/javascript" src='/${contextPath}/dwr/interface/DWRProviderService.js'></script>
<script type="text/javascript" src='/${contextPath}/scripts/jquery/autocomplete/OpenmrsAutoComplete.js'></script>
<script type="text/javascript" src='/${contextPath}/scripts/jquery/autocomplete/jquery.ui.autocomplete.autoSelect.js'></script>

<style type="text/css">

	body {
		font-family: "OpenSans", Arial, sans-serif;
		-webkit-font-smoothing: subpixel-antialiased;
		max-width: 1000px;
		margin: 10px auto;
		background: #eeeeee;
		color: #363463;
		font-size: 16px;
	}
	
	table {
		width: auto;
	}
	
	table th, table td {
		padding: 0px 0px;
		border: none;
		background: #F9F9F9;
	}
	
	table tr {
		border: none;
	}
	
	.gwt-ListBox, .gwt-TextBox, gwt-CheckBox, gwt-RadioButton {
 		min-width: 80%;
		color: #363463;
		display: block;
		margin: 0;
		margin-top: 1%;
		background-color: #FFF;
		border: 1px solid #DDD;
		font-family: inherit;
		font-size: 100%;
	}
	
	.gwt-ListBox:focus, .gwt-TextBox:focus {
	    outline: none;
	    background: #fffdf7;
	}
	
	.purcforms-repeat-border td {
		padding: 2px 5px;
	}
	
	.purcforms-group-border {
		background: #F9F9F9;
	}
	
	.purcforms-horizontal-grid-line, .purcforms-vertical-grid-line {
		display: block;
		position: absolute;
	}
	
	.popupSearchForm {
		width: 500px;
		height: 390px;
		padding: 2px;
		background-color: whitesmoke;
		border: 1px solid gray;
		position: absolute;
		display: block;
		z-index: 10;
		margin: 5px;
		overflow-y: auto;
	}

	.smallButton {
		font-size: .7em;
		border: 0px solid lightgrey;
		cursor: pointer;
		width: 0px;
		height: 0px;
		margin: 0px;
	}

	.description {
		font-size: .9em;
		padding-left: 10px;
		color: gray;
	}

	.closeButton {
		border: 1px solid gray;
		background-color: lightpink;
		font-size: .6em;
		color: black;
		float: right;
		margin: 2px;
		padding: 1px;
		cursor: pointer;
	}
	
	#proposeConceptForm { display: none; }
	.alert { color: red; }
	
</style>

<script type="text/javascript" src="/${contextPath}/moduleResources/xforms/formrunner/FormRunner.nocache.js"> </script>

<iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>

<div id="purcformrunner"></div>

<div id="formId" style="visibility:hidden;">${formId}</div>
<div id="patientId" style="visibility:hidden;">${patientId}</div>

<div id="dateTimeSubmitFormat" style="visibility:hidden;">${dateTimeSubmitFormat}</div>
<div id="dateTimeDisplayFormat" style="visibility:hidden;">${dateTimeDisplayFormat}</div>

<div id="dateSubmitFormat" style="visibility:hidden;">${dateSubmitFormat}</div>
<div id="dateDisplayFormat" style="visibility:hidden;">${dateDisplayFormat}</div>

<div id="entityIdName" style="visibility:hidden;">patientId</div>
<div id="formIdName" style="visibility:hidden;">formId</div>
    
<div id="entityFormDefDownloadUrlSuffix" style="visibility:hidden;">${entityFormDefDownloadUrlSuffix}</div>
<div id="formDataUploadUrlSuffix" style="visibility:hidden;">${formDataUploadUrlSuffix}</div>
<div id="afterSubmitUrlSuffix" style="visibility:hidden;">${afterSubmitUrlSuffix}</div>
<div id="afterCancelUrlSuffix" style="visibility:hidden;">${afterCancelUrlSuffix}</div>
<div id="externalSourceUrlSuffix" style="visibility:hidden;">moduleServlet/xforms/widgetValueDownload?</div>
<div id="multimediaUrlSuffix" style="visibility:hidden;">moduleServlet/xforms/multimediaDownload</div>

<div id="defaultFontFamily" style="visibility:hidden;">${defaultFontFamily}</div>
<div id="defaultFontSize" style="visibility:hidden;">${defaultFontSize}</div>
<div id="defaultGroupBoxHeaderBgColor" style="visibility:hidden;">#eeeeee</div>

<div id="appendEntityIdAfterSubmit" style="visibility:hidden;">1</div>
<div id="appendEntityIdAfterCancel" style="visibility:hidden;">1</div>

<div id="timeSubmitFormat" style="visibility:hidden;">${timeSubmitFormat}</div>
<div id="timeDisplayFormat" style="visibility:hidden;">${timeDisplayFormat}</div>

<div id="showSubmitSuccessMsg" style="visibility:hidden;">${showSubmitSuccessMsg}</div>

<div id="localeKey" style="visibility:hidden;">${localeKey}</div>
<div id="decimalSeparators" style="visibility:hidden;">${decimalSeparators}</div>
<div id="formatXml" style="visibility:hidden;">${formatXml}</div>

<div id="proposeConceptForm">
	<br />
	${ ui.message("ConceptProposal.proposeInfo" )}
	<br /><br />
	<b>${ ui.message("ConceptProposal.originalText") }></b><br />
	<textarea name="originalText" id="proposedText" rows="4" cols="60" /></textarea><br />
	<br />
	<span class="alert">
		${ ui.message("ConceptProposal.proposeWarning") }
	</span>
</div>


<div id="searchConcepts" style="height:0px;width:">
    <input type="text" id="conceptId_id_selection" />
	<input type="hidden" name="conceptId" id="conceptId_id" />
	<input type="text" name="conceptId_other" id="conceptId_id_other" style="display:none" value=""/>
</div>
<div id="searchProviders" style="height:0px;width:0px">
    <input type="text" id="providerId_id_selection" />
</div>
<div id="searchPersons" style="height:0px;width:0px">
    <input type="text" id="personId_id_selection" />
</div>
<div id="searchLocations" style="height:0px;width:0px">   
	<input type="text" id="locationId_id_selection" value="" onblur="updateLocationFields(this)"  
		placeholder="${ ui.message("xforms.location.search.placeholder") }" />
</div>
<script type="text/javascript">
	var jsDateFormat = '<openmrs:datePattern localize="false"/>';
	var jsLocale = '<%= org.openmrs.api.context.Context.getLocale() %>';

	var locationNameIdMap = new Object();
	var locationNames = [];

	<% locations.each { loc -> %>
		locationNames.push("${loc.name}");
		locationNameIdMap["${loc.name}"] = "${loc.locationId}";
	<% } %>
	
	jq('input#locationId_id_selection').autocomplete({
		source: locationNames,
		select: function(event, ui) {
					valElement.value = ui.item.value;
					txtElement.innerHTML = locationNameIdMap[ui.item.value];
					
					var parent = searchElement.parentNode;
					parent.removeChild(searchElement);
					parent.appendChild(valElement);
					
					valElement.focus();
				}
	});
	
	function updateLocationFields(searchField){
		if(locationNameIdMap[jq.trim(jq(searchField).val())] == undefined)
			jq(searchField).val('');
		if(jq.trim(jq(searchField).val()) == '')
			jq(valElement).val('');
	}
	
</script>


<script language="javascript">
	
	var searchElement;
	var conceptSearchElement;
	var providerSearchElement;
	var locationSearchElement;
	var personSearchElement;
	var options;

	var PurcformsText = {
	    	file: "${ ui.message("xforms.file") }",
	    	view: "${ ui.message("xforms.view") }",
	    	item: "${ ui.message("xforms.item") }",
	    	tools: "${ ui.message("xforms.tools") }",
	    	help: "${ ui.message("xforms.help") }",
	    	newItem: "${ ui.message("xforms.newItem") }",
	    	open: "${ ui.message("xforms.open") }",
	    	save: "${ ui.message("xforms.save") }",
	    	saveAs: "${ ui.message("xforms.saveAs") }",

	    	openLayout: "${ ui.message("xforms.foropenLayoutms") }",
	    	saveLayout: "${ ui.message("xforms.saveLayout") }",
	    	openLanguageText: "${ ui.message("xforms.openLanguageText") }",
	    	saveLanguageText: "${ ui.message("xforms.saveLanguageText") }",
	    	close: "${ ui.message("xforms.close") }",

	    	refresh: "${ ui.message("xforms.refresh") }",
	    	addNew: "${ ui.message("xforms.addNew") }",
	    	addNewChild: "${ ui.message("xforms.addNewChild") }",
	    	deleteSelected: "${ ui.message("xforms.deleteSelected") }",
	    	moveUp: "${ ui.message("xforms.moveUp") }",
	    	moveDown: "${ ui.message("xforms.moveDown") }",
	    	cut: "${ ui.message("xforms.cut") }",
	    	copy: "${ ui.message("xforms.copy") }",
	    	paste: "${ ui.message("xforms.paste") }",
	    	
	    	format: "${ ui.message("xforms.format") }",
	    	languages: "${ ui.message("xforms.languages") }",
	    	options: "${ ui.message("xforms.options") }",

	    	helpContents: "${ ui.message("xforms.helpContents") }",
	    	about: "${ ui.message("xforms.about") }",

	    	forms: "${ ui.message("xforms.forms") }",
	    	widgetProperties: "${ ui.message("xforms.widgetProperties") }",
	    	properties: "${ ui.message("xforms.properties") }",
	    	xformsSource: "${ ui.message("xforms.xformsSource") }",
	    	designSurface: "${ ui.message("xforms.designSurface") }",
	    	layoutXml: "${ ui.message("xforms.layoutXml") }",
	    	languageXml: "${ ui.message("xforms.languageXml") }",
	    	preview: "${ ui.message("xforms.preview") }",
	    	modelXml: "${ ui.message("xforms.modelXml") }",

	    	text: "${ ui.message("xforms.text") }",
	    	helpText: "${ ui.message("xforms.helpText") }",
	    	type: "${ ui.message("xforms.type") }",
	    	binding: "${ ui.message("xforms.binding") }",
	    	visible: "${ ui.message("xforms.visible") }",
	    	enabled: "${ ui.message("xforms.enabled") }",
	    	locked: "${ ui.message("xforms.locked") }",
	    	required: "${ ui.message("xforms.required") }",
	    	defaultValue: "${ ui.message("xforms.defaultValue") }",
	    	descriptionTemplate: "${ ui.message("xforms.descriptionTemplate") }",

	    	language: "${ ui.message("xforms.language") }",
	    	skipLogic: "${ ui.message("xforms.skipLogic") }",
	    	validationLogic: "${ ui.message("xforms.validationLogic") }",
	    	dynamicLists: "${ ui.message("xforms.dynamicLists") }",

	    	valuesFor: "${ ui.message("xforms.valuesFor") }",
	    	whenAnswerFor: "${ ui.message("xforms.whenAnswerFor") }",
	    	isEqualTo: "${ ui.message("xforms.isEqualTo") }",
	    	forQuestion: "${ ui.message("xforms.forQuestion") }",
	    	enable: "${ ui.message("xforms.enable") }",
	    	disable: "${ ui.message("xforms.disable") }",
	    	show: "${ ui.message("xforms.show") }",
	    	hide: "${ ui.message("xforms.hide") }",
	    	makeRequired: "${ ui.message("xforms.makeRequired") }",

	    	when: "${ ui.message("xforms.when") }",
	    	ofTheFollowingApply: "${ ui.message("xforms.ofTheFollowingApply") }",
	    	all: "${ ui.message("xforms.all") }",
	    	any: "${ ui.message("xforms.any") }",
	    	none: "${ ui.message("xforms.none") }",
	    	notAll: "${ ui.message("xforms.notAll") }",

	    	addNewCondition: "${ ui.message("xforms.addNewCondition") }",

	    	isEqualTo: "${ ui.message("xforms.isEqualTo") }",
	    	isNotEqual: "${ ui.message("xforms.isNotEqual") }",
	    	isLessThan: "${ ui.message("xforms.isLessThan") }",
	    	isLessThanOrEqual: "${ ui.message("xforms.isLessThanOrEqual") }",
	    	isGreaterThan: "${ ui.message("xforms.isGreaterThan") }",
	    	isGreaterThanOrEqual: "${ ui.message("xforms.isGreaterThanOrEqual") }",
	    	isNull: "${ ui.message("xforms.isNull") }",
	    	isNotNull: "${ ui.message("xforms.isNotNull") }",
	    	isInList: "${ ui.message("xforms.isInList") }",
	    	isNotInList: "${ ui.message("xforms.isNotInList") }",
	    	startsWith: "${ ui.message("xforms.startsWith") }",
	    	doesNotStartWith: "${ ui.message("xforms.doesNotStartWith") }",
	    	endsWith: "${ ui.message("xforms.endsWith") }",
	    	doesNotEndWith: "${ ui.message("xforms.doesNotEndWith") }",
	    	contains: "${ ui.message("xforms.contains") }",
	    	doesNotContain: "${ ui.message("xforms.doesNotContain") }",
	    	isBetween: "${ ui.message("xforms.isBetween") }",
	    	isNotBetween: "${ ui.message("xforms.isNotBetween") }",

			isValidWhen: "${ ui.message("xforms.isValidWhen") }",
			errorMessage: "${ ui.message("xforms.errorMessage") }",
			question: "${ ui.message("xforms.question") }",

			addField: "${ ui.message("xforms.addField") }",
			submit: "${ ui.message("xforms.submit") }",
			addWidget: "${ ui.message("xforms.addWidget") }",
			newTab: "${ ui.message("xforms.newTab") }",
			deleteTab: "${ ui.message("xforms.deleteTab") }",
			selectAll: "${ ui.message("xforms.selectAll") }",
			load: "${ ui.message("xforms.load") }",
			
			label: "${ ui.message("xforms.label") }",
			textBox: "${ ui.message("xforms.textBox") }",
			checkBox: "${ ui.message("xforms.checkBox") }",
			radioButton: "${ ui.message("xforms.radioButton") }",
			dropdownList: "${ ui.message("xforms.dropdownList") }",
			textArea: "${ ui.message("xforms.textArea") }",
			button: "${ ui.message("xforms.button") }",
			datePicker: "${ ui.message("xforms.datePicker") }",
			groupBox: "${ ui.message("xforms.groupBox") }",
			repeatSection: "${ ui.message("xforms.repeatSection") }",
			picture: "${ ui.message("xforms.picture") }",
			videoAudio: "${ ui.message("xforms.videoAudio") }",
			listBox: "${ ui.message("xforms.listBox") }",

			deleteWidgetPrompt: "${ ui.message("xforms.deleteWidgetPrompt") }",
			deleteTreeItemPrompt: "${ ui.message("xforms.deleteTreeItemPrompt") }",
			selectDeleteItem: "${ ui.message("xforms.selectDeleteItem") }",

			selectedPage: "${ ui.message("xforms.selectedPage") }",
			shouldNotSharePageBinding: "${ ui.message("xforms.shouldNotSharePageBinding") }",
			selectedQuestion: "${ ui.message("xforms.selectedQuestion") }",
			shouldNotShareQuestionBinding: "${ ui.message("xforms.shouldNotShareQuestionBinding") }",
			selectedOption: "${ ui.message("xforms.selectedOption") }",
			shouldNotShareOptionBinding: "${ ui.message("xforms.shouldNotShareOptionBinding") }",
			newForm: "${ ui.message("xforms.newForm") }",
			page: "${ ui.message("xforms.page") }",
			option: "${ ui.message("xforms.option") }",
			noDataFound: "${ ui.message("xforms.noDataFound") }",

			formSaveSuccess: "${ ui.message("xforms.formSaveSuccess") }",
			selectSaveItem: "${ ui.message("xforms.selectSaveItem") }",
			deleteAllWidgetsFirst: "${ ui.message("xforms.deleteAllWidgetsFirst") }",
			deleteAllTabWidgetsFirst: "${ ui.message("xforms.deleteAllTabWidgetsFirst") }",
			cantDeleteAllTabs: "${ ui.message("xforms.cantDeleteAllTabs") }",
			noFormId: "${ ui.message("xforms.noFormId") }",
			divFound: "${ ui.message("xforms.noFormId") }",
			noFormLayout: "${ ui.message("xforms.noFormLayout") }",
			formSubmitSuccess: "${ ui.message("xforms.formSubmitSuccess") }",
			missingDataNode: "${ ui.message("xforms.missingDataNode") }",

			openingForm: "${ ui.message("xforms.openingForm") }",
			openingFormLayout: "${ ui.message("xforms.openingFormLayout") }",
			savingForm: "${ ui.message("xforms.savingForm") }",
			savingFormLayout: "${ ui.message("xforms.savingFormLayout") }",
			refreshingForm: "${ ui.message("xforms.refreshingForm") }",
			translatingFormLanguage: "${ ui.message("xforms.translatingFormLanguage") }",
			savingLanguageText: "${ ui.message("xforms.savingLanguageText") }",
			refreshingDesignSurface: "${ ui.message("xforms.refreshingDesignSurface") }",
			loadingDesignSurface: "${ ui.message("xforms.loadingDesignSurface") }",
			refreshingPreview: "${ ui.message("xforms.refreshingPreview") }",

			count: "${ ui.message("xforms.count") }",
			clickToPlay: "${ ui.message("xforms.clickToPlay") }",
			loadingPreview: "${ ui.message("xforms.loadingPreview") }",
			unexpectedFailure: "${ ui.message("xforms.unexpectedFailure") }",
			uncaughtException: "${ ui.message("xforms.uncaughtException") }",
			causedBy: "${ ui.message("xforms.causedBy") }",
			openFile: "${ ui.message("xforms.openFile") }",
			saveFileAs: "${ ui.message("xforms.saveFileAs") }",

			alignLeft: "${ ui.message("xforms.alignLeft") }",
			alignRight: "${ ui.message("xforms.alignRight") }",
			alignTop: "${ ui.message("xforms.alignTop") }",
			alignBottom: "${ ui.message("xforms.alignBottom") }",
			makeSameWidth: "${ ui.message("xforms.makeSameWidth") }",
			makeSameHeight: "${ ui.message("xforms.makeSameHeight") }",
			makeSameSize: "${ ui.message("xforms.makeSameSize") }",
			layout: "${ ui.message("xforms.layout") }",
			deleteTabPrompt: "${ ui.message("xforms.deleteTabPrompt") }",

			text: "${ ui.message("xforms.text") }",
		    toolTip: "${ ui.message("xforms.toolTip") }",
		    childBinding: "${ ui.message("xforms.childBinding") }",
		    width: "${ ui.message("xforms.width") }",
		    height: "${ ui.message("xforms.height") }",
		    left: "${ ui.message("xforms.left") }",
		    top: "${ ui.message("xforms.top") }",
		    tabIndex: "${ ui.message("xforms.tabIndex") }",
		    repeat: "${ ui.message("xforms.repeat") }",
		    externalSource: "${ ui.message("xforms.externalSource") }",
		    displayField: "${ ui.message("xforms.displayField") }",
		    valueField: "${ ui.message("xforms.valueField") }",
		    fontFamily: "${ ui.message("xforms.fontFamily") }",
		    foreColor: "${ ui.message("xforms.foreColor") }",
		    fontWeight: "${ ui.message("xforms.fontWeight") }",
		    fontStyle: "${ ui.message("xforms.fontStyle") }",
		    fontSize: "${ ui.message("xforms.fontSize") }",
		    textDecoration: "${ ui.message("xforms.textDecoration") }",
		    textAlign: "${ ui.message("xforms.textAlign") }",
		    backgroundColor: "${ ui.message("xforms.backgroundColor") }",
		    borderStyle: "${ ui.message("xforms.borderStyle") }",
		    borderWidth: "${ ui.message("xforms.borderWidth") }",
		    borderColor: "${ ui.message("xforms.borderColor") }",
		    aboutMessage: "${ ui.message("xforms.aboutMessage") }",
		    more: "${ ui.message("xforms.more") }",
		    requiredErrorMsg: "${ ui.message("xforms.requiredErrorMsg") }",
		    questionTextDesc: "${ ui.message("xforms.questionTextDesc") }",
		    questionDescDesc: "${ ui.message("xforms.questionDescDesc") }",
		    questionIdDesc: "${ ui.message("xforms.questionIdDesc") }",
		    defaultValDesc: "${ ui.message("xforms.defaultValDesc") }",
		    questionTypeDesc: "${ ui.message("xforms.questionTypeDesc") }",
		    qtnTypeText: "${ ui.message("xforms.qtnTypeText") }",
		    qtnTypeNumber: "${ ui.message("xforms.qtnTypeNumber") }",
		    qtnTypeDecimal: "${ ui.message("xforms.qtnTypeDecimal") }",
		    qtnTypeDate: "${ ui.message("xforms.qtnTypeDate") }",
		    qtnTypeTime: "${ ui.message("xforms.qtnTypeTime") }",
		    qtnTypeDateTime: "${ ui.message("xforms.qtnTypeDateTime") }",
		    qtnTypeBoolean: "${ ui.message("xforms.qtnTypeBoolean") }",
		    qtnTypeSingleSelect: "${ ui.message("xforms.qtnTypeSingleSelect") }",
		    qtnTypeMultSelect: "${ ui.message("xforms.qtnTypeMultSelect") }",
		    qtnTypeRepeat: "${ ui.message("xforms.qtnTypeRepeat") }",
		    qtnTypePicture: "${ ui.message("xforms.qtnTypePicture") }",
		    qtnTypeVideo: "${ ui.message("xforms.qtnTypeVideo") }",
		    qtnTypeAudio: "${ ui.message("xforms.qtnTypeAudio") }",
		    qtnTypeSingleSelectDynamic: "${ ui.message("xforms.qtnTypeSingleSelectDynamic") }",
		    deleteCondition: "${ ui.message("xforms.deleteCondition") }",
    		addCondition: "${ ui.message("xforms.addCondition") }",
    		value: "${ ui.message("xforms.value") }",
    		questionValue: "${ ui.message("xforms.questionValue") }",
    		and: "${ ui.message("xforms.and") }",
       		deleteItemPrompt: "${ ui.message("xforms.deleteItemPrompt") }",
    		changeWidgetTypePrompt: "${ ui.message("xforms.changeWidgetTypePrompt") }",
    		removeRowPrompt: "${ ui.message("xforms.removeRowPrompt") }",
    		remove: "${ ui.message("xforms.remove") }",
    		browse: "${ ui.message("xforms.browse") }",
    		clear: "${ ui.message("xforms.clear") }",
    		deleteItem: "${ ui.message("xforms.deleteItem") }",
    		cancel: "${ ui.message("xforms.cancel") }",
    		clickToAddNewCondition: "${ ui.message("xforms.clickToAddNewCondition") }",
    		qtnTypeGPS: "${ ui.message("xforms.qtnTypeGPS") }",
    		qtnTypeBarcode: "${ ui.message("xforms.qtnTypeBarcode") }",
    		qtnTypeGroup: "${ ui.message("xforms.qtnTypeGroup") }",
    		palette: "${ ui.message("xforms.palette") }",
    		saveAsXhtml: "${ ui.message("xforms.saveAsXhtml") }",
    		groupWidgets: "${ ui.message("xforms.groupWidgets") }",
    		action: "${ ui.message("xforms.action") }",
    		submitting: "${ ui.message("xforms.submitting") }",
    		authenticationPrompt: "${ ui.message("xforms.authenticationPrompt") }",
    		invalidUser: "${ ui.message("xforms.invalidUser") }",
    		login: "${ ui.message("xforms.login") }",
    		userName: "${ ui.message("xforms.userName") }",
    		password: "${ ui.message("xforms.password") }",
    		noSelection: "${ ui.message("xforms.noSelection") }",
    		cancelFormPrompt: "${ ui.message("xforms.cancelFormPrompt") }",
    		print: "${ ui.message("xforms.print") }",
    		yes: "${ ui.message("xforms.yes") }",
    		no: "${ ui.message("xforms.no") }",
       		searchServer: "${ ui.message("xforms.searchServer") }",
       		recording: "${ ui.message("xforms.recording") }",
       		search: "${ ui.message("xforms.search") }",
       		processingMsg: "${ ui.message("xforms.processingMsg") }",
       		length: "${ ui.message("xforms.length") }",
       		clickForOtherQuestions: "${ ui.message("xforms.clickForOtherQuestions") }",
       		ok: "${ ui.message("xforms.ok") }",
       		undo: "${ ui.message("xforms.undo") }",
       		redo: "${ ui.message("xforms.redo") }",
       		loading: "${ ui.message("xforms.loading") }",
       		allQuestions: "${ ui.message("xforms.allQuestions") }",
       		selectedQuestions: "${ ui.message("xforms.selectedQuestions") }",
       		otherQuestions: "${ ui.message("xforms.otherQuestions") }",
       		wrongFormat: "${ ui.message("xforms.wrongFormat") }",
       		timeWidget: "${ ui.message("xforms.timeWidget") }",
			dateTimeWidget: "${ ui.message("xforms.dateTimeWidget") }",
			lockWidgets: "${ ui.message("xforms.lockWidgets") }",
			unLockWidgets: "${ ui.message("xforms.unLockWidgets") }",
			changeWidgetH: "${ ui.message("xforms.changeWidgetH") }",
			changeWidgetV: "${ ui.message("xforms.changeWidgetV") }",
			changeToTextBoxWidget: "${ ui.message("xforms.changeToTextBoxWidget") }",
			saveAsPurcForm: "${ ui.message("xforms.saveAsPurcForm") }",
			localeChangePrompt: "${ ui.message("xforms.localeChangePrompt") }",
			javaScriptSource: "${ ui.message("xforms.javaScriptSource") }",
       		calculation: "${ ui.message("xforms.calculation") }",
       		id: "${ ui.message("xforms.id") }",
       		formKey: "${ ui.message("xforms.formKey") }",
       		logo: "${ ui.message("xforms.logo") }",
       		filterField: "${ ui.message("xforms.filterField") }",
       		table: "${ ui.message("xforms.table") }",
      		horizontalLine: "${ ui.message("xforms.horizontalLine") }",
       		verticalLine: "${ ui.message("xforms.verticalLine") }",
       		addRowsBelow: "${ ui.message("xforms.addRowsBelow") }",
       		addRowsAbove: "${ ui.message("xforms.addRowsAbove") }",
       		addColumnsRight: "${ ui.message("xforms.addColumnsRight") }",
       		addColumnsLeft: "${ ui.message("xforms.addColumnsLeft") }",
       		numberOfRowsPrompt: "${ ui.message("xforms.numberOfRowsPrompt") }",
       		numberOfColumnsPrompt: "${ ui.message("xforms.numberOfColumnsPrompt") }",
       		deleteColumn: "${ ui.message("xforms.deleteColumn") }",
       		deleteRow: "${ ui.message("xforms.deleteRow") }",
       		repeatChildDataNodeNotFound: "${ ui.message("xforms.repeatChildDataNodeNotFound") }",
       		selectedFormField: "${ ui.message("xforms.selectedFormField") }",
       		edit: "${ ui.message("xforms.edit") }",
       		find: "${ ui.message("xforms.find") }",
       		css: "${ ui.message("xforms.css") }",
       		bold: "${ ui.message("xforms.bold") }",
       		italic: "${ ui.message("xforms.italic") }",
       		underline: "${ ui.message("xforms.underline") }",
       		mergeCells: "${ ui.message("xforms.mergeCells") }",
       		exclusiveOption: "${ ui.message("xforms.exclusiveOption") }",
       		otherProperties: "${ ui.message("xforms.otherProperties") }",
       		exclusiveQuestion: "${ ui.message("xforms.exclusiveQuestion") }",
       		cls: "${ ui.message("xforms.cls") }"
	};
	
	function searchExternal(key,value,parentElement,textElement,valueElement,filterField){
		if (key == 'date') {
			showCalendar(valueElement, 100);
			return;
		}
		else if (key == 'datetime') {
			showDateTimePicker(valueElement, 100);
			return;
		}
		else if (key == 'time') {
			showTimePicker(valueElement, 100);
			return;
		}
		
		if (typeof(dojo) != "undefined"){
			var searchWidget = dojo.widget.manager.getWidgetById("conceptId_search");
			//parentElement.appendChild(searchWidget.domNode.parentNode);
			searchWidget.includeClasses = (filterField == null ? [] : filterField.split(","));
		
			var selectionWidget = dojo.widget.manager.getWidgetById("conceptId_selection");
	 		selectionWidget.displayNode = textElement;
	
			selectionWidget.hiddenInputNode = valueElement;
			
			searchWidget.clearSearch();
			searchWidget.toggleShowing();
	
	
			var left = dojo.style.totalOffsetLeft(parentElement.parentNode, false) + dojo.style.getBorderBoxWidth(parentElement.parentNode) + 10;
			if (left + dojo.style.getBorderBoxWidth(searchWidget.domNode) > dojo.html.getViewportWidth())
				left = dojo.html.getViewportWidth() - dojo.style.getBorderBoxWidth(searchWidget.domNode) - 10 + dojo.html.getScrollLeft();
			
			var top = dojo.style.totalOffsetTop(parentElement.parentNode, true);
			var scrollTop = dojo.html.getScrollTop();
			var boxHeight = dojo.style.getBorderBoxHeight(searchWidget.domNode);
			var viewportHeight = dojo.html.getViewportHeight();
			if ((top + boxHeight - scrollTop) > viewportHeight - 5)
				top = viewportHeight - boxHeight + scrollTop - 10;
		
			dojo.style.setPositivePixelValue(searchWidget.domNode, "top", top);
	
			dojo.style.setPositivePixelValue(searchWidget.domNode, "left", left);
			
			searchWidget.inputNode.select();
			searchWidget.inputNode.value = value;
		}
		else{
			key = jq.trim(key).toLowerCase();
			if(key != 'concept' && key != 'location' && key != 'provider' && key != 'person'){
				alert("The external source property '"+key+"' is invalid, you need to specify the appropriate one "+
						"from the following from the xforms design page: concept, location, provider and person");
				return;
			}
			
			//If we had previously displayed a search widget, remove it and add the original value widget.
			if (searchElement != null) {
				var parent = searchElement.parentNode;
				if (parent != null) {
			    	parent.removeChild(searchElement);
			    	parent.appendChild(valElement);
				}
			}
			
			valElement = valueElement;
			txtElement = textElement;
			
			var searchInputId;
			var placeHolderText;
			var callback;
			var createCallback;
			var isSearchElementNull = false;
			if(key == 'concept'){
				searchInputId = 'conceptId_id_selection';
				placeHolderText = '${ ui.message("Concept.search.placeholder") }';
				var includeC = (filterField == null ? "" : filterField).split(",");
				var excludeC = "".split(",");
				var includeD = "".split(",");
				var excludeD = "".split(",");

				// the typical callback
				if (options == null)
    				options = {includeClasses:includeC, excludeClasses:excludeC, includeDatatypes:includeD, excludeDatatypes:excludeD};
    			else
    				options.includeClasses = includeC;
    				
    			createCallback = new CreateCallback(options);
    			callback = createCallback.conceptCallback();
				if(conceptSearchElement == null){
					isSearchElementNull = true;
					conceptSearchElement = document.getElementById(searchInputId);
				}
				searchElement = conceptSearchElement;
			}else if(key == 'provider'){
				searchInputId = 'providerId_id_selection';
				placeHolderText = '${ ui.message("Provider.search.placeholder") }';
				callback = new CreateCallback().providerCallback();
				if(providerSearchElement == null){
					isSearchElementNull = true;			
					providerSearchElement = document.getElementById(searchInputId);
				}
				searchElement = providerSearchElement;
			}else if(key == 'person'){
				searchInputId = 'personId_id_selection';
				placeHolderText = '${ ui.message("Person.search.placeholder") }';
				callback = new CreateCallback().personCallback();
				if(personSearchElement == null){
					isSearchElementNull = true;
					personSearchElement = document.getElementById(searchInputId);
				}
				searchElement = personSearchElement;
			}else if(key == 'location'){
				if(locationSearchElement == null){
					isSearchElementNull = true;
					locationSearchElement = document.getElementById('locationId_id_selection');
				}
				searchElement = locationSearchElement;
			}
			
			if (createCallback) {
				// This is what maps each ConceptListItem or LocationListItem returned object to a name in the dropdown
				createCallback.displayNamedObject = function(origQuery) { return function(item) {
					// dwr sometimes puts strings into the results, just display those
					if (typeof item == 'string') {
						return null;
					}
					
					// item is a ConceptListItem or LocationListItem object
					// add a space so the term highlighter below thinks the first word is a word
					var textShown = " " + item.name;					
					var value = item.name;
					
					return { label: textShown, value: value, object: item};
				}; };
			}
			
			//we use a custom autocomplete for location widget since there is 
			//no autocomplete call back for locations in the core
			if(key != 'location'){
				// set up the autocomplete
				new AutoComplete(searchInputId, callback, {
					select: function(event, ui) {
						if (ui.item.object) {
							funcItemIdAutoCompleteOnSelect(ui.item.object, ui.item, key);
						}
					},
		           	placeholder:placeHolderText,
		           	autoSelect: true
				});
			}
				
			if(isSearchElementNull == true)
				searchElement.parentNode.removeChild(searchElement);
			
			searchElement.style.height = valueElement.parentNode.parentNode.parentNode.parentNode.style.height;
			searchElement.style.width = valueElement.parentNode.parentNode.parentNode.parentNode.style.width;
			
			var parent = valueElement.parentNode;
			parent.removeChild(valueElement);
			
			parent.appendChild(searchElement);
			searchElement.focus();
			searchElement.value = value;
		}
	}
	
	function isUserAuthenticated(){
		DWRXformsService.isAuthenticated(checkIfLoggedInCallback);
	}

	function authenticateUser(username, password){
		DWRXformsService.authenticate(username,password,checkIfLoggedInCallback);
	}

	function checkIfLoggedInCallback(isLoggedIn) {
		authenticationCallback(isLoggedIn);
	}

	function initialize(){
		//var selectionWidget = dojo.widget.manager.getWidgetById("conceptId_selection");
		//selectionWidget.changeButton.style.display = "none";

		if (typeof(dojo) != "undefined"){
			
			dojo.addOnLoad( function() {
				dojo.event.topic.subscribe("conceptId_search/select", 
					function(msg) {
						if (msg) {
							var concept = msg.objs[0];
							var conceptPopup = dojo.widget.manager.getWidgetById("conceptId_selection");
							conceptPopup.displayNode.innerHTML = concept.conceptId + "^" + concept.name + "^99DCT";
							conceptPopup.hiddenInputNode.value = concept.name;						
							conceptPopup.hiddenInputNode.focus();
						}
					}
				);
			})	

		}
		else{
			document.getElementById("searchConcepts").style.visibility="hidden";
			document.getElementById("searchProviders").style.visibility="hidden";
			document.getElementById("searchPersons").style.visibility="hidden";
			document.getElementById("searchLocations").style.visibility="hidden";
		}
	}
	
	function funcItemIdAutoCompleteOnSelect(selectedObj, item, key) {
		if(key == 'concept'){
			valElement.value = selectedObj.name;
			txtElement.innerHTML = selectedObj.conceptId + "^" + selectedObj.name + "^99DCT";
		}else if(key == 'provider'){
			valElement.value = selectedObj.displayName;
			txtElement.innerHTML = selectedObj.providerId;
		}else if(key == 'person'){
			valElement.value = selectedObj.personName;
			txtElement.innerHTML = selectedObj.uuid;
		}
		
		var parent = searchElement.parentNode;
		parent.removeChild(searchElement);
		parent.appendChild(valElement);
		
		valElement.focus();
		
		searchElement = null;
		valElement = null;
	}
	
	
	function showProposeConceptForm() {
		jq('#proposedText').val("");
		jq('#proposeConceptForm').dialog('open');
		document.getElementById('proposedText').focus();
		jq('#proposedText').focus();

		return false;
	}

	function proposeConcept() {
		var box = document.getElementById('proposedText');
		if (box.value == '')  {
			alert("Proposed Concept text must be entered. Or simply click Cancel");
			box.focus();
		}
		else {
			jq('#proposeConceptForm').dialog("close");
			DWRConceptService.findProposedConcepts(box.value, preProposedConcepts);
		}
	}

	function preProposedConcepts(concepts) {
		if (concepts.length == 0) {
			var conceptName = document.getElementById('proposedText').value;
			valElement.value = conceptName;
    		txtElement.innerHTML = 'PROPOSED' + "^" + conceptName + "^99DCT";
			var parent = searchElement.parentNode;
    		parent.removeChild(searchElement);
    		parent.appendChild(valElement);
    		
    		valElement.focus();
    		
    		searchElement = null;
    		valElement = null;
		}
		else {
			//display a box telling them to pick a preposed concept:
			alert('${ ui.message("ConceptProposal.proposeDuplicate") }');
			removeAutoCompleteWidget();
		}
	}

    function removeAutoCompleteWidget() {
		var parent = searchElement.parentNode;
    	parent.removeChild(searchElement);
    	parent.appendChild(valElement);
		valElement.focus();
		
		searchElement = null;
		valElement = null;
    }

    jq(document).ready(function() {
		jq('#proposeConceptForm').dialog({
			autoOpen: false,
			modal: true,
			title: '${ ui.message("ConceptProposal.proposeNewConcept") }',
			width: '30%',
			zIndex: 100,
			close: function() { jq("#problem_concept").autocomplete("close"); },
			buttons: { '${ ui.message("ConceptProposal.propose") }': function() { proposeConcept(); },
					   '${ ui.message("general.cancel") }': function() { jq(this).dialog("close"); removeAutoCompleteWidget();}
			}
		});
		
	});
	
</script>
