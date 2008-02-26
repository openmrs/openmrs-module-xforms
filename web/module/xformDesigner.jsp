<%@ include file="/WEB-INF/template/include.jsp" %>

<script type="text/javascript" src='${pageContext.request.contextPath}/dwr/engine.js'></script>
<script type="text/javascript" src='${pageContext.request.contextPath}/dwr/util.js'></script>
<script type="text/javascript" src='${pageContext.request.contextPath}/dwr/interface/DWRXformsService.js'></script>

<script type="text/javascript">
	var djConfig = {debugAtAllCosts: false, isDebug: true };
</script>

<openmrs:htmlInclude file="/module/xforms/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.TabContainer");
	dojo.require("dojo.widget.LinkPane");
	dojo.require("dojo.widget.ContentPane");
	dojo.require("dojo.widget.LayoutContainer");
	dojo.require("dojo.widget.Checkbox");
	dojo.require("dojo.widget.Menu2");
	
	dojo.require("dojo.widget.Toolbar");
    dojo.require("dojo.widget.SplitContainer");
    dojo.require("dojo.widget.AccordionContainer");
    dojo.require("dojo.widget.FloatingPane");
    dojo.require("dojo.widget.Editor2");
    dojo.require("dojo.widget.Tree");
    dojo.require("dojo.widget.TreeContextMenu");
	dojo.require("dojo.widget.TreeNode");
	dojo.require("dojo.widget.TreeSelector");
	dojo.require("dojo.widget.TreeLoadingController");
	
	dojo.require("dojo.widget.TaskBar");
	dojo.require("dojo.widget.ComboBox");
	dojo.require("dojo.widget.Button");
	dojo.require("dojo.widget.FilteringTable");
	dojo.require("dojo.widget.Textbox");
	
</script>

<style type="text/css">
	table thead td, table thead th {
		background:#94BEFF;
	}
	input,select {
		width: 100%;
	}
</style>

