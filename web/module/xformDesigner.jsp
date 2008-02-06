<%@ include file="/WEB-INF/template/include.jsp" %>

<script type="text/javascript" src='${pageContext.request.contextPath}/dwr/engine.js'></script>
<script type="text/javascript" src='${pageContext.request.contextPath}/dwr/util.js'></script>
<script type="text/javascript" src='${pageContext.request.contextPath}/dwr/interface/DWRXformsService.js'></script>

<script type="text/javascript">
	var djConfig = {debugAtAllCosts: false, isDebug: true };
</script>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />
 
<script type="text/javascript">
			dojo.require("dojo.html.*");
			dojo.require("dojo.event.*");
			dojo.require("dojo.io.*");
			
            dojo.require("dojo.widget.Toolbar");
            dojo.require("dojo.widget.LayoutContainer");
            dojo.require("dojo.widget.SplitContainer");
            dojo.require("dojo.widget.AccordionContainer");
            dojo.require("dojo.widget.AccordionPane");
            dojo.require("dojo.widget.TabContainer");
            dojo.require("dojo.widget.ContentPane");
            dojo.require("dojo.widget.FloatingPane");
            dojo.require("dojo.widget.Editor");
            dojo.require("dojo.widget.Tree");
            dojo.require("dojo.widget.TreeContextMenu");
			dojo.require("dojo.widget.TreeNode");
			dojo.require("dojo.widget.TreeSelector");
			dojo.require("dojo.widget.TreeLoadingController");
            
            dojo.hostenv.writeIncludes();
</script>

<script type="text/javascript">
	
	var xformDoc;
	   
   	function populateNodes(parentTreeNode,parentXformNode){
   		if(parentXformNode.nodeType != 1 || !parentXformNode.childNodes)
   			return;
   			
   		var childTreeNode = parentTreeNode;
   		var childXformNode;
    	for (var i=0;i<parentXformNode.childNodes.length;i++)
		{
			childXformNode = parentXformNode.childNodes[i];
			if(childXformNode.nodeType == 1 && childXformNode.localName.toString() == "label"){
				childTreeNode = dojo.widget.createWidget("TreeNode",{title:childXformNode.childNodes[0].nodeValue});
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
   	
	var TreeBuilder = {
		djWdgt: null,
		myTreeWidget: null,
   		buildTree:function (){
     		myTreeWidget = dojo.widget.createWidget("Tree",{
       		widgetId:"myNewTreeWidget",
       		DNDMode:"both",
       		DNDAcceptTypes:["myNewTreeWidget"]});
     	},
   		addTreeContextMenu:function (){
  			var djWdgt = dojo.widget;
  			var ctxMenu = djWdgt.createWidget("TreeContextMenu",{});
  			
   			ctxMenu.addChild(djWdgt.createWidget(
   				"TreeMenuItem",{caption:"Add Child Menu Item",widgetId:"ctxAdd"}));
   				
   			ctxMenu.addChild(djWdgt.createWidget(
     			"TreeMenuItem",{caption:"Delete This Menu Item",widgetId:"ctxDelete"}));
     			
   			document.body.appendChild(ctxMenu.domNode);
   			var myTree = dojo.widget.manager.getWidgetById("myNewTreeWidget");
   			/* Bind the context menu to the tree */
   			ctxMenu.listenTree(myTree);
 		},
 		addController: function(){
     		dojo.widget.createWidget("TreeBasicController",
       			{widgetId:"myTreeController",DNDController:"create"});
   		},
   		bindEvents: function(){
     		/* Bind the functions in the TreeActions object to the context menu entries */
     		dojo.event.topic.subscribe("ctxAdd/engage",
       			function (menuItem) { TreeActions.addNewNode(menuItem.getTreeNode(),
         			"myTreeController"); }
     		);
     		
     		dojo.event.topic.subscribe("ctxDelete/engage",
       			function (menuItem) { TreeActions.removeNode(menuItem.getTreeNode(),
         			"myTreeController"); }
     		);
   		},
   		init: function(){
      		this.djWdgt = dojo.widget;
       		this.addTreeContextMenu();
     		this.addController();
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
   		addNewNode: function(parent,controllerId){
     		this.controller = dojo.widget.manager.getWidgetById(controllerId);
     		if (!parent.isFolder) {
       			parent.setFolder();
     	}
     	var res = this.controller.createChild(parent, 0, { title: "New node" });
   		},
   		removeNode: function(node,controllerId){
     		if (!node) {
       			alert("Nothing selected to delete");
       			return false;
     		}
     		this.controller = dojo.widget.manager.getWidgetById(controllerId);
     		var res = this.controller.removeNode(node, dojo.lang.hitch(this));
   		}
 	};

 	dojo.addOnLoad(function(){TreeBuilder.setupTree()});

</script>

<div dojoType="LayoutContainer" style="width: 100%; height: 100%; padding: 0; margin: 0; border: 0;">
   
   <div dojoType="Toolbar" layoutAlign="top"> The toolbar will go here</div>
   
   <div dojoType="SplitContainer" layoutAlign="client" orientation="horizontal" sizerWidth="5" persist = "false" activeSizing="true">
                
   		<div dojoType="ContentPane" sizeShare="1" style="background-color:lightblue;">
	        <div id="myWidgetContainer">
				<span id="treePlaceHolder" style="background-color:#F00; color:#FFF;">
				      Loading ...
				</span>
			</div>
		</div>
	    
	    <div dojoType="ContentPane" sizeShare="4" style="background-color:white;">
	    	This will be the design surface	        
	    </div>
    
   </div>
     
</div>

