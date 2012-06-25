dojo.require("dojo.widget.openmrs.ConceptSearch");
dojo.require("dojo.widget.openmrs.FieldSearch");
dojo.require("dojo.widget.openmrs.OpenmrsPopup");

dojo.require("dojo.widget.Tree");
dojo.require("dojo.widget.TreeBasicController");
dojo.require("dojo.widget.TreeSelector");
dojo.require("dojo.widget.TreeNode");
dojo.require("dojo.widget.TreeContextMenu");

dojo.hostenv.writeIncludes();

var fieldSearch = null;		// field search widget
var cSelection = null;		// concept search widget
var tree;
var controller;
var treeSelector;
var searchTreeSelector;
var searchTree;
var selectedNode = null;
var nodesToAdd = [];		// nodes not added on first pass (because their parent wasn't loaded yet)
var searchTreeNodes = [];	// saves tree nodes created during search (in order to delete them)

dojo.addOnLoad( function(){

	useLoadingMessage();

	dojo.event.topic.subscribe('treeContextMenuEdit/engage',
		function (menuItem) { editClicked( menuItem.getTreeNode()); }
	);
	
	dojo.event.topic.subscribe('treeContextMenuCreate/engage',
		function (menuItem) { createClicked( menuItem.getTreeNode()); }
	);

	dojo.event.topic.subscribe('treeContextMenuRemove/engage',
		function (menuItem) { removeClicked( menuItem.getTreeNode()); }
	);

	// initialize the tree
	controller = dojo.widget.manager.getWidgetById('treeController');
	tree = dojo.widget.manager.getWidgetById('tree');
	treeSelector = dojo.widget.manager.getWidgetById('treeSelector');
	searchTreeSelector = dojo.widget.manager.getWidgetById('searchTreeSelector');
	fieldSearch = dojo.widget.manager.getWidgetById('fieldSearch');
	//cSelection = dojo.widget.manager.getWidgetById('cSelection');
	
	tree.DNDAcceptTypes = ["*"];
	DWRXformsService.getJSTree(evalTreeJS);
	
	dojo.event.topic.subscribe(tree.eventNames.moveTo, new nodeMoved(), "execute");
	dojo.event.topic.subscribe(tree.eventNames.removeNode, new nodeRemoved(), "execute");
	dojo.event.topic.subscribe(searchTreeSelector.eventNames.select, new nodeSelected(), "execute");
	dojo.event.topic.subscribe(treeSelector.eventNames.dblselect, function ( msg ){ editClicked(msg.node); } );
	
	// Get div and input for editable formfields
	tree.editDiv = document.getElementById("editFormField");
	
	tree.fieldIdInput = document.getElementById("fieldId");
	tree.fieldNameInput = document.getElementById("fieldName");
	tree.tabIndexInput = document.getElementById("tabIndex");
	tree.isNewInput = document.getElementById("isNew");
	tree.saveFieldButton = document.getElementById("saveFormField");
	tree.cancelFieldButton = document.getElementById("cancelFormField");
	
	tree.containerNode.style.display = "";
	
	dojo.event.topic.subscribe(tree.eventNames.createDOMNode, new domNodeCreated(), "execute");
	
	dojo.event.topic.subscribe("cSearch/select", 
		function(msg) {
			var obj = msg.objs;
			if (msg.objs.length)
				obj = msg.objs[0];
	});
		
	dojo.event.topic.subscribe("fieldSearch/objectsFound", 
			function(msg) {
				var link = '<a href="../../dictionary/concept.form">Add New Concept</a>';
				msg.objs.push(link); //setup links for appending to the end
			}
		);
	
	dojo.event.topic.subscribe("fieldSearch/select", 
		function(msg) {
			for (o in msg.objs) {
				var obj = msg.objs[o];
				var child = dojo.widget.byId(obj.widgetId);
				
				var oldParent = child.parent;
				var oldTree = child.tree;
				
				var newParent = tree;
				
				if (treeSelector.selectedNode)
					newParent = treeSelector.selectedNode.parent;
				
				// create a new node for this item so that it mimicks the "move" functionality
				var node = addNode(newParent, copyObject(child.data), null, null, null, treeSelector.selectedNode);
				
				// newParent.doAddChild(child, newParent.children.length);
				
				if (newParent.expand)
					newParent.expand();
				
				var newParent = node.parent;
				var newTree = node.tree;
		
				var message = {
						oldParent: oldParent, oldTree: oldTree,
						newParent: newParent, newTree: newTree,
						child: node,
						skipEdit: true
				};
				
				dojo.html.removeClass(node.titleNode, "fieldConceptHit");
				dojo.dom.removeChildren(node.afterLabelNode);
				node.afterLabelNode.appendChild(getRemoveLink(node.data));
				
				dojo.event.topic.publish(tree.eventNames.moveFrom, message);
				dojo.event.topic.publish(tree.eventNames.moveTo, message);
			}
			
			fieldSearch.clearSearch();
			fieldSearch.inputNode.value = "";
			fieldSearch.inputNode.focus();
			return false;
		}
	);
	
	if (fieldSearch) {
		fieldSearch.inputNode.select();
		
		fieldSearch.allowAutoJump = function() { return false; };
		
		// remove the nodes that were added in the search
		fieldSearch.onRemoveAllRows = function onRemoveAllRows(tbody) {
				while(searchTreeNodes.length) {
					searchTreeNodes[0].destroy();
					searchTreeNodes.splice(0,1);
				}
			};
		
		fieldSearch.getCellFunctions =  function() {
				return [this.simpleClosure(this, "getNumber"), 
						this.simpleClosure(this, "getFieldContent")
						];
			};
			
		fieldSearch.getFieldContent = function(obj) {
			if (typeof obj == 'string') return obj;
			
			var domNode = document.createElement("span");
			
			var data = getData(obj);
			domNode.title = data.title;
			
			// create a mini tree
			var properties = { //id: "miniTree", 
							DNDMode: "between", 
							showRootGrid: false,
							DNDAcceptTypes: ["*"],
							selector: "searchTreeSelector"};
							
			var parentNode = domNode;
			
			var miniTree = dojo.widget.createWidget("Tree", properties, parentNode, "last");
			searchTreeNodes.push(miniTree);
			
			var node = addNode(miniTree, data, data.label);
			
			obj.widgetId = node.widgetId;
			
			miniTree.actionIsDisabled = function(action) {
					if (!action || action.toUpperCase() == "MOVE")
						return false;
					return true;
				};
				
			if (obj.fieldId && obj.concept) {
				var d2 = getData(obj.concept);
				var n2 = addNode(miniTree, d2, d2["label"]);
				dojo.html.addClass(n2.titleNode, "fieldConceptHit");
				node.afterLabelNode.appendChild(document.createTextNode(" -"));
				domNode.className = "treeNodeRow";
			}
			
			return domNode;
		};
	}
});