<script type="text/javascript">
	
	var xformDoc;
	   
	function Refresh(){
		if(dojo.widget.manager.getWidgetById("mainTabContainer").selectedChildWidget.widgetId.toString() == "tabPreviewXform"){
			var f = document.getElementById('xformEntryIframe');
			f.contentWindow.location.reload(true);
		}
	}
	
	function saveXform(){
		DWRXformsService.saveXform({xformData:getXformXml(),formId:${formId}},saveXformResult);
	}
	
	function saveXformResult(ret){
		if(ret)
			alert("XForm saved successfully.");
		else
			alert("Failed to save XForm.");
	}
	
   	function populateNodes(parentTreeNode,parentXformNode){
   		if(parentXformNode.nodeType != 1 || !parentXformNode.childNodes)
   			return;
   			
   		var childTreeNode = parentTreeNode;
   		var childXformNode;
    	for (var i=0;i<parentXformNode.childNodes.length;i++)
		{
			childXformNode = parentXformNode.childNodes[i];
			if(childXformNode.nodeType == 1 && childXformNode.localName.toString() == "label"){
				childTreeNode = dojo.widget.createWidget("TreeNode",{title:childXformNode.childNodes[0].nodeValue, object:childXformNode.parentNode, childIconSrc:(childXformNode.parentNode.getAttribute("bind")) ? "../../images/note2.gif" : "../../images/add.gif"});
	       		parentTreeNode.addChild(childTreeNode);
	       	}
	       	else if(childXformNode.nodeType == 1 && childXformNode.childNodes)
	       		populateNodes(childTreeNode,childXformNode);
		}
   	}
   	      
   	function displayTree(){
   		var treeContainer = document.getElementById("myWidgetContainer");
	    var placeHolder = document.getElementById("treePlaceHolder");
	    treeContainer.replaceChild(myTreeWidget.domNode,placeHolder);
   	}
   	
   	function getXformXml(){
   		if(xformDoc.xml) //for IE this is the xml
   			return xformDoc.xml;
   			
   		var xmlSerializer = new XMLSerializer();
  		return xmlSerializer.serializeToString(xformDoc);
   	}
   	
   	function serializeNode (node) {
		  if (typeof XMLSerializer != 'undefined') {
		    return new XMLSerializer().serializeToString(node);
		  }
		  else if (typeof node.xml != 'undefined') {
		    return node.xml;
		  }
		  else if (typeof printNode != 'undefined') {
		    return printNode(node);
		  }
		  else if (typeof Packages != 'undefined') {
		    try {
		      var stringWriter = new java.io.StringWriter();
		      Packages.org.apache.batik.dom.util.DOMUtilities.writeNode(
		        node, stringWriter);
		      return stringWriter.toString();
		    }
		    catch (e) {
		      // might want to handle problem here
		      return '';
		    }
		  }
		  else {
		    // might want to handle problem here
		    return '';
		  }
	}
	
	function onTreeNodeSelection(message){
		var treeNode = message.node;
				
		var xformNode = treeNode.object;
		var labelNode = getChildNode(xformNode,"label");
		document.getElementById('txtText').value = labelNode.childNodes[0].nodeValue;
		
		var helpText = '';
		var helpNode = getChildNode(xformNode,"hint");
		if(helpNode && helpNode.childNodes[0])
			helpText = helpNode.childNodes[0].nodeValue;
		document.getElementById('txtHelpText').value = helpText;
		
		var node;
		var binding = xformNode.getAttribute("bind");
		if(!binding){
			document.getElementById("cboType").selectedIndex = -1;
			dojo.widget.manager.getWidgetById("chkRequired").setValue(false);
			dojo.widget.manager.getWidgetById("chkReadonly").setValue(false);
			document.getElementById("cboBinding").selectedIndex = -1;
			return;
		}
		
		var nodes = xformDoc.documentElement.getElementsByTagName("bind");
   		for(var i=0; i<nodes.length; i++){
   			node = nodes[i];
   			if(node.getAttribute("id").toString() == binding.toString()){
   				document.getElementById("cboBinding").value = node.getAttribute("nodeset");
   				
   				if(node.getAttribute("type"))
   					document.getElementById("cboType").value = node.getAttribute("type");
   				
   				var val = false;
   				if(node.getAttribute("required"))
   					val = (node.getAttribute("required").toString() == "true()") ? true : false;
    					
    			dojo.widget.manager.getWidgetById("chkRequired").setValue(val);
   				
   				val = false;
   				if(node.getAttribute("readonly"))
   					val = (node.getAttribute("readonly").toString() == "true()") ? true : false;
   				dojo.widget.manager.getWidgetById("chkReadonly").setValue(val);
   				
   				break;
   			}
   		}
	}
	
	function onTreeNodeDoubleSelection(message){
		//alert('double click');
	}
   	
   	function onTreeNodeMoveTo(message){   		
   		var node = message.child.object;
   		var parentNode = node.parentNode;   		
   		var isField = node.localName.toString() != "item";
    		
   		//remove text from old position where the node was
   		if(node.nextSibling.nodeType != 1)
   			parentNode.removeChild(node.nextSibling);
   			
   		//insert right before the node following our insert postion.
   		var nextSibling = message.child.getNextSibling().object;
   		parentNode.insertBefore(node,nextSibling);
   		
   		//put some new line and indention after the newly inserted node.
   		parentNode.insertBefore(xformDoc.createTextNode('\r\n'+ (isField ? '  ' : '    ')),nextSibling);
   		
    	showXformSource();
    }
   	
   	//Gets a child node with a given name.
   	function getChildNode(parentNode,childNodeName){
   		for(var i=0; i<parentNode.childNodes.length; i++){
   			if(parentNode.childNodes[i].nodeType == 1 && parentNode.childNodes[i].localName.toString() == childNodeName.toString())
   				return parentNode.childNodes[i];
   		}
   		return null;
   	}
   	
   	//Gets the position of a child node with a given name.
   	function getChildNodePos(parentNode,childNodeName){
   		for(var i=0; i<parentNode.childNodes.length; i++){
   			if(parentNode.childNodes[i].nodeType == 1 && parentNode.childNodes[i].localName.toString() == childNodeName.toString())
   				return i;
   		}
   		return null;
   	}
   	
   	function onApplyProperties(){
    	var treeSelector = dojo.widget.manager.getWidgetById("treeSelector");
   		if(treeSelector.selectedNode){
   			var labelText = document.getElementById('txtText').value;
   			treeSelector.selectedNode.titleNode.innerHTML = labelText;
   			
   			var node  = treeSelector.selectedNode.object;
   			var binding = node.getAttribute("bind");
   			if(binding){	
   				var tempNode = getChildNode(node,"label");
    			if(tempNode)
   					tempNode.childNodes[0].nodeValue = document.getElementById('txtText').value;    				
   				
   				var shortText = document.getElementById('txtShortText').value;
   				if(shortText && shortText.toString().length > 0){
   					
   					alert("Short Text= "+document.getElementById('txtShortText').value);
   				}
 									
 				var helpText = document.getElementById('txtHelpText').value;
 				tempNode = getChildNode(node,"hint");
 				if(helpText && helpText.length > 0){
    				if(!tempNode){
     					node.appendChild(xformDoc.createTextNode('  '));
 
   						tempNode = xformDoc.createElementNS(node.namespaceURI,"hint");
   	     				node.appendChild(tempNode);
     					
     					node.appendChild(xformDoc.createTextNode('\r\n  '));
       				}
     				
   					if(!tempNode.childNodes[0])
 						tempNode.appendChild(xformDoc.createTextNode(''));
 					
   					tempNode.childNodes[0].nodeValue = helpText;
   				}
   				else{
   					if(tempNode)
   						removeNode(tempNode);
    				}
   				
   				var control = document.getElementById("cboBinding");
   				binding = getNodeSetBinding(control.options[control.selectedIndex].value);
    			node.setAttribute("bind",binding);
   				
   				var bindingNode = getBindingNode(binding);
   				if(dojo.widget.manager.getWidgetById("chkRequired").checked)
   					bindingNode.setAttribute("required","true()");
   				else
   					bindingNode.removeAttribute("required");
	   				
	   			if(dojo.widget.manager.getWidgetById("chkReadonly").checked)
	   				bindingNode.setAttribute("readonly","true()");
	   			else
	   				bindingNode.removeAttribute("readonly");
	   			
   				control = document.getElementById("cboType");
   				bindingNode.setAttribute("type",control.options[control.selectedIndex].value);
      		}
      		else{
      			var labelNode = getChildNode(node,"label");
      			labelNode.childNodes[0].nodeValue = labelText;
      		}
      		
      		showXformSource();
   		}
   	}
   	
   	//gets the binding node
   	function getBindingNode(nodeName){
   		var nodes = xformDoc.documentElement.getElementsByTagName("bind");
   		for(var i=0; i<nodes.length; i++){
   			if(nodes[i].getAttribute("id").toString() == nodeName.toString())
   				return nodes[i];
   		}
   	}
   	
   	//gets the biding id of a given nodeset value.
   	function getNodeSetBinding(nodeset){
   		var nodes = xformDoc.documentElement.getElementsByTagName("bind");
   		for(var i=0; i<nodes.length; i++){
   			if(nodes[i].getAttribute("nodeset").toString() == nodeset.toString())
   				return nodes[i].getAttribute("id");
   		}
   	}
   	
   	function fillBindings(){
   		var opt,opt_txt,binding, widget = document.getElementById("cboBinding");
   		
   		var nodes = xformDoc.documentElement.getElementsByTagName("bind");
   		for(var i=0; i<nodes.length; i++){
   			binding = nodes[i].getAttribute("nodeset");
	   		opt = document.createElement("option");
			opt_txt = document.createTextNode(binding);
			opt.appendChild(opt_txt);
			opt.setAttribute("value", binding);
			widget.appendChild(opt);
		}
		
		document.getElementById("cboBinding").selectedIndex = -1;
   	}
   	
   	function fillTypes(){
   		var opt,opt_txt,type, widget = document.getElementById("cboType");
   		
	   	var nodes = xformDoc.documentElement.getElementsByTagName("simpleType");
   		for(var i=0; i<nodes.length; i++){
   			type = "openmrstype:" + nodes[i].getAttribute("name");
	   		opt = document.createElement("option");
			opt_txt = document.createTextNode(type);
			opt.appendChild(opt_txt);
			opt.setAttribute("value", type);
			widget.appendChild(opt);
   		}
   		
   		var nodes = xformDoc.documentElement.getElementsByTagName("complexType");
   		for(var i=0; i<nodes.length; i++){
   			type = "openmrstype:" + nodes[i].getAttribute("name");
	   		opt = document.createElement("option");
			opt_txt = document.createTextNode(type);
			opt.appendChild(opt_txt);
			opt.setAttribute("value", type);
			widget.appendChild(opt);
   		}
   		
   		document.getElementById("cboType").selectedIndex = -1;
   	}
   	
   	function showXformSource(){
   		document.getElementById("divXformSource").innerHTML = '<textarea style="width: 100%; height: 100%">'+getXformXml()+'</textarea>';
   	}
   	
   	//Removes a node from out XForms document.
   	function removeNode(node){
   		removeNodeFormatText(node);
   		node.parentNode.removeChild(node);
   	}
   	
   	//Removes node formating text (like indention) from our XForms document
   	function removeNodeFormatText(node){
   		var prevNode = node.previousSibling;
   		var nextNode = node.nextSibling;
   		
   		if(prevNode && prevNode.nodeType != 1)
   			node.parentNode.removeChild(prevNode);
   			
   		if(nextNode && nextNode.nodeType != 1 && nextNode.nodeValue && nextNode.nodeValue.toString() == "\r\n  ")
   			node.parentNode.removeChild(nextNode);
   	}
   	
	var TreeBuilder = {
		djWdgt: null,
		myTreeWidget: null,
   		buildTree:function (){
   			
   			//var myControllerSelector = dojo.widget.createWidget( "TreeController",{widgetId : "treeSelector"} );
   			
   			var myTreeController= dojo.widget.createWidget("TreeBasicController",{widgetId:"myTreeController",DNDController:"create"});
       			
   			var myTreeSelector = dojo.widget.createWidget( "TreeSelector",{widgetId : "treeSelector"} );
			dojo.event.topic.subscribe( myTreeSelector.eventNames.select,onTreeNodeSelection);
			dojo.event.topic.subscribe( myTreeSelector.eventNames.dblselect,onTreeNodeDoubleSelection);
			
     		myTreeWidget = dojo.widget.createWidget("Tree",{
       		widgetId:"myNewTreeWidget",
       		selector:"treeSelector",
       		controller:"myTreeController",
        	DNDMode:"between",
       		DNDAcceptTypes:["myNewTreeWidget"]});
       		
       		dojo.event.topic.subscribe( myTreeWidget.eventNames.moveTo,onTreeNodeMoveTo);
      	},
   		addTreeContextMenu:function (){
  			var djWdgt = dojo.widget;
  			var ctxMenu = djWdgt.createWidget("TreeContextMenu",{});
  			
   			ctxMenu.addChild(djWdgt.createWidget(
   				"TreeMenuItem",{caption:"Add Field",widgetId:"ctxAddField"}));
   				
   			ctxMenu.addChild(djWdgt.createWidget(
   				"TreeMenuItem",{caption:"Add Child",widgetId:"ctxAddChild"}));
   				
   			ctxMenu.addChild(djWdgt.createWidget(
     			"TreeMenuItem",{caption:"Delete",widgetId:"ctxDelete"}));
     		
     		ctxMenu.addChild(djWdgt.createWidget(
     			"TreeMenuItem",{caption:"Properties",widgetId:"ctxProperties"}));
     			
   			document.body.appendChild(ctxMenu.domNode);
   			var myTree = dojo.widget.manager.getWidgetById("myNewTreeWidget");
   			ctxMenu.listenTree(myTree);
 		},
    	bindEvents: function(){
     		/* Bind the functions in the TreeActions object to the context menu entries */
     		dojo.event.topic.subscribe("ctxAddField/engage",
       			function (menuItem) { TreeActions.addFieldNode(menuItem.getTreeNode(),
         			"myTreeController"); }
     		);
     		
     		dojo.event.topic.subscribe("ctxAddChild/engage",
       			function (menuItem) { TreeActions.addChildNode(menuItem.getTreeNode(),
         			"myTreeController"); }
     		);
     		
     		dojo.event.topic.subscribe("ctxDelete/engage",
       			function (menuItem) { TreeActions.removeNode(menuItem.getTreeNode(),
         			"myTreeController"); }
     		);
     		
     		dojo.event.topic.subscribe("ctxProperties/engage",
       			function (menuItem) { TreeActions.showNodeProperties(menuItem.getTreeNode(),
         			"myTreeController"); }
     		);
   		},
   		init: function(){
      		this.djWdgt = dojo.widget;
       		this.addTreeContextMenu();
     		this.bindEvents();
   		},
   		populateTree:function(data){
   			// code for IE
			if (window.ActiveXObject)
			{
				xformDoc=new ActiveXObject("Microsoft.XMLDOM");
				xformDoc.async="false";
				xformDoc.loadXML(data);
			}
			// code for Mozilla, Firefox, Opera, etc.
			else
			{
				var parser=new DOMParser();
				xformDoc=parser.parseFromString(data,"text/xml");
			}

			populateNodes(dojo.widget.manager.getWidgetById("myNewTreeWidget"),xformDoc.documentElement);
			displayTree();
			fillBindings();
			fillTypes();
			showXformSource();
	   	},
   		loadXmlDocument: function(){
 			DWRXformsService.getXform('${formId}',this.populateTree);
		},
 		setupTree: function(){
 			this.buildTree();
 			this.init();
 			this.loadXmlDocument();
  		}
	}
 
  	var TreeActions = {
   		addFieldNode: function(parent,controllerId){
   			
     		this.controller = dojo.widget.manager.getWidgetById(controllerId);
     		if (!parent.isFolder) 
       			parent.setFolder();
     		
     		var treeSelector = dojo.widget.manager.getWidgetById("treeSelector");
   			if(treeSelector.selectedNode){   				
	     		var node = xformDoc.createElementNS(treeSelector.selectedNode.object.namespaceURI,"input");
	     		var child = xformDoc.createElementNS(treeSelector.selectedNode.object.namespaceURI,"label");
	     		
	     		node.appendChild(xformDoc.createTextNode('\r\n    '));
	     		node.appendChild(child);
	     		node.appendChild(xformDoc.createTextNode('\r\n  '));
	     		
	     		child.appendChild(xformDoc.createTextNode("New Field"));
	     		node.setAttribute("bind","binding");
	     		
	     		xformDoc.documentElement.appendChild(xformDoc.createTextNode('  '));
	     		xformDoc.documentElement.appendChild(node);
	     		xformDoc.documentElement.appendChild(xformDoc.createTextNode('\r\n'));
	     		
	     		var childTreeNode = dojo.widget.createWidget("TreeNode",{title:"New Field", object:node, childIconSrc:"../../images/note2.gif"});
	       		dojo.widget.manager.getWidgetById("myNewTreeWidget").addChild(childTreeNode);	
	     		//var res = this.controller.createChild(dojo.widget.manager.getWidgetById("myNewTreeWidget"), 0, { title: "New Field", object:node, childIconSrc:"../../images/note2.gif"});
	     	}
   		},
   		addChildNode: function(parent,controllerId){
     		this.controller = dojo.widget.manager.getWidgetById(controllerId);
     		if (!parent.isFolder) 
       			parent.setFolder();
       			
   			var treeSelector = dojo.widget.manager.getWidgetById("treeSelector");
   			if(treeSelector.selectedNode){
     			var node = xformDoc.createElementNS(treeSelector.selectedNode.object.namespaceURI,"input");
 	     		var child = xformDoc.createElementNS(treeSelector.selectedNode.object.namespaceURI,"label");
	     		node.appendChild(child);
	     		child.appendChild(xformDoc.createTextNode("New Child"));
     			var res = this.controller.createChild(parent, 0, { title: "New Child", object:node, childIconSrc:"../../images/add.gif" });
     		}
   		},
   		removeNode: function(node,controllerId){
   			if(!confirm("Do you really want to delete this item ["+dojo.string.trim(node.title)+"] ?"))
   				return;
   				
     		if (!node) {
       			alert("Nothing selected to delete");
       			return false;
     		}
     		this.controller = dojo.widget.manager.getWidgetById(controllerId);
     		var res = this.controller.removeNode(node, dojo.lang.hitch(this));
     		
     		removeNode(node.object);
     		showXformSource();
   		},
   		showNodeProperties: function(node,controllerId){
   			dojo.widget.manager.getWidgetById("mainTabContainer").selectChild(dojo.widget.manager.getWidgetById("tabProperties"));
   		}
 	};

 	dojo.addOnLoad(function(){TreeBuilder.setupTree()});

