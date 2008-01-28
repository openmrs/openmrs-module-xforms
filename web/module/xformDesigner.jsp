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
            dojo.require("dojo.widget.AccordionPane");
            dojo.require("dojo.widget.TabContainer");
            dojo.require("dojo.widget.ContentPane");
            dojo.require("dojo.widget.FloatingPane");
            dojo.require("dojo.widget.Editor");
            dojo.require("dojo.widget.Tree");
            dojo.require("dojo.widget.TreeContextMenu");
            dojo.require("dojo.event.*");
			dojo.require("dojo.io.*");
			dojo.require("dojo.widget.TreeNode");
			dojo.require("dojo.widget.TreeSelector");
			dojo.require("dojo.widget.TreeLoadingController");
            
            dojo.hostenv.writeIncludes();
</script>

    <div class="dojo-Tree">
     <div class="dojo-TreeNode" title="World"></div>
     <div class="dojo-TreeNode" title="Business">
       <div class="dojo-TreeNode" title="News">
         <div class="dojo-TreeNode" title="Main"></div>
         <div class="dojo-TreeNode" title="Company News"></div>
         <div class="dojo-TreeNode" title="Economy"></div>
       </div>
       <div class="dojo-TreeNode" title="Markets"></div>
       <div class="dojo-TreeNode" title="Technology"></div>
       <div class="dojo-TreeNode" title="Jobs and Economy"></div>
     </div>
     <div class="dojo-TreeNode" title="Sports"></div>
   </div>

     <dl class="dojo-TreeContextMenu"  id="treeContextMenu">
      <dt class="dojo-TreeMenuItem" id="ctxMenuAdd" caption="Add Child" >
      <dt class="dojo-TreeMenuItem" id="ctxMenuDel" caption="Remove Item">
   </dl>

   <div class="dojo-Tree" menu="treeContextMenu"></div>
 
    <script type="text/javascript">
   var DemoTreeManager = {
     djWdgt: null,
     myTreeWidget: null,
     addTreeContextMenu: function(){
       var ctxMenu = this.djWdgt.createWidget("TreeContextMenu",{});
       ctxMenu.addChild(this.djWdgt.createWidget(
         "TreeMenuItem",{caption:"Add Child Menu Item",
           widgetId:"ctxAdd"}));
       ctxMenu.addChild(this.djWdgt.createWidget(
         "TreeMenuItem",{caption:"Delete this Menu Item",
           widgetId:"ctxDelete"`}));
       document.body.appendChild(ctxMenu.domNode);
       /* Bind the context menu to the tree */
       ctxMenu.listenTree(this.myTreeWidget);
     },
     addController: function(){
       this.djWdgt.createWidget(
         "TreeBasicController",
         {widgetId:"myTreeController",DNDController:"create"}
       );
     },
     bindEvents: function(){
       /* Bind the functions in the TreeActions object to the
          context menu entries */
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
       /* Initialize this object */
       this.djWdgt = dojo.widget;
       this.myTreeWidget = this.djWdgt.manager.
         getWidgetById("myTreeWidget");
       this.addTreeContextMenu();
       this.addController();
       this.bindEvents();
     }
   };
    
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
         alert("Nothing selected to delete",);
         return false;
       }
       this.controller = dojo.widget.manager.getWidgetById(controllerId);
       var res = this.controller.removeNode(node, dojo.lang.hitch(this));
     }
   };
    
   dojo.addOnLoad(function(){
     DemoTreeManager.init()
     });
    
   </script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