var domNodeCreated = function(val) {
	dojo.debug("domNodeCreated: " + val);
	this.value = val;
	this.execute = function(msg) {
		var child = msg.source;
		if (child && child.labelNode && child.data) {
			child.labelNode.onmouseover = function() {
					var widg = dojo.widget.byNode(this.parentNode);
					if (widg && widg.data) {
						var data = widg.data;
						var s = "";
						//if (data.formFieldId)
						//	s += " FormField Id: " + data.formFieldId + " ";
						if (data.fieldId)
							s += " Field Id: " + data.fieldId + " ";
						if (data.conceptId)
							s += " Concept Id: " + data.conceptId + " ";
							
						window.status = s;
					}
			}
		}
		var t = 8;
	};
}

var nodeMoved = function() {
	this.value = null;
	this.execute = function(msg) {
		if (msg.oldTree != msg.newTree) {
			// add the item back in our search list
			var isFieldNode = false;
			if (msg.child.data.fieldId)
				isFieldNode = true;
			// add node back into search tree
			var node = addNode(msg.oldTree, copyObject(msg.child.data), msg.child.title, 0, isFieldNode);
			
			if (msg.oldTree.children.length > 1) {
				if (isFieldNode)
					node.afterLabelNode.appendChild(document.createTextNode(" -"));
				else
					dojo.html.addClass(node.titleNode, "fieldConceptHit");
			}
			
			msg.oldTree.containerNode.style.display = "";
			msg.newTree.containerNode.style.display = "";
			
			if (msg.child.data.isSet)
				DWRConceptService.getConceptSet(addConceptSet(msg.child), msg.child.data.conceptId);
			
			if (msg.skipEdit) {
				//updateSortWeight(msg.child);
				save(msg.child, /* formNotUsed */ true);
			}
			else { // if we moved from another tree, open up the edit div
				selectedNode = msg.child;
				var closure = function(target) { return function() { editClicked(target); }; };
				setTimeout(closure(msg.child), 0);
			}
			dojo.html.removeClass(msg.child.titleNode, "fieldConceptHit");
			dojo.dom.removeChildren(msg.child.afterLabelNode);
			msg.child.afterLabelNode.appendChild(getRemoveLink(msg.child.data));
			msg.child.unMarkSelected();
			
		}
		else if (msg.oldParent != msg.newParent) {
			//updateSortWeight(msg.child);
			// save node's new parent 
			save(msg.child, true);
			if (msg.oldParent && msg.oldParent.updateExpandIcon)
				msg.oldParent.updateExpandIcon();
		}
		else {
			// changing the order of things
			//updateSortWeight(msg.child);
			save(msg.child, true);
		}
		
	};
}