</script>
	<div> ${pageContext.request.contextPath} </div>
	<div dojoType="LayoutContainer" style="width: 100%; height: 100%; padding: 0; margin: 0; border: 0;">
   
	<div dojoType="MenuBar2" layoutAlign="top">
		<div dojoType="MenuBarItem2" caption="File" submenuId="submenu1"></div>
	</div>

   <div id="topMenu" dojoType="ContentPane" layoutAlign="top" class="header" style="padding-bottom: 5px;">
		<div style="float: left; margin-right: 10px;">
			<button dojoType="Button" onclick="alert('pretending to download new mail');">
				<img src="../../images/file.gif" height=18 width=18>
				New
			</button>
		</div>
		<div style="float: left;">
			<button dojoType="Button" onclick='open("Mail/NewMessage.html",null,"height=500,width=600,status=yes,toolbar=no,menubar=no,location=no");'>
				<img src="../../images/open.gif" height=18 width=18>
				Open
			</button>
		</div>
		<div style="float: right;">
			<button dojoType="Button" onclick="saveXform();">
				<img src="../../images/save.gif" height=18 width=18>
				Save
			</button>
		</div>
		<div align=center>OpenMRS XForms Designer</div>
		
	</div>
   
   <div dojoType="SplitContainer" layoutAlign="client" orientation="horizontal" sizerWidth="3" persist = "false" activeSizing="true">
        
 	        <div dojoType="AccordionContainer"  sizeShare="1" duration="200" >
	   		
		   		<div id="myWidgetContainer" dojoType="ContentPane" selected="true" label="Fields" style="overflow: auto;" >
					<div id="treePlaceHolder" style="background-color:#F00; color:#FFF;">
					      Loading ...
					</div>
				</div>
				
				<div dojoType="ContentPane" label="Model">
					The second panel
				</div>
				
				<div dojoType="ContentPane" label="Pallete" style="overflow: auto;">
					<table cellspacing="10" cellpadding="10">
					<tr>
						<td>
						<button dojoType="Button" style="height:18; width:18;">
							Input 
						</button> 
						</td>
					</tr>
					<tr>
						<td> <button dojoType="Button"> Select1 </button> </td>
					</tr>
					<tr>
						<td> <button dojoType="Button"> Select </button> </td>
					</tr>
					<tr>
						<td> <button dojoType="Button"> TextArea </button> </td>
					</tr>
					<tr>
						<td> <button dojoType="Button"> Output </button> </td>
					</tr>
					<tr>
						<td> <button dojoType="Button"> Secret </button> </td>
					</tr>
					<tr>
						<td> <button dojoType="Button"> Range </button> </td>
					</tr>
					<tr>
						<td> <button dojoType="Button"> Upload </button> </td>
					</tr>
					<tr>
						<td> <button dojoType="Button"> Submit </button> </td>
					</tr>
					<tr>
						<td> <button dojoType="Button"> Triger </button> </td>
					</tr>
					</table>
				</div>
				
		    </div>
		
	    
	    <div dojoType="ContentPane" sizeShare="4" style="background-color:white;">
	    	
			<div id="mainTabContainer" dojoType="TabContainer" widgetId="mainTabContainer" tabposition="bottom" style="width: 100%; height: 100%" selectedChild="tabProperties" >
	
				<div widgetId="tabProperties" dojoType="ContentPane" widgetId="tabProperties" label="Properties" >
					<table border="1" width="100%">
						<thead>
							<tr>
								<th field="Name" dataType="String">Name</th>
								<th field="Value" dataType="String">Value</th>
							</tr>
						</thead>
						<tbody>
							<tr><td>Required</td><td><input type="checkbox" id="chkRequired" widgetId="chkRequired" dojoType="Checkbox"/></td></tr>
							<tr><td>Readonly</td><td><input type="checkbox" id="chkReadonly" widgetId="chkReadonly" dojoType="Checkbox"/></td></tr>
							<tr><td>Text</td><td><input id="txtText" style="width: 100%; height: 100%" /></td></tr>
							<tr><td>Short Text</td><td><input id="txtShortText" style="width: 100%; height: 100%" /></td></tr>
							<tr><td>Help Text</td><td><input id="txtHelpText" style="width: 100%; height: 100%" /></td></tr>
							<tr><td>Binding</td><td><select id="cboBinding"> </select> </td></tr>
							<tr>
								<td>Type</td>
								<td>
									<select id="cboType" >
									<option value="xsd:string" >Text</option>
									<option value="xsd:int" >Integer</option>
									<option value="xsd:date" >Date</option>
									</select>
								</td>
							</tr>
						</tbody>
					</table>
					<button dojoType="Button" onclick="onApplyProperties();">
						<img src="../../images/markRead.gif" height=18 width=18>
						Apply
					</button>
				</div>
				
				<div widgetId="tabXformSource" dojoType="ContentPane" label="XForm Source" style="display: none">
					<div id="divXformSource"> </div>  
				</div>
				
				<div widgetId="tabXhtmlXsltSource" dojoType="ContentPane" label="XHTML XSLT Source" style="display: none">
					<div id="divXhtmlXsltSource"> </div>  
				</div>
				
				<div widgetId="tabHtmlFormXsltSource" dojoType="ContentPane" label="HTML Form XSLT Source" style="display: none">
					<div id="divHtmlFormXsltSource"> </div>  
				</div>
		
				<div widgetId="tabDesign" dojoType="ContentPane" label="Design" style="display: none">
				</div>
				
				<div widgetId="tabPreviewXform" dojoType="ContentPane" label="Preview XForm" style="display: none">
					<iframe 
						src ="<%= request.getContextPath() %>/moduleServlet/xforms/xformDownload?target=xformentry&formId=${formId}"
						width="100%"
						height="100%"
						frameborder="0"
						id="xformEntryIframe"
						name="xformEntryIframe">
					</iframe>
				</div>
				
				<div widgetId="tabPreviewHtmlForm" dojoType="ContentPane" label="Preview HTML Form" style="display: none">
					The preview HTML Form goes here.
				</div>

			</div>

	    </div>
    
   </div>
   
   	<div dojoType="TaskBar" id="mytaskbar" layoutAlign="bottom" hasShadow="true" resizable="false">
	</div>
