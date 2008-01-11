<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<script type="text/javascript">
	var djConfig = {debugAtAllCosts: false, isDebug: false };
</script>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<style>
	#tabBlock {
	
	top: 150px;
	
	left: 120px;
	
	width: 320px;
	
	height: 80px;
	
	}
 </style>
 
<script type="text/javascript">
            dojo.require("dojo.widget.Toolbar");
            dojo.require("dojo.widget.LayoutContainer");
            dojo.require("dojo.widget.SplitContainer");
            dojo.require("dojo.widget.AccordionContainer");
            dojo.require("dojo.widget.AccordionPane");
            dojo.require("dojo.widget.TabContainer");
            dojo.require("dojo.widget.ContentPane");
            dojo.require("dojo.widget.FloatingPane");
            dojo.require("dojo.widget.Editor");
            
            dojo.hostenv.writeIncludes();
</script>

<div dojoType="TabContainer" id="mainTabContainer” >
	<div dojoType="ContentPane" label="tab1" id="tab1">Contents of Tab 1 Pane</div>
	<div dojoType="ContentPane" label="tab1" id="tab1">Contents of Tab 2 Pane</div>
</div>

dojo.hostenv.writeIncludes();

/*function helloPressed() 
{ 
	alert('You pressed the button'); 
} 
function init() 
{ 
	var helloButton = dojo.widget.byId('helloButton');
	dojo.event.connect(helloButton, 'onClick', 'helloPressed') 
} 
dojo.addOnLoad(init);*/ 
//<button dojoType="Button" widgetId="helloButton">Hello World!</button> 


<%@ include file="/WEB-INF/template/footer.jsp" %>