var addConceptSet = function(newParent) {
	return function(concepts) {
		for (var i=0; i<concepts.length; i++) {
			var data = getData(concepts[i]);
			data.sortWeight = i * 100.0;
			var node = addNode(newParent, data);
		}
	}
}

var nodeRemoved = function(val) {
	this.value = val;
	this.execute = function(msg) {
		DWREngine.setAsync(false);
		removeNode(msg.child);
		DWREngine.setAsync(true);
	};
}

function removeNode(node) {
	if (node.data && node.data["fieldId"]) { //formFieldId
		for (child in node.children) {
			removeNode(node.children[child]);
		}
		DWRXformsService.deleteFormField(node.data["fieldId"]); //formFieldId
		
		if (node.parent && node.parent.updateExpandIcon && node.parent.tree)
			node.parent.updateExpandIcon();
	}
}

var nodeSelected = function(val) {
	this.value = val;
	this.execute = function(msg) {
		// mimic drag and drop "move" action
		if (msg.node) {
			if (treeSelector.selectedNode) {
				var node = treeSelector.selectedNode;
				var parent = treeSelector.selectedNode.parent;
				if (parent == null)
					parent = tree;
				var insertIndex = parent.children.length;
				var children = parent.children;
				if (children != null) {
					for (var i=0; i < children.length; i++) {
						if (children[i] == node) {
							insertIndex = i + 1;
							break;
						}
					}
				}
				tree.controller.move(msg.node, parent, insertIndex);
			}
			else
				tree.controller.move(msg.node, tree, tree.children.length);
		}
	};
}

// process create operation 
function createClicked(selNode) {
	if (!selNode) {
		if (treeSelector.selectedNode)
			selNode = treeSelector.selectedNode;
		else
			selNode = tree;
	}
		
	if (selNode.actionIsDisabled(selNode.actions.ADDCHILD))
		return false;

	//this.controller = dojo.widget.manager.getWidgetById(controllerId);
	var newChild = controller.createChild(selNode, 0, { suggestedTitle: "New node" });
	selectedNode = newChild;
	editClicked(newChild);
	tree.fieldNameInput.focus();
}

function removeClicked(selectedNode, skipConfirm) {
	if (selectedNode.actionIsDisabled(selectedNode.actions.REMOVE))
		return false;
	
	var answer = true;
	if (!skipConfirm)
		answer = confirm("Delete this node and all of its children?");
	
	if (answer)
		controller.removeNode(selectedNode);
}