</div>

<div dojoType="PopupMenu2" widgetId="submenu1" contextMenuForWindow="true">
	<div dojoType="MenuItem2" caption="Enabled Item" onClick="alert('Hello world');"></div>
	<div dojoType="MenuItem2" caption="Disabled Item" disabled="true"></div>
	<div dojoType="MenuItem2" caption="Refresh" onClick="Refresh();" ></div>
	<div dojoType="MenuSeparator2" ></div>
	<div dojoType="MenuItem2" iconSrc="../../src/widget/templates/buttons/cut.gif" caption="Cut" accelKey="Ctrl+C"
		onClick="alert('not actually cutting anything, just a test!')"></div>
	<div dojoType="MenuItem2" iconSrc="../../src/widget/templates/buttons/copy.gif" caption="Copy" accelKey="Ctrl+X"
		onClick="alert('not actually copying anything, just a test!')"></div>
	<div dojoType="MenuItem2" iconSrc="../../src/widget/templates/buttons/paste.gif" caption="Paste" accelKey="Ctrl+V"
		onClick="alert('not actually pasting anything, just a test!')"></div>
	<div dojoType="MenuSeparator2"></div>
	<div dojoType="MenuItem2" caption="Enabled Submenu" submenuId="submenu2"></div>
	<div dojoType="MenuItem2" caption="Disabled Submenu" submenuId="submenu2" disabled="true"></div>
</div>


