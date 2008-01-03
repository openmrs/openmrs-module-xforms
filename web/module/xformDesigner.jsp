<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<script type="text/javascript">
	var djConfig = {debugAtAllCosts: false, isDebug: false };
</script>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
            dojo.require("dojo.widget.Toolbar");
            dojo.require("dojo.widget.LayoutContainer");
            dojo.require("dojo.widget.SplitContainer");
            dojo.require("dojo.widget.AccordionContainer");
            dojo.require("dojo.widget.TabContainer");
            dojo.require("dojo.widget.ContentPane");
            
            dojo.hostenv.writeIncludes();
</script>

<div dojoType="dojo.widget.SplitContainer" id="rightPane"
        orientation="vertical"  sizerWidth="5"  activeSizing="0">
        <div id="listPane" dojoType="dojo.widget.ContentPane" sizeMin="20" sizeShare="20">
              Message List will go here
        </div>
                                       
        <div id="message" dojoType="dojo.widget.ContentPane" sizeMin="20" sizeShare="80">
            Message will go here
        </div>
</div> <!--  End right hand side split container -->


<%@ include file="/WEB-INF/template/footer.jsp" %>