function editClicked(node) {
	selectedNode = node;
	
	var domNode = node.domNode;
	
	tree.containerNode.style.display = "";
	tree.editDiv.style.display = "";
	
	var s = tree.editDiv.style;
	s.left = dojo.style.getAbsoluteX(node.domNode, true) + "px";
	s.top = dojo.style.getAbsoluteY(node.domNode, true) + "px";
	var w = dojo.style.getPixelValue(tree.domNode, "width");
	if (!w) w = dojo.style.getPixelValue(tree.domNode, "offsetWidth");
	if (!w) w = dojo.style.getBorderBoxWidth(tree.domNode);
	if (!w) w = 500;
	if (w != 0)
		s.width = w + "px";
	dojo.debug("s.width: " + s.width);
	//setFieldDisabled(false);
	
	// add edit values fields here
	if (node.data) {
		var data = node.data;
		// field info
		tree.fieldNameInput.value = data["fieldName"];
		
		if(typeof(data["tabIndex"]) == 'undefined')
			data["tabIndex"] = 0;
		
		if(typeof(data["isNew"]) == 'undefined')
			data["isNew"] = true;
		
		tree.tabIndexInput.value = data["tabIndex"];
		tree.isNewInput.value = data["isNew"];
		
		if (data["fieldId"]) 
			tree.fieldIdInput.value = data["fieldId"];
		else 
			tree.fieldIdInput.value = tree.tableNameInput.value = "";
	}
	else
		node.data = [];
	
	s.display = "";
	
	// focus on save if save button is shown (not hidden due to unpublished form)
	if (tree.saveFieldButton)
		tree.saveFieldButton.focus();
	else
		tree.cancelFieldButton.focus();
	
	tree.fieldIdInput.focus();
}


function evalTreeJS(js) {
	dojo.debug("evaluating js");
	if (js.indexOf("DWR") == -1) {
		eval(js);
	}
	dojo.debug("done evaluating js");
	
	document.getElementById('loadingTreeMessage').style.display = "none";	

}

function clearFormField() {
	tree.fieldIdInput.value = '';
	tree.fieldNameInput.value = '';
	tree.tabIndexInput.value = '';
	tree.isNewInput.value = '';
}

function editAllFields() {
	if (tree && tree.fieldIdInput) {
		var val = tree.fieldIdInput.value;
		if (val && val.length)
			window.open("field.form?fieldId=" + val);
		else
			alert("Field widget does not exist yet.  It will be created when you save this formField");
	}
	
	return false;
}

function getFieldLabel(data) {
	var fieldLabel = "";
	if (data)
		fieldLabel += data["fieldName"] + " (" + data["fieldId"] + ")";
	
	return fieldLabel;
}

function save(target, formNotUsed) {
	if (target && target.data) {
		var data = target.data;
		var changed = true;
		
		if (changed) {
		
			if (!formNotUsed) {				
				if (tree.fieldIdInput.value != 'undefined' && tree.fieldIdInput.value != '')
					data["fieldId"] = tree.fieldIdInput.value;
				data["fieldName"] = tree.fieldNameInput.value;
				data["tabIndex"] = tree.tabIndexInput.value;
				data["isNew"] = tree.isNewInput.value;
			} // end "if (!formNotUsed)"
			
			// save the field to the database
			selectedNode = target;
			//DWRXformsService.saveFormField(endSaveFormField(target), data.fieldId, data.fieldName, data.tabIndex, data.isNew);
			DWRXformsService.saveFormField(data.fieldId, data.fieldName, data.tabIndex, data.isNew);
			tree.isNewInput.value = false;
			data.isNew = false;
			
			// update the field label in the tree
			target.titleNode.innerHTML = target.title = getFieldLabel(data);
			if (!formPublished && selectedNode) {
				dojo.dom.removeChildren(selectedNode.afterLabelNode);
				selectedNode.afterLabelNode.appendChild(getRemoveLink(data));
			}
			
			//target.unMarkSelected();
		}
		//else
		//	tree.editDiv.style.display = "none";

	}
	
	tree.editDiv.style.display = "none";
	
	return false;
}

function endSaveFormField(target) {
	return function(savedFormFieldIds) {
		// close edit box and set ids on parent
		cancelClicked(savedFormFieldIds, target);

		// if the node just saved was a set, save the children as well
		if (target.data.isSet) {
			for (var i=0; i<target.children.length;i++) {
				save(target.children[i], /* formNotUsed */ true);
			}
		}
	}
}

function sortWeightError() {
	alert("Error assigning sort weight. \n\nUpdate Element Sort Order according to field Number and Part using the link on this page\n\The current visual order of elements may not be correct.");
	return null;
}

