/*
 * jsTreeModel 0.99
 * http://jsorm.com/
 *
 * Dual licensed under the MIT and GPL licenses (same as jQuery):
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 * 
 * Created for Tufin www.tufin.com
 * Contributed to public source through the good offices of Tufin
 *
 * $Date: 2011-01-24 $
 * $Revision:  $
 */

/*global window, jQuery*/

/* 
 * This plugin gets jstree to use a class model to retrieve data, creating great dynamism
 */
(function ($) {
	var nodeInterface = ["hasChildren","getAttr","getName","getProps","openNode","closeNode"];
	// ensure that something matches an interface
	var validateInterface = function(obj,inter) {
		var valid = true, i;
		obj = obj || {};
		inter = [].concat(inter);

		for (i=0;i<inter.length;i++) {
			if (!obj.hasOwnProperty(inter[i]) || typeof(obj[inter[i]]) !== "function") {
				valid = false;
			}
		}
		return(valid);
	};
	$.jstree.plugin("model_data", {
		__init : function() {
			var s = this._get_settings().model_data;
			var anim = this._get_settings().core.animation;
			var container = this.get_container();
			//s.data._elm = container;
			// when a node is closed, if progressive_clean is in place, we clean up the node
			if (s.progressive_unload) {
				container.bind("after_close.jstree", $.proxy(function (e, data) {
					// remove the children, if we have not just reloaded it
					var obj = data.rslt.obj;
					if (!obj.data("jstree-model-loaded")) {
						data.rslt.obj.children("ul").detach();
					}
				}, this));
			}
		},
		defaults : { 
			data : false,
			correct_state : true,
			progressive_render : false,
			progressive_unload : false
		},
		_fn : {
			// called to load a node, given the node object, success callback, and error callback
			load_node : function (obj, s_call, e_call) { 
				var _this = this; 
				// just run load_node_model, which is specialized for the model-based data set
				this.load_node_model(obj, function () { _this.__callback({ "obj" : obj }); s_call.call(this); }, e_call); 
			},
			// check if a particular node is loaded
			_is_loaded : function (obj) { 
				var s = this._get_settings().model_data, d, ret;
				obj = this._get_node(obj);
				//if (!obj || obj === -1 || obj.children("ul").children("li").length > 0) {
				if (!obj || obj === -1 || obj.data("jstree-model-loaded")) {
					ret = true;
				} else {
					ret = false;
				}
				return(ret);
			},
			// load a specific node and its children from the model, given the object, success callback, and failure callback
			load_node_model : function (obj, s_call, e_call) {
				var s = this.get_settings().model_data, d, c,
					error_func = function () {},
					success_func = function () {}, uNode, node, that = this;
				// get the jQuery LI node from the object, or -1 if the container
				obj = this._get_node(obj);
				// if this is a real element and not the root
				//    - if already loading, do nothing
				//    - if not, mark as loading
				if(obj && obj !== -1) {
					if(obj.data("jstree-is-loading")) { return; }
					else { obj.data("jstree-is-loading",true); }
				}
				// make sure we have data set that fits the function
				if (!s.data || typeof(s.data) !== "function") {
					throw "Data settings model function not supplied.";
				} else if (!validateInterface(s.data(),nodeInterface)) {
					throw "Data settings model does not have valid interface.";
				} else {
					// behave differently if we are at the root or not
					// root, get its children; not root, get itself
					node = !obj || obj === -1 ? s.data() : obj.data("jstree-model");
					uNode = !obj || obj === -1 ? this.get_container().empty() : obj;


					// listen for the changes about which we care
					node._elm = uNode;
					// bindings
					if (uNode === this.get_container() && !uNode.data("jstree-model-init")) {
						uNode.data("jstree-model-init",true);
						uNode.bind("close_node.jstree",function(e,data){
							data.rslt.obj.data("jstree-model").closeNode();
							data.rslt.obj.data("jstree-model-loaded",false);
						});
						(function(that) {
							node.bind("addChildren.jstree",function(e,target,children,index) {
								var tmp, ul;
								var parent = target._elm;
								// parse the children we got, add them to the existing node
								children = [].concat(children);
								if (children.length > 0) {
									tmp = that._parse_model(parent,children);
									if (tmp) {
										// is there already a ul?
										ul = parent.children("ul");
										if (!ul || ul.length < 1) {
											ul = $("<ul></ul>").appendTo(parent);
										}
										// where do we add them?
										if (isNaN(index)) {
											ul.append(tmp);
										} else {
											ul.children("li:eq("+index+")").after(tmp);
										}
									}
								}
							}).bind("removeChildren.jstree",function(e,target,children,index) {

							}).bind("nodeChange.jstree",function(e,target) {

							});
						}(this));
					}

					// now open the node - which is what happens when jstree calls load_node
					// but first clean the node to be safe - this should happen after_close above, but
					//   might get missed if it is open/close quickly
					if (s.progressive_unload) {
						uNode.children("ul").detach();
					}
					node.openNode(function(){
						if (obj && obj.data) {
							obj.data("jstree-is-loading",false);
							obj.data("jstree-model-loaded",true);
						}
						
						if (s_call && typeof(s_call) === "function") {
							s_call.call(that);
						}
					},true);
				}
			},
			_parse_model : function (parent, m) {
				var d = false, 
					p = this._get_settings(),
					s = p.model_data,
					t = p.core.html_titles,
					tmp, i, j, ul1, ul2, js, c, name, type, id, attr, that = this, props,
					cleanNode, rollback;

				if(!m) { return d; }
				// do we have a series of children?
				if($.isArray(m)) {
					d = $();
					if(!m.length) { return false; }
					for(i = 0, j = m.length; i < j; i++) {
						tmp = this._parse_model(parent, m[i]);
						if(tmp.length) { d = d.add(tmp); }
					}
				}
				else {
					// ensure it meets the interface requirements
					if (!validateInterface(m,nodeInterface)) {
						return d;
					}
					attr = m.getAttr();
					props = m.getProps() || {};
					js = {attr: attr, data: m.getName(), state: props.state};
					name = [].concat(m.getName());
					type = m.getType && typeof(m.getType) === "function" ? m.getType() : null;
					id = attr.id;


					// stub rollback and clean_node
					rollback = this.get_rollback;
					this.get_rollback = function(){};
					cleanNode = this.clean_node;
					this.clean_node = function(){};

					// create the node
					d = this.create_node(parent, "last", js,null,true);

					// return the rollback and clean_node
					this.__rollback = rollback;
					this.clean_node = cleanNode;


					// type support
					if (type) {
						d.attr(s.type_attr || "rel",type);
					}
					// id prefix support
					if (id) {
						d.attr("id",(s.id_prefix || "")+id);
					}

					// save the instance for this data on the node itself
					d.data("jstree-model",m);

					// listen for the changes about which we care
					m._elm = d;

					// if we have children, either get them if !progressive_render, or indicate that we are closed if progressive_render
					if(m.hasChildren()) { 
						if(s.progressive_render && js.state !== "open") {
							d.addClass("jstree-closed").removeClass("jstree-open jstree-leaf");
						} else {
							m.openNode(function(){
								d.data("jstree-model-loaded",true);								
							});
						}
					} else {
						d.addClass("jstree-leaf").removeClass("jstree-open jstree-closed");
					}
					// now do our own cleanNode business
					d.prev("li").removeClass("jstree-last");
					if (d.next("li").length === 0) {
						d.addClass("jstree-last");
					}

				}
				return d;
			}
		}
	});
}(jQuery));