function addNode(addToTree, data, label, attemptCount, insertNode, insertAfterNode) {
	if (data) {
		var parent;
		if (data.parent && typeof data.parent != "object")
			parent = dojo.widget.byId(data.parent);
		else
			parent = addToTree;
		
		if (!parent) {
			// the nodes are being added in the wrong order
			// add this node to a list to finish later (after the parent is added, hopefully)
			if (attemptCount && attemptCount > 100)
				alert("Parent node of formField " + data.fieldId + " has not been loaded yet (parent: " + data.parent + ")");
			else {
				nodesToAdd.push({data: data, label: label, attemptCount: attemptCount ? attemptCount : 0});
				setTimeout("addLeftoverNodes()", 10);
			}
			return;
		}
		
		var div = document.createElement("div");
		div.id=data.fieldId;
		addToTree.domNode.appendChild(div);

		var ext = false;
		if (!label) {
			label = getFieldLabel(data);
			if (!formPublished)
				ext = getRemoveLink(data);
		}
		
		var props = [];
		props.title = label;
		props.id = data.fieldId ? data.fieldId : null;
		
		var node = dojo.widget.createWidget("TreeNode", props, div);
		node.data = data;
		
		if (ext) {
			dojo.dom.removeChildren(node.afterLabelNode);
			node.afterLabelNode.appendChild(ext);
		}
		
		if (formPublished)
			node.actionsDisabled = [node.actions.ADDCHILD, node.actions.REMOVE];
		
		var insertIndex = null;
		
		if (insertNode)
			insertIndex = 0;
		else if (insertAfterNode) {
			var children = parent.children;
			if (children != null) {
				for (var i=0; i < children.length; i++) {
					if (children[i] == insertAfterNode) {
						insertIndex = i + 1;
						break;
					}
				}
			}
		}
		
		if (insertIndex != null)
			parent.addChild(node, insertIndex);
		else
			parent.addChild(node);
		
		node.titleNode.innerHTML = props.title;
		
		return node;
	}
}


function getRemoveLink(data) {
	var ext = document.createElement("a");
	ext.onclick=function(){
			var node = dojo.widget.manager.getWidgetByNode(this.parentNode.parentNode);
			removeClicked(node);
		};
	ext.className="delete";
	ext.widgetId=data.fieldId;
	ext.innerHTML = " &nbsp; &nbsp; ";
	return ext;
}


function addLeftoverNodes() {
	while(nodesToAdd.length) {
		var msg = nodesToAdd[0];
		addNode(tree, msg.data, msg.label, msg.attemptCount+1);
		nodesToAdd = nodesToAdd.slice(1, nodesToAdd.length);
	}
}

var cancelClicked = function(savedNodeIds, target) {
	//tree.editDiv.style.display = "none";
	
	if (target == null)
		target = selectedNode;
		
	if (savedNodeIds && target.data) {
		if (savedNodeIds[0] == 0) {
			// remote call (dwr) returned an error
			alert("There was an error while processing your request. Consult the error logs");
		}
		else {
			// remote call went smoothly
			target.data["fieldId"] = savedNodeIds[0];
		}
	}
	
	clearFormField();
	tree.editDiv.style.display = "none";
	
	selectedNode = null;
}

// getting data from field object	
function getData(obj) {
	var data = new Object();
	
	// object is a conceptListItem
	if (obj.conceptId != null) {
		data.id = data["conceptId"] = obj.conceptId;
		data["fieldName"] = data["conceptName"] = obj.name;
		data["label"] = "CONCEPT." + obj.name;
		data.title = "Concept Id: " + obj.conceptId;
		data.isSet = obj.isSet;
		data.id = data["fieldId"] = obj.conceptId;
	}
	// or object is a fieldListItem
	else if (obj.fieldId != null) {
		data.id = data["fieldId"] = obj.fieldId;
		data["fieldName"] = obj.name;
	}
	
	return data;
}

function copyObject(src)
{
	var dest = new Object();
	var i;
	
	for (i in src)
	dest[i] = src[i];
	
	return dest;
}

/*
	Tests whether the given value is not null and 
	the integer has a value
*/
function valueExists(val, defaultValue)
{
	if (val != null && val.toString().length > 0)
		return val;
		
	return defaultValue;
}