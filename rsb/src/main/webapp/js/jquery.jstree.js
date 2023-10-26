/*! jsTree - v3.3.15 - 2023-02-20 - (MIT) */

!(function (e) {
  "use strict";
  "function" == typeof define && define.amd
    ? define(["jquery"], e)
    : "undefined" != typeof module && module.exports
    ? (module.exports = e(require("jquery")))
    : e(jQuery);
})(function (E, P) {
  "use strict";
  if (!E.jstree) {
    var s = 0,
      a = !1,
      n = !1,
      d = !1,
      r = [],
      e = E("script:last").attr("src"),
      b = window.document,
      c = window.setImmediate,
      i = window.Promise;
    !c &&
      i &&
      (c = function (e, t) {
        i.resolve(t).then(e);
      }),
      (E.jstree = {
        version: "3.3.15",
        defaults: { plugins: [] },
        plugins: {},
        path: e && -1 !== e.indexOf("/") ? e.replace(/\/[^\/]+$/, "") : "",
        idregex: /[\\:&!^|()\[\]<>@*'+~#";.,=\- \/${}%?`]/g,
        root: "#",
      }),
      (E.jstree.create = function (e, i) {
        var r = new E.jstree.core(++s),
          t = i;
        return (
          (i = E.extend(!0, {}, E.jstree.defaults, i)),
          t && t.plugins && (i.plugins = t.plugins),
          E.each(i.plugins, function (e, t) {
            "core" !== e && (r = r.plugin(t, i[t]));
          }),
          E(e).data("jstree", r),
          r.init(e, i),
          r
        );
      }),
      (E.jstree.destroy = function () {
        E(".jstree:jstree").jstree("destroy"), E(b).off(".jstree");
      }),
      (E.jstree.core = function (e) {
        (this._id = e),
          (this._cnt = 0),
          (this._wrk = null),
          (this._data = {
            core: {
              themes: { name: !1, dots: !1, icons: !1, ellipsis: !1 },
              selected: [],
              last_error: {},
              working: !1,
              worker_queue: [],
              focused: null,
            },
          });
      }),
      (E.jstree.reference = function (t) {
        var i = null,
          e = null;
        if (
          (!t || !t.id || (t.tagName && t.nodeType) || (t = t.id),
          !e || !e.length)
        )
          try {
            e = E(t);
          } catch (e) {}
        if (!e || !e.length)
          try {
            e = E("#" + t.replace(E.jstree.idregex, "\\$&"));
          } catch (e) {}
        return (
          e &&
          e.length &&
          (e = e.closest(".jstree")).length &&
          (e = e.data("jstree"))
            ? (i = e)
            : E(".jstree").each(function () {
                var e = E(this).data("jstree");
                if (e && e._model.data[t]) return (i = e), !1;
              }),
          i
        );
      }),
      (E.fn.jstree = function (i) {
        var r = "string" == typeof i,
          s = Array.prototype.slice.call(arguments, 1),
          a = null;
        return (
          !(!0 === i && !this.length) &&
          (this.each(function () {
            var e = E.jstree.reference(this),
              t = r && e ? e[i] : null;
            if (
              ((a = r && t ? t.apply(e, s) : null),
              e ||
                r ||
                (i !== P && !E.isPlainObject(i)) ||
                E.jstree.create(this, i),
              null !== (a = (e && !r) || !0 === i ? e || !1 : a) && a !== P)
            )
              return !1;
          }),
          null !== a && a !== P ? a : this)
        );
      }),
      (E.expr.pseudos.jstree = E.expr.createPseudo(function (e) {
        return function (e) {
          return E(e).hasClass("jstree") && E(e).data("jstree") !== P;
        };
      })),
      (E.jstree.defaults.core = {
        data: !1,
        strings: !1,
        check_callback: !1,
        error: E.noop,
        animation: 200,
        multiple: !0,
        themes: {
          name: !1,
          url: !1,
          dir: !1,
          dots: !0,
          icons: !0,
          ellipsis: !1,
          stripes: !1,
          variant: !1,
          responsive: !1,
        },
        expand_selected_onload: !0,
        worker: !0,
        force_text: !1,
        dblclick_toggle: !0,
        loaded_state: !1,
        restore_focus: !0,
        compute_elements_positions: !1,
        keyboard: {
          "ctrl-space": function (e) {
            (e.type = "click"), E(e.currentTarget).trigger(e);
          },
          enter: function (e) {
            (e.type = "click"), E(e.currentTarget).trigger(e);
          },
          left: function (e) {
            var e;
            e.preventDefault(),
              this.is_open(e.currentTarget)
                ? this.close_node(e.currentTarget)
                : (e = this.get_parent(e.currentTarget)) &&
                  e.id !== E.jstree.root &&
                  this.get_node(e, !0)
                    .children(".jstree-anchor")
                    .trigger("focus");
          },
          up: function (e) {
            e.preventDefault();
            var e = this.get_prev_dom(e.currentTarget);
            e && e.length && e.children(".jstree-anchor").trigger("focus");
          },
          right: function (e) {
            var e;
            e.preventDefault(),
              this.is_closed(e.currentTarget)
                ? this.open_node(e.currentTarget, function (e) {
                    this.get_node(e, !0)
                      .children(".jstree-anchor")
                      .trigger("focus");
                  })
                : !this.is_open(e.currentTarget) ||
                  ((e = this.get_node(e.currentTarget, !0).children(
                    ".jstree-children"
                  )[0]) &&
                    E(this._firstChild(e))
                      .children(".jstree-anchor")
                      .trigger("focus"));
          },
          down: function (e) {
            e.preventDefault();
            var e = this.get_next_dom(e.currentTarget);
            e && e.length && e.children(".jstree-anchor").trigger("focus");
          },
          "*": function (e) {
            this.open_all();
          },
          home: function (e) {
            e.preventDefault();
            var e = this._firstChild(this.get_container_ul()[0]);
            e &&
              E(e)
                .children(".jstree-anchor")
                .filter(":visible")
                .trigger("focus");
          },
          end: function (e) {
            e.preventDefault(),
              this.element
                .find(".jstree-anchor")
                .filter(":visible")
                .last()
                .trigger("focus");
          },
          f2: function (e) {
            e.preventDefault(), this.edit(e.currentTarget);
          },
        },
      }),
      (E.jstree.core.prototype = {
        plugin: function (e, t) {
          var i = E.jstree.plugins[e];
          return i
            ? ((this._data[e] = {}), (i.prototype = this), new i(t, this))
            : this;
        },
        init: function (e, t) {
          (this._model = {
            data: {},
            changed: [],
            force_full_redraw: !1,
            redraw_timeout: !1,
            default_state: {
              loaded: !0,
              opened: !1,
              selected: !1,
              disabled: !1,
            },
          }),
            (this._model.data[E.jstree.root] = {
              id: E.jstree.root,
              parent: null,
              parents: [],
              children: [],
              children_d: [],
              state: { loaded: !1 },
            }),
            (this.element = E(e).addClass("jstree jstree-" + this._id)),
            (this.settings = t),
            (this._data.core.ready = !1),
            (this._data.core.loaded = !1),
            (this._data.core.rtl = "rtl" === this.element.css("direction")),
            this.element[this._data.core.rtl ? "addClass" : "removeClass"](
              "jstree-rtl"
            ),
            this.element.attr("role", "tree"),
            this.settings.core.multiple &&
              this.element.attr("aria-multiselectable", !0),
            this.element.attr("tabindex") || this.element.attr("tabindex", "0"),
            this.bind(),
            this.trigger("init"),
            (this._data.core.original_container_html = this.element
              .find(" > ul > li")
              .clone(!0)),
            this._data.core.original_container_html
              .find("li")
              .addBack()
              .contents()
              .filter(function () {
                return (
                  3 === this.nodeType &&
                  (!this.nodeValue || /^\s+$/.test(this.nodeValue))
                );
              })
              .remove(),
            this.element.html(
              "<ul class='jstree-container-ul jstree-children' role='group'><li id='j" +
                this._id +
                "_loading' class='jstree-initial-node jstree-loading jstree-leaf jstree-last' role='none'><i class='jstree-icon jstree-ocl'></i><a class='jstree-anchor' role='treeitem' href='#'><i class='jstree-icon jstree-themeicon-hidden'></i>" +
                this.get_string("Loading ...") +
                "</a></li></ul>"
            ),
            this.element.attr(
              "aria-activedescendant",
              "j" + this._id + "_loading"
            ),
            (this._data.core.li_height =
              this.get_container_ul().children("li").first().outerHeight() ||
              24),
            (this._data.core.node = this._create_prototype_node()),
            this.trigger("loading"),
            this.load_node(E.jstree.root);
        },
        destroy: function (e) {
          if ((this.trigger("destroy"), this._wrk))
            try {
              window.URL.revokeObjectURL(this._wrk), (this._wrk = null);
            } catch (e) {}
          e || this.element.empty(), this.teardown();
        },
        _create_prototype_node: function () {
          var e = b.createElement("LI"),
            t,
            i;
          return (
            e.setAttribute("role", "none"),
            ((t = b.createElement("I")).className = "jstree-icon jstree-ocl"),
            t.setAttribute("role", "presentation"),
            e.appendChild(t),
            ((t = b.createElement("A")).className = "jstree-anchor"),
            t.setAttribute("href", "#"),
            t.setAttribute("tabindex", "-1"),
            t.setAttribute("role", "treeitem"),
            ((i = b.createElement("I")).className =
              "jstree-icon jstree-themeicon"),
            i.setAttribute("role", "presentation"),
            t.appendChild(i),
            e.appendChild(t),
            (t = i = null),
            e
          );
        },
        _kbevent_to_func: function (e) {
          var t = {
              8: "Backspace",
              9: "Tab",
              13: "Enter",
              19: "Pause",
              27: "Esc",
              32: "Space",
              33: "PageUp",
              34: "PageDown",
              35: "End",
              36: "Home",
              37: "Left",
              38: "Up",
              39: "Right",
              40: "Down",
              44: "Print",
              45: "Insert",
              46: "Delete",
              96: "Numpad0",
              97: "Numpad1",
              98: "Numpad2",
              99: "Numpad3",
              100: "Numpad4",
              101: "Numpad5",
              102: "Numpad6",
              103: "Numpad7",
              104: "Numpad8",
              105: "Numpad9",
              "-13": "NumpadEnter",
              112: "F1",
              113: "F2",
              114: "F3",
              115: "F4",
              116: "F5",
              117: "F6",
              118: "F7",
              119: "F8",
              120: "F9",
              121: "F10",
              122: "F11",
              123: "F12",
              144: "Numlock",
              145: "Scrolllock",
              16: "Shift",
              17: "Ctrl",
              18: "Alt",
              48: "0",
              49: "1",
              50: "2",
              51: "3",
              52: "4",
              53: "5",
              54: "6",
              55: "7",
              56: "8",
              57: "9",
              59: ";",
              61: "=",
              65: "a",
              66: "b",
              67: "c",
              68: "d",
              69: "e",
              70: "f",
              71: "g",
              72: "h",
              73: "i",
              74: "j",
              75: "k",
              76: "l",
              77: "m",
              78: "n",
              79: "o",
              80: "p",
              81: "q",
              82: "r",
              83: "s",
              84: "t",
              85: "u",
              86: "v",
              87: "w",
              88: "x",
              89: "y",
              90: "z",
              107: "+",
              109: "-",
              110: ".",
              186: ";",
              187: "=",
              188: ",",
              189: "-",
              190: ".",
              191: "/",
              192: "`",
              219: "[",
              220: "\\",
              221: "]",
              222: "'",
              111: "/",
              106: "*",
              173: "-",
            },
            i = [];
          if (
            (e.ctrlKey && i.push("ctrl"),
            e.altKey && i.push("alt"),
            e.shiftKey && i.push("shift"),
            i.push(t[e.which] ? t[e.which].toLowerCase() : e.which),
            "shift-shift" === (i = i.sort().join("-").toLowerCase()) ||
              "ctrl-ctrl" === i ||
              "alt-alt" === i)
          )
            return null;
          var r = this.settings.core.keyboard,
            s,
            a;
          for (s in r)
            if (
              r.hasOwnProperty(s) &&
              (a =
                "-" !== (a = s) && "+" !== a
                  ? (a = a
                      .replace("--", "-MINUS")
                      .replace("+-", "-MINUS")
                      .replace("++", "-PLUS")
                      .replace("-+", "-PLUS"))
                      .split(/-|\+/)
                      .sort()
                      .join("-")
                      .replace("MINUS", "-")
                      .replace("PLUS", "+")
                      .toLowerCase()
                  : a) === i
            )
              return r[s];
          return null;
        },
        teardown: function () {
          this.unbind(),
            this.element
              .removeClass("jstree")
              .removeData("jstree")
              .find("[class^='jstree']")
              .addBack()
              .attr("class", function () {
                return this.className.replace(/jstree[^ ]*|$/gi, "");
              }),
            (this.element = null);
        },
        bind: function () {
          var s = "",
            a = null,
            t = 0;
          this.element
            .on("dblclick.jstree", function (e) {
              if (
                e.target.tagName &&
                "input" === e.target.tagName.toLowerCase()
              )
                return !0;
              if (b.selection && b.selection.empty) b.selection.empty();
              else if (window.getSelection) {
                var e = window.getSelection();
                try {
                  e.removeAllRanges(), e.collapse();
                } catch (e) {}
              }
            })
            .on(
              "mousedown.jstree",
              function (e) {
                e.target === this.element[0] &&
                  (e.preventDefault(), (t = +new Date()));
              }.bind(this)
            )
            .on("mousedown.jstree", ".jstree-ocl", function (e) {
              e.preventDefault();
            })
            .on(
              "click.jstree",
              ".jstree-ocl",
              function (e) {
                this.toggle_node(e.target);
              }.bind(this)
            )
            .on(
              "dblclick.jstree",
              ".jstree-anchor",
              function (e) {
                if (
                  e.target.tagName &&
                  "input" === e.target.tagName.toLowerCase()
                )
                  return !0;
                this.settings.core.dblclick_toggle &&
                  this.toggle_node(e.target);
              }.bind(this)
            )
            .on(
              "click.jstree",
              ".jstree-anchor",
              function (e) {
                e.preventDefault(),
                  e.currentTarget !== b.activeElement &&
                    E(e.currentTarget).trigger("focus"),
                  this.activate_node(e.currentTarget, e);
              }.bind(this)
            )
            .on(
              "keydown.jstree",
              ".jstree-anchor",
              function (e) {
                if (
                  e.target.tagName &&
                  "input" === e.target.tagName.toLowerCase()
                )
                  return !0;
                this._data.core.rtl &&
                  (37 === e.which
                    ? (e.which = 39)
                    : 39 === e.which && (e.which = 37));
                var t = this._kbevent_to_func(e);
                if (t) {
                  var e = t.call(this, e);
                  if (!1 === e || !0 === e) return e;
                }
              }.bind(this)
            )
            .on(
              "load_node.jstree",
              function (e, t) {
                t.status &&
                  (t.node.id !== E.jstree.root ||
                    this._data.core.loaded ||
                    ((this._data.core.loaded = !0),
                    this._firstChild(this.get_container_ul()[0]) &&
                      this.element.attr(
                        "aria-activedescendant",
                        this._firstChild(this.get_container_ul()[0]).id
                      ),
                    this.trigger("loaded")),
                  this._data.core.ready ||
                    setTimeout(
                      function () {
                        if (
                          this.element &&
                          !this.get_container_ul().find(".jstree-loading")
                            .length
                        ) {
                          if (
                            ((this._data.core.ready = !0),
                            this._data.core.selected.length)
                          ) {
                            if (this.settings.core.expand_selected_onload) {
                              for (
                                var e = [],
                                  t,
                                  i,
                                  t = 0,
                                  i = this._data.core.selected.length;
                                t < i;
                                t++
                              )
                                e = e.concat(
                                  this._model.data[this._data.core.selected[t]]
                                    .parents
                                );
                              for (
                                t = 0,
                                  i = (e = E.vakata.array_unique(e)).length;
                                t < i;
                                t++
                              )
                                this.open_node(e[t], !1, 0);
                            }
                            this.trigger("changed", {
                              action: "ready",
                              selected: this._data.core.selected,
                            });
                          }
                          this.trigger("ready");
                        }
                      }.bind(this),
                      0
                    ));
              }.bind(this)
            )
            .on(
              "keypress.jstree",
              function (e) {
                if (
                  e.target.tagName &&
                  "input" === e.target.tagName.toLowerCase()
                )
                  return !0;
                a && clearTimeout(a),
                  (a = setTimeout(function () {
                    s = "";
                  }, 500));
                var i = String.fromCharCode(e.which).toLowerCase(),
                  t = this.element.find(".jstree-anchor").filter(":visible"),
                  e = t.index(b.activeElement) || 0,
                  r = !1;
                if (1 < (s += i).length) {
                  if (
                    (t.slice(e).each(
                      function (e, t) {
                        if (0 === E(t).text().toLowerCase().indexOf(s))
                          return E(t).trigger("focus"), !(r = !0);
                      }.bind(this)
                    ),
                    r)
                  )
                    return;
                  if (
                    (t.slice(0, e).each(
                      function (e, t) {
                        if (0 === E(t).text().toLowerCase().indexOf(s))
                          return E(t).trigger("focus"), !(r = !0);
                      }.bind(this)
                    ),
                    r)
                  )
                    return;
                }
                new RegExp(
                  "^" + i.replace(/[-\/\\^$*+?.()|[\]{}]/g, "\\$&") + "+$"
                ).test(s) &&
                  (t.slice(e + 1).each(
                    function (e, t) {
                      if (E(t).text().toLowerCase().charAt(0) === i)
                        return E(t).trigger("focus"), !(r = !0);
                    }.bind(this)
                  ),
                  r ||
                    t.slice(0, e + 1).each(
                      function (e, t) {
                        if (E(t).text().toLowerCase().charAt(0) === i)
                          return E(t).trigger("focus"), !(r = !0);
                      }.bind(this)
                    ));
              }.bind(this)
            )
            .on(
              "init.jstree",
              function () {
                var e = this.settings.core.themes;
                (this._data.core.themes.dots = e.dots),
                  (this._data.core.themes.stripes = e.stripes),
                  (this._data.core.themes.icons = e.icons),
                  (this._data.core.themes.ellipsis = e.ellipsis),
                  this.set_theme(e.name || "default", e.url),
                  this.set_theme_variant(e.variant);
              }.bind(this)
            )
            .on(
              "loading.jstree",
              function () {
                this[this._data.core.themes.dots ? "show_dots" : "hide_dots"](),
                  this[
                    this._data.core.themes.icons ? "show_icons" : "hide_icons"
                  ](),
                  this[
                    this._data.core.themes.stripes
                      ? "show_stripes"
                      : "hide_stripes"
                  ](),
                  this[
                    this._data.core.themes.ellipsis
                      ? "show_ellipsis"
                      : "hide_ellipsis"
                  ]();
              }.bind(this)
            )
            .on(
              "blur.jstree",
              ".jstree-anchor",
              function (e) {
                (this._data.core.focused = null),
                  E(e.currentTarget)
                    .filter(".jstree-hovered")
                    .trigger("mouseleave"),
                  this.element.attr("tabindex", "0"),
                  E(e.currentTarget).attr("tabindex", "-1");
              }.bind(this)
            )
            .on(
              "focus.jstree",
              ".jstree-anchor",
              function (e) {
                var t = this.get_node(e.currentTarget);
                t && (t.id || 0 === t.id) && (this._data.core.focused = t.id),
                  this.element
                    .find(".jstree-hovered")
                    .not(e.currentTarget)
                    .trigger("mouseleave"),
                  E(e.currentTarget).trigger("mouseenter"),
                  this.element.attr("tabindex", "-1"),
                  E(e.currentTarget).attr("tabindex", "0");
              }.bind(this)
            )
            .on(
              "focus.jstree",
              function () {
                var e;
                500 < +new Date() - t &&
                  !this._data.core.focused &&
                  this.settings.core.restore_focus &&
                  ((t = 0),
                  (e = this.get_node(
                    this.element.attr("aria-activedescendant"),
                    !0
                  )) && e.find("> .jstree-anchor").trigger("focus"));
              }.bind(this)
            )
            .on(
              "mouseenter.jstree",
              ".jstree-anchor",
              function (e) {
                this.hover_node(e.currentTarget);
              }.bind(this)
            )
            .on(
              "mouseleave.jstree",
              ".jstree-anchor",
              function (e) {
                this.dehover_node(e.currentTarget);
              }.bind(this)
            );
        },
        unbind: function () {
          this.element.off(".jstree"), E(b).off(".jstree-" + this._id);
        },
        trigger: function (e, t) {
          ((t = t || {}).instance = this).element.triggerHandler(
            e.replace(".jstree", "") + ".jstree",
            t
          );
        },
        get_container: function () {
          return this.element;
        },
        get_container_ul: function () {
          return this.element.children(".jstree-children").first();
        },
        get_string: function (e) {
          var t = this.settings.core.strings;
          return E.vakata.is_function(t)
            ? t.call(this, e)
            : t && t[e]
            ? t[e]
            : e;
        },
        _firstChild: function (e) {
          e = e ? e.firstChild : null;
          while (null !== e && 1 !== e.nodeType) e = e.nextSibling;
          return e;
        },
        _nextSibling: function (e) {
          e = e ? e.nextSibling : null;
          while (null !== e && 1 !== e.nodeType) e = e.nextSibling;
          return e;
        },
        _previousSibling: function (e) {
          e = e ? e.previousSibling : null;
          while (null !== e && 1 !== e.nodeType) e = e.previousSibling;
          return e;
        },
        get_node: function (e, t) {
          var i;
          (e = e && (e.id || 0 === e.id) ? e.id : e) instanceof E &&
            e.length &&
            e[0].id &&
            (e = e[0].id);
          try {
            if (this._model.data[e]) e = this._model.data[e];
            else if (
              "string" == typeof e &&
              this._model.data[e.replace(/^#/, "")]
            )
              e = this._model.data[e.replace(/^#/, "")];
            else if (
              "string" == typeof e &&
              (i = E("#" + e.replace(E.jstree.idregex, "\\$&"), this.element))
                .length &&
              this._model.data[i.closest(".jstree-node").attr("id")]
            )
              e = this._model.data[i.closest(".jstree-node").attr("id")];
            else if (
              (i = this.element.find(e)).length &&
              this._model.data[i.closest(".jstree-node").attr("id")]
            )
              e = this._model.data[i.closest(".jstree-node").attr("id")];
            else {
              if (!(i = this.element.find(e)).length || !i.hasClass("jstree"))
                return !1;
              e = this._model.data[E.jstree.root];
            }
            return (e = t
              ? e.id === E.jstree.root
                ? this.element
                : E("#" + e.id.replace(E.jstree.idregex, "\\$&"), this.element)
              : e);
          } catch (e) {
            return !1;
          }
        },
        get_path: function (e, t, i) {
          if (
            !(e = e.parents ? e : this.get_node(e)) ||
            e.id === E.jstree.root ||
            !e.parents
          )
            return !1;
          var r,
            s,
            a = [];
          for (
            a.push(i ? e.id : e.text), r = 0, s = e.parents.length;
            r < s;
            r++
          )
            a.push(i ? e.parents[r] : this.get_text(e.parents[r]));
          return (a = a.reverse().slice(1)), t ? a.join(t) : a;
        },
        get_next_dom: function (e, t) {
          var i;
          if ((e = this.get_node(e, !0))[0] === this.element[0]) {
            i = this._firstChild(this.get_container_ul()[0]);
            while (i && 0 === i.offsetHeight) i = this._nextSibling(i);
            return !!i && E(i);
          }
          if (!e || !e.length) return !1;
          if (t) {
            i = e[0];
            do {
              i = this._nextSibling(i);
            } while (i && 0 === i.offsetHeight);
            return !!i && E(i);
          }
          if (e.hasClass("jstree-open")) {
            i = this._firstChild(e.children(".jstree-children")[0]);
            while (i && 0 === i.offsetHeight) i = this._nextSibling(i);
            if (null !== i) return E(i);
          }
          i = e[0];
          do {
            i = this._nextSibling(i);
          } while (i && 0 === i.offsetHeight);
          return null !== i
            ? E(i)
            : e
                .parentsUntil(".jstree", ".jstree-node")
                .nextAll(".jstree-node:visible")
                .first();
        },
        get_prev_dom: function (e, t) {
          var i;
          if ((e = this.get_node(e, !0))[0] === this.element[0]) {
            i = this.get_container_ul()[0].lastChild;
            while (i && 0 === i.offsetHeight) i = this._previousSibling(i);
            return !!i && E(i);
          }
          if (!e || !e.length) return !1;
          if (t) {
            i = e[0];
            do {
              i = this._previousSibling(i);
            } while (i && 0 === i.offsetHeight);
            return !!i && E(i);
          }
          i = e[0];
          do {
            i = this._previousSibling(i);
          } while (i && 0 === i.offsetHeight);
          if (null === i)
            return (
              !(
                !(i = e[0].parentNode.parentNode) ||
                !i.className ||
                -1 === i.className.indexOf("jstree-node")
              ) && E(i)
            );
          e = E(i);
          while (e.hasClass("jstree-open"))
            e = e
              .children(".jstree-children")
              .first()
              .children(".jstree-node:visible:last");
          return e;
        },
        get_parent: function (e) {
          return (
            !(!(e = this.get_node(e)) || e.id === E.jstree.root) && e.parent
          );
        },
        get_children_dom: function (e) {
          return (e = this.get_node(e, !0))[0] === this.element[0]
            ? this.get_container_ul().children(".jstree-node")
            : !(!e || !e.length) &&
                e.children(".jstree-children").children(".jstree-node");
        },
        is_parent: function (e) {
          return (
            (e = this.get_node(e)) &&
            (!1 === e.state.loaded || 0 < e.children.length)
          );
        },
        is_loaded: function (e) {
          return (e = this.get_node(e)) && e.state.loaded;
        },
        is_loading: function (e) {
          return (e = this.get_node(e)) && e.state && e.state.loading;
        },
        is_open: function (e) {
          return (e = this.get_node(e)) && e.state.opened;
        },
        is_closed: function (e) {
          return (e = this.get_node(e)) && this.is_parent(e) && !e.state.opened;
        },
        is_leaf: function (e) {
          return !this.is_parent(e);
        },
        load_node: function (n, d) {
          var e = this.get_node(n, !0),
            t,
            i,
            r,
            s,
            a;
          if (E.vakata.is_array(n)) return this._load_nodes(n.slice(), d), !0;
          if (!(n = this.get_node(n))) return d && d.call(this, n, !1), !1;
          if (n.state.loaded) {
            for (n.state.loaded = !1, r = 0, s = n.parents.length; r < s; r++)
              this._model.data[n.parents[r]].children_d = E.vakata.array_filter(
                this._model.data[n.parents[r]].children_d,
                function (e) {
                  return -1 === E.inArray(e, n.children_d);
                }
              );
            for (t = 0, i = n.children_d.length; t < i; t++)
              this._model.data[n.children_d[t]].state.selected && (a = !0),
                delete this._model.data[n.children_d[t]];
            a &&
              (this._data.core.selected = E.vakata.array_filter(
                this._data.core.selected,
                function (e) {
                  return -1 === E.inArray(e, n.children_d);
                }
              )),
              (n.children = []),
              (n.children_d = []),
              a &&
                this.trigger("changed", {
                  action: "load_node",
                  node: n,
                  selected: this._data.core.selected,
                });
          }
          return (
            (n.state.failed = !1),
            (n.state.loading = !0),
            (n.id !== E.jstree.root ? e.children(".jstree-anchor") : e).attr(
              "aria-busy",
              !0
            ),
            e.addClass("jstree-loading"),
            this._load_node(
              n,
              function (e) {
                ((n = this._model.data[n.id]).state.loading = !1),
                  (n.state.loaded = e),
                  (n.state.failed = !n.state.loaded);
                for (
                  var t = this.get_node(n, !0),
                    i = 0,
                    r = 0,
                    s = this._model.data,
                    a = !1,
                    i = 0,
                    r = n.children.length;
                  i < r;
                  i++
                )
                  if (s[n.children[i]] && !s[n.children[i]].state.hidden) {
                    a = !0;
                    break;
                  }
                n.state.loaded &&
                  t &&
                  t.length &&
                  (t.removeClass("jstree-closed jstree-open jstree-leaf"),
                  a
                    ? "#" !== n.id &&
                      t.addClass(
                        n.state.opened ? "jstree-open" : "jstree-closed"
                      )
                    : t.addClass("jstree-leaf")),
                  (n.id !== E.jstree.root
                    ? t.children(".jstree-anchor")
                    : t
                  ).attr("aria-busy", !1),
                  t.removeClass("jstree-loading"),
                  this.trigger("load_node", { node: n, status: e }),
                  d && d.call(this, n, e);
              }.bind(this)
            ),
            !0
          );
        },
        _load_nodes: function (e, t, i, r) {
          for (
            var s = !0,
              a = function () {
                this._load_nodes(e, t, !0);
              },
              n = this._model.data,
              d,
              o,
              c = [],
              d = 0,
              o = e.length;
            d < o;
            d++
          )
            n[e[d]] &&
              ((!n[e[d]].state.loaded && !n[e[d]].state.failed) || (!i && r)) &&
              (this.is_loading(e[d]) || this.load_node(e[d], a), (s = !1));
          if (s) {
            for (d = 0, o = e.length; d < o; d++)
              n[e[d]] && n[e[d]].state.loaded && c.push(e[d]);
            t && !t.done && (t.call(this, c), (t.done = !0));
          }
        },
        load_all: function (e, t) {
          if (((e = e || E.jstree.root), !(e = this.get_node(e)))) return !1;
          var i = [],
            r = this._model.data,
            s = r[e.id].children_d,
            a,
            n;
          for (
            e.state && !e.state.loaded && i.push(e.id), a = 0, n = s.length;
            a < n;
            a++
          )
            r[s[a]] && r[s[a]].state && !r[s[a]].state.loaded && i.push(s[a]);
          i.length
            ? this._load_nodes(i, function () {
                this.load_all(e, t);
              })
            : (t && t.call(this, e), this.trigger("load_all", { node: e }));
        },
        _load_node: function (s, a) {
          var e = this.settings.core.data,
            t,
            n = function e() {
              return 3 !== this.nodeType && 8 !== this.nodeType;
            };
          return e
            ? E.vakata.is_function(e)
              ? e.call(
                  this,
                  s,
                  function (e) {
                    !1 === e
                      ? a.call(this, !1)
                      : this[
                          "string" == typeof e
                            ? "_append_html_data"
                            : "_append_json_data"
                        ](
                          s,
                          "string" == typeof e
                            ? E(E.parseHTML(e)).filter(n)
                            : e,
                          function (e) {
                            a.call(this, e);
                          }
                        );
                  }.bind(this)
                )
              : "object" == typeof e
              ? e.url
                ? ((e = E.extend(!0, {}, e)),
                  E.vakata.is_function(e.url) && (e.url = e.url.call(this, s)),
                  E.vakata.is_function(e.data) &&
                    (e.data = e.data.call(this, s)),
                  E.ajax(e)
                    .done(
                      function (e, t, i) {
                        var r = i.getResponseHeader("Content-Type");
                        return (r && -1 !== r.indexOf("json")) ||
                          "object" == typeof e
                          ? this._append_json_data(s, e, function (e) {
                              a.call(this, e);
                            })
                          : (r && -1 !== r.indexOf("html")) ||
                            "string" == typeof e
                          ? this._append_html_data(
                              s,
                              E(E.parseHTML(e)).filter(n),
                              function (e) {
                                a.call(this, e);
                              }
                            )
                          : ((this._data.core.last_error = {
                              error: "ajax",
                              plugin: "core",
                              id: "core_04",
                              reason: "Could not load node",
                              data: JSON.stringify({ id: s.id, xhr: i }),
                            }),
                            this.settings.core.error.call(
                              this,
                              this._data.core.last_error
                            ),
                            a.call(this, !1));
                      }.bind(this)
                    )
                    .fail(
                      function (e) {
                        (this._data.core.last_error = {
                          error: "ajax",
                          plugin: "core",
                          id: "core_04",
                          reason: "Could not load node",
                          data: JSON.stringify({ id: s.id, xhr: e }),
                        }),
                          a.call(this, !1),
                          this.settings.core.error.call(
                            this,
                            this._data.core.last_error
                          );
                      }.bind(this)
                    ))
                : ((t = E.vakata.is_array(e)
                    ? E.extend(!0, [], e)
                    : E.isPlainObject(e)
                    ? E.extend(!0, {}, e)
                    : e),
                  s.id === E.jstree.root
                    ? this._append_json_data(s, t, function (e) {
                        a.call(this, e);
                      })
                    : ((this._data.core.last_error = {
                        error: "nodata",
                        plugin: "core",
                        id: "core_05",
                        reason: "Could not load node",
                        data: JSON.stringify({ id: s.id }),
                      }),
                      this.settings.core.error.call(
                        this,
                        this._data.core.last_error
                      ),
                      a.call(this, !1)))
              : "string" == typeof e
              ? s.id === E.jstree.root
                ? this._append_html_data(
                    s,
                    E(E.parseHTML(e)).filter(n),
                    function (e) {
                      a.call(this, e);
                    }
                  )
                : ((this._data.core.last_error = {
                    error: "nodata",
                    plugin: "core",
                    id: "core_06",
                    reason: "Could not load node",
                    data: JSON.stringify({ id: s.id }),
                  }),
                  this.settings.core.error.call(
                    this,
                    this._data.core.last_error
                  ),
                  a.call(this, !1))
              : a.call(this, !1)
            : s.id === E.jstree.root
            ? this._append_html_data(
                s,
                this._data.core.original_container_html.clone(!0),
                function (e) {
                  a.call(this, e);
                }
              )
            : a.call(this, !1);
        },
        _node_changed: function (e) {
          (e = this.get_node(e)) &&
            -1 === E.inArray(e.id, this._model.changed) &&
            this._model.changed.push(e.id);
        },
        _append_html_data: function (e, t, i) {
          ((e = this.get_node(e)).children = []), (e.children_d = []);
          var t = t.is("ul") ? t.children() : t,
            r = e.id,
            s = [],
            a = [],
            n = this._model.data,
            d = n[r],
            e = this._data.core.selected.length,
            o,
            c,
            l;
          for (
            t.each(
              function (e, t) {
                (o = this._parse_model_from_html(
                  E(t),
                  r,
                  d.parents.concat()
                )) &&
                  (s.push(o),
                  a.push(o),
                  n[o].children_d.length && (a = a.concat(n[o].children_d)));
              }.bind(this)
            ),
              d.children = s,
              d.children_d = a,
              c = 0,
              l = d.parents.length;
            c < l;
            c++
          )
            n[d.parents[c]].children_d = n[d.parents[c]].children_d.concat(a);
          this.trigger("model", { nodes: a, parent: r }),
            r !== E.jstree.root
              ? (this._node_changed(r), this.redraw())
              : (this.get_container_ul()
                  .children(".jstree-initial-node")
                  .remove(),
                this.redraw(!0)),
            this._data.core.selected.length !== e &&
              this.trigger("changed", {
                action: "model",
                selected: this._data.core.selected,
              }),
            i.call(this, !0);
        },
        _append_json_data: function (e, t, o, i) {
          if (null !== this.element) {
            ((e = this.get_node(e)).children = []),
              (e.children_d = []),
              t.d && "string" == typeof (t = t.d) && (t = JSON.parse(t)),
              E.vakata.is_array(t) || (t = [t]);
            var r = null,
              s = {
                df: this._model.default_state,
                dat: t,
                par: e.id,
                m: this._model.data,
                t_id: this._id,
                t_cnt: this._cnt,
                sel: this._data.core.selected,
              },
              v = this,
              a = function (e, c) {
                var t = (e = e.data ? e.data : e).dat,
                  i = e.par,
                  r = [],
                  s = [],
                  l = [],
                  h = e.df,
                  _ = e.t_id,
                  g = e.t_cnt,
                  u = e.m,
                  a = u[i],
                  e = e.sel,
                  n,
                  d,
                  o,
                  f,
                  p = function (e, t, i) {
                    (i = i ? i.concat() : []), t && i.unshift(t);
                    var r = e.id.toString(),
                      s,
                      a,
                      n,
                      d,
                      o = {
                        id: r,
                        text: e.text || "",
                        icon: e.icon === c || e.icon,
                        parent: t,
                        parents: i,
                        children: e.children || [],
                        children_d: e.children_d || [],
                        data: e.data,
                        state: {},
                        li_attr: { id: !1 },
                        a_attr: { href: "#" },
                        original: !1,
                      };
                    for (s in h) h.hasOwnProperty(s) && (o.state[s] = h[s]);
                    if (
                      (e &&
                        e.data &&
                        e.data.jstree &&
                        e.data.jstree.icon &&
                        (o.icon = e.data.jstree.icon),
                      (o.icon !== c && null !== o.icon && "" !== o.icon) ||
                        (o.icon = !0),
                      e && e.data && ((o.data = e.data), e.data.jstree))
                    )
                      for (s in e.data.jstree)
                        e.data.jstree.hasOwnProperty(s) &&
                          (o.state[s] = e.data.jstree[s]);
                    if (e && "object" == typeof e.state)
                      for (s in e.state)
                        e.state.hasOwnProperty(s) && (o.state[s] = e.state[s]);
                    if (e && "object" == typeof e.li_attr)
                      for (s in e.li_attr)
                        e.li_attr.hasOwnProperty(s) &&
                          (o.li_attr[s] = e.li_attr[s]);
                    if (
                      (o.li_attr.id || (o.li_attr.id = r),
                      e && "object" == typeof e.a_attr)
                    )
                      for (s in e.a_attr)
                        e.a_attr.hasOwnProperty(s) &&
                          (o.a_attr[s] = e.a_attr[s]);
                    for (
                      e &&
                        e.children &&
                        !0 === e.children &&
                        ((o.state.loaded = !1),
                        (o.children = []),
                        (o.children_d = [])),
                        s = 0,
                        a = (u[o.id] = o).children.length;
                      s < a;
                      s++
                    )
                      (n = p(u[o.children[s]], o.id, i)),
                        (d = u[n]),
                        o.children_d.push(n),
                        d.children_d.length &&
                          (o.children_d = o.children_d.concat(d.children_d));
                    return (
                      delete e.data,
                      delete e.children,
                      (u[o.id].original = e),
                      o.state.selected && l.push(o.id),
                      o.id
                    );
                  },
                  m = function (e, t, i) {
                    (i = i ? i.concat() : []), t && i.unshift(t);
                    var r = !1,
                      s,
                      a,
                      n,
                      d,
                      o;
                    do {
                      r = "j" + _ + "_" + ++g;
                    } while (u[r]);
                    for (s in ((o = {
                      id: !1,
                      text: "string" == typeof e ? e : "",
                      icon: "object" != typeof e || e.icon === c || e.icon,
                      parent: t,
                      parents: i,
                      children: [],
                      children_d: [],
                      data: null,
                      state: {},
                      li_attr: { id: !1 },
                      a_attr: { href: "#" },
                      original: !1,
                    }),
                    h))
                      h.hasOwnProperty(s) && (o.state[s] = h[s]);
                    if (
                      (e && (e.id || 0 === e.id) && (o.id = e.id.toString()),
                      e && e.text && (o.text = e.text),
                      e &&
                        e.data &&
                        e.data.jstree &&
                        e.data.jstree.icon &&
                        (o.icon = e.data.jstree.icon),
                      (o.icon !== c && null !== o.icon && "" !== o.icon) ||
                        (o.icon = !0),
                      e && e.data && ((o.data = e.data), e.data.jstree))
                    )
                      for (s in e.data.jstree)
                        e.data.jstree.hasOwnProperty(s) &&
                          (o.state[s] = e.data.jstree[s]);
                    if (e && "object" == typeof e.state)
                      for (s in e.state)
                        e.state.hasOwnProperty(s) && (o.state[s] = e.state[s]);
                    if (e && "object" == typeof e.li_attr)
                      for (s in e.li_attr)
                        e.li_attr.hasOwnProperty(s) &&
                          (o.li_attr[s] = e.li_attr[s]);
                    if (
                      (o.li_attr.id &&
                        !o.id &&
                        0 !== o.id &&
                        (o.id = o.li_attr.id.toString()),
                      o.id || 0 === o.id || (o.id = r),
                      o.li_attr.id || (o.li_attr.id = o.id),
                      e && "object" == typeof e.a_attr)
                    )
                      for (s in e.a_attr)
                        e.a_attr.hasOwnProperty(s) &&
                          (o.a_attr[s] = e.a_attr[s]);
                    if (e && e.children && e.children.length) {
                      for (s = 0, a = e.children.length; s < a; s++)
                        (n = m(e.children[s], o.id, i)),
                          (d = u[n]),
                          o.children.push(n),
                          d.children_d.length &&
                            (o.children_d = o.children_d.concat(d.children_d));
                      o.children_d = o.children_d.concat(o.children);
                    }
                    return (
                      e &&
                        e.children &&
                        !0 === e.children &&
                        ((o.state.loaded = !1),
                        (o.children = []),
                        (o.children_d = [])),
                      delete e.data,
                      delete e.children,
                      (o.original = e),
                      (u[o.id] = o).state.selected && l.push(o.id),
                      o.id
                    );
                  };
                if (t.length && t[0].id !== c && t[0].parent !== c) {
                  for (d = 0, o = t.length; d < o; d++)
                    t[d].children || (t[d].children = []),
                      t[d].state || (t[d].state = {}),
                      (u[t[d].id.toString()] = t[d]);
                  for (d = 0, o = t.length; d < o; d++)
                    u[t[d].parent.toString()]
                      ? (u[t[d].parent.toString()].children.push(
                          t[d].id.toString()
                        ),
                        a.children_d.push(t[d].id.toString()))
                      : void 0 !== v &&
                        ((v._data.core.last_error = {
                          error: "parse",
                          plugin: "core",
                          id: "core_07",
                          reason: "Node with invalid parent",
                          data: JSON.stringify({
                            id: t[d].id.toString(),
                            parent: t[d].parent.toString(),
                          }),
                        }),
                        v.settings.core.error.call(v, v._data.core.last_error));
                  for (d = 0, o = a.children.length; d < o; d++)
                    (n = p(u[a.children[d]], i, a.parents.concat())),
                      s.push(n),
                      u[n].children_d.length && (s = s.concat(u[n].children_d));
                  for (d = 0, o = a.parents.length; d < o; d++)
                    u[a.parents[d]].children_d =
                      u[a.parents[d]].children_d.concat(s);
                  f = { cnt: g, mod: u, sel: e, par: i, dpc: s, add: l };
                } else {
                  for (d = 0, o = t.length; d < o; d++)
                    (n = m(t[d], i, a.parents.concat())) &&
                      (r.push(n),
                      s.push(n),
                      u[n].children_d.length &&
                        (s = s.concat(u[n].children_d)));
                  for (
                    a.children = r,
                      a.children_d = s,
                      d = 0,
                      o = a.parents.length;
                    d < o;
                    d++
                  )
                    u[a.parents[d]].children_d =
                      u[a.parents[d]].children_d.concat(s);
                  f = { cnt: g, mod: u, sel: e, par: i, dpc: s, add: l };
                }
                if ("undefined" != typeof window && void 0 !== window.document)
                  return f;
                postMessage(f);
              },
              n = function (e, t) {
                if (null !== this.element) {
                  var i, r;
                  for (i in ((this._cnt = e.cnt), (r = this._model.data)))
                    r.hasOwnProperty(i) &&
                      r[i].state &&
                      r[i].state.loading &&
                      e.mod[i] &&
                      (e.mod[i].state.loading = !0);
                  if (((this._model.data = e.mod), t)) {
                    var s,
                      a = e.add,
                      n = e.sel,
                      d = this._data.core.selected.slice(),
                      r = this._model.data;
                    if (
                      n.length !== d.length ||
                      E.vakata.array_unique(n.concat(d)).length !== n.length
                    ) {
                      for (i = 0, s = n.length; i < s; i++)
                        -1 === E.inArray(n[i], a) &&
                          -1 === E.inArray(n[i], d) &&
                          (r[n[i]].state.selected = !1);
                      for (i = 0, s = d.length; i < s; i++)
                        -1 === E.inArray(d[i], n) &&
                          (r[d[i]].state.selected = !0);
                    }
                  }
                  e.add.length &&
                    (this._data.core.selected = this._data.core.selected.concat(
                      e.add
                    )),
                    this.trigger("model", { nodes: e.dpc, parent: e.par }),
                    e.par !== E.jstree.root
                      ? (this._node_changed(e.par), this.redraw())
                      : this.redraw(!0),
                    e.add.length &&
                      this.trigger("changed", {
                        action: "model",
                        selected: this._data.core.selected,
                      }),
                    !t && c
                      ? c(function () {
                          o.call(v, !0);
                        })
                      : o.call(v, !0);
                }
              };
            if (
              this.settings.core.worker &&
              window.Blob &&
              window.URL &&
              window.Worker
            )
              try {
                null === this._wrk &&
                  (this._wrk = window.URL.createObjectURL(
                    new window.Blob(["self.onmessage = " + a.toString()], {
                      type: "text/javascript",
                    })
                  )),
                  !this._data.core.working || i
                    ? ((this._data.core.working = !0),
                      ((r = new window.Worker(this._wrk)).onmessage = function (
                        e
                      ) {
                        n.call(this, e.data, !0);
                        try {
                          r.terminate(), (r = null);
                        } catch (e) {}
                        this._data.core.worker_queue.length
                          ? this._append_json_data.apply(
                              this,
                              this._data.core.worker_queue.shift()
                            )
                          : (this._data.core.working = !1);
                      }.bind(this)),
                      (r.onerror = function (e) {
                        n.call(this, a(s), !1),
                          this._data.core.worker_queue.length
                            ? this._append_json_data.apply(
                                this,
                                this._data.core.worker_queue.shift()
                              )
                            : (this._data.core.working = !1);
                      }.bind(this)),
                      s.par
                        ? r.postMessage(s)
                        : this._data.core.worker_queue.length
                        ? this._append_json_data.apply(
                            this,
                            this._data.core.worker_queue.shift()
                          )
                        : (this._data.core.working = !1))
                    : this._data.core.worker_queue.push([e, t, o, !0]);
              } catch (e) {
                n.call(this, a(s), !1),
                  this._data.core.worker_queue.length
                    ? this._append_json_data.apply(
                        this,
                        this._data.core.worker_queue.shift()
                      )
                    : (this._data.core.working = !1);
              }
            else n.call(this, a(s), !1);
          }
        },
        _parse_model_from_html: function (e, t, i) {
          (i = i ? [].concat(i) : []), t && i.unshift(t);
          var r,
            s,
            a = this._model.data,
            n = {
              id: !1,
              text: !1,
              icon: !0,
              parent: t,
              parents: i,
              children: [],
              children_d: [],
              data: null,
              state: {},
              li_attr: { id: !1 },
              a_attr: { href: "#" },
              original: !1,
            },
            d,
            t,
            o;
          for (d in this._model.default_state)
            this._model.default_state.hasOwnProperty(d) &&
              (n.state[d] = this._model.default_state[d]);
          if (
            ((t = E.vakata.attributes(e, !0)),
            E.each(t, function (e, t) {
              return (
                !(t = E.vakata.trim(t)).length ||
                ((n.li_attr[e] = t), void ("id" === e && (n.id = t.toString())))
              );
            }),
            (t = e.children("a").first()).length &&
              ((t = E.vakata.attributes(t, !0)),
              E.each(t, function (e, t) {
                (t = E.vakata.trim(t)).length && (n.a_attr[e] = t);
              })),
            (t = (
              e.children("a").first().length ? e.children("a").first() : e
            ).clone())
              .children("ins, i, ul")
              .remove(),
            (t = t.html()),
            (t = E("<div></div>").html(t)),
            (n.text = this.settings.core.force_text ? t.text() : t.html()),
            (t = e.data()),
            (n.data = t ? E.extend(!0, {}, t) : null),
            (n.state.opened = e.hasClass("jstree-open")),
            (n.state.selected = e.children("a").hasClass("jstree-clicked")),
            (n.state.disabled = e.children("a").hasClass("jstree-disabled")),
            n.data && n.data.jstree)
          )
            for (d in n.data.jstree)
              n.data.jstree.hasOwnProperty(d) &&
                (n.state[d] = n.data.jstree[d]);
          (t = e.children("a").children(".jstree-themeicon")).length &&
            (n.icon = !t.hasClass("jstree-themeicon-hidden") && t.attr("rel")),
            n.state.icon !== P && (n.icon = n.state.icon),
            (n.icon !== P && null !== n.icon && "" !== n.icon) || (n.icon = !0),
            (t = e.children("ul").children("li"));
          do {
            o = "j" + this._id + "_" + ++this._cnt;
          } while (a[o]);
          return (
            (n.id = n.li_attr.id ? n.li_attr.id.toString() : o),
            t.length
              ? (t.each(
                  function (e, t) {
                    (r = this._parse_model_from_html(E(t), n.id, i)),
                      (s = this._model.data[r]),
                      n.children.push(r),
                      s.children_d.length &&
                        (n.children_d = n.children_d.concat(s.children_d));
                  }.bind(this)
                ),
                (n.children_d = n.children_d.concat(n.children)))
              : e.hasClass("jstree-closed") && (n.state.loaded = !1),
            n.li_attr.class &&
              (n.li_attr.class = n.li_attr.class
                .replace("jstree-closed", "")
                .replace("jstree-open", "")),
            n.a_attr.class &&
              (n.a_attr.class = n.a_attr.class
                .replace("jstree-clicked", "")
                .replace("jstree-disabled", "")),
            (a[n.id] = n).state.selected && this._data.core.selected.push(n.id),
            n.id
          );
        },
        _parse_model_from_flat_json: function (e, t, i) {
          (i = i ? i.concat() : []), t && i.unshift(t);
          var r = e.id.toString(),
            s = this._model.data,
            a = this._model.default_state,
            n,
            d,
            o,
            c,
            l = {
              id: r,
              text: e.text || "",
              icon: e.icon === P || e.icon,
              parent: t,
              parents: i,
              children: e.children || [],
              children_d: e.children_d || [],
              data: e.data,
              state: {},
              li_attr: { id: !1 },
              a_attr: { href: "#" },
              original: !1,
            };
          for (n in a) a.hasOwnProperty(n) && (l.state[n] = a[n]);
          if (
            (e &&
              e.data &&
              e.data.jstree &&
              e.data.jstree.icon &&
              (l.icon = e.data.jstree.icon),
            (l.icon !== P && null !== l.icon && "" !== l.icon) || (l.icon = !0),
            e && e.data && ((l.data = e.data), e.data.jstree))
          )
            for (n in e.data.jstree)
              e.data.jstree.hasOwnProperty(n) &&
                (l.state[n] = e.data.jstree[n]);
          if (e && "object" == typeof e.state)
            for (n in e.state)
              e.state.hasOwnProperty(n) && (l.state[n] = e.state[n]);
          if (e && "object" == typeof e.li_attr)
            for (n in e.li_attr)
              e.li_attr.hasOwnProperty(n) && (l.li_attr[n] = e.li_attr[n]);
          if (
            (l.li_attr.id || (l.li_attr.id = r),
            e && "object" == typeof e.a_attr)
          )
            for (n in e.a_attr)
              e.a_attr.hasOwnProperty(n) && (l.a_attr[n] = e.a_attr[n]);
          for (
            e &&
              e.children &&
              !0 === e.children &&
              ((l.state.loaded = !1), (l.children = []), (l.children_d = [])),
              n = 0,
              d = (s[l.id] = l).children.length;
            n < d;
            n++
          )
            (c =
              s[
                (o = this._parse_model_from_flat_json(
                  s[l.children[n]],
                  l.id,
                  i
                ))
              ]),
              l.children_d.push(o),
              c.children_d.length &&
                (l.children_d = l.children_d.concat(c.children_d));
          return (
            delete e.data,
            delete e.children,
            (s[l.id].original = e),
            l.state.selected && this._data.core.selected.push(l.id),
            l.id
          );
        },
        _parse_model_from_json: function (e, t, i) {
          (i = i ? i.concat() : []), t && i.unshift(t);
          var r = !1,
            s,
            a,
            n,
            d,
            o = this._model.data,
            c = this._model.default_state,
            l;
          do {
            r = "j" + this._id + "_" + ++this._cnt;
          } while (o[r]);
          for (s in ((l = {
            id: !1,
            text: "string" == typeof e ? e : "",
            icon: "object" != typeof e || e.icon === P || e.icon,
            parent: t,
            parents: i,
            children: [],
            children_d: [],
            data: null,
            state: {},
            li_attr: { id: !1 },
            a_attr: { href: "#" },
            original: !1,
          }),
          c))
            c.hasOwnProperty(s) && (l.state[s] = c[s]);
          if (
            (e && (e.id || 0 === e.id) && (l.id = e.id.toString()),
            e && e.text && (l.text = e.text),
            e &&
              e.data &&
              e.data.jstree &&
              e.data.jstree.icon &&
              (l.icon = e.data.jstree.icon),
            (l.icon !== P && null !== l.icon && "" !== l.icon) || (l.icon = !0),
            e && e.data && ((l.data = e.data), e.data.jstree))
          )
            for (s in e.data.jstree)
              e.data.jstree.hasOwnProperty(s) &&
                (l.state[s] = e.data.jstree[s]);
          if (e && "object" == typeof e.state)
            for (s in e.state)
              e.state.hasOwnProperty(s) && (l.state[s] = e.state[s]);
          if (e && "object" == typeof e.li_attr)
            for (s in e.li_attr)
              e.li_attr.hasOwnProperty(s) && (l.li_attr[s] = e.li_attr[s]);
          if (
            (l.li_attr.id &&
              !l.id &&
              0 !== l.id &&
              (l.id = l.li_attr.id.toString()),
            l.id || 0 === l.id || (l.id = r),
            l.li_attr.id || (l.li_attr.id = l.id),
            e && "object" == typeof e.a_attr)
          )
            for (s in e.a_attr)
              e.a_attr.hasOwnProperty(s) && (l.a_attr[s] = e.a_attr[s]);
          if (e && e.children && e.children.length) {
            for (s = 0, a = e.children.length; s < a; s++)
              (d =
                o[(n = this._parse_model_from_json(e.children[s], l.id, i))]),
                l.children.push(n),
                d.children_d.length &&
                  (l.children_d = l.children_d.concat(d.children_d));
            l.children_d = l.children.concat(l.children_d);
          }
          return (
            e &&
              e.children &&
              !0 === e.children &&
              ((l.state.loaded = !1), (l.children = []), (l.children_d = [])),
            delete e.data,
            delete e.children,
            (l.original = e),
            (o[l.id] = l).state.selected && this._data.core.selected.push(l.id),
            l.id
          );
        },
        _redraw: function () {
          for (
            var e = (
                this._model.force_full_redraw
                  ? this._model.data[E.jstree.root].children
                  : this._model.changed
              ).concat([]),
              t = b.createElement("UL"),
              i,
              r,
              s,
              a = this._data.core.focused,
              r = 0,
              s = e.length;
            r < s;
            r++
          )
            (i = this.redraw_node(e[r], !0, this._model.force_full_redraw)) &&
              this._model.force_full_redraw &&
              t.appendChild(i);
          this._model.force_full_redraw &&
            ((t.className = this.get_container_ul()[0].className),
            t.setAttribute("role", "presentation"),
            this.element.empty().append(t)),
            null !== a &&
              this.settings.core.restore_focus &&
              ((i = this.get_node(a, !0)) &&
              i.length &&
              i.children(".jstree-anchor")[0] !== b.activeElement
                ? i.children(".jstree-anchor").trigger("focus")
                : (this._data.core.focused = null)),
            (this._model.force_full_redraw = !1),
            (this._model.changed = []),
            this.trigger("redraw", { nodes: e });
        },
        redraw: function (e) {
          e && (this._model.force_full_redraw = !0), this._redraw();
        },
        draw_children: function (e) {
          var t = this.get_node(e),
            i = !1,
            r = !1,
            s = !1,
            a = b;
          if (!t) return !1;
          if (t.id === E.jstree.root) return this.redraw(!0);
          if (!(e = this.get_node(e, !0)) || !e.length) return !1;
          if (
            (e.children(".jstree-children").remove(),
            (e = e[0]),
            t.children.length && t.state.loaded)
          ) {
            for (
              (s = a.createElement("UL")).setAttribute("role", "group"),
                s.className = "jstree-children",
                i = 0,
                r = t.children.length;
              i < r;
              i++
            )
              s.appendChild(this.redraw_node(t.children[i], !0, !0));
            e.appendChild(s);
          }
        },
        redraw_node: function (e, t, i, r) {
          var s = this.get_node(e),
            a = !1,
            n = !1,
            d = !1,
            o = !1,
            c = !1,
            l = !1,
            h = "",
            _ = b,
            g = this._model.data,
            u = !1,
            f = !1,
            p = null,
            m = 0,
            v = 0,
            j = !1,
            k = !1;
          if (!s) return !1;
          if (s.id === E.jstree.root) return this.redraw(!0);
          if (
            ((t = t || 0 === s.children.length),
            (e = b.querySelector
              ? this.element[0].querySelector(
                  "#" +
                    (-1 !== "0123456789".indexOf(s.id[0])
                      ? "\\3" +
                        s.id[0] +
                        " " +
                        s.id.substr(1).replace(E.jstree.idregex, "\\$&")
                      : s.id.replace(E.jstree.idregex, "\\$&"))
                )
              : b.getElementById(s.id)))
          )
            (e = E(e)),
              i ||
                ((a = e.parent().parent()[0]) === this.element[0] && (a = null),
                (n = e.index())),
              (t =
                !t &&
                s.children.length &&
                !e.children(".jstree-children").length
                  ? !0
                  : t) || (d = e.children(".jstree-children")[0]),
              (u = e.children(".jstree-anchor")[0] === b.activeElement),
              e.remove();
          else if (((t = !0), !i)) {
            if (
              !(
                null ===
                  (a =
                    s.parent !== E.jstree.root
                      ? E(
                          "#" + s.parent.replace(E.jstree.idregex, "\\$&"),
                          this.element
                        )[0]
                      : null) ||
                (a && g[s.parent].state.opened)
              )
            )
              return !1;
            n = E.inArray(
              s.id,
              (null === a ? g[E.jstree.root] : g[s.parent]).children
            );
          }
          for (o in ((e = this._data.core.node.cloneNode(!0)),
          (h = "jstree-node "),
          s.li_attr))
            s.li_attr.hasOwnProperty(o) &&
              "id" !== o &&
              ("class" !== o
                ? e.setAttribute(o, s.li_attr[o])
                : (h += s.li_attr[o]));
          for (
            s.a_attr.id || (s.a_attr.id = s.id + "_anchor"),
              e.childNodes[1].setAttribute("aria-selected", !!s.state.selected),
              e.childNodes[1].setAttribute("aria-level", s.parents.length),
              this.settings.core.compute_elements_positions &&
                (e.childNodes[1].setAttribute(
                  "aria-setsize",
                  g[s.parent].children.length
                ),
                e.childNodes[1].setAttribute(
                  "aria-posinset",
                  g[s.parent].children.indexOf(s.id) + 1
                )),
              s.state.disabled &&
                e.childNodes[1].setAttribute("aria-disabled", !0),
              o = 0,
              c = s.children.length;
            o < c;
            o++
          )
            if (!g[s.children[o]].state.hidden) {
              j = !0;
              break;
            }
          if (
            null !== s.parent &&
            g[s.parent] &&
            !s.state.hidden &&
            ((o = E.inArray(s.id, g[s.parent].children)), (k = s.id), -1 !== o)
          )
            for (o++, c = g[s.parent].children.length; o < c; o++)
              if (
                (k = !g[g[s.parent].children[o]].state.hidden
                  ? g[s.parent].children[o]
                  : k) !== s.id
              )
                break;
          for (c in (s.state.hidden && (h += " jstree-hidden"),
          s.state.loading && (h += " jstree-loading"),
          s.state.loaded && !j
            ? (h += " jstree-leaf")
            : ((h +=
                s.state.opened && s.state.loaded
                  ? " jstree-open"
                  : " jstree-closed"),
              e.childNodes[1].setAttribute(
                "aria-expanded",
                s.state.opened && s.state.loaded
              )),
          k === s.id && (h += " jstree-last"),
          (e.id = s.id),
          (e.className = h),
          (h =
            (s.state.selected ? " jstree-clicked" : "") +
            (s.state.disabled ? " jstree-disabled" : "")),
          s.a_attr))
            s.a_attr.hasOwnProperty(c) &&
              (("href" === c && "#" === s.a_attr[c]) ||
                ("class" !== c
                  ? e.childNodes[1].setAttribute(c, s.a_attr[c])
                  : (h += " " + s.a_attr[c])));
          if (
            (h.length && (e.childNodes[1].className = "jstree-anchor " + h),
            ((s.icon && !0 !== s.icon) || !1 === s.icon) &&
              (!1 === s.icon
                ? (e.childNodes[1].childNodes[0].className +=
                    " jstree-themeicon-hidden")
                : -1 === s.icon.indexOf("/") && -1 === s.icon.indexOf(".")
                ? (e.childNodes[1].childNodes[0].className +=
                    " " + s.icon + " jstree-themeicon-custom")
                : ((e.childNodes[1].childNodes[0].style.backgroundImage =
                    'url("' + s.icon + '")'),
                  (e.childNodes[1].childNodes[0].style.backgroundPosition =
                    "center center"),
                  (e.childNodes[1].childNodes[0].style.backgroundSize = "auto"),
                  (e.childNodes[1].childNodes[0].className +=
                    " jstree-themeicon-custom"))),
            this.settings.core.force_text
              ? e.childNodes[1].appendChild(_.createTextNode(s.text))
              : (e.childNodes[1].innerHTML += s.text),
            t && s.children.length && (s.state.opened || r) && s.state.loaded)
          ) {
            for (
              (l = _.createElement("UL")).setAttribute("role", "group"),
                l.className = "jstree-children",
                o = 0,
                c = s.children.length;
              o < c;
              o++
            )
              l.appendChild(this.redraw_node(s.children[o], t, !0));
            e.appendChild(l);
          }
          if ((d && e.appendChild(d), !i)) {
            for (
              o = 0, c = (a = a || this.element[0]).childNodes.length;
              o < c;
              o++
            )
              if (
                a.childNodes[o] &&
                a.childNodes[o].className &&
                -1 !== a.childNodes[o].className.indexOf("jstree-children")
              ) {
                p = a.childNodes[o];
                break;
              }
            p ||
              ((p = _.createElement("UL")).setAttribute("role", "group"),
              (p.className = "jstree-children"),
              a.appendChild(p)),
              n < (a = p).childNodes.length
                ? a.insertBefore(e, a.childNodes[n])
                : a.appendChild(e),
              u &&
                ((m = this.element[0].scrollTop),
                (v = this.element[0].scrollLeft),
                e.childNodes[1].focus(),
                (this.element[0].scrollTop = m),
                (this.element[0].scrollLeft = v));
          }
          return (
            s.state.opened &&
              !s.state.loaded &&
              ((s.state.opened = !1),
              setTimeout(
                function () {
                  this.open_node(s.id, !1, 0);
                }.bind(this),
                0
              )),
            e
          );
        },
        open_node: function (e, i, r) {
          var t, s, a, n;
          if (E.vakata.is_array(e)) {
            for (t = 0, s = (e = e.slice()).length; t < s; t++)
              this.open_node(e[t], i, r);
            return !0;
          }
          return (
            !(!(e = this.get_node(e)) || e.id === E.jstree.root) &&
            ((r = r === P ? this.settings.core.animation : r),
            this.is_closed(e)
              ? this.is_loaded(e)
                ? ((a = this.get_node(e, !0)),
                  (n = this),
                  a.length &&
                    (r &&
                      a.children(".jstree-children").length &&
                      a.children(".jstree-children").stop(!0, !0),
                    e.children.length &&
                      !this._firstChild(a.children(".jstree-children")[0]) &&
                      this.draw_children(e),
                    r
                      ? (this.trigger("before_open", { node: e }),
                        a
                          .children(".jstree-children")
                          .css("display", "none")
                          .end()
                          .removeClass("jstree-closed")
                          .addClass("jstree-open")
                          .children(".jstree-anchor")
                          .attr("aria-expanded", !0)
                          .end()
                          .children(".jstree-children")
                          .stop(!0, !0)
                          .slideDown(r, function () {
                            (this.style.display = ""),
                              n.element && n.trigger("after_open", { node: e });
                          }))
                      : (this.trigger("before_open", { node: e }),
                        (a[0].className = a[0].className.replace(
                          "jstree-closed",
                          "jstree-open"
                        )),
                        a[0].childNodes[1].setAttribute("aria-expanded", !0))),
                  (e.state.opened = !0),
                  i && i.call(this, e, !0),
                  a.length || this.trigger("before_open", { node: e }),
                  this.trigger("open_node", { node: e }),
                  (r && a.length) || this.trigger("after_open", { node: e }),
                  !0)
                : this.is_loading(e)
                ? setTimeout(
                    function () {
                      this.open_node(e, i, r);
                    }.bind(this),
                    500
                  )
                : void this.load_node(e, function (e, t) {
                    return t
                      ? this.open_node(e, i, r)
                      : !!i && i.call(this, e, !1);
                  })
              : (i && i.call(this, e, !1), !1))
          );
        },
        _open_to: function (e) {
          if (!(e = this.get_node(e)) || e.id === E.jstree.root) return !1;
          for (var t, i, r = e.parents, t = 0, i = r.length; t < i; t += 1)
            t !== E.jstree.root && this.open_node(r[t], !1, 0);
          return E("#" + e.id.replace(E.jstree.idregex, "\\$&"), this.element);
        },
        close_node: function (e, t) {
          var i, r, s, a;
          if (E.vakata.is_array(e)) {
            for (i = 0, r = (e = e.slice()).length; i < r; i++)
              this.close_node(e[i], t);
            return !0;
          }
          return (
            !(!(e = this.get_node(e)) || e.id === E.jstree.root) &&
            !this.is_closed(e) &&
            ((t = t === P ? this.settings.core.animation : t),
            (a = (s = this).get_node(e, !0)),
            (e.state.opened = !1),
            this.trigger("close_node", { node: e }),
            void (a.length
              ? t
                ? a
                    .children(".jstree-children")
                    .attr("style", "display:block !important")
                    .end()
                    .removeClass("jstree-open")
                    .addClass("jstree-closed")
                    .children(".jstree-anchor")
                    .attr("aria-expanded", !1)
                    .end()
                    .children(".jstree-children")
                    .stop(!0, !0)
                    .slideUp(t, function () {
                      (this.style.display = ""),
                        a.children(".jstree-children").remove(),
                        s.element && s.trigger("after_close", { node: e });
                    })
                : ((a[0].className = a[0].className.replace(
                    "jstree-open",
                    "jstree-closed"
                  )),
                  a.children(".jstree-anchor").attr("aria-expanded", !1),
                  a.children(".jstree-children").remove(),
                  this.trigger("after_close", { node: e }))
              : this.trigger("after_close", { node: e })))
          );
        },
        toggle_node: function (e) {
          var t, i;
          if (E.vakata.is_array(e)) {
            for (t = 0, i = (e = e.slice()).length; t < i; t++)
              this.toggle_node(e[t]);
            return !0;
          }
          return this.is_closed(e)
            ? this.open_node(e)
            : this.is_open(e)
            ? this.close_node(e)
            : void 0;
        },
        open_all: function (e, i, r) {
          if (((e = e || E.jstree.root), !(e = this.get_node(e)))) return !1;
          var t =
              e.id === E.jstree.root
                ? this.get_container_ul()
                : this.get_node(e, !0),
            s,
            a,
            n;
          if (!t.length) {
            for (s = 0, a = e.children_d.length; s < a; s++)
              this.is_closed(this._model.data[e.children_d[s]]) &&
                (this._model.data[e.children_d[s]].state.opened = !0);
            return this.trigger("open_all", { node: e });
          }
          (r = r || t),
            (t = (n = this).is_closed(e)
              ? t.find(".jstree-closed").addBack()
              : t.find(".jstree-closed")).each(function () {
              n.open_node(
                this,
                function (e, t) {
                  t && this.is_parent(e) && this.open_all(e, i, r);
                },
                i || 0
              );
            }),
            0 === r.find(".jstree-closed").length &&
              this.trigger("open_all", { node: this.get_node(r) });
        },
        close_all: function (e, t) {
          if (((e = e || E.jstree.root), !(e = this.get_node(e)))) return !1;
          var i =
              e.id === E.jstree.root
                ? this.get_container_ul()
                : this.get_node(e, !0),
            r = this,
            s,
            a;
          for (
            i.length &&
              ((i = this.is_open(e)
                ? i.find(".jstree-open").addBack()
                : i.find(".jstree-open")),
              E(i.get().reverse()).each(function () {
                r.close_node(this, t || 0);
              })),
              s = 0,
              a = e.children_d.length;
            s < a;
            s++
          )
            this._model.data[e.children_d[s]].state.opened = !1;
          this.trigger("close_all", { node: e });
        },
        is_disabled: function (e) {
          return (e = this.get_node(e)) && e.state && e.state.disabled;
        },
        enable_node: function (e) {
          var t, i;
          if (E.vakata.is_array(e)) {
            for (t = 0, i = (e = e.slice()).length; t < i; t++)
              this.enable_node(e[t]);
            return !0;
          }
          if (!(e = this.get_node(e)) || e.id === E.jstree.root) return !1;
          (e.state.disabled = !1),
            this.get_node(e, !0)
              .children(".jstree-anchor")
              .removeClass("jstree-disabled")
              .attr("aria-disabled", !1),
            this.trigger("enable_node", { node: e });
        },
        disable_node: function (e) {
          var t, i;
          if (E.vakata.is_array(e)) {
            for (t = 0, i = (e = e.slice()).length; t < i; t++)
              this.disable_node(e[t]);
            return !0;
          }
          if (!(e = this.get_node(e)) || e.id === E.jstree.root) return !1;
          (e.state.disabled = !0),
            this.get_node(e, !0)
              .children(".jstree-anchor")
              .addClass("jstree-disabled")
              .attr("aria-disabled", !0),
            this.trigger("disable_node", { node: e });
        },
        is_hidden: function (e) {
          return !0 === (e = this.get_node(e)).state.hidden;
        },
        hide_node: function (e, t) {
          var i, r;
          if (E.vakata.is_array(e)) {
            for (i = 0, r = (e = e.slice()).length; i < r; i++)
              this.hide_node(e[i], !0);
            return t || this.redraw(), !0;
          }
          if (!(e = this.get_node(e)) || e.id === E.jstree.root) return !1;
          e.state.hidden ||
            ((e.state.hidden = !0),
            this._node_changed(e.parent),
            t || this.redraw(),
            this.trigger("hide_node", { node: e }));
        },
        show_node: function (e, t) {
          var i, r;
          if (E.vakata.is_array(e)) {
            for (i = 0, r = (e = e.slice()).length; i < r; i++)
              this.show_node(e[i], !0);
            return t || this.redraw(), !0;
          }
          if (!(e = this.get_node(e)) || e.id === E.jstree.root) return !1;
          e.state.hidden &&
            ((e.state.hidden = !1),
            this._node_changed(e.parent),
            t || this.redraw(),
            this.trigger("show_node", { node: e }));
        },
        hide_all: function (e) {
          var t,
            i = this._model.data,
            r = [];
          for (t in i)
            i.hasOwnProperty(t) &&
              t !== E.jstree.root &&
              !i[t].state.hidden &&
              ((i[t].state.hidden = !0), r.push(t));
          return (
            (this._model.force_full_redraw = !0),
            e || this.redraw(),
            this.trigger("hide_all", { nodes: r }),
            r
          );
        },
        show_all: function (e) {
          var t,
            i = this._model.data,
            r = [];
          for (t in i)
            i.hasOwnProperty(t) &&
              t !== E.jstree.root &&
              i[t].state.hidden &&
              ((i[t].state.hidden = !1), r.push(t));
          return (
            (this._model.force_full_redraw = !0),
            e || this.redraw(),
            this.trigger("show_all", { nodes: r }),
            r
          );
        },
        activate_node: function (e, t) {
          if (this.is_disabled(e)) return !1;
          if (
            ((t && "object" == typeof t) || (t = {}),
            (this._data.core.last_clicked =
              this._data.core.last_clicked &&
              this._data.core.last_clicked.id !== P
                ? this.get_node(this._data.core.last_clicked.id)
                : null),
            this._data.core.last_clicked &&
              !this._data.core.last_clicked.state.selected &&
              (this._data.core.last_clicked = null),
            !this._data.core.last_clicked &&
              this._data.core.selected.length &&
              (this._data.core.last_clicked = this.get_node(
                this._data.core.selected[this._data.core.selected.length - 1]
              )),
            this.settings.core.multiple &&
              (t.metaKey || t.ctrlKey || t.shiftKey) &&
              (!t.shiftKey ||
                (this._data.core.last_clicked &&
                  this.get_parent(e) &&
                  this.get_parent(e) === this._data.core.last_clicked.parent)))
          )
            if (t.shiftKey) {
              for (
                var i = this.get_node(e).id,
                  r = this._data.core.last_clicked.id,
                  s = this.get_node(
                    this._data.core.last_clicked.parent
                  ).children,
                  a = !1,
                  n,
                  d,
                  n = 0,
                  d = s.length;
                n < d;
                n += 1
              )
                s[n] === i && (a = !a),
                  s[n] === r && (a = !a),
                  this.is_disabled(s[n]) || (!a && s[n] !== i && s[n] !== r)
                    ? t.ctrlKey || this.deselect_node(s[n], !0, t)
                    : this.is_hidden(s[n]) || this.select_node(s[n], !0, !1, t);
              this.trigger("changed", {
                action: "select_node",
                node: this.get_node(e),
                selected: this._data.core.selected,
                event: t,
              });
            } else
              this.is_selected(e)
                ? this.deselect_node(e, !1, t)
                : (t.ctrlKey &&
                    (this._data.core.last_clicked = this.get_node(e)),
                  this.select_node(e, !1, !1, t));
          else
            !this.settings.core.multiple &&
            (t.metaKey || t.ctrlKey || t.shiftKey) &&
            this.is_selected(e)
              ? this.deselect_node(e, !1, t)
              : ((this.is_selected(e) &&
                  1 === this._data.core.selected.length) ||
                  (this.deselect_all(!0), this.select_node(e, !1, !1, t)),
                (this._data.core.last_clicked = this.get_node(e)));
          this.trigger("activate_node", { node: this.get_node(e), event: t });
        },
        hover_node: function (e) {
          if (
            !(e = this.get_node(e, !0)) ||
            !e.length ||
            e.children(".jstree-hovered").length
          )
            return !1;
          var t = this.element.find(".jstree-hovered"),
            i = this.element;
          t && t.length && this.dehover_node(t),
            e.children(".jstree-anchor").addClass("jstree-hovered"),
            this.trigger("hover_node", { node: this.get_node(e) }),
            setTimeout(function () {
              i.attr("aria-activedescendant", e[0].id);
            }, 0);
        },
        dehover_node: function (e) {
          if (
            !(e = this.get_node(e, !0)) ||
            !e.length ||
            !e.children(".jstree-hovered").length
          )
            return !1;
          e.children(".jstree-anchor").removeClass("jstree-hovered"),
            this.trigger("dehover_node", { node: this.get_node(e) });
        },
        select_node: function (e, t, i, r) {
          var s, a, n, d;
          if (E.vakata.is_array(e)) {
            for (a = 0, n = (e = e.slice()).length; a < n; a++)
              this.select_node(e[a], t, i, r);
            return !0;
          }
          if (!(e = this.get_node(e)) || e.id === E.jstree.root) return !1;
          (s = this.get_node(e, !0)),
            e.state.selected ||
              ((e.state.selected = !0),
              this._data.core.selected.push(e.id),
              (s = !i ? this._open_to(e) : s) &&
                s.length &&
                s
                  .children(".jstree-anchor")
                  .addClass("jstree-clicked")
                  .attr("aria-selected", !0),
              this.trigger("select_node", {
                node: e,
                selected: this._data.core.selected,
                event: r,
              }),
              t ||
                this.trigger("changed", {
                  action: "select_node",
                  node: e,
                  selected: this._data.core.selected,
                  event: r,
                }));
        },
        deselect_node: function (e, t, i) {
          var r, s, a;
          if (E.vakata.is_array(e)) {
            for (r = 0, s = (e = e.slice()).length; r < s; r++)
              this.deselect_node(e[r], t, i);
            return !0;
          }
          if (!(e = this.get_node(e)) || e.id === E.jstree.root) return !1;
          (a = this.get_node(e, !0)),
            e.state.selected &&
              ((e.state.selected = !1),
              (this._data.core.selected = E.vakata.array_remove_item(
                this._data.core.selected,
                e.id
              )),
              a.length &&
                a
                  .children(".jstree-anchor")
                  .removeClass("jstree-clicked")
                  .attr("aria-selected", !1),
              this.trigger("deselect_node", {
                node: e,
                selected: this._data.core.selected,
                event: i,
              }),
              t ||
                this.trigger("changed", {
                  action: "deselect_node",
                  node: e,
                  selected: this._data.core.selected,
                  event: i,
                }));
        },
        select_all: function (e) {
          var t = this._data.core.selected.concat([]),
            i,
            r;
          for (
            this._data.core.selected =
              this._model.data[E.jstree.root].children_d.concat(),
              i = 0,
              r = this._data.core.selected.length;
            i < r;
            i++
          )
            this._model.data[this._data.core.selected[i]] &&
              (this._model.data[this._data.core.selected[i]].state.selected =
                !0);
          this.redraw(!0),
            this.trigger("select_all", { selected: this._data.core.selected }),
            e ||
              this.trigger("changed", {
                action: "select_all",
                selected: this._data.core.selected,
                old_selection: t,
              });
        },
        deselect_all: function (e) {
          for (
            var t = this._data.core.selected.concat([]),
              i,
              r,
              i = 0,
              r = this._data.core.selected.length;
            i < r;
            i++
          )
            this._model.data[this._data.core.selected[i]] &&
              (this._model.data[this._data.core.selected[i]].state.selected =
                !1);
          (this._data.core.selected = []),
            this.element
              .find(".jstree-clicked")
              .removeClass("jstree-clicked")
              .attr("aria-selected", !1),
            this.trigger("deselect_all", {
              selected: this._data.core.selected,
              node: t,
            }),
            e ||
              this.trigger("changed", {
                action: "deselect_all",
                selected: this._data.core.selected,
                old_selection: t,
              });
        },
        is_selected: function (e) {
          return (
            !(!(e = this.get_node(e)) || e.id === E.jstree.root) &&
            e.state.selected
          );
        },
        get_selected: function (e) {
          return e
            ? E.map(
                this._data.core.selected,
                function (e) {
                  return this.get_node(e);
                }.bind(this)
              )
            : this._data.core.selected.slice();
        },
        get_top_selected: function (e) {
          for (
            var t = this.get_selected(!0),
              i = {},
              r,
              s,
              a,
              n,
              r = 0,
              s = t.length;
            r < s;
            r++
          )
            i[t[r].id] = t[r];
          for (r = 0, s = t.length; r < s; r++)
            for (a = 0, n = t[r].children_d.length; a < n; a++)
              i[t[r].children_d[a]] && delete i[t[r].children_d[a]];
          for (r in ((t = []), i)) i.hasOwnProperty(r) && t.push(r);
          return e
            ? E.map(
                t,
                function (e) {
                  return this.get_node(e);
                }.bind(this)
              )
            : t;
        },
        get_bottom_selected: function (e) {
          for (
            var t = this.get_selected(!0), i = [], r, s, r = 0, s = t.length;
            r < s;
            r++
          )
            t[r].children.length || i.push(t[r].id);
          return e
            ? E.map(
                i,
                function (e) {
                  return this.get_node(e);
                }.bind(this)
              )
            : i;
        },
        get_state: function () {
          var e = {
              core: {
                open: [],
                loaded: [],
                scroll: {
                  left: this.element.scrollLeft(),
                  top: this.element.scrollTop(),
                },
                selected: [],
              },
            },
            t;
          for (t in this._model.data)
            this._model.data.hasOwnProperty(t) &&
              t !== E.jstree.root &&
              (this._model.data[t].state.loaded &&
                this.settings.core.loaded_state &&
                e.core.loaded.push(t),
              this._model.data[t].state.opened && e.core.open.push(t),
              this._model.data[t].state.selected && e.core.selected.push(t));
          return e;
        },
        set_state: function (t, i) {
          if (t) {
            if (
              (t.core &&
                t.core.selected &&
                t.core.initial_selection === P &&
                (t.core.initial_selection = this._data.core.selected
                  .concat([])
                  .sort()
                  .join(",")),
              t.core)
            ) {
              var e, r, s, a, n;
              if (t.core.loaded)
                return (
                  this.settings.core.loaded_state &&
                  E.vakata.is_array(t.core.loaded) &&
                  t.core.loaded.length
                    ? this._load_nodes(t.core.loaded, function (e) {
                        delete t.core.loaded, this.set_state(t, i);
                      })
                    : (delete t.core.loaded, this.set_state(t, i)),
                  !1
                );
              if (t.core.open)
                return (
                  E.vakata.is_array(t.core.open) && t.core.open.length
                    ? this._load_nodes(t.core.open, function (e) {
                        this.open_node(e, !1, 0),
                          delete t.core.open,
                          this.set_state(t, i);
                      })
                    : (delete t.core.open, this.set_state(t, i)),
                  !1
                );
              if (t.core.scroll)
                return (
                  t.core.scroll &&
                    t.core.scroll.left !== P &&
                    this.element.scrollLeft(t.core.scroll.left),
                  t.core.scroll &&
                    t.core.scroll.top !== P &&
                    this.element.scrollTop(t.core.scroll.top),
                  delete t.core.scroll,
                  this.set_state(t, i),
                  !1
                );
              if (t.core.selected)
                return (
                  (a = this),
                  (t.core.initial_selection !== P &&
                    t.core.initial_selection !==
                      this._data.core.selected.concat([]).sort().join(",")) ||
                    (this.deselect_all(),
                    E.each(t.core.selected, function (e, t) {
                      a.select_node(t, !1, !0);
                    })),
                  delete t.core.initial_selection,
                  delete t.core.selected,
                  this.set_state(t, i),
                  !1
                );
              for (n in t)
                t.hasOwnProperty(n) &&
                  "core" !== n &&
                  -1 === E.inArray(n, this.settings.plugins) &&
                  delete t[n];
              if (E.isEmptyObject(t.core))
                return delete t.core, this.set_state(t, i), !1;
            }
            return E.isEmptyObject(t)
              ? ((t = null), i && i.call(this), this.trigger("set_state"), !1)
              : !0;
          }
          return !1;
        },
        refresh: function (e, t) {
          (this._data.core.state = !0 === t ? {} : this.get_state()),
            t &&
              E.vakata.is_function(t) &&
              (this._data.core.state = t.call(this, this._data.core.state)),
            (this._cnt = 0),
            (this._model.data = {}),
            (this._model.data[E.jstree.root] = {
              id: E.jstree.root,
              parent: null,
              parents: [],
              children: [],
              children_d: [],
              state: { loaded: !1 },
            }),
            (this._data.core.selected = []),
            (this._data.core.last_clicked = null),
            (this._data.core.focused = null);
          var i = this.get_container_ul()[0].className;
          e ||
            (this.element.html(
              "<ul class='" +
                i +
                "' role='group'><li class='jstree-initial-node jstree-loading jstree-leaf jstree-last' role='none' id='j" +
                this._id +
                "_loading'><i class='jstree-icon jstree-ocl'></i><a class='jstree-anchor' role='treeitem' href='#'><i class='jstree-icon jstree-themeicon-hidden'></i>" +
                this.get_string("Loading ...") +
                "</a></li></ul>"
            ),
            this.element.attr(
              "aria-activedescendant",
              "j" + this._id + "_loading"
            )),
            this.load_node(E.jstree.root, function (e, t) {
              t &&
                ((this.get_container_ul()[0].className = i),
                this._firstChild(this.get_container_ul()[0]) &&
                  this.element.attr(
                    "aria-activedescendant",
                    this._firstChild(this.get_container_ul()[0]).id
                  ),
                this.set_state(
                  E.extend(!0, {}, this._data.core.state),
                  function () {
                    this.trigger("refresh");
                  }
                )),
                (this._data.core.state = null);
            });
        },
        refresh_node: function (t) {
          if (!(t = this.get_node(t)) || t.id === E.jstree.root) return !1;
          var i = [],
            e = [],
            r = this._data.core.selected.concat([]);
          e.push(t.id),
            !0 === t.state.opened && i.push(t.id),
            this.get_node(t, !0)
              .find(".jstree-open")
              .each(function () {
                e.push(this.id), i.push(this.id);
              }),
            this._load_nodes(
              e,
              function (e) {
                this.open_node(i, !1, 0),
                  this.select_node(r),
                  this.trigger("refresh_node", { node: t, nodes: e });
              }.bind(this),
              !1,
              !0
            );
        },
        set_id: function (e, t) {
          if (!(e = this.get_node(e)) || e.id === E.jstree.root) return !1;
          var i,
            r,
            s = this._model.data,
            a = e.id;
          for (
            t = t.toString(),
              s[e.parent].children[E.inArray(e.id, s[e.parent].children)] = t,
              i = 0,
              r = e.parents.length;
            i < r;
            i++
          )
            s[e.parents[i]].children_d[
              E.inArray(e.id, s[e.parents[i]].children_d)
            ] = t;
          for (i = 0, r = e.children.length; i < r; i++)
            s[e.children[i]].parent = t;
          for (i = 0, r = e.children_d.length; i < r; i++)
            s[e.children_d[i]].parents[
              E.inArray(e.id, s[e.children_d[i]].parents)
            ] = t;
          return (
            -1 !== (i = E.inArray(e.id, this._data.core.selected)) &&
              (this._data.core.selected[i] = t),
            (i = this.get_node(e.id, !0)) &&
              (i.attr("id", t),
              this.element.attr("aria-activedescendant") === e.id &&
                this.element.attr("aria-activedescendant", t)),
            delete s[e.id],
            (e.id = t),
            (s[(e.li_attr.id = t)] = e),
            this.trigger("set_id", { node: e, new: e.id, old: a }),
            !0
          );
        },
        get_text: function (e) {
          return !(!(e = this.get_node(e)) || e.id === E.jstree.root) && e.text;
        },
        set_text: function (e, t) {
          var i, r;
          if (E.vakata.is_array(e)) {
            for (i = 0, r = (e = e.slice()).length; i < r; i++)
              this.set_text(e[i], t);
            return !0;
          }
          return (
            !(!(e = this.get_node(e)) || e.id === E.jstree.root) &&
            ((e.text = t),
            this.get_node(e, !0).length && this.redraw_node(e.id),
            this.trigger("set_text", { obj: e, text: t }),
            !0)
          );
        },
        get_json: function (e, t, i) {
          if (!(e = this.get_node(e || E.jstree.root))) return !1;
          t && t.flat && !i && (i = []);
          var r = {
              id: e.id,
              text: e.text,
              icon: this.get_icon(e),
              li_attr: E.extend(!0, {}, e.li_attr),
              a_attr: E.extend(!0, {}, e.a_attr),
              state: {},
              data:
                (!t || !t.no_data) &&
                E.extend(!0, E.vakata.is_array(e.data) ? [] : {}, e.data),
            },
            s,
            a;
          if (
            (t && t.flat ? (r.parent = e.parent) : (r.children = []),
            t && t.no_state)
          )
            delete r.state;
          else
            for (s in e.state)
              e.state.hasOwnProperty(s) && (r.state[s] = e.state[s]);
          if (
            (t && t.no_li_attr && delete r.li_attr,
            t && t.no_a_attr && delete r.a_attr,
            t &&
              t.no_id &&
              (delete r.id,
              r.li_attr && r.li_attr.id && delete r.li_attr.id,
              r.a_attr && r.a_attr.id && delete r.a_attr.id),
            t && t.flat && e.id !== E.jstree.root && i.push(r),
            !t || !t.no_children)
          )
            for (s = 0, a = e.children.length; s < a; s++)
              t && t.flat
                ? this.get_json(e.children[s], t, i)
                : r.children.push(this.get_json(e.children[s], t));
          return t && t.flat ? i : e.id === E.jstree.root ? r.children : r;
        },
        create_node: function (e, t, i, r, s) {
          if ((null === e && (e = E.jstree.root), !(e = this.get_node(e))))
            return !1;
          if (
            !(i = i === P ? "last" : i).toString().match(/^(before|after)$/) &&
            !s &&
            !this.is_loaded(e)
          )
            return this.load_node(e, function () {
              this.create_node(e, t, i, r, !0);
            });
          var a, n, d, o;
          switch (
            ((t =
              "string" ==
              typeof (t = t || { text: this.get_string("New node") })
                ? { text: t }
                : E.extend(!0, {}, t)).text === P &&
              (t.text = this.get_string("New node")),
            (i =
              e.id === E.jstree.root &&
              "after" === (i = "before" === i ? "first" : i)
                ? "last"
                : i))
          ) {
            case "before":
              (a = this.get_node(e.parent)),
                (i = E.inArray(e.id, a.children)),
                (e = a);
              break;
            case "after":
              (a = this.get_node(e.parent)),
                (i = E.inArray(e.id, a.children) + 1),
                (e = a);
              break;
            case "inside":
            case "first":
              i = 0;
              break;
            case "last":
              i = e.children.length;
              break;
            default:
              i = i || 0;
          }
          if (
            (i > e.children.length && (i = e.children.length),
            t.id === P && (t.id = !0),
            !this.check("create_node", t, e, i))
          )
            return (
              this.settings.core.error.call(this, this._data.core.last_error),
              !1
            );
          if (
            (!0 === t.id && delete t.id,
            !(t = this._parse_model_from_json(t, e.id, e.parents.concat())))
          )
            return !1;
          for (
            a = this.get_node(t),
              (n = []).push(t),
              n = n.concat(a.children_d),
              this.trigger("model", { nodes: n, parent: e.id }),
              e.children_d = e.children_d.concat(n),
              d = 0,
              o = e.parents.length;
            d < o;
            d++
          )
            this._model.data[e.parents[d]].children_d =
              this._model.data[e.parents[d]].children_d.concat(n);
          for (t = a, a = [], d = 0, o = e.children.length; d < o; d++)
            a[i <= d ? d + 1 : d] = e.children[d];
          return (
            (a[i] = t.id),
            (e.children = a),
            this.redraw_node(e, !0),
            this.trigger("create_node", {
              node: this.get_node(t),
              parent: e.id,
              position: i,
            }),
            r && r.call(this, this.get_node(t)),
            t.id
          );
        },
        rename_node: function (e, t) {
          var i, r, s;
          if (E.vakata.is_array(e)) {
            for (i = 0, r = (e = e.slice()).length; i < r; i++)
              this.rename_node(e[i], t);
            return !0;
          }
          return (
            !(!(e = this.get_node(e)) || e.id === E.jstree.root) &&
            ((s = e.text),
            this.check("rename_node", e, this.get_parent(e), t)
              ? (this.set_text(e, t),
                this.trigger("rename_node", { node: e, text: t, old: s }),
                !0)
              : (this.settings.core.error.call(
                  this,
                  this._data.core.last_error
                ),
                !1))
          );
        },
        delete_node: function (e) {
          var t, i, r, s, a, n, d, o, c, l, h, s;
          if (E.vakata.is_array(e)) {
            for (t = 0, i = (e = e.slice()).length; t < i; t++)
              this.delete_node(e[t]);
            return !0;
          }
          if (!(e = this.get_node(e)) || e.id === E.jstree.root) return !1;
          if (
            ((r = this.get_node(e.parent)),
            (s = E.inArray(e.id, r.children)),
            (l = !1),
            !this.check("delete_node", e, r, s))
          )
            return (
              this.settings.core.error.call(this, this._data.core.last_error),
              !1
            );
          for (
            -1 !== s && (r.children = E.vakata.array_remove(r.children, s)),
              (a = e.children_d.concat([])).push(e.id),
              n = 0,
              d = e.parents.length;
            n < d;
            n++
          )
            this._model.data[e.parents[n]].children_d = E.vakata.array_filter(
              this._model.data[e.parents[n]].children_d,
              function (e) {
                return -1 === E.inArray(e, a);
              }
            );
          for (o = 0, c = a.length; o < c; o++)
            if (this._model.data[a[o]].state.selected) {
              l = !0;
              break;
            }
          for (
            l &&
              (this._data.core.selected = E.vakata.array_filter(
                this._data.core.selected,
                function (e) {
                  return -1 === E.inArray(e, a);
                }
              )),
              this.trigger("delete_node", { node: e, parent: r.id }),
              l &&
                this.trigger("changed", {
                  action: "delete_node",
                  node: e,
                  selected: this._data.core.selected,
                  parent: r.id,
                }),
              o = 0,
              c = a.length;
            o < c;
            o++
          )
            delete this._model.data[a[o]];
          return (
            -1 !== E.inArray(this._data.core.focused, a) &&
              ((this._data.core.focused = null),
              (h = this.element[0].scrollTop),
              (s = this.element[0].scrollLeft),
              r.id === E.jstree.root
                ? this._model.data[E.jstree.root].children[0] &&
                  this.get_node(this._model.data[E.jstree.root].children[0], !0)
                    .children(".jstree-anchor")
                    .trigger("focus")
                : this.get_node(r, !0)
                    .children(".jstree-anchor")
                    .trigger("focus"),
              (this.element[0].scrollTop = h),
              (this.element[0].scrollLeft = s)),
            this.redraw_node(r, !0),
            !0
          );
        },
        check: function (e, t, i, r, s) {
          (t = t && t.id ? t : this.get_node(t)),
            (i = i && i.id ? i : this.get_node(i));
          var a = e.match(/^(move_node|copy_node|create_node)$/i) ? i : t,
            n = this.settings.core.check_callback;
          if ("move_node" === e || "copy_node" === e) {
            if (
              !(
                (s && s.is_multi) ||
                "move_node" !== e ||
                E.inArray(t.id, i.children) !== r
              )
            )
              return !(this._data.core.last_error = {
                error: "check",
                plugin: "core",
                id: "core_08",
                reason: "Moving node to its current position",
                data: JSON.stringify({
                  chk: e,
                  pos: r,
                  obj: !(!t || !t.id) && t.id,
                  par: !(!i || !i.id) && i.id,
                }),
              });
            if (
              !(
                (s && s.is_multi) ||
                (t.id !== i.id &&
                  ("move_node" !== e || E.inArray(t.id, i.children) !== r) &&
                  -1 === E.inArray(i.id, t.children_d))
              )
            )
              return !(this._data.core.last_error = {
                error: "check",
                plugin: "core",
                id: "core_01",
                reason: "Moving parent inside child",
                data: JSON.stringify({
                  chk: e,
                  pos: r,
                  obj: !(!t || !t.id) && t.id,
                  par: !(!i || !i.id) && i.id,
                }),
              });
          }
          return (a = a && a.data ? a.data : a) &&
            a.functions &&
            (!1 === a.functions[e] || !0 === a.functions[e])
            ? (!1 === a.functions[e] &&
                (this._data.core.last_error = {
                  error: "check",
                  plugin: "core",
                  id: "core_02",
                  reason: "Node data prevents function: " + e,
                  data: JSON.stringify({
                    chk: e,
                    pos: r,
                    obj: !(!t || !t.id) && t.id,
                    par: !(!i || !i.id) && i.id,
                  }),
                }),
              a.functions[e])
            : !(
                !1 === n ||
                (E.vakata.is_function(n) &&
                  !1 === n.call(this, e, t, i, r, s)) ||
                (n && !1 === n[e])
              ) ||
                !(this._data.core.last_error = {
                  error: "check",
                  plugin: "core",
                  id: "core_03",
                  reason:
                    "User config for core.check_callback prevents function: " +
                    e,
                  data: JSON.stringify({
                    chk: e,
                    pos: r,
                    obj: !(!t || !t.id) && t.id,
                    par: !(!i || !i.id) && i.id,
                  }),
                });
        },
        last_error: function () {
          return this._data.core.last_error;
        },
        move_node: function (e, t, i, r, s, a, n) {
          var d, o, c, l, h, _, g, u, f, p, m, v, j, k;
          if (((t = this.get_node(t)), (i = i === P ? 0 : i), !t)) return !1;
          if (
            !i.toString().match(/^(before|after)$/) &&
            !s &&
            !this.is_loaded(t)
          )
            return this.load_node(t, function () {
              this.move_node(e, t, i, r, !0, !1, n);
            });
          if (E.vakata.is_array(e)) {
            if (1 !== e.length) {
              for (d = 0, o = e.length; d < o; d++)
                (f = this.move_node(e[d], t, i, r, s, !1, n)) &&
                  ((t = f), (i = "after"));
              return this.redraw(), !0;
            }
            e = e[0];
          }
          if (
            !(e = e && e.id !== P ? e : this.get_node(e)) ||
            e.id === E.jstree.root
          )
            return !1;
          if (
            ((c = (e.parent || E.jstree.root).toString()),
            (h =
              i.toString().match(/^(before|after)$/) && t.id !== E.jstree.root
                ? this.get_node(t.parent)
                : t),
            (g =
              !(_ =
                n ||
                (this._model.data[e.id] ? this : E.jstree.reference(e.id))) ||
              !_._id ||
              this._id !== _._id),
            (l =
              _ && _._id && c && _._model.data[c] && _._model.data[c].children
                ? E.inArray(e.id, _._model.data[c].children)
                : -1),
            _ && _._id && (e = _._model.data[e.id]),
            g)
          )
            return (
              !!(f = this.copy_node(e, t, i, r, s, !1, n)) &&
              (_ && _.delete_node(e), f)
            );
          switch (
            (i =
              t.id === E.jstree.root &&
              "after" === (i = "before" === i ? "first" : i)
                ? "last"
                : i)
          ) {
            case "before":
              i = E.inArray(t.id, h.children);
              break;
            case "after":
              i = E.inArray(t.id, h.children) + 1;
              break;
            case "inside":
            case "first":
              i = 0;
              break;
            case "last":
              i = h.children.length;
              break;
            default:
              i = i || 0;
          }
          if (
            (i > h.children.length && (i = h.children.length),
            !this.check("move_node", e, h, i, {
              core: !0,
              origin: n,
              is_multi: _ && _._id && _._id !== this._id,
              is_foreign: !_ || !_._id,
            }))
          )
            return (
              this.settings.core.error.call(this, this._data.core.last_error),
              !1
            );
          if (e.parent === h.id) {
            for (
              u = h.children.concat(),
                -1 !== (f = E.inArray(e.id, u)) &&
                  ((u = E.vakata.array_remove(u, f)), f < i && i--),
                f = [],
                p = 0,
                m = u.length;
              p < m;
              p++
            )
              f[i <= p ? p + 1 : p] = u[p];
            (f[i] = e.id),
              (h.children = f),
              this._node_changed(h.id),
              this.redraw(h.id === E.jstree.root);
          } else {
            for (
              (f = e.children_d.concat()).push(e.id),
                p = 0,
                m = e.parents.length;
              p < m;
              p++
            ) {
              for (
                u = [],
                  v = 0,
                  j = (k = _._model.data[e.parents[p]].children_d).length;
                v < j;
                v++
              )
                -1 === E.inArray(k[v], f) && u.push(k[v]);
              _._model.data[e.parents[p]].children_d = u;
            }
            for (
              _._model.data[c].children = E.vakata.array_remove_item(
                _._model.data[c].children,
                e.id
              ),
                p = 0,
                m = h.parents.length;
              p < m;
              p++
            )
              this._model.data[h.parents[p]].children_d =
                this._model.data[h.parents[p]].children_d.concat(f);
            for (u = [], p = 0, m = h.children.length; p < m; p++)
              u[i <= p ? p + 1 : p] = h.children[p];
            for (
              u[i] = e.id,
                h.children = u,
                h.children_d.push(e.id),
                h.children_d = h.children_d.concat(e.children_d),
                e.parent = h.id,
                (f = h.parents.concat()).unshift(h.id),
                k = e.parents.length,
                f = (e.parents = f).concat(),
                p = 0,
                m = e.children_d.length;
              p < m;
              p++
            )
              (this._model.data[e.children_d[p]].parents = this._model.data[
                e.children_d[p]
              ].parents.slice(0, -1 * k)),
                Array.prototype.push.apply(
                  this._model.data[e.children_d[p]].parents,
                  f
                );
            (c !== E.jstree.root && h.id !== E.jstree.root) ||
              (this._model.force_full_redraw = !0),
              this._model.force_full_redraw ||
                (this._node_changed(c), this._node_changed(h.id)),
              a || this.redraw();
          }
          return (
            r && r.call(this, e, h, i),
            this.trigger("move_node", {
              node: e,
              parent: h.id,
              position: i,
              old_parent: c,
              old_position: l,
              is_multi: _ && _._id && _._id !== this._id,
              is_foreign: !_ || !_._id,
              old_instance: _,
              new_instance: this,
            }),
            e.id
          );
        },
        copy_node: function (e, t, i, r, s, a, n) {
          var d, o, c, l, h, _, g, u, f, p, m;
          if (((t = this.get_node(t)), (i = i === P ? 0 : i), !t)) return !1;
          if (
            !i.toString().match(/^(before|after)$/) &&
            !s &&
            !this.is_loaded(t)
          )
            return this.load_node(t, function () {
              this.copy_node(e, t, i, r, !0, !1, n);
            });
          if (E.vakata.is_array(e)) {
            if (1 !== e.length) {
              for (d = 0, o = e.length; d < o; d++)
                (l = this.copy_node(e[d], t, i, r, s, !0, n)) &&
                  ((t = l), (i = "after"));
              return this.redraw(), !0;
            }
            e = e[0];
          }
          if (
            !(e = e && e.id !== P ? e : this.get_node(e)) ||
            e.id === E.jstree.root
          )
            return !1;
          switch (
            ((u = (e.parent || E.jstree.root).toString()),
            (f =
              i.toString().match(/^(before|after)$/) && t.id !== E.jstree.root
                ? this.get_node(t.parent)
                : t),
            (m =
              !(p =
                n ||
                (this._model.data[e.id] ? this : E.jstree.reference(e.id))) ||
              !p._id ||
              this._id !== p._id),
            p && p._id && (e = p._model.data[e.id]),
            (i =
              t.id === E.jstree.root &&
              "after" === (i = "before" === i ? "first" : i)
                ? "last"
                : i))
          ) {
            case "before":
              i = E.inArray(t.id, f.children);
              break;
            case "after":
              i = E.inArray(t.id, f.children) + 1;
              break;
            case "inside":
            case "first":
              i = 0;
              break;
            case "last":
              i = f.children.length;
              break;
            default:
              i = i || 0;
          }
          if (
            (i > f.children.length && (i = f.children.length),
            !this.check("copy_node", e, f, i, {
              core: !0,
              origin: n,
              is_multi: p && p._id && p._id !== this._id,
              is_foreign: !p || !p._id,
            }))
          )
            return (
              this.settings.core.error.call(this, this._data.core.last_error),
              !1
            );
          if (
            !(g = p
              ? p.get_json(e, { no_id: !0, no_data: !0, no_state: !0 })
              : e)
          )
            return !1;
          if (
            (!0 === g.id && delete g.id,
            !(g = this._parse_model_from_json(g, f.id, f.parents.concat())))
          )
            return !1;
          for (
            l = this.get_node(g),
              e && e.state && !1 === e.state.loaded && (l.state.loaded = !1),
              (c = []).push(g),
              c = c.concat(l.children_d),
              this.trigger("model", { nodes: c, parent: f.id }),
              h = 0,
              _ = f.parents.length;
            h < _;
            h++
          )
            this._model.data[f.parents[h]].children_d =
              this._model.data[f.parents[h]].children_d.concat(c);
          for (c = [], h = 0, _ = f.children.length; h < _; h++)
            c[i <= h ? h + 1 : h] = f.children[h];
          return (
            (c[i] = l.id),
            (f.children = c),
            f.children_d.push(l.id),
            (f.children_d = f.children_d.concat(l.children_d)),
            f.id === E.jstree.root && (this._model.force_full_redraw = !0),
            this._model.force_full_redraw || this._node_changed(f.id),
            a || this.redraw(f.id === E.jstree.root),
            r && r.call(this, l, f, i),
            this.trigger("copy_node", {
              node: l,
              original: e,
              parent: f.id,
              position: i,
              old_parent: u,
              old_position:
                p && p._id && u && p._model.data[u] && p._model.data[u].children
                  ? E.inArray(e.id, p._model.data[u].children)
                  : -1,
              is_multi: p && p._id && p._id !== this._id,
              is_foreign: !p || !p._id,
              old_instance: p,
              new_instance: this,
            }),
            l.id
          );
        },
        cut: function (e) {
          if (
            ((e = e || this._data.core.selected.concat()),
            !(e = !E.vakata.is_array(e) ? [e] : e).length)
          )
            return !1;
          for (var t = [], i, r, s, r = 0, s = e.length; r < s; r++)
            (i = this.get_node(e[r])) &&
              (i.id || 0 === i.id) &&
              i.id !== E.jstree.root &&
              t.push(i);
          if (!t.length) return !1;
          (a = t), (n = "move_node"), (d = this).trigger("cut", { node: e });
        },
        copy: function (e) {
          if (
            ((e = e || this._data.core.selected.concat()),
            !(e = !E.vakata.is_array(e) ? [e] : e).length)
          )
            return !1;
          for (var t = [], i, r, s, r = 0, s = e.length; r < s; r++)
            (i = this.get_node(e[r])) &&
              i.id !== P &&
              i.id !== E.jstree.root &&
              t.push(i);
          if (!t.length) return !1;
          (a = t), (n = "copy_node"), (d = this).trigger("copy", { node: e });
        },
        get_buffer: function () {
          return { mode: n, node: a, inst: d };
        },
        can_paste: function () {
          return !1 !== n && !1 !== a;
        },
        paste: function (e, t) {
          if (
            !(
              (e = this.get_node(e)) &&
              n &&
              n.match(/^(copy_node|move_node)$/) &&
              a
            )
          )
            return !1;
          this[n](a, e, t, !1, !1, !1, d) &&
            this.trigger("paste", { parent: e.id, node: a, mode: n }),
            (d = n = a = !1);
        },
        clear_buffer: function () {
          (d = n = a = !1), this.trigger("clear_buffer");
        },
        edit: function (r, e, s) {
          var t,
            i,
            a,
            n,
            d,
            o,
            c,
            t,
            l,
            h = !1;
          return (
            !!(r = this.get_node(r)) &&
            (this.check("edit", r, this.get_parent(r))
              ? ((l = r),
                (e = "string" == typeof e ? e : r.text),
                this.set_text(r, ""),
                (r = this._open_to(r)),
                (l.text = e),
                (t = this._data.core.rtl),
                (i = this.element.width()),
                (this._data.core.focused = l.id),
                (a = r.children(".jstree-anchor").trigger("focus")),
                (n = E("<span></span>")),
                (d = e),
                (o = E("<div></div>", {
                  css: {
                    position: "absolute",
                    top: "-200px",
                    left: t ? "0px" : "-1000px",
                    visibility: "hidden",
                  },
                }).appendTo(b.body)),
                (c = E("<input />", {
                  value: d,
                  class: "jstree-rename-input",
                  css: {
                    padding: "0",
                    border: "1px solid silver",
                    "box-sizing": "border-box",
                    display: "inline-block",
                    height: this._data.core.li_height + "px",
                    lineHeight: this._data.core.li_height + "px",
                    width: "150px",
                  },
                  blur: function (e) {
                    e.stopImmediatePropagation(), e.preventDefault();
                    var t,
                      i = n.children(".jstree-rename-input").val(),
                      e = this.settings.core.force_text,
                      e;
                    "" === i && (i = d),
                      o.remove(),
                      n.replaceWith(a),
                      n.remove(),
                      (d = e
                        ? d
                        : E("<div></div>").append(E.parseHTML(d)).html()),
                      (r = this.get_node(r)),
                      this.set_text(r, d),
                      (e = !!this.rename_node(
                        r,
                        e
                          ? E("<div></div>").text(i).text()
                          : E("<div></div>").append(E.parseHTML(i)).html()
                      )) || this.set_text(r, d),
                      (this._data.core.focused = l.id),
                      setTimeout(
                        function () {
                          var e = this.get_node(l.id, !0);
                          e.length &&
                            ((this._data.core.focused = l.id),
                            e.children(".jstree-anchor").trigger("focus"));
                        }.bind(this),
                        0
                      ),
                      s && s.call(this, l, e, h, i),
                      (c = null);
                  }.bind(this),
                  keydown: function (e) {
                    var t = e.which;
                    27 === t && ((h = !0), (this.value = d)),
                      (27 !== t &&
                        13 !== t &&
                        37 !== t &&
                        38 !== t &&
                        39 !== t &&
                        40 !== t &&
                        32 !== t) ||
                        e.stopImmediatePropagation(),
                      (27 !== t && 13 !== t) ||
                        (e.preventDefault(), this.blur());
                  },
                  click: function (e) {
                    e.stopImmediatePropagation();
                  },
                  mousedown: function (e) {
                    e.stopImmediatePropagation();
                  },
                  keyup: function (e) {
                    c.width(Math.min(o.text("pW" + this.value).width(), i));
                  },
                  keypress: function (e) {
                    if (13 === e.which) return !1;
                  },
                })),
                (t = {
                  fontFamily: a.css("fontFamily") || "",
                  fontSize: a.css("fontSize") || "",
                  fontWeight: a.css("fontWeight") || "",
                  fontStyle: a.css("fontStyle") || "",
                  fontStretch: a.css("fontStretch") || "",
                  fontVariant: a.css("fontVariant") || "",
                  letterSpacing: a.css("letterSpacing") || "",
                  wordSpacing: a.css("wordSpacing") || "",
                }),
                n
                  .attr("class", a.attr("class"))
                  .append(a.contents().clone())
                  .append(c),
                a.replaceWith(n),
                o.css(t),
                c
                  .css(t)
                  .width(Math.min(o.text("pW" + c[0].value).width(), i))[0]
                  .select(),
                void E(b).one(
                  "mousedown.jstree touchstart.jstree dnd_start.vakata",
                  function (e) {
                    c && e.target !== c && E(c).trigger("blur");
                  }
                ))
              : (this.settings.core.error.call(
                  this,
                  this._data.core.last_error
                ),
                !1))
          );
        },
        set_theme: function (e, t) {
          if (!e) return !1;
          var i, i;
          (t =
            !0 === t
              ? (i =
                  (i = this.settings.core.themes.dir) ||
                  E.jstree.path + "/themes") +
                "/" +
                e +
                "/style.css"
              : t) &&
            -1 === E.inArray(t, r) &&
            (E("head").append(
              '<link rel="stylesheet" href="' + t + '" type="text/css" />'
            ),
            r.push(t)),
            this._data.core.themes.name &&
              this.element.removeClass("jstree-" + this._data.core.themes.name),
            (this._data.core.themes.name = e),
            this.element.addClass("jstree-" + e),
            this.element[
              this.settings.core.themes.responsive ? "addClass" : "removeClass"
            ]("jstree-" + e + "-responsive"),
            this.trigger("set_theme", { theme: e });
        },
        get_theme: function () {
          return this._data.core.themes.name;
        },
        set_theme_variant: function (e) {
          this._data.core.themes.variant &&
            this.element.removeClass(
              "jstree-" +
                this._data.core.themes.name +
                "-" +
                this._data.core.themes.variant
            ),
            (this._data.core.themes.variant = e) &&
              this.element.addClass(
                "jstree-" +
                  this._data.core.themes.name +
                  "-" +
                  this._data.core.themes.variant
              );
        },
        get_theme_variant: function () {
          return this._data.core.themes.variant;
        },
        show_stripes: function () {
          (this._data.core.themes.stripes = !0),
            this.get_container_ul().addClass("jstree-striped"),
            this.trigger("show_stripes");
        },
        hide_stripes: function () {
          (this._data.core.themes.stripes = !1),
            this.get_container_ul().removeClass("jstree-striped"),
            this.trigger("hide_stripes");
        },
        toggle_stripes: function () {
          this._data.core.themes.stripes
            ? this.hide_stripes()
            : this.show_stripes();
        },
        show_dots: function () {
          (this._data.core.themes.dots = !0),
            this.get_container_ul().removeClass("jstree-no-dots"),
            this.trigger("show_dots");
        },
        hide_dots: function () {
          (this._data.core.themes.dots = !1),
            this.get_container_ul().addClass("jstree-no-dots"),
            this.trigger("hide_dots");
        },
        toggle_dots: function () {
          this._data.core.themes.dots ? this.hide_dots() : this.show_dots();
        },
        show_icons: function () {
          (this._data.core.themes.icons = !0),
            this.get_container_ul().removeClass("jstree-no-icons"),
            this.trigger("show_icons");
        },
        hide_icons: function () {
          (this._data.core.themes.icons = !1),
            this.get_container_ul().addClass("jstree-no-icons"),
            this.trigger("hide_icons");
        },
        toggle_icons: function () {
          this._data.core.themes.icons ? this.hide_icons() : this.show_icons();
        },
        show_ellipsis: function () {
          (this._data.core.themes.ellipsis = !0),
            this.get_container_ul().addClass("jstree-ellipsis"),
            this.trigger("show_ellipsis");
        },
        hide_ellipsis: function () {
          (this._data.core.themes.ellipsis = !1),
            this.get_container_ul().removeClass("jstree-ellipsis"),
            this.trigger("hide_ellipsis");
        },
        toggle_ellipsis: function () {
          this._data.core.themes.ellipsis
            ? this.hide_ellipsis()
            : this.show_ellipsis();
        },
        set_icon: function (e, t) {
          var i, r, s, a;
          if (E.vakata.is_array(e)) {
            for (i = 0, r = (e = e.slice()).length; i < r; i++)
              this.set_icon(e[i], t);
            return !0;
          }
          return (
            !(!(e = this.get_node(e)) || e.id === E.jstree.root) &&
            ((a = e.icon),
            (e.icon = !0 === t || null === t || t === P || "" === t || t),
            (s = this.get_node(e, !0)
              .children(".jstree-anchor")
              .children(".jstree-themeicon")),
            !1 === t
              ? (s
                  .removeClass("jstree-themeicon-custom " + a)
                  .css("background", "")
                  .removeAttr("rel"),
                this.hide_icon(e))
              : (!0 === t || null === t || t === P || "" === t
                  ? s
                      .removeClass("jstree-themeicon-custom " + a)
                      .css("background", "")
                      .removeAttr("rel")
                  : -1 === t.indexOf("/") && -1 === t.indexOf(".")
                  ? (s.removeClass(a).css("background", ""),
                    s.addClass(t + " jstree-themeicon-custom").attr("rel", t))
                  : (s.removeClass(a).css("background", ""),
                    s
                      .addClass("jstree-themeicon-custom")
                      .css(
                        "background",
                        "url('" + t + "') center center no-repeat"
                      )
                      .attr("rel", t)),
                !1 === a && this.show_icon(e)),
            !0)
          );
        },
        get_icon: function (e) {
          return !(!(e = this.get_node(e)) || e.id === E.jstree.root) && e.icon;
        },
        hide_icon: function (e) {
          var t, i;
          if (E.vakata.is_array(e)) {
            for (t = 0, i = (e = e.slice()).length; t < i; t++)
              this.hide_icon(e[t]);
            return !0;
          }
          return (
            !(!(e = this.get_node(e)) || e === E.jstree.root) &&
            ((e.icon = !1),
            this.get_node(e, !0)
              .children(".jstree-anchor")
              .children(".jstree-themeicon")
              .addClass("jstree-themeicon-hidden"),
            !0)
          );
        },
        show_icon: function (e) {
          var t, i, r;
          if (E.vakata.is_array(e)) {
            for (t = 0, i = (e = e.slice()).length; t < i; t++)
              this.show_icon(e[t]);
            return !0;
          }
          return (
            !(!(e = this.get_node(e)) || e === E.jstree.root) &&
            ((r = this.get_node(e, !0)),
            (e.icon =
              !r.length ||
              r
                .children(".jstree-anchor")
                .children(".jstree-themeicon")
                .attr("rel")),
            e.icon || (e.icon = !0),
            r
              .children(".jstree-anchor")
              .children(".jstree-themeicon")
              .removeClass("jstree-themeicon-hidden"),
            !0)
          );
        },
      }),
      (E.vakata = {}),
      (E.vakata.attributes = function (e, i) {
        e = E(e)[0];
        var r = i ? {} : [];
        return (
          e &&
            e.attributes &&
            E.each(e.attributes, function (e, t) {
              -1 ===
                E.inArray(t.name.toLowerCase(), [
                  "style",
                  "contenteditable",
                  "hasfocus",
                  "tabindex",
                ]) &&
                null !== t.value &&
                "" !== E.vakata.trim(t.value) &&
                (i ? (r[t.name] = t.value) : r.push(t.name));
            }),
          r
        );
      }),
      (E.vakata.array_unique = function (e) {
        for (var t = [], i, r, s, a = {}, i = 0, s = e.length; i < s; i++)
          a[e[i]] === P && (t.push(e[i]), (a[e[i]] = !0));
        return t;
      }),
      (E.vakata.array_remove = function (e, t) {
        return e.splice(t, 1), e;
      }),
      (E.vakata.array_remove_item = function (e, t) {
        var t = E.inArray(t, e);
        return -1 !== t ? E.vakata.array_remove(e, t) : e;
      }),
      (E.vakata.array_filter = function (e, t, i, r, s) {
        if (e.filter) return e.filter(t, i);
        for (s in ((r = []), e))
          ~~s + "" == s + "" &&
            0 <= s &&
            t.call(i, e[s], +s, e) &&
            r.push(e[s]);
        return r;
      }),
      (E.vakata.trim = function (e) {
        return String.prototype.trim
          ? String.prototype.trim.call(e.toString())
          : e.toString().replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, "");
      }),
      (E.vakata.is_function = function (e) {
        return "function" == typeof e && "number" != typeof e.nodeType;
      }),
      (E.vakata.is_array =
        Array.isArray ||
        function (e) {
          return "[object Array]" === Object.prototype.toString.call(e);
        }),
      Function.prototype.bind ||
        (Function.prototype.bind = function () {
          var t = this,
            i = arguments[0],
            r = Array.prototype.slice.call(arguments, 1);
          if ("function" != typeof t)
            throw new TypeError(
              "Function.prototype.bind - what is trying to be bound is not callable"
            );
          return function () {
            var e = r.concat(Array.prototype.slice.call(arguments));
            return t.apply(i, e);
          };
        }),
      (E.jstree.plugins.changed = function (e, a) {
        var n = [];
        (this.trigger = function (e, t) {
          var i, r;
          if (((t = t || {}), "changed" === e.replace(".jstree", ""))) {
            t.changed = { selected: [], deselected: [] };
            for (var s = {}, i = 0, r = n.length; i < r; i++) s[n[i]] = 1;
            for (i = 0, r = t.selected.length; i < r; i++)
              s[t.selected[i]]
                ? (s[t.selected[i]] = 2)
                : t.changed.selected.push(t.selected[i]);
            for (i = 0, r = n.length; i < r; i++)
              1 === s[n[i]] && t.changed.deselected.push(n[i]);
            n = t.selected.slice();
          }
          a.trigger.call(this, e, t);
        }),
          (this.refresh = function (e, t) {
            return (n = []), a.refresh.apply(this, arguments);
          });
      });
    var l = b.createElement("I"),
      h,
      _,
      g,
      o,
      u,
      f,
      p,
      w;
    (l.className = "jstree-icon jstree-checkbox"),
      l.setAttribute("role", "presentation"),
      (E.jstree.defaults.checkbox = {
        visible: !0,
        three_state: !0,
        whole_node: !0,
        keep_selected_style: !0,
        cascade: "",
        tie_selection: !0,
        cascade_to_disabled: !0,
        cascade_to_hidden: !0,
      }),
      (E.jstree.plugins.checkbox = function (e, o) {
        (this.bind = function () {
          o.bind.call(this),
            (this._data.checkbox.uto = !1),
            (this._data.checkbox.selected = []),
            this.settings.checkbox.three_state &&
              (this.settings.checkbox.cascade = "up+down+undetermined"),
            this.element
              .on(
                "init.jstree",
                function () {
                  (this._data.checkbox.visible =
                    this.settings.checkbox.visible),
                    this.settings.checkbox.keep_selected_style ||
                      this.element.addClass("jstree-checkbox-no-clicked"),
                    this.settings.checkbox.tie_selection &&
                      this.element.addClass("jstree-checkbox-selection");
                }.bind(this)
              )
              .on(
                "loading.jstree",
                function () {
                  this[
                    this._data.checkbox.visible
                      ? "show_checkboxes"
                      : "hide_checkboxes"
                  ]();
                }.bind(this)
              ),
            -1 !== this.settings.checkbox.cascade.indexOf("undetermined") &&
              this.element.on(
                "changed.jstree uncheck_node.jstree check_node.jstree uncheck_all.jstree check_all.jstree move_node.jstree copy_node.jstree redraw.jstree open_node.jstree",
                function () {
                  this._data.checkbox.uto &&
                    clearTimeout(this._data.checkbox.uto),
                    (this._data.checkbox.uto = setTimeout(
                      this._undetermined.bind(this),
                      50
                    ));
                }.bind(this)
              ),
            this.settings.checkbox.tie_selection ||
              this.element.on(
                "model.jstree",
                function (e, t) {
                  for (
                    var i = this._model.data,
                      r = i[t.parent],
                      s = t.nodes,
                      a,
                      n,
                      a = 0,
                      n = s.length;
                    a < n;
                    a++
                  )
                    (i[s[a]].state.checked =
                      i[s[a]].state.checked ||
                      (i[s[a]].original &&
                        i[s[a]].original.state &&
                        i[s[a]].original.state.checked)),
                      i[s[a]].state.checked &&
                        this._data.checkbox.selected.push(s[a]);
                }.bind(this)
              ),
            (-1 === this.settings.checkbox.cascade.indexOf("up") &&
              -1 === this.settings.checkbox.cascade.indexOf("down")) ||
              this.element
                .on(
                  "model.jstree",
                  function (e, t) {
                    var i = this._model.data,
                      r = i[t.parent],
                      s = t.nodes,
                      a = [],
                      n,
                      d,
                      o,
                      c,
                      l,
                      h,
                      t = this.settings.checkbox.cascade,
                      _ = this.settings.checkbox.tie_selection;
                    if (-1 !== t.indexOf("down"))
                      if (r.state[_ ? "selected" : "checked"]) {
                        for (d = 0, o = s.length; d < o; d++)
                          i[s[d]].state[_ ? "selected" : "checked"] = !0;
                        this._data[_ ? "core" : "checkbox"].selected =
                          this._data[_ ? "core" : "checkbox"].selected.concat(
                            s
                          );
                      } else
                        for (d = 0, o = s.length; d < o; d++)
                          if (i[s[d]].state[_ ? "selected" : "checked"]) {
                            for (
                              c = 0, l = i[s[d]].children_d.length;
                              c < l;
                              c++
                            )
                              i[i[s[d]].children_d[c]].state[
                                _ ? "selected" : "checked"
                              ] = !0;
                            this._data[_ ? "core" : "checkbox"].selected =
                              this._data[
                                _ ? "core" : "checkbox"
                              ].selected.concat(i[s[d]].children_d);
                          }
                    if (-1 !== t.indexOf("up")) {
                      for (d = 0, o = r.children_d.length; d < o; d++)
                        i[r.children_d[d]].children.length ||
                          a.push(i[r.children_d[d]].parent);
                      for (
                        c = 0, l = (a = E.vakata.array_unique(a)).length;
                        c < l;
                        c++
                      ) {
                        r = i[a[c]];
                        while (r && r.id !== E.jstree.root) {
                          for (d = n = 0, o = r.children.length; d < o; d++)
                            n +=
                              i[r.children[d]].state[
                                _ ? "selected" : "checked"
                              ];
                          if (n !== o) break;
                          (r.state[_ ? "selected" : "checked"] = !0),
                            this._data[_ ? "core" : "checkbox"].selected.push(
                              r.id
                            ),
                            (h = this.get_node(r, !0)) &&
                              h.length &&
                              h
                                .children(".jstree-anchor")
                                .attr("aria-selected", !0)
                                .addClass(
                                  _ ? "jstree-clicked" : "jstree-checked"
                                ),
                            (r = this.get_node(r.parent));
                        }
                      }
                    }
                    this._data[_ ? "core" : "checkbox"].selected =
                      E.vakata.array_unique(
                        this._data[_ ? "core" : "checkbox"].selected
                      );
                  }.bind(this)
                )
                .on(
                  this.settings.checkbox.tie_selection
                    ? "select_node.jstree"
                    : "check_node.jstree",
                  function (e, t) {
                    var i = this,
                      r = t.node,
                      s = this._model.data,
                      a = this.get_node(r.parent),
                      n,
                      d,
                      o,
                      c,
                      t = this.settings.checkbox.cascade,
                      l = this.settings.checkbox.tie_selection,
                      h = {},
                      _ = this._data[l ? "core" : "checkbox"].selected;
                    for (n = 0, d = _.length; n < d; n++) h[_[n]] = !0;
                    if (-1 !== t.indexOf("down"))
                      for (
                        var g = this._cascade_new_checked_state(r.id, !0),
                          u = r.children_d.concat(r.id),
                          n = 0,
                          d = u.length;
                        n < d;
                        n++
                      )
                        -1 < g.indexOf(u[n]) ? (h[u[n]] = !0) : delete h[u[n]];
                    if (-1 !== t.indexOf("up"))
                      while (a && a.id !== E.jstree.root) {
                        for (n = o = 0, d = a.children.length; n < d; n++)
                          o +=
                            s[a.children[n]].state[l ? "selected" : "checked"];
                        if (o !== d) break;
                        (a.state[l ? "selected" : "checked"] = !0),
                          (h[a.id] = !0),
                          (c = this.get_node(a, !0)) &&
                            c.length &&
                            c
                              .children(".jstree-anchor")
                              .attr("aria-selected", !0)
                              .addClass(
                                l ? "jstree-clicked" : "jstree-checked"
                              ),
                          (a = this.get_node(a.parent));
                      }
                    for (n in ((_ = []), h)) h.hasOwnProperty(n) && _.push(n);
                    this._data[l ? "core" : "checkbox"].selected = _;
                  }.bind(this)
                )
                .on(
                  this.settings.checkbox.tie_selection
                    ? "deselect_all.jstree"
                    : "uncheck_all.jstree",
                  function (e, t) {
                    for (
                      var i = this.get_node(E.jstree.root),
                        r = this._model.data,
                        s,
                        a,
                        n,
                        s = 0,
                        a = i.children_d.length;
                      s < a;
                      s++
                    )
                      (n = r[i.children_d[s]]) &&
                        n.original &&
                        n.original.state &&
                        n.original.state.undetermined &&
                        (n.original.state.undetermined = !1);
                  }.bind(this)
                )
                .on(
                  this.settings.checkbox.tie_selection
                    ? "deselect_node.jstree"
                    : "uncheck_node.jstree",
                  function (e, t) {
                    var i = this,
                      r = t.node,
                      s = this.get_node(r, !0),
                      a,
                      n,
                      d,
                      o = this.settings.checkbox.cascade,
                      c = this.settings.checkbox.tie_selection,
                      t = this._data[c ? "core" : "checkbox"].selected,
                      l = {},
                      h = [],
                      _ = r.children_d.concat(r.id),
                      g,
                      t;
                    if (
                      (-1 !== o.indexOf("down") &&
                        ((g = this._cascade_new_checked_state(r.id, !1)),
                        (t = E.vakata.array_filter(t, function (e) {
                          return -1 === _.indexOf(e) || -1 < g.indexOf(e);
                        }))),
                      -1 !== o.indexOf("up") && -1 === t.indexOf(r.id))
                    ) {
                      for (a = 0, n = r.parents.length; a < n; a++)
                        ((d = this._model.data[r.parents[a]]).state[
                          c ? "selected" : "checked"
                        ] = !1),
                          d &&
                            d.original &&
                            d.original.state &&
                            d.original.state.undetermined &&
                            (d.original.state.undetermined = !1),
                          (d = this.get_node(r.parents[a], !0)) &&
                            d.length &&
                            d
                              .children(".jstree-anchor")
                              .attr("aria-selected", !1)
                              .removeClass(
                                c ? "jstree-clicked" : "jstree-checked"
                              );
                      t = E.vakata.array_filter(t, function (e) {
                        return -1 === r.parents.indexOf(e);
                      });
                    }
                    this._data[c ? "core" : "checkbox"].selected = t;
                  }.bind(this)
                ),
            -1 !== this.settings.checkbox.cascade.indexOf("up") &&
              this.element
                .on(
                  "delete_node.jstree",
                  function (e, t) {
                    var i = this.get_node(t.parent),
                      r = this._model.data,
                      s,
                      a,
                      n,
                      d,
                      o = this.settings.checkbox.tie_selection;
                    while (
                      i &&
                      i.id !== E.jstree.root &&
                      !i.state[o ? "selected" : "checked"]
                    ) {
                      for (s = n = 0, a = i.children.length; s < a; s++)
                        n += r[i.children[s]].state[o ? "selected" : "checked"];
                      if (!(0 < a && n === a)) break;
                      (i.state[o ? "selected" : "checked"] = !0),
                        this._data[o ? "core" : "checkbox"].selected.push(i.id),
                        (d = this.get_node(i, !0)) &&
                          d.length &&
                          d
                            .children(".jstree-anchor")
                            .attr("aria-selected", !0)
                            .addClass(o ? "jstree-clicked" : "jstree-checked"),
                        (i = this.get_node(i.parent));
                    }
                  }.bind(this)
                )
                .on(
                  "move_node.jstree",
                  function (e, t) {
                    var i = t.is_multi,
                      r = t.old_parent,
                      t = this.get_node(t.parent),
                      s = this._model.data,
                      a,
                      n,
                      d,
                      o,
                      c,
                      l = this.settings.checkbox.tie_selection;
                    if (!i) {
                      a = this.get_node(r);
                      while (
                        a &&
                        a.id !== E.jstree.root &&
                        !a.state[l ? "selected" : "checked"]
                      ) {
                        for (d = n = 0, o = a.children.length; d < o; d++)
                          n +=
                            s[a.children[d]].state[l ? "selected" : "checked"];
                        if (!(0 < o && n === o)) break;
                        (a.state[l ? "selected" : "checked"] = !0),
                          this._data[l ? "core" : "checkbox"].selected.push(
                            a.id
                          ),
                          (c = this.get_node(a, !0)) &&
                            c.length &&
                            c
                              .children(".jstree-anchor")
                              .attr("aria-selected", !0)
                              .addClass(
                                l ? "jstree-clicked" : "jstree-checked"
                              ),
                          (a = this.get_node(a.parent));
                      }
                    }
                    a = t;
                    while (a && a.id !== E.jstree.root) {
                      for (d = n = 0, o = a.children.length; d < o; d++)
                        n += s[a.children[d]].state[l ? "selected" : "checked"];
                      if (n === o)
                        a.state[l ? "selected" : "checked"] ||
                          ((a.state[l ? "selected" : "checked"] = !0),
                          this._data[l ? "core" : "checkbox"].selected.push(
                            a.id
                          ),
                          (c = this.get_node(a, !0)) &&
                            c.length &&
                            c
                              .children(".jstree-anchor")
                              .attr("aria-selected", !0)
                              .addClass(
                                l ? "jstree-clicked" : "jstree-checked"
                              ));
                      else {
                        if (!a.state[l ? "selected" : "checked"]) break;
                        (a.state[l ? "selected" : "checked"] = !1),
                          (this._data[l ? "core" : "checkbox"].selected =
                            E.vakata.array_remove_item(
                              this._data[l ? "core" : "checkbox"].selected,
                              a.id
                            )),
                          (c = this.get_node(a, !0)) &&
                            c.length &&
                            c
                              .children(".jstree-anchor")
                              .attr("aria-selected", !1)
                              .removeClass(
                                l ? "jstree-clicked" : "jstree-checked"
                              );
                      }
                      a = this.get_node(a.parent);
                    }
                  }.bind(this)
                );
        }),
          (this.get_undetermined = function (e) {
            if (-1 === this.settings.checkbox.cascade.indexOf("undetermined"))
              return [];
            for (
              var i,
                r,
                s,
                a,
                n = {},
                d = this._model.data,
                t = this.settings.checkbox.tie_selection,
                o = this._data[t ? "core" : "checkbox"].selected,
                c = [],
                l = this,
                h = [],
                i = 0,
                r = o.length;
              i < r;
              i++
            )
              if (d[o[i]] && d[o[i]].parents)
                for (s = 0, a = d[o[i]].parents.length; s < a; s++) {
                  if (n[d[o[i]].parents[s]] !== P) break;
                  d[o[i]].parents[s] !== E.jstree.root &&
                    ((n[d[o[i]].parents[s]] = !0), c.push(d[o[i]].parents[s]));
                }
            for (
              this.element
                .find(".jstree-closed")
                .not(":has(.jstree-children)")
                .each(function () {
                  var e = l.get_node(this),
                    t;
                  if (e)
                    if (e.state.loaded) {
                      for (i = 0, r = e.children_d.length; i < r; i++)
                        if (
                          !(t = d[e.children_d[i]]).state.loaded &&
                          t.original &&
                          t.original.state &&
                          t.original.state.undetermined &&
                          !0 === t.original.state.undetermined
                        )
                          for (
                            n[t.id] === P &&
                              t.id !== E.jstree.root &&
                              ((n[t.id] = !0), c.push(t.id)),
                              s = 0,
                              a = t.parents.length;
                            s < a;
                            s++
                          )
                            n[t.parents[s]] === P &&
                              t.parents[s] !== E.jstree.root &&
                              ((n[t.parents[s]] = !0), c.push(t.parents[s]));
                    } else if (
                      e.original &&
                      e.original.state &&
                      e.original.state.undetermined &&
                      !0 === e.original.state.undetermined
                    )
                      for (
                        n[e.id] === P &&
                          e.id !== E.jstree.root &&
                          ((n[e.id] = !0), c.push(e.id)),
                          s = 0,
                          a = e.parents.length;
                        s < a;
                        s++
                      )
                        n[e.parents[s]] === P &&
                          e.parents[s] !== E.jstree.root &&
                          ((n[e.parents[s]] = !0), c.push(e.parents[s]));
                }),
                i = 0,
                r = c.length;
              i < r;
              i++
            )
              d[c[i]].state[t ? "selected" : "checked"] ||
                h.push(e ? d[c[i]] : c[i]);
            return h;
          }),
          (this._undetermined = function () {
            if (null !== this.element) {
              var e = this.get_undetermined(!1),
                t,
                i,
                r;
              for (
                this.element
                  .find(".jstree-undetermined")
                  .removeClass("jstree-undetermined"),
                  t = 0,
                  i = e.length;
                t < i;
                t++
              )
                (r = this.get_node(e[t], !0)) &&
                  r.length &&
                  r
                    .children(".jstree-anchor")
                    .children(".jstree-checkbox")
                    .addClass("jstree-undetermined");
            }
          }),
          (this.redraw_node = function (e, t, i, r) {
            if ((e = o.redraw_node.apply(this, arguments))) {
              for (
                var s, a, n = null, d = null, s = 0, a = e.childNodes.length;
                s < a;
                s++
              )
                if (
                  e.childNodes[s] &&
                  e.childNodes[s].className &&
                  -1 !== e.childNodes[s].className.indexOf("jstree-anchor")
                ) {
                  n = e.childNodes[s];
                  break;
                }
              n &&
                (!this.settings.checkbox.tie_selection &&
                  this._model.data[e.id].state.checked &&
                  (n.className += " jstree-checked"),
                (d = l.cloneNode(!1)),
                this._model.data[e.id].state.checkbox_disabled &&
                  (d.className += " jstree-checkbox-disabled"),
                n.insertBefore(d, n.childNodes[0]));
            }
            return (
              i ||
                -1 === this.settings.checkbox.cascade.indexOf("undetermined") ||
                (this._data.checkbox.uto &&
                  clearTimeout(this._data.checkbox.uto),
                (this._data.checkbox.uto = setTimeout(
                  this._undetermined.bind(this),
                  50
                ))),
              e
            );
          }),
          (this.show_checkboxes = function () {
            (this._data.core.themes.checkboxes = !0),
              this.get_container_ul().removeClass("jstree-no-checkboxes");
          }),
          (this.hide_checkboxes = function () {
            (this._data.core.themes.checkboxes = !1),
              this.get_container_ul().addClass("jstree-no-checkboxes");
          }),
          (this.toggle_checkboxes = function () {
            this._data.core.themes.checkboxes
              ? this.hide_checkboxes()
              : this.show_checkboxes();
          }),
          (this.is_undetermined = function (e) {
            e = this.get_node(e);
            var t = this.settings.checkbox.cascade,
              i,
              r,
              s = this.settings.checkbox.tie_selection,
              a = this._data[s ? "core" : "checkbox"].selected,
              n = this._model.data;
            if (
              !e ||
              !0 === e.state[s ? "selected" : "checked"] ||
              -1 === t.indexOf("undetermined") ||
              (-1 === t.indexOf("down") && -1 === t.indexOf("up"))
            )
              return !1;
            if (!e.state.loaded && !0 === e.original.state.undetermined)
              return !0;
            for (i = 0, r = e.children_d.length; i < r; i++)
              if (
                -1 !== E.inArray(e.children_d[i], a) ||
                (!n[e.children_d[i]].state.loaded &&
                  n[e.children_d[i]].original.state.undetermined)
              )
                return !0;
            return !1;
          }),
          (this.disable_checkbox = function (e) {
            var t, i, r;
            if (E.vakata.is_array(e)) {
              for (t = 0, i = (e = e.slice()).length; t < i; t++)
                this.disable_checkbox(e[t]);
              return !0;
            }
            if (!(e = this.get_node(e)) || e.id === E.jstree.root) return !1;
            (r = this.get_node(e, !0)),
              e.state.checkbox_disabled ||
                ((e.state.checkbox_disabled = !0),
                r &&
                  r.length &&
                  r
                    .children(".jstree-anchor")
                    .children(".jstree-checkbox")
                    .addClass("jstree-checkbox-disabled"),
                this.trigger("disable_checkbox", { node: e }));
          }),
          (this.enable_checkbox = function (e) {
            var t, i, r;
            if (E.vakata.is_array(e)) {
              for (t = 0, i = (e = e.slice()).length; t < i; t++)
                this.enable_checkbox(e[t]);
              return !0;
            }
            if (!(e = this.get_node(e)) || e.id === E.jstree.root) return !1;
            (r = this.get_node(e, !0)),
              e.state.checkbox_disabled &&
                ((e.state.checkbox_disabled = !1),
                r &&
                  r.length &&
                  r
                    .children(".jstree-anchor")
                    .children(".jstree-checkbox")
                    .removeClass("jstree-checkbox-disabled"),
                this.trigger("enable_checkbox", { node: e }));
          }),
          (this.activate_node = function (e, t) {
            return (
              !E(t.target).hasClass("jstree-checkbox-disabled") &&
              (this.settings.checkbox.tie_selection &&
                (this.settings.checkbox.whole_node ||
                  E(t.target).hasClass("jstree-checkbox")) &&
                (t.ctrlKey = !0),
              this.settings.checkbox.tie_selection ||
              (!this.settings.checkbox.whole_node &&
                !E(t.target).hasClass("jstree-checkbox"))
                ? o.activate_node.call(this, e, t)
                : !this.is_disabled(e) &&
                  (this.is_checked(e)
                    ? this.uncheck_node(e, t)
                    : this.check_node(e, t),
                  void this.trigger("activate_node", {
                    node: this.get_node(e),
                  })))
            );
          }),
          (this.delete_node = function (e) {
            if (this.settings.checkbox.tie_selection || E.vakata.is_array(e))
              return o.delete_node.call(this, e);
            var t,
              i,
              r,
              s = !1;
            if (!(e = this.get_node(e)) || e.id === E.jstree.root) return !1;
            for (
              (t = e.children_d.concat([])).push(e.id), i = 0, r = t.length;
              i < r;
              i++
            )
              if (this._model.data[t[i]].state.checked) {
                s = !0;
                break;
              }
            return (
              s &&
                (this._data.checkbox.selected = E.vakata.array_filter(
                  this._data.checkbox.selected,
                  function (e) {
                    return -1 === E.inArray(e, t);
                  }
                )),
              o.delete_node.call(this, e)
            );
          }),
          (this._cascade_new_checked_state = function (e, t) {
            var i = this,
              r = this.settings.checkbox.tie_selection,
              s = this._model.data[e],
              a = [],
              n = [],
              d,
              o,
              c;
            if (
              (!this.settings.checkbox.cascade_to_disabled &&
                s.state.disabled) ||
              (!this.settings.checkbox.cascade_to_hidden && s.state.hidden)
            )
              (c = this.get_checked_descendants(e)),
                s.state[r ? "selected" : "checked"] && c.push(s.id),
                (a = a.concat(c));
            else {
              if (s.children)
                for (d = 0, o = s.children.length; d < o; d++) {
                  var l = s.children[d],
                    c = i._cascade_new_checked_state(l, t),
                    a = a.concat(c);
                  -1 < c.indexOf(l) && n.push(l);
                }
              var h = i.get_node(s, !0),
                e = 0 < n.length && n.length < s.children.length;
              s.original &&
                s.original.state &&
                s.original.state.undetermined &&
                (s.original.state.undetermined = e),
                !e && t && n.length === s.children.length
                  ? ((s.state[r ? "selected" : "checked"] = t),
                    a.push(s.id),
                    h
                      .children(".jstree-anchor")
                      .attr("aria-selected", !0)
                      .addClass(r ? "jstree-clicked" : "jstree-checked"))
                  : ((s.state[r ? "selected" : "checked"] = !1),
                    h
                      .children(".jstree-anchor")
                      .attr("aria-selected", !1)
                      .removeClass(r ? "jstree-clicked" : "jstree-checked"));
            }
            return a;
          }),
          (this.get_checked_descendants = function (e) {
            var t = this,
              i = t.settings.checkbox.tie_selection,
              e = t._model.data[e];
            return E.vakata.array_filter(e.children_d, function (e) {
              return t._model.data[e].state[i ? "selected" : "checked"];
            });
          }),
          (this.check_node = function (e, t) {
            if (this.settings.checkbox.tie_selection)
              return this.select_node(e, !1, !0, t);
            var i, r, s, a;
            if (E.vakata.is_array(e)) {
              for (r = 0, s = (e = e.slice()).length; r < s; r++)
                this.check_node(e[r], t);
              return !0;
            }
            if (!(e = this.get_node(e)) || e.id === E.jstree.root) return !1;
            (i = this.get_node(e, !0)),
              e.state.checked ||
                ((e.state.checked = !0),
                this._data.checkbox.selected.push(e.id),
                i &&
                  i.length &&
                  i.children(".jstree-anchor").addClass("jstree-checked"),
                this.trigger("check_node", {
                  node: e,
                  selected: this._data.checkbox.selected,
                  event: t,
                }));
          }),
          (this.uncheck_node = function (e, t) {
            if (this.settings.checkbox.tie_selection)
              return this.deselect_node(e, !1, t);
            var i, r, s;
            if (E.vakata.is_array(e)) {
              for (i = 0, r = (e = e.slice()).length; i < r; i++)
                this.uncheck_node(e[i], t);
              return !0;
            }
            if (!(e = this.get_node(e)) || e.id === E.jstree.root) return !1;
            (s = this.get_node(e, !0)),
              e.state.checked &&
                ((e.state.checked = !1),
                (this._data.checkbox.selected = E.vakata.array_remove_item(
                  this._data.checkbox.selected,
                  e.id
                )),
                s.length &&
                  s.children(".jstree-anchor").removeClass("jstree-checked"),
                this.trigger("uncheck_node", {
                  node: e,
                  selected: this._data.checkbox.selected,
                  event: t,
                }));
          }),
          (this.check_all = function () {
            if (this.settings.checkbox.tie_selection) return this.select_all();
            var e = this._data.checkbox.selected.concat([]),
              t,
              i;
            for (
              this._data.checkbox.selected =
                this._model.data[E.jstree.root].children_d.concat(),
                t = 0,
                i = this._data.checkbox.selected.length;
              t < i;
              t++
            )
              this._model.data[this._data.checkbox.selected[t]] &&
                (this._model.data[
                  this._data.checkbox.selected[t]
                ].state.checked = !0);
            this.redraw(!0),
              this.trigger("check_all", {
                selected: this._data.checkbox.selected,
              });
          }),
          (this.uncheck_all = function () {
            if (this.settings.checkbox.tie_selection)
              return this.deselect_all();
            for (
              var e = this._data.checkbox.selected.concat([]),
                t,
                i,
                t = 0,
                i = this._data.checkbox.selected.length;
              t < i;
              t++
            )
              this._model.data[this._data.checkbox.selected[t]] &&
                (this._model.data[
                  this._data.checkbox.selected[t]
                ].state.checked = !1);
            (this._data.checkbox.selected = []),
              this.element
                .find(".jstree-checked")
                .removeClass("jstree-checked"),
              this.trigger("uncheck_all", {
                selected: this._data.checkbox.selected,
                node: e,
              });
          }),
          (this.is_checked = function (e) {
            return this.settings.checkbox.tie_selection
              ? this.is_selected(e)
              : !(!(e = this.get_node(e)) || e.id === E.jstree.root) &&
                  e.state.checked;
          }),
          (this.get_checked = function (e) {
            return this.settings.checkbox.tie_selection
              ? this.get_selected(e)
              : e
              ? E.map(
                  this._data.checkbox.selected,
                  function (e) {
                    return this.get_node(e);
                  }.bind(this)
                )
              : this._data.checkbox.selected.slice();
          }),
          (this.get_top_checked = function (e) {
            if (this.settings.checkbox.tie_selection)
              return this.get_top_selected(e);
            for (
              var t = this.get_checked(!0),
                i = {},
                r,
                s,
                a,
                n,
                r = 0,
                s = t.length;
              r < s;
              r++
            )
              i[t[r].id] = t[r];
            for (r = 0, s = t.length; r < s; r++)
              for (a = 0, n = t[r].children_d.length; a < n; a++)
                i[t[r].children_d[a]] && delete i[t[r].children_d[a]];
            for (r in ((t = []), i)) i.hasOwnProperty(r) && t.push(r);
            return e
              ? E.map(
                  t,
                  function (e) {
                    return this.get_node(e);
                  }.bind(this)
                )
              : t;
          }),
          (this.get_bottom_checked = function (e) {
            if (this.settings.checkbox.tie_selection)
              return this.get_bottom_selected(e);
            for (
              var t = this.get_checked(!0), i = [], r, s, r = 0, s = t.length;
              r < s;
              r++
            )
              t[r].children.length || i.push(t[r].id);
            return e
              ? E.map(
                  i,
                  function (e) {
                    return this.get_node(e);
                  }.bind(this)
                )
              : i;
          }),
          (this.load_node = function (e, t) {
            var i, r, s, a, n, d;
            if (
              !E.vakata.is_array(e) &&
              !this.settings.checkbox.tie_selection &&
              (d = this.get_node(e)) &&
              d.state.loaded
            )
              for (i = 0, r = d.children_d.length; i < r; i++)
                this._model.data[d.children_d[i]].state.checked &&
                  (this._data.checkbox.selected = E.vakata.array_remove_item(
                    this._data.checkbox.selected,
                    d.children_d[i]
                  ));
            return o.load_node.apply(this, arguments);
          }),
          (this.get_state = function () {
            var e = o.get_state.apply(this, arguments);
            return (
              this.settings.checkbox.tie_selection ||
                (e.checkbox = this._data.checkbox.selected.slice()),
              e
            );
          }),
          (this.set_state = function (e, t) {
            var i = o.set_state.apply(this, arguments),
              r;
            return i && e.checkbox
              ? (this.settings.checkbox.tie_selection ||
                  (this.uncheck_all(),
                  (r = this),
                  E.each(e.checkbox, function (e, t) {
                    r.check_node(t);
                  })),
                delete e.checkbox,
                this.set_state(e, t),
                !1)
              : i;
          }),
          (this.refresh = function (e, t) {
            return (
              this.settings.checkbox.tie_selection &&
                (this._data.checkbox.selected = []),
              o.refresh.apply(this, arguments)
            );
          });
      }),
      (E.jstree.defaults.conditionalselect = function () {
        return !0;
      }),
      (E.jstree.plugins.conditionalselect = function (e, i) {
        this.activate_node = function (e, t) {
          if (this.settings.conditionalselect.call(this, this.get_node(e), t))
            return i.activate_node.call(this, e, t);
        };
      }),
      (E.jstree.defaults.contextmenu = {
        select_node: !0,
        show_at_node: !0,
        items: function (e, t) {
          return {
            create: {
              separator_before: !1,
              separator_after: !0,
              _disabled: !1,
              label: "Create",
              action: function (e) {
                var i = E.jstree.reference(e.reference),
                  e = i.get_node(e.reference);
                i.create_node(e, {}, "last", function (t) {
                  try {
                    i.edit(t);
                  } catch (e) {
                    setTimeout(function () {
                      i.edit(t);
                    }, 0);
                  }
                });
              },
            },
            rename: {
              separator_before: !1,
              separator_after: !1,
              _disabled: !1,
              label: "Rename",
              action: function (e) {
                var t = E.jstree.reference(e.reference),
                  e = t.get_node(e.reference);
                t.edit(e);
              },
            },
            remove: {
              separator_before: !1,
              icon: !1,
              separator_after: !1,
              _disabled: !1,
              label: "Delete",
              action: function (e) {
                var t = E.jstree.reference(e.reference),
                  e = t.get_node(e.reference);
                t.is_selected(e)
                  ? t.delete_node(t.get_selected())
                  : t.delete_node(e);
              },
            },
            ccp: {
              separator_before: !0,
              icon: !1,
              separator_after: !1,
              label: "Edit",
              action: !1,
              submenu: {
                cut: {
                  separator_before: !1,
                  separator_after: !1,
                  label: "Cut",
                  action: function (e) {
                    var t = E.jstree.reference(e.reference),
                      e = t.get_node(e.reference);
                    t.is_selected(e) ? t.cut(t.get_top_selected()) : t.cut(e);
                  },
                },
                copy: {
                  separator_before: !1,
                  icon: !1,
                  separator_after: !1,
                  label: "Copy",
                  action: function (e) {
                    var t = E.jstree.reference(e.reference),
                      e = t.get_node(e.reference);
                    t.is_selected(e) ? t.copy(t.get_top_selected()) : t.copy(e);
                  },
                },
                paste: {
                  separator_before: !1,
                  icon: !1,
                  _disabled: function (e) {
                    return !E.jstree.reference(e.reference).can_paste();
                  },
                  separator_after: !1,
                  label: "Paste",
                  action: function (e) {
                    var t = E.jstree.reference(e.reference),
                      e = t.get_node(e.reference);
                    t.paste(e);
                  },
                },
              },
            },
          };
        },
      }),
      (E.jstree.plugins.contextmenu = function (e, a) {
        (this.bind = function () {
          a.bind.call(this);
          var i = 0,
            r = null,
            t,
            s;
          this.element
            .on(
              "init.jstree loading.jstree ready.jstree",
              function () {
                this.get_container_ul().addClass("jstree-contextmenu");
              }.bind(this)
            )
            .on(
              "contextmenu.jstree",
              ".jstree-anchor",
              function (e, t) {
                "input" !== e.target.tagName.toLowerCase() &&
                  (e.preventDefault(),
                  (i = e.ctrlKey ? +new Date() : 0),
                  (t || r) && (i = +new Date() + 1e4),
                  r && clearTimeout(r),
                  this.is_loading(e.currentTarget) ||
                    this.show_contextmenu(
                      e.currentTarget,
                      e.pageX,
                      e.pageY,
                      e
                    ));
              }.bind(this)
            )
            .on(
              "click.jstree",
              ".jstree-anchor",
              function (e) {
                this._data.contextmenu.visible &&
                  (!i || 250 < +new Date() - i) &&
                  E.vakata.context.hide(),
                  (i = 0);
              }.bind(this)
            )
            .on("touchstart.jstree", ".jstree-anchor", function (e) {
              e.originalEvent &&
                e.originalEvent.changedTouches &&
                e.originalEvent.changedTouches[0] &&
                ((t = e.originalEvent.changedTouches[0].clientX),
                (s = e.originalEvent.changedTouches[0].clientY),
                (r = setTimeout(function () {
                  E(e.currentTarget).trigger("contextmenu", !0);
                }, 750)));
            })
            .on("touchmove.vakata.jstree", function (e) {
              r &&
                e.originalEvent &&
                e.originalEvent.changedTouches &&
                e.originalEvent.changedTouches[0] &&
                (10 < Math.abs(t - e.originalEvent.changedTouches[0].clientX) ||
                  10 <
                    Math.abs(s - e.originalEvent.changedTouches[0].clientY)) &&
                (clearTimeout(r), E.vakata.context.hide());
            })
            .on("touchend.vakata.jstree", function (e) {
              r && clearTimeout(r);
            }),
            E(b).on(
              "context_hide.vakata.jstree",
              function (e, t) {
                (this._data.contextmenu.visible = !1),
                  E(t.reference).removeClass("jstree-context");
              }.bind(this)
            );
        }),
          (this.teardown = function () {
            this._data.contextmenu.visible && E.vakata.context.hide(),
              E(b).off("context_hide.vakata.jstree"),
              a.teardown.call(this);
          }),
          (this.show_contextmenu = function (t, i, r, e) {
            if (!(t = this.get_node(t)) || t.id === E.jstree.root) return !1;
            var s = this.settings.contextmenu,
              a,
              n = this.get_node(t, !0).children(".jstree-anchor"),
              d = !1,
              o = !1;
            (!s.show_at_node && i !== P && r !== P) ||
              ((d = n.offset()),
              (i = d.left),
              (r = d.top + this._data.core.li_height)),
              this.settings.contextmenu.select_node &&
                !this.is_selected(t) &&
                this.activate_node(t, e),
              (o = s.items),
              E.vakata.is_function(o) &&
                (o = o.call(
                  this,
                  t,
                  function (e) {
                    this._show_contextmenu(t, i, r, e);
                  }.bind(this)
                )),
              E.isPlainObject(o) && this._show_contextmenu(t, i, r, o);
          }),
          (this._show_contextmenu = function (e, t, i, r) {
            var s,
              a = this.get_node(e, !0).children(".jstree-anchor");
            E(b).one(
              "context_show.vakata.jstree",
              function (e, t) {
                var i =
                  "jstree-contextmenu jstree-" +
                  this.get_theme() +
                  "-contextmenu";
                E(t.element).addClass(i), a.addClass("jstree-context");
              }.bind(this)
            ),
              (this._data.contextmenu.visible = !0),
              E.vakata.context.show(a, { x: t, y: i }, r),
              this.trigger("show_contextmenu", { node: e, x: t, y: i });
          });
      }),
      (g = {
        element: (_ = !1),
        reference: !1,
        position_x: 0,
        position_y: 0,
        items: [],
        html: "",
        is_visible: !1,
      }),
      ((h = E).vakata.context = {
        settings: { hide_onmouseleave: 0, icons: !0 },
        _trigger: function (e) {
          h(b).triggerHandler("context_" + e + ".vakata", {
            reference: g.reference,
            element: g.element,
            position: { x: g.position_x, y: g.position_y },
          });
        },
        _execute: function (e) {
          return (
            !(
              !(e = g.items[e]) ||
              (e._disabled &&
                (!h.vakata.is_function(e._disabled) ||
                  e._disabled({
                    item: e,
                    reference: g.reference,
                    element: g.element,
                  }))) ||
              !e.action
            ) &&
            e.action.call(null, {
              item: e,
              reference: g.reference,
              element: g.element,
              position: { x: g.position_x, y: g.position_y },
            })
          );
        },
        _parse: function (e, t) {
          if (!e) return !1;
          t || ((g.html = ""), (g.items = []));
          var i = "",
            r = !1,
            s;
          return (
            t && (i += "<ul>"),
            h.each(e, function (e, t) {
              return (
                !t ||
                (g.items.push(t),
                !r &&
                  t.separator_before &&
                  (i +=
                    "<li class='vakata-context-separator'><a href='#' " +
                    (h.vakata.context.settings.icons
                      ? ""
                      : 'class="vakata-context-no-icons"') +
                    ">&#160;</a></li>"),
                (r = !1),
                (i +=
                  "<li class='" +
                  (t._class || "") +
                  (!0 === t._disabled ||
                  (h.vakata.is_function(t._disabled) &&
                    t._disabled({
                      item: t,
                      reference: g.reference,
                      element: g.element,
                    }))
                    ? " vakata-contextmenu-disabled "
                    : "") +
                  "' " +
                  (t.shortcut ? " data-shortcut='" + t.shortcut + "' " : "") +
                  ">"),
                (i +=
                  "<a href='#' rel='" +
                  (g.items.length - 1) +
                  "' " +
                  (t.title ? "title='" + t.title + "'" : "") +
                  ">"),
                h.vakata.context.settings.icons &&
                  ((i += "<i "),
                  t.icon &&
                    (-1 !== t.icon.indexOf("/") || -1 !== t.icon.indexOf(".")
                      ? (i +=
                          " style='background:url(\"" +
                          t.icon +
                          "\") center center no-repeat' ")
                      : (i += " class='" + t.icon + "' ")),
                  (i +=
                    "></i><span class='vakata-contextmenu-sep'>&#160;</span>")),
                (i +=
                  (h.vakata.is_function(t.label)
                    ? t.label({
                        item: e,
                        reference: g.reference,
                        element: g.element,
                      })
                    : t.label) +
                  (t.shortcut
                    ? ' <span class="vakata-contextmenu-shortcut vakata-contextmenu-shortcut-' +
                      t.shortcut +
                      '">' +
                      (t.shortcut_label || "") +
                      "</span>"
                    : "") +
                  "</a>"),
                t.submenu &&
                  (s = h.vakata.context._parse(t.submenu, !0)) &&
                  (i += s),
                (i += "</li>"),
                void (
                  t.separator_after &&
                  ((i +=
                    "<li class='vakata-context-separator'><a href='#' " +
                    (h.vakata.context.settings.icons
                      ? ""
                      : 'class="vakata-context-no-icons"') +
                    ">&#160;</a></li>"),
                  (r = !0))
                ))
              );
            }),
            (i = i.replace(
              /<li class\='vakata-context-separator'\><\/li\>$/,
              ""
            )),
            t && (i += "</ul>"),
            t || ((g.html = i), h.vakata.context._trigger("parse")),
            10 < i.length && i
          );
        },
        _show_submenu: function (e) {
          var t, i, r, s, a, n, d, o;
          (e = h(e)).length &&
            e.children("ul").length &&
            ((t = e.children("ul")),
            (r = (i = e.offset().left) + e.outerWidth()),
            (s = e.offset().top),
            (a = t.width()),
            (n = t.height()),
            (d = h(window).width() + h(window).scrollLeft()),
            (o = h(window).height() + h(window).scrollTop()),
            _
              ? e[
                  r - (a + 10 + e.outerWidth()) < 0 ? "addClass" : "removeClass"
                ]("vakata-context-left")
              : e[d < r + a && d - r < i ? "addClass" : "removeClass"](
                  "vakata-context-right"
                ),
            o < s + n + 10 && t.css("bottom", "-1px"),
            e.hasClass("vakata-context-right")
              ? i < a && t.css("margin-right", i - a)
              : d - r < a && t.css("margin-left", d - r - a),
            t.show());
        },
        show: function (e, t, i) {
          var r,
            s,
            a,
            n,
            d,
            o,
            c,
            i,
            l = !0;
          switch ((g.element && g.element.length && g.element.width(""), !0)) {
            case !t && !e:
              return !1;
            case !!t && !!e:
              (g.reference = e), (g.position_x = t.x), (g.position_y = t.y);
              break;
            case !t && !!e:
              (r = (g.reference = e).offset()),
                (g.position_x = r.left + e.outerHeight()),
                (g.position_y = r.top);
              break;
            case !!t && !e:
              (g.position_x = t.x), (g.position_y = t.y);
          }
          e &&
            !i &&
            h(e).data("vakata_contextmenu") &&
            (i = h(e).data("vakata_contextmenu")),
            h.vakata.context._parse(i) && g.element.html(g.html),
            g.items.length &&
              (g.element.appendTo(b.body),
              (s = g.element),
              (a = g.position_x),
              (n = g.position_y),
              (d = s.width()),
              (o = s.height()),
              (c = h(window).width() + h(window).scrollLeft()),
              (i = h(window).height() + h(window).scrollTop()),
              _ &&
                (a -= s.outerWidth() - h(e).outerWidth()) <
                  h(window).scrollLeft() + 20 &&
                (a = h(window).scrollLeft() + 20),
              g.element
                .css({
                  left: (a = c < a + d + 20 ? c - (d + 20) : a),
                  top: (n = i < n + o + 20 ? i - (o + 20) : n),
                })
                .show()
                .find("a")
                .first()
                .trigger("focus")
                .parent()
                .addClass("vakata-context-hover"),
              (g.is_visible = !0),
              h.vakata.context._trigger("show"));
        },
        hide: function () {
          g.is_visible &&
            (g.element
              .hide()
              .find("ul")
              .hide()
              .end()
              .find(":focus")
              .trigger("blur")
              .end()
              .detach(),
            (g.is_visible = !1),
            h.vakata.context._trigger("hide"));
        },
      }),
      h(function () {
        _ = "rtl" === h(b.body).css("direction");
        var i = !1;
        (g.element = h("<ul class='vakata-context'></ul>")),
          g.element
            .on("mouseenter", "li", function (e) {
              e.stopImmediatePropagation(),
                h.contains(this, e.relatedTarget) ||
                  (i && clearTimeout(i),
                  g.element
                    .find(".vakata-context-hover")
                    .removeClass("vakata-context-hover")
                    .end(),
                  h(this)
                    .siblings()
                    .find("ul")
                    .hide()
                    .end()
                    .end()
                    .parentsUntil(".vakata-context", "li")
                    .addBack()
                    .addClass("vakata-context-hover"),
                  h.vakata.context._show_submenu(this));
            })
            .on("mouseleave", "li", function (e) {
              h.contains(this, e.relatedTarget) ||
                h(this)
                  .find(".vakata-context-hover")
                  .addBack()
                  .removeClass("vakata-context-hover");
            })
            .on("mouseleave", function (e) {
              var t;
              h(this)
                .find(".vakata-context-hover")
                .removeClass("vakata-context-hover"),
                h.vakata.context.settings.hide_onmouseleave &&
                  (i = setTimeout(
                    ((t = this),
                    function () {
                      h.vakata.context.hide();
                    }),
                    h.vakata.context.settings.hide_onmouseleave
                  ));
            })
            .on("click", "a", function (e) {
              e.preventDefault(),
                h(this)
                  .trigger("blur")
                  .parent()
                  .hasClass("vakata-context-disabled") ||
                  !1 === h.vakata.context._execute(h(this).attr("rel")) ||
                  h.vakata.context.hide();
            })
            .on("keydown", "a", function (e) {
              var t = null;
              switch (e.which) {
                case 13:
                case 32:
                  (e.type = "click"),
                    e.preventDefault(),
                    h(e.currentTarget).trigger(e);
                  break;
                case 37:
                  g.is_visible &&
                    (g.element
                      .find(".vakata-context-hover")
                      .last()
                      .closest("li")
                      .first()
                      .find("ul")
                      .hide()
                      .find(".vakata-context-hover")
                      .removeClass("vakata-context-hover")
                      .end()
                      .end()
                      .children("a")
                      .trigger("focus"),
                    e.stopImmediatePropagation(),
                    e.preventDefault());
                  break;
                case 38:
                  g.is_visible &&
                    ((t = !(t = g.element
                      .find("ul:visible")
                      .addBack()
                      .last()
                      .children(".vakata-context-hover")
                      .removeClass("vakata-context-hover")
                      .prevAll("li:not(.vakata-context-separator)")
                      .first()).length
                      ? g.element
                          .find("ul:visible")
                          .addBack()
                          .last()
                          .children("li:not(.vakata-context-separator)")
                          .last()
                      : t)
                      .addClass("vakata-context-hover")
                      .children("a")
                      .trigger("focus"),
                    e.stopImmediatePropagation(),
                    e.preventDefault());
                  break;
                case 39:
                  g.is_visible &&
                    (g.element
                      .find(".vakata-context-hover")
                      .last()
                      .children("ul")
                      .show()
                      .children("li:not(.vakata-context-separator)")
                      .removeClass("vakata-context-hover")
                      .first()
                      .addClass("vakata-context-hover")
                      .children("a")
                      .trigger("focus"),
                    e.stopImmediatePropagation(),
                    e.preventDefault());
                  break;
                case 40:
                  g.is_visible &&
                    ((t = !(t = g.element
                      .find("ul:visible")
                      .addBack()
                      .last()
                      .children(".vakata-context-hover")
                      .removeClass("vakata-context-hover")
                      .nextAll("li:not(.vakata-context-separator)")
                      .first()).length
                      ? g.element
                          .find("ul:visible")
                          .addBack()
                          .last()
                          .children("li:not(.vakata-context-separator)")
                          .first()
                      : t)
                      .addClass("vakata-context-hover")
                      .children("a")
                      .trigger("focus"),
                    e.stopImmediatePropagation(),
                    e.preventDefault());
                  break;
                case 27:
                  h.vakata.context.hide(), e.preventDefault();
              }
            })
            .on("keydown", function (e) {
              e.preventDefault();
              var e = g.element
                .find(".vakata-contextmenu-shortcut-" + e.which)
                .parent();
              e.parent().not(".vakata-context-disabled") && e.trigger("click");
            }),
          h(b)
            .on("mousedown.vakata.jstree", function (e) {
              g.is_visible &&
                g.element[0] !== e.target &&
                !h.contains(g.element[0], e.target) &&
                h.vakata.context.hide();
            })
            .on("context_show.vakata.jstree", function (e, t) {
              g.element
                .find("li:has(ul)")
                .children("a")
                .addClass("vakata-context-parent"),
                _ &&
                  g.element
                    .addClass("vakata-context-rtl")
                    .css("direction", "rtl"),
                g.element.find("ul").hide().end();
            });
      }),
      (E.jstree.defaults.dnd = {
        copy: !0,
        open_timeout: 500,
        is_draggable: !0,
        check_while_dragging: !0,
        always_copy: !1,
        inside_pos: 0,
        drag_selection: !0,
        touch: !0,
        large_drop_target: !1,
        large_drag_target: !1,
        use_html5: !1,
        blank_space_drop: !1,
      }),
      (E.jstree.plugins.dnd = function (e, d) {
        (this.init = function (e, t) {
          d.init.call(this, e, t),
            (this.settings.dnd.use_html5 =
              this.settings.dnd.use_html5 &&
              "draggable" in b.createElement("span"));
        }),
          (this.bind = function () {
            d.bind.call(this),
              this.element.on(
                this.settings.dnd.use_html5
                  ? "dragstart.jstree"
                  : "mousedown.jstree touchstart.jstree",
                this.settings.dnd.large_drag_target
                  ? ".jstree-node"
                  : ".jstree-anchor",
                function (e) {
                  if (
                    this.settings.dnd.large_drag_target &&
                    E(e.target).closest(".jstree-node")[0] !== e.currentTarget
                  )
                    return !0;
                  if (
                    "touchstart" === e.type &&
                    (!this.settings.dnd.touch ||
                      ("selected" === this.settings.dnd.touch &&
                        !E(e.currentTarget)
                          .closest(".jstree-node")
                          .children(".jstree-anchor")
                          .hasClass("jstree-clicked")))
                  )
                    return !0;
                  var t = this.get_node(e.target),
                    i =
                      this.is_selected(t) && this.settings.dnd.drag_selection
                        ? this.get_top_selected().length
                        : 1,
                    r =
                      1 < i
                        ? i + " " + this.get_string("nodes")
                        : this.get_text(e.currentTarget);
                  if (
                    (this.settings.core.force_text &&
                      (r = E.vakata.html.escape(r)),
                    t &&
                      (t.id || 0 === t.id) &&
                      t.id !== E.jstree.root &&
                      (1 === e.which ||
                        "touchstart" === e.type ||
                        "dragstart" === e.type) &&
                      (!0 === this.settings.dnd.is_draggable ||
                        (E.vakata.is_function(this.settings.dnd.is_draggable) &&
                          this.settings.dnd.is_draggable.call(
                            this,
                            1 < i ? this.get_top_selected(!0) : [t],
                            e
                          ))))
                  ) {
                    if (
                      ((o = {
                        jstree: !0,
                        origin: this,
                        obj: this.get_node(t, !0),
                        nodes: 1 < i ? this.get_top_selected() : [t.id],
                      }),
                      (u = e.currentTarget),
                      !this.settings.dnd.use_html5)
                    )
                      return (
                        this.element.trigger("mousedown.jstree"),
                        E.vakata.dnd.start(
                          e,
                          o,
                          '<div id="jstree-dnd" class="jstree-' +
                            this.get_theme() +
                            " jstree-" +
                            this.get_theme() +
                            "-" +
                            this.get_theme_variant() +
                            " " +
                            (this.settings.core.themes.responsive
                              ? " jstree-dnd-responsive"
                              : "") +
                            '"><i class="jstree-icon jstree-er"></i>' +
                            r +
                            '<ins class="jstree-copy">+</ins></div>'
                        )
                      );
                    E.vakata.dnd._trigger("start", e, {
                      helper: E(),
                      element: u,
                      data: o,
                    });
                  }
                }.bind(this)
              ),
              this.settings.dnd.use_html5 &&
                this.element
                  .on("dragover.jstree", function (e) {
                    return (
                      e.preventDefault(),
                      E.vakata.dnd._trigger("move", e, {
                        helper: E(),
                        element: u,
                        data: o,
                      }),
                      !1
                    );
                  })
                  .on(
                    "drop.jstree",
                    function (e) {
                      return (
                        e.preventDefault(),
                        E.vakata.dnd._trigger("stop", e, {
                          helper: E(),
                          element: u,
                          data: o,
                        }),
                        !1
                      );
                    }.bind(this)
                  );
          }),
          (this.redraw_node = function (e, t, i, r) {
            if (
              (e = d.redraw_node.apply(this, arguments)) &&
              this.settings.dnd.use_html5
            )
              if (this.settings.dnd.large_drag_target)
                e.setAttribute("draggable", !0);
              else {
                for (
                  var s, a, n = null, s = 0, a = e.childNodes.length;
                  s < a;
                  s++
                )
                  if (
                    e.childNodes[s] &&
                    e.childNodes[s].className &&
                    -1 !== e.childNodes[s].className.indexOf("jstree-anchor")
                  ) {
                    n = e.childNodes[s];
                    break;
                  }
                n && n.setAttribute("draggable", !0);
              }
            return e;
          });
      }),
      E(function () {
        var N = !1,
          T = !1,
          O = !1,
          A = !1,
          S = E('<div id="jstree-marker">&#160;</div>').hide();
        E(b)
          .on("dragover.vakata.jstree", function (e) {
            u &&
              E.vakata.dnd._trigger("move", e, {
                helper: E(),
                element: u,
                data: o,
              });
          })
          .on("drop.vakata.jstree", function (e) {
            u &&
              (E.vakata.dnd._trigger("stop", e, {
                helper: E(),
                element: u,
                data: o,
              }),
              (o = u = null));
          })
          .on("dnd_start.vakata.jstree", function (e, t) {
            (O = N = !1), t && t.data && t.data.jstree && S.appendTo(b.body);
          })
          .on("dnd_move.vakata.jstree", function (e, s) {
            var a = s.event.target !== O.target;
            if (
              (A &&
                ((s.event && "dragover" === s.event.type && !a) ||
                  clearTimeout(A)),
              s &&
                s.data &&
                s.data.jstree &&
                (!s.event.target.id || "jstree-marker" !== s.event.target.id))
            ) {
              O = s.event;
              var n = E.jstree.reference(s.event.target),
                d = !1,
                o = !1,
                t = !1,
                i,
                c,
                l,
                h,
                _,
                g,
                u,
                f,
                p,
                m,
                v,
                j,
                k,
                b,
                y,
                x,
                w,
                C;
              if (n && n._data && n._data.dnd)
                if (
                  (S.attr(
                    "class",
                    "jstree-" +
                      n.get_theme() +
                      (n.settings.core.themes.responsive
                        ? " jstree-dnd-responsive"
                        : "")
                  ),
                  (x =
                    s.data.origin &&
                    (s.data.origin.settings.dnd.always_copy ||
                      (s.data.origin.settings.dnd.copy &&
                        (s.event.metaKey || s.event.ctrlKey)))),
                  s.helper
                    .children()
                    .attr(
                      "class",
                      "jstree-" +
                        n.get_theme() +
                        " jstree-" +
                        n.get_theme() +
                        "-" +
                        n.get_theme_variant() +
                        " " +
                        (n.settings.core.themes.responsive
                          ? " jstree-dnd-responsive"
                          : "")
                    )
                    .find(".jstree-copy")
                    .first()
                    [x ? "show" : "hide"](),
                  (s.event.target !== n.element[0] &&
                    s.event.target !== n.get_container_ul()[0]) ||
                    (0 !== n.get_container_ul().children().length &&
                      !n.settings.dnd.blank_space_drop))
                ) {
                  if (
                    (d = n.settings.dnd.large_drop_target
                      ? E(s.event.target)
                          .closest(".jstree-node")
                          .children(".jstree-anchor")
                      : E(s.event.target).closest(".jstree-anchor")) &&
                    d.length &&
                    d
                      .parent()
                      .is(".jstree-closed, .jstree-open, .jstree-leaf") &&
                    ((o = d.offset()),
                    (t =
                      (s.event.pageY !== P ? s.event : s.event.originalEvent)
                        .pageY - o.top),
                    (h = d.outerHeight()),
                    (u =
                      t < h / 3
                        ? ["b", "i", "a"]
                        : h - h / 3 < t
                        ? ["a", "i", "b"]
                        : h / 2 < t
                        ? ["i", "a", "b"]
                        : ["i", "b", "a"]),
                    E.each(u, function (e, t) {
                      switch (t) {
                        case "b":
                          (c = o.left - 6),
                            (l = o.top),
                            (_ = n.get_parent(d)),
                            (g = d.parent().index()),
                            (C = "jstree-below");
                          break;
                        case "i":
                          (b = n.settings.dnd.inside_pos),
                            (y = n.get_node(d.parent())),
                            (c = o.left - 2),
                            (l = o.top + h / 2 + 1),
                            (_ = y.id),
                            (g =
                              "first" === b
                                ? 0
                                : "last" === b
                                ? y.children.length
                                : Math.min(b, y.children.length)),
                            (C = "jstree-inside");
                          break;
                        case "a":
                          (c = o.left - 6),
                            (l = o.top + h),
                            (_ = n.get_parent(d)),
                            (g = d.parent().index() + 1),
                            (C = "jstree-above");
                      }
                      for (f = !0, p = 0, m = s.data.nodes.length; p < m; p++)
                        if (
                          ((v =
                            s.data.origin &&
                            (s.data.origin.settings.dnd.always_copy ||
                              (s.data.origin.settings.dnd.copy &&
                                (s.event.metaKey || s.event.ctrlKey)))
                              ? "copy_node"
                              : "move_node"),
                          (j = g),
                          "move_node" == v &&
                            "a" === t &&
                            s.data.origin &&
                            s.data.origin === n &&
                            _ === n.get_parent(s.data.nodes[p]) &&
                            ((k = n.get_node(_)),
                            j > E.inArray(s.data.nodes[p], k.children) && --j),
                          !(f =
                            f &&
                            ((n &&
                              n.settings &&
                              n.settings.dnd &&
                              !1 === n.settings.dnd.check_while_dragging) ||
                              n.check(
                                v,
                                s.data.origin && s.data.origin !== n
                                  ? s.data.origin.get_node(s.data.nodes[p])
                                  : s.data.nodes[p],
                                _,
                                j,
                                {
                                  dnd: !0,
                                  ref: n.get_node(d.parent()),
                                  pos: t,
                                  origin: s.data.origin,
                                  is_multi:
                                    s.data.origin && s.data.origin !== n,
                                  is_foreign: !s.data.origin,
                                }
                              ))))
                        ) {
                          n && n.last_error && (T = n.last_error());
                          break;
                        }
                      var i, r;
                      if (
                        ("i" === t &&
                          d.parent().is(".jstree-closed") &&
                          n.settings.dnd.open_timeout &&
                          ((s.event && "dragover" === s.event.type && !a) ||
                            (A && clearTimeout(A),
                            (A = setTimeout(
                              ((r = d),
                              function () {
                                i.open_node(r);
                              }),
                              (i = n).settings.dnd.open_timeout
                            )))),
                        f)
                      )
                        return (
                          (w = n.get_node(_, !0)).hasClass(
                            ".jstree-dnd-parent"
                          ) ||
                            (E(".jstree-dnd-parent").removeClass(
                              "jstree-dnd-parent"
                            ),
                            w.addClass("jstree-dnd-parent")),
                          (N = {
                            ins: n,
                            par: _,
                            pos:
                              "i" !== t ||
                              "last" !== b ||
                              0 !== g ||
                              n.is_loaded(y)
                                ? g
                                : "last",
                          }),
                          S.css({ left: c + "px", top: l + "px" }).show(),
                          S.removeClass(
                            "jstree-above jstree-inside jstree-below"
                          ).addClass(C),
                          s.helper
                            .find(".jstree-icon")
                            .first()
                            .removeClass("jstree-er")
                            .addClass("jstree-ok"),
                          s.event.originalEvent &&
                            s.event.originalEvent.dataTransfer &&
                            (s.event.originalEvent.dataTransfer.dropEffect = x
                              ? "copy"
                              : "move"),
                          (T = {}),
                          !(u = !0)
                        );
                    }),
                    !0 === u)
                  )
                    return;
                } else {
                  for (f = !0, p = 0, m = s.data.nodes.length; p < m; p++)
                    if (
                      !(f =
                        f &&
                        n.check(
                          s.data.origin &&
                            (s.data.origin.settings.dnd.always_copy ||
                              (s.data.origin.settings.dnd.copy &&
                                (s.event.metaKey || s.event.ctrlKey)))
                            ? "copy_node"
                            : "move_node",
                          s.data.origin && s.data.origin !== n
                            ? s.data.origin.get_node(s.data.nodes[p])
                            : s.data.nodes[p],
                          E.jstree.root,
                          "last",
                          {
                            dnd: !0,
                            ref: n.get_node(E.jstree.root),
                            pos: "i",
                            origin: s.data.origin,
                            is_multi: s.data.origin && s.data.origin !== n,
                            is_foreign: !s.data.origin,
                          }
                        ))
                    )
                      break;
                  if (f)
                    return (
                      (N = { ins: n, par: E.jstree.root, pos: "last" }),
                      S.hide(),
                      s.helper
                        .find(".jstree-icon")
                        .first()
                        .removeClass("jstree-er")
                        .addClass("jstree-ok"),
                      void (
                        s.event.originalEvent &&
                        s.event.originalEvent.dataTransfer &&
                        (s.event.originalEvent.dataTransfer.dropEffect = x
                          ? "copy"
                          : "move")
                      )
                    );
                }
              E(".jstree-dnd-parent").removeClass("jstree-dnd-parent"),
                (N = !1),
                s.helper
                  .find(".jstree-icon")
                  .removeClass("jstree-ok")
                  .addClass("jstree-er"),
                s.event.originalEvent && s.event.originalEvent.dataTransfer,
                S.hide();
            }
          })
          .on("dnd_scroll.vakata.jstree", function (e, t) {
            t &&
              t.data &&
              t.data.jstree &&
              (S.hide(),
              (O = N = !1),
              t.helper
                .find(".jstree-icon")
                .first()
                .removeClass("jstree-ok")
                .addClass("jstree-er"));
          })
          .on("dnd_stop.vakata.jstree", function (e, t) {
            if (
              (E(".jstree-dnd-parent").removeClass("jstree-dnd-parent"),
              A && clearTimeout(A),
              t && t.data && t.data.jstree)
            ) {
              S.hide().detach();
              var i,
                r,
                s = [];
              if (N) {
                for (i = 0, r = t.data.nodes.length; i < r; i++)
                  s[i] = t.data.origin
                    ? t.data.origin.get_node(t.data.nodes[i])
                    : t.data.nodes[i];
                N.ins[
                  t.data.origin &&
                  (t.data.origin.settings.dnd.always_copy ||
                    (t.data.origin.settings.dnd.copy &&
                      (t.event.metaKey || t.event.ctrlKey)))
                    ? "copy_node"
                    : "move_node"
                ](s, N.par, N.pos, !1, !1, !1, t.data.origin);
              } else
                (i = E(t.event.target).closest(".jstree")).length &&
                  T &&
                  T.error &&
                  "check" === T.error &&
                  (i = i.jstree(!0)) &&
                  i.settings.core.error.call(this, T);
              N = O = !1;
            }
          })
          .on("keyup.jstree keydown.jstree", function (e, t) {
            (t = E.vakata.dnd._get()) &&
              t.data &&
              t.data.jstree &&
              ("keyup" === e.type && 27 === e.which
                ? (A && clearTimeout(A),
                  (A = O = T = N = !1),
                  S.hide().detach(),
                  E.vakata.dnd._clean())
                : (t.helper
                    .find(".jstree-copy")
                    .first()
                    [
                      t.data.origin &&
                      (t.data.origin.settings.dnd.always_copy ||
                        (t.data.origin.settings.dnd.copy &&
                          (e.metaKey || e.ctrlKey)))
                        ? "show"
                        : "hide"
                    ](),
                  O &&
                    ((O.metaKey = e.metaKey),
                    (O.ctrlKey = e.ctrlKey),
                    E.vakata.dnd._trigger("move", O))));
          });
      }),
      (p = {
        element: !((f = E).vakata.html = {
          div: f("<div></div>"),
          escape: function (e) {
            return f.vakata.html.div.text(e).html();
          },
          strip: function (e) {
            return f.vakata.html.div.empty().append(f.parseHTML(e)).text();
          },
        }),
        target: !1,
        is_down: !1,
        is_drag: !1,
        helper: !1,
        helper_w: 0,
        data: !1,
        init_x: 0,
        init_y: 0,
        scroll_l: 0,
        scroll_t: 0,
        scroll_e: !1,
        scroll_i: !1,
        is_touch: !1,
      }),
      (f.vakata.dnd = {
        settings: {
          scroll_speed: 10,
          scroll_proximity: 20,
          helper_left: 5,
          helper_top: 10,
          threshold: 5,
          threshold_touch: 10,
        },
        _trigger: function (e, t, i) {
          ((i = i === P ? f.vakata.dnd._get() : i).event = t),
            f(b).triggerHandler("dnd_" + e + ".vakata", i);
        },
        _get: function () {
          return { data: p.data, element: p.element, helper: p.helper };
        },
        _clean: function () {
          p.helper && p.helper.remove(),
            p.scroll_i && (clearInterval(p.scroll_i), (p.scroll_i = !1)),
            (p = {
              element: !1,
              target: !1,
              is_down: !1,
              is_drag: !1,
              helper: !1,
              helper_w: 0,
              data: !1,
              init_x: 0,
              init_y: 0,
              scroll_l: 0,
              scroll_t: 0,
              scroll_e: !1,
              scroll_i: !1,
              is_touch: !1,
            }),
            (u = null),
            f(b).off(
              "mousemove.vakata.jstree touchmove.vakata.jstree",
              f.vakata.dnd.drag
            ),
            f(b).off(
              "mouseup.vakata.jstree touchend.vakata.jstree",
              f.vakata.dnd.stop
            );
        },
        _scroll: function (e) {
          if (!p.scroll_e || (!p.scroll_l && !p.scroll_t))
            return (
              p.scroll_i && (clearInterval(p.scroll_i), (p.scroll_i = !1)), !1
            );
          if (!p.scroll_i)
            return (p.scroll_i = setInterval(f.vakata.dnd._scroll, 100)), !1;
          if (!0 === e) return !1;
          var t = p.scroll_e.scrollTop(),
            e = p.scroll_e.scrollLeft();
          p.scroll_e.scrollTop(
            t + p.scroll_t * f.vakata.dnd.settings.scroll_speed
          ),
            p.scroll_e.scrollLeft(
              e + p.scroll_l * f.vakata.dnd.settings.scroll_speed
            ),
            (t === p.scroll_e.scrollTop() && e === p.scroll_e.scrollLeft()) ||
              f.vakata.dnd._trigger("scroll", p.scroll_e);
        },
        start: function (e, t, i) {
          "touchstart" === e.type &&
            e.originalEvent &&
            e.originalEvent.changedTouches &&
            e.originalEvent.changedTouches[0] &&
            ((e.pageX = e.originalEvent.changedTouches[0].pageX),
            (e.pageY = e.originalEvent.changedTouches[0].pageY),
            (e.target = b.elementFromPoint(
              e.originalEvent.changedTouches[0].pageX - window.pageXOffset,
              e.originalEvent.changedTouches[0].pageY - window.pageYOffset
            ))),
            p.is_drag && f.vakata.dnd.stop({});
          try {
            (e.currentTarget.unselectable = "on"),
              (e.currentTarget.onselectstart = function () {
                return !1;
              }),
              e.currentTarget.style &&
                ((e.currentTarget.style.touchAction = "none"),
                (e.currentTarget.style.msTouchAction = "none"),
                (e.currentTarget.style.MozUserSelect = "none"));
          } catch (e) {}
          return (
            (p.init_x = e.pageX),
            (p.init_y = e.pageY),
            (p.data = t),
            (p.is_down = !0),
            (p.element = e.currentTarget),
            (p.target = e.target),
            (p.is_touch = "touchstart" === e.type),
            !1 !== i &&
              (p.helper = f("<div id='vakata-dnd'></div>").html(i).css({
                display: "block",
                margin: "0",
                padding: "0",
                position: "absolute",
                top: "-2000px",
                lineHeight: "16px",
                zIndex: "10000",
              })),
            f(b).on(
              "mousemove.vakata.jstree touchmove.vakata.jstree",
              f.vakata.dnd.drag
            ),
            f(b).on(
              "mouseup.vakata.jstree touchend.vakata.jstree",
              f.vakata.dnd.stop
            ),
            !1
          );
        },
        drag: function (i) {
          if (
            ("touchmove" === i.type &&
              i.originalEvent &&
              i.originalEvent.changedTouches &&
              i.originalEvent.changedTouches[0] &&
              ((i.pageX = i.originalEvent.changedTouches[0].pageX),
              (i.pageY = i.originalEvent.changedTouches[0].pageY),
              (i.target = b.elementFromPoint(
                i.originalEvent.changedTouches[0].pageX - window.pageXOffset,
                i.originalEvent.changedTouches[0].pageY - window.pageYOffset
              ))),
            p.is_down)
          ) {
            if (!p.is_drag) {
              if (
                !(
                  Math.abs(i.pageX - p.init_x) >
                    (p.is_touch
                      ? f.vakata.dnd.settings.threshold_touch
                      : f.vakata.dnd.settings.threshold) ||
                  Math.abs(i.pageY - p.init_y) >
                    (p.is_touch
                      ? f.vakata.dnd.settings.threshold_touch
                      : f.vakata.dnd.settings.threshold)
                )
              )
                return;
              p.helper &&
                (p.helper.appendTo(b.body),
                (p.helper_w = p.helper.outerWidth())),
                (p.is_drag = !0),
                f(p.target).one("click.vakata", !1),
                f.vakata.dnd._trigger("start", i);
            }
            var e = !1,
              t = !1,
              r = !1,
              s = !1,
              a = !1,
              n = !1,
              d = !1,
              o = !1,
              c = !1,
              l = !1;
            return (
              (p.scroll_t = 0),
              (p.scroll_l = 0),
              (p.scroll_e = !1),
              f(f(i.target).parentsUntil("body").addBack().get().reverse())
                .filter(function () {
                  return (
                    this.ownerDocument &&
                    /^auto|scroll$/.test(f(this).css("overflow")) &&
                    (this.scrollHeight > this.offsetHeight ||
                      this.scrollWidth > this.offsetWidth)
                  );
                })
                .each(function () {
                  var e = f(this),
                    t = e.offset();
                  if (
                    (this.scrollHeight > this.offsetHeight &&
                      (t.top + e.height() - i.pageY <
                        f.vakata.dnd.settings.scroll_proximity &&
                        (p.scroll_t = 1),
                      i.pageY - t.top <
                        f.vakata.dnd.settings.scroll_proximity &&
                        (p.scroll_t = -1)),
                    this.scrollWidth > this.offsetWidth &&
                      (t.left + e.width() - i.pageX <
                        f.vakata.dnd.settings.scroll_proximity &&
                        (p.scroll_l = 1),
                      i.pageX - t.left <
                        f.vakata.dnd.settings.scroll_proximity &&
                        (p.scroll_l = -1)),
                    p.scroll_t || p.scroll_l)
                  )
                    return (p.scroll_e = f(this)), !1;
                }),
              p.scroll_e ||
                ((e = f(b)),
                (t = f(window)),
                (r = e.height()),
                (s = t.height()),
                (a = e.width()),
                (n = t.width()),
                (d = e.scrollTop()),
                (o = e.scrollLeft()),
                s < r &&
                  i.pageY - d < f.vakata.dnd.settings.scroll_proximity &&
                  (p.scroll_t = -1),
                s < r &&
                  s - (i.pageY - d) < f.vakata.dnd.settings.scroll_proximity &&
                  (p.scroll_t = 1),
                n < a &&
                  i.pageX - o < f.vakata.dnd.settings.scroll_proximity &&
                  (p.scroll_l = -1),
                n < a &&
                  n - (i.pageX - o) < f.vakata.dnd.settings.scroll_proximity &&
                  (p.scroll_l = 1),
                (p.scroll_t || p.scroll_l) && (p.scroll_e = e)),
              p.scroll_e && f.vakata.dnd._scroll(!0),
              p.helper &&
                ((c = parseInt(i.pageY + f.vakata.dnd.settings.helper_top, 10)),
                (l = parseInt(i.pageX + f.vakata.dnd.settings.helper_left, 10)),
                a && l + p.helper_w > a && (l = a - (p.helper_w + 2)),
                p.helper.css({
                  left: l + "px",
                  top: (c = r && r < c + 25 ? r - 50 : c) + "px",
                })),
              f.vakata.dnd._trigger("move", i),
              !1
            );
          }
        },
        stop: function (e) {
          var t;
          return (
            "touchend" === e.type &&
              e.originalEvent &&
              e.originalEvent.changedTouches &&
              e.originalEvent.changedTouches[0] &&
              ((e.pageX = e.originalEvent.changedTouches[0].pageX),
              (e.pageY = e.originalEvent.changedTouches[0].pageY),
              (e.target = b.elementFromPoint(
                e.originalEvent.changedTouches[0].pageX - window.pageXOffset,
                e.originalEvent.changedTouches[0].pageY - window.pageYOffset
              ))),
            p.is_drag
              ? (e.target !== p.target && f(p.target).off("click.vakata"),
                f.vakata.dnd._trigger("stop", e))
              : "touchend" === e.type &&
                e.target === p.target &&
                ((t = setTimeout(function () {
                  f(e.target).trigger("click");
                }, 100)),
                f(e.target).one("click", function () {
                  t && clearTimeout(t);
                })),
            f.vakata.dnd._clean(),
            !1
          );
        },
      }),
      (E.jstree.defaults.massload = null),
      (E.jstree.plugins.massload = function (e, l) {
        (this.init = function (e, t) {
          (this._data.massload = {}), l.init.call(this, e, t);
        }),
          (this._load_nodes = function (a, n, d, o) {
            var e = this.settings.massload,
              t = [],
              i = this._model.data,
              r,
              s,
              c;
            if (!d) {
              for (r = 0, s = a.length; r < s; r++)
                (i[a[r]] &&
                  (i[a[r]].state.loaded || i[a[r]].state.failed) &&
                  !o) ||
                  (t.push(a[r]),
                  (c = this.get_node(a[r], !0)) &&
                    c.length &&
                    c.addClass("jstree-loading").attr("aria-busy", !0));
              if (((this._data.massload = {}), t.length)) {
                if (E.vakata.is_function(e))
                  return e.call(
                    this,
                    t,
                    function (e) {
                      var t, i;
                      if (e)
                        for (t in e)
                          e.hasOwnProperty(t) &&
                            (this._data.massload[t] = e[t]);
                      for (t = 0, i = a.length; t < i; t++)
                        (c = this.get_node(a[t], !0)) &&
                          c.length &&
                          c.removeClass("jstree-loading").attr("aria-busy", !1);
                      l._load_nodes.call(this, a, n, d, o);
                    }.bind(this)
                  );
                if ("object" == typeof e && e && e.url)
                  return (
                    (e = E.extend(!0, {}, e)),
                    E.vakata.is_function(e.url) &&
                      (e.url = e.url.call(this, t)),
                    E.vakata.is_function(e.data) &&
                      (e.data = e.data.call(this, t)),
                    E.ajax(e)
                      .done(
                        function (e, t, i) {
                          var r, s;
                          if (e)
                            for (r in e)
                              e.hasOwnProperty(r) &&
                                (this._data.massload[r] = e[r]);
                          for (r = 0, s = a.length; r < s; r++)
                            (c = this.get_node(a[r], !0)) &&
                              c.length &&
                              c
                                .removeClass("jstree-loading")
                                .attr("aria-busy", !1);
                          l._load_nodes.call(this, a, n, d, o);
                        }.bind(this)
                      )
                      .fail(
                        function (e) {
                          l._load_nodes.call(this, a, n, d, o);
                        }.bind(this)
                      )
                  );
              }
            }
            return l._load_nodes.call(this, a, n, d, o);
          }),
          (this._load_node = function (e, t) {
            var i = this._data.massload[e.id],
              r = null,
              i;
            return i
              ? ((r = this[
                  "string" == typeof i
                    ? "_append_html_data"
                    : "_append_json_data"
                ](
                  e,
                  "string" == typeof i
                    ? E(E.parseHTML(i)).filter(function () {
                        return 3 !== this.nodeType;
                      })
                    : i,
                  function (e) {
                    t.call(this, e);
                  }
                )),
                (i = this.get_node(e.id, !0)) &&
                  i.length &&
                  i.removeClass("jstree-loading").attr("aria-busy", !1),
                delete this._data.massload[e.id],
                r)
              : l._load_node.call(this, e, t);
          });
      }),
      (E.jstree.defaults.search = {
        ajax: !1,
        fuzzy: !1,
        case_sensitive: !1,
        show_only_matches: !1,
        show_only_matches_children: !1,
        close_opened_onclear: !0,
        search_leaves_only: !1,
        search_callback: !1,
      }),
      (E.jstree.plugins.search = function (e, d) {
        (this.bind = function () {
          d.bind.call(this),
            (this._data.search.str = ""),
            (this._data.search.dom = E()),
            (this._data.search.res = []),
            (this._data.search.opn = []),
            (this._data.search.som = !1),
            (this._data.search.smc = !1),
            (this._data.search.hdn = []),
            this.element
              .on(
                "search.jstree",
                function (e, t) {
                  if (this._data.search.som && t.res.length) {
                    for (
                      var i = this._model.data,
                        r,
                        s,
                        a = [],
                        n,
                        d,
                        r = 0,
                        s = t.res.length;
                      r < s;
                      r++
                    )
                      if (
                        i[t.res[r]] &&
                        !i[t.res[r]].state.hidden &&
                        (a.push(t.res[r]),
                        (a = a.concat(i[t.res[r]].parents)),
                        this._data.search.smc)
                      )
                        for (
                          n = 0, d = i[t.res[r]].children_d.length;
                          n < d;
                          n++
                        )
                          i[i[t.res[r]].children_d[n]] &&
                            !i[i[t.res[r]].children_d[n]].state.hidden &&
                            a.push(i[t.res[r]].children_d[n]);
                    (a = E.vakata.array_remove_item(
                      E.vakata.array_unique(a),
                      E.jstree.root
                    )),
                      (this._data.search.hdn = this.hide_all(!0)),
                      this.show_node(a, !0),
                      this.redraw(!0);
                  }
                }.bind(this)
              )
              .on(
                "clear_search.jstree",
                function (e, t) {
                  this._data.search.som &&
                    t.res.length &&
                    (this.show_node(this._data.search.hdn, !0),
                    this.redraw(!0));
                }.bind(this)
              );
        }),
          (this.search = function (r, e, t, i, s, a) {
            if (!1 === r || "" === E.vakata.trim(r.toString()))
              return this.clear_search();
            (i = (i = this.get_node(i)) && (i.id || 0 === i.id) ? i.id : null),
              (r = r.toString());
            var n = this.settings.search,
              d = n.ajax || !1,
              o = this._model.data,
              c = null,
              l = [],
              h = [],
              _,
              g;
            if (
              (this._data.search.res.length && !s && this.clear_search(),
              t === P && (t = n.show_only_matches),
              a === P && (a = n.show_only_matches_children),
              !e && !1 !== d)
            )
              return E.vakata.is_function(d)
                ? d.call(
                    this,
                    r,
                    function (e) {
                      e && e.d && (e = e.d),
                        this._load_nodes(
                          E.vakata.is_array(e) ? E.vakata.array_unique(e) : [],
                          function () {
                            this.search(r, !0, t, i, s, a);
                          }
                        );
                    }.bind(this),
                    i
                  )
                : ((d = E.extend({}, d)).data || (d.data = {}),
                  (d.data.str = r),
                  i && (d.data.inside = i),
                  this._data.search.lastRequest &&
                    this._data.search.lastRequest.abort(),
                  (this._data.search.lastRequest = E.ajax(d)
                    .fail(
                      function () {
                        (this._data.core.last_error = {
                          error: "ajax",
                          plugin: "search",
                          id: "search_01",
                          reason: "Could not load search parents",
                          data: JSON.stringify(d),
                        }),
                          this.settings.core.error.call(
                            this,
                            this._data.core.last_error
                          );
                      }.bind(this)
                    )
                    .done(
                      function (e) {
                        e && e.d && (e = e.d),
                          this._load_nodes(
                            E.vakata.is_array(e)
                              ? E.vakata.array_unique(e)
                              : [],
                            function () {
                              this.search(r, !0, t, i, s, a);
                            }
                          );
                      }.bind(this)
                    )),
                  this._data.search.lastRequest);
            if (
              (s ||
                ((this._data.search.str = r),
                (this._data.search.dom = E()),
                (this._data.search.res = []),
                (this._data.search.opn = []),
                (this._data.search.som = t),
                (this._data.search.smc = a)),
              (c = new E.vakata.search(r, !0, {
                caseSensitive: n.case_sensitive,
                fuzzy: n.fuzzy,
              })),
              E.each(o[i || E.jstree.root].children_d, function (e, t) {
                var i = o[t];
                i.text &&
                  !i.state.hidden &&
                  (!n.search_leaves_only ||
                    (i.state.loaded && 0 === i.children.length)) &&
                  ((n.search_callback && n.search_callback.call(this, r, i)) ||
                    (!n.search_callback && c.search(i.text).isMatch)) &&
                  (l.push(t), (h = h.concat(i.parents)));
              }),
              l.length)
            ) {
              for (_ = 0, g = (h = E.vakata.array_unique(h)).length; _ < g; _++)
                h[_] !== E.jstree.root &&
                  o[h[_]] &&
                  !0 === this.open_node(h[_], null, 0) &&
                  this._data.search.opn.push(h[_]);
              s
                ? ((this._data.search.dom = this._data.search.dom.add(
                    E(
                      this.element[0].querySelectorAll(
                        "#" +
                          E.map(l, function (e) {
                            return -1 !== "0123456789".indexOf(e[0])
                              ? "\\3" +
                                  e[0] +
                                  " " +
                                  e.substr(1).replace(E.jstree.idregex, "\\$&")
                              : e.replace(E.jstree.idregex, "\\$&");
                          }).join(", #")
                      )
                    )
                  )),
                  (this._data.search.res = E.vakata.array_unique(
                    this._data.search.res.concat(l)
                  )))
                : ((this._data.search.dom = E(
                    this.element[0].querySelectorAll(
                      "#" +
                        E.map(l, function (e) {
                          return -1 !== "0123456789".indexOf(e[0])
                            ? "\\3" +
                                e[0] +
                                " " +
                                e.substr(1).replace(E.jstree.idregex, "\\$&")
                            : e.replace(E.jstree.idregex, "\\$&");
                        }).join(", #")
                    )
                  )),
                  (this._data.search.res = l)),
                this._data.search.dom
                  .children(".jstree-anchor")
                  .addClass("jstree-search");
            }
            this.trigger("search", {
              nodes: this._data.search.dom,
              str: r,
              res: this._data.search.res,
              show_only_matches: t,
            });
          }),
          (this.clear_search = function () {
            this.settings.search.close_opened_onclear &&
              this.close_node(this._data.search.opn, 0),
              this.trigger("clear_search", {
                nodes: this._data.search.dom,
                str: this._data.search.str,
                res: this._data.search.res,
              }),
              this._data.search.res.length &&
                ((this._data.search.dom = E(
                  this.element[0].querySelectorAll(
                    "#" +
                      E.map(this._data.search.res, function (e) {
                        return -1 !== "0123456789".indexOf(e[0])
                          ? "\\3" +
                              e[0] +
                              " " +
                              e.substr(1).replace(E.jstree.idregex, "\\$&")
                          : e.replace(E.jstree.idregex, "\\$&");
                      }).join(", #")
                  )
                )),
                this._data.search.dom
                  .children(".jstree-anchor")
                  .removeClass("jstree-search")),
              (this._data.search.str = ""),
              (this._data.search.res = []),
              (this._data.search.opn = []),
              (this._data.search.dom = E());
          }),
          (this.redraw_node = function (e, t, i, r) {
            if (
              (e = d.redraw_node.apply(this, arguments)) &&
              -1 !== E.inArray(e.id, this._data.search.res)
            ) {
              for (
                var s, a, n = null, s = 0, a = e.childNodes.length;
                s < a;
                s++
              )
                if (
                  e.childNodes[s] &&
                  e.childNodes[s].className &&
                  -1 !== e.childNodes[s].className.indexOf("jstree-anchor")
                ) {
                  n = e.childNodes[s];
                  break;
                }
              n && (n.className += " jstree-search");
            }
            return e;
          });
      }),
      ((w = E).vakata.search = function (p, e, m) {
        (m = m || {}),
          !1 !== (m = w.extend({}, w.vakata.search.defaults, m)).fuzzy &&
            (m.fuzzy = !0),
          (p = m.caseSensitive ? p : p.toLowerCase());
        var v = m.location,
          i = m.distance,
          j = m.threshold,
          k = p.length,
          b,
          y,
          x,
          t;
        return (
          32 < k && (m.fuzzy = !1),
          m.fuzzy &&
            ((b = 1 << (k - 1)),
            (y = (function () {
              for (var e = {}, t = 0, t = 0; t < k; t++) e[p.charAt(t)] = 0;
              for (t = 0; t < k; t++) e[p.charAt(t)] |= 1 << (k - t - 1);
              return e;
            })()),
            (x = function (e, t) {
              var e = e / k,
                t = Math.abs(v - t);
              return i ? e + t / i : t ? 1 : e;
            })),
          (t = function (e) {
            if (
              ((e = m.caseSensitive
                ? e.toString()
                : e.toString().toLowerCase()),
              p === e || -1 !== e.indexOf(p))
            )
              return { isMatch: !0, score: 0 };
            if (!m.fuzzy) return { isMatch: !1, score: 1 };
            var t,
              i,
              r = e.length,
              s = j,
              a = e.indexOf(p, v),
              n,
              d,
              o = k + r,
              c,
              l,
              h,
              _,
              g,
              u = 1,
              f = [];
            for (
              -1 !== a &&
                ((s = Math.min(x(0, a), s)),
                -1 !== (a = e.lastIndexOf(p, v + k)) &&
                  (s = Math.min(x(0, a), s))),
                a = -1,
                t = 0;
              t < k;
              t++
            ) {
              (n = 0), (d = o);
              while (n < d)
                x(t, v + d) <= s ? (n = d) : (o = d),
                  (d = Math.floor((o - n) / 2 + n));
              for (
                o = d,
                  l = Math.max(1, v - d + 1),
                  h = Math.min(v + d, r) + k,
                  (_ = new Array(h + 2))[h + 1] = (1 << t) - 1,
                  i = h;
                l <= i;
                i--
              )
                if (
                  ((g = y[e.charAt(i - 1)]),
                  (_[i] =
                    0 === t
                      ? ((_[i + 1] << 1) | 1) & g
                      : (((_[i + 1] << 1) | 1) & g) |
                        ((c[i + 1] | c[i]) << 1) |
                        1 |
                        c[i + 1]),
                  _[i] & b && (u = x(t, i - 1)) <= s)
                ) {
                  if (((s = u), f.push((a = i - 1)), !(v < a))) break;
                  l = Math.max(1, 2 * v - a);
                }
              if (x(t + 1, v) > s) break;
              c = _;
            }
            return { isMatch: 0 <= a, score: u };
          }),
          !0 === e ? { search: t } : t(e)
        );
      }),
      (w.vakata.search.defaults = {
        location: 0,
        distance: 100,
        threshold: 0.6,
        fuzzy: !1,
        caseSensitive: !1,
      }),
      (E.jstree.defaults.sort = function (e, t) {
        return this.get_text(e) > this.get_text(t) ? 1 : -1;
      });
    var m = !(E.jstree.plugins.sort = function (e, t) {
        (this.bind = function () {
          t.bind.call(this),
            this.element
              .on(
                "model.jstree",
                function (e, t) {
                  this.sort(t.parent, !0);
                }.bind(this)
              )
              .on(
                "rename_node.jstree create_node.jstree",
                function (e, t) {
                  this.sort(t.parent || t.node.parent, !1),
                    this.redraw_node(t.parent || t.node.parent, !0);
                }.bind(this)
              )
              .on(
                "move_node.jstree copy_node.jstree",
                function (e, t) {
                  this.sort(t.parent, !1), this.redraw_node(t.parent, !0);
                }.bind(this)
              );
        }),
          (this.sort = function (e, t) {
            var i, r;
            if (
              (e = this.get_node(e)) &&
              e.children &&
              e.children.length &&
              (e.children.sort(this.settings.sort.bind(this)), t)
            )
              for (i = 0, r = e.children_d.length; i < r; i++)
                this.sort(e.children_d[i], !1);
          });
      }),
      t,
      v;
    (E.jstree.defaults.state = {
      key: "jstree",
      events:
        "changed.jstree open_node.jstree close_node.jstree check_node.jstree uncheck_node.jstree",
      ttl: !1,
      filter: !1,
      preserve_loaded: !1,
    }),
      (E.jstree.plugins.state = function (e, t) {
        (this.bind = function () {
          t.bind.call(this);
          var i = function () {
            this.element.on(
              this.settings.state.events,
              function () {
                m && clearTimeout(m),
                  (m = setTimeout(
                    function () {
                      this.save_state();
                    }.bind(this),
                    100
                  ));
              }.bind(this)
            ),
              this.trigger("state_ready");
          }.bind(this);
          this.element.on(
            "ready.jstree",
            function (e, t) {
              this.element.one("restore_state.jstree", i),
                this.restore_state() || i();
            }.bind(this)
          );
        }),
          (this.save_state = function () {
            var e = this.get_state();
            this.settings.state.preserve_loaded || delete e.core.loaded;
            var e = {
              state: e,
              ttl: this.settings.state.ttl,
              sec: +new Date(),
            };
            E.vakata.storage.set(this.settings.state.key, JSON.stringify(e));
          }),
          (this.restore_state = function () {
            var i = E.vakata.storage.get(this.settings.state.key);
            if (i)
              try {
                i = JSON.parse(i);
              } catch (e) {
                return !1;
              }
            return (
              !(i && i.ttl && i.sec && +new Date() - i.sec > i.ttl) &&
              !!(i =
                (i = i && i.state ? i.state : i) &&
                E.vakata.is_function(this.settings.state.filter)
                  ? this.settings.state.filter.call(this, i)
                  : i) &&
              (this.settings.state.preserve_loaded || delete i.core.loaded,
              this.element.one("set_state.jstree", function (e, t) {
                t.instance.trigger("restore_state", {
                  state: E.extend(!0, {}, i),
                });
              }),
              this.set_state(i),
              !0)
            );
          }),
          (this.clear_state = function () {
            return E.vakata.storage.del(this.settings.state.key);
          });
      }),
      (E.vakata.storage = {
        set: function (e, t) {
          return window.localStorage.setItem(e, t);
        },
        get: function (e) {
          return window.localStorage.getItem(e);
        },
        del: function (e) {
          return window.localStorage.removeItem(e);
        },
      }),
      (E.jstree.defaults.types = { default: {} }),
      (E.jstree.defaults.types[E.jstree.root] = {}),
      (E.jstree.plugins.types = function (e, l) {
        (this.init = function (e, t) {
          var i, r;
          if (t && t.types && t.types.default)
            for (i in t.types)
              if (
                "default" !== i &&
                i !== E.jstree.root &&
                t.types.hasOwnProperty(i)
              )
                for (r in t.types.default)
                  t.types.default.hasOwnProperty(r) &&
                    t.types[i][r] === P &&
                    (t.types[i][r] = t.types.default[r]);
          l.init.call(this, e, t),
            (this._model.data[E.jstree.root].type = E.jstree.root);
        }),
          (this.refresh = function (e, t) {
            l.refresh.call(this, e, t),
              (this._model.data[E.jstree.root].type = E.jstree.root);
          }),
          (this.bind = function () {
            this.element.on(
              "model.jstree",
              function (e, t) {
                for (
                  var i = this._model.data,
                    r = t.nodes,
                    s = this.settings.types,
                    a,
                    n,
                    d = "default",
                    o,
                    a = 0,
                    n = r.length;
                  a < n;
                  a++
                ) {
                  if (
                    ((d = "default"),
                    i[r[a]].original &&
                      i[r[a]].original.type &&
                      s[i[r[a]].original.type] &&
                      (d = i[r[a]].original.type),
                    i[r[a]].data &&
                      i[r[a]].data.jstree &&
                      i[r[a]].data.jstree.type &&
                      s[i[r[a]].data.jstree.type] &&
                      (d = i[r[a]].data.jstree.type),
                    (i[r[a]].type = d),
                    !0 === i[r[a]].icon &&
                      s[d].icon !== P &&
                      (i[r[a]].icon = s[d].icon),
                    s[d].li_attr !== P && "object" == typeof s[d].li_attr)
                  )
                    for (o in s[d].li_attr)
                      s[d].li_attr.hasOwnProperty(o) &&
                        "id" !== o &&
                        (i[r[a]].li_attr[o] === P
                          ? (i[r[a]].li_attr[o] = s[d].li_attr[o])
                          : "class" === o &&
                            (i[r[a]].li_attr.class =
                              s[d].li_attr.class +
                              " " +
                              i[r[a]].li_attr.class));
                  if (s[d].a_attr !== P && "object" == typeof s[d].a_attr)
                    for (o in s[d].a_attr)
                      s[d].a_attr.hasOwnProperty(o) &&
                        "id" !== o &&
                        (i[r[a]].a_attr[o] === P
                          ? (i[r[a]].a_attr[o] = s[d].a_attr[o])
                          : "href" === o && "#" === i[r[a]].a_attr[o]
                          ? (i[r[a]].a_attr.href = s[d].a_attr.href)
                          : "class" === o &&
                            (i[r[a]].a_attr.class =
                              s[d].a_attr.class + " " + i[r[a]].a_attr.class));
                }
                i[E.jstree.root].type = E.jstree.root;
              }.bind(this)
            ),
              l.bind.call(this);
          }),
          (this.get_json = function (e, t, i) {
            var r,
              s,
              a = this._model.data,
              n = t ? E.extend(!0, {}, t, { no_id: !1 }) : {},
              d = l.get_json.call(this, e, n, i);
            if (!1 === d) return !1;
            if (E.vakata.is_array(d))
              for (r = 0, s = d.length; r < s; r++)
                (d[r].type =
                  (d[r].id || 0 === d[r].id) && a[d[r].id] && a[d[r].id].type
                    ? a[d[r].id].type
                    : "default"),
                  t &&
                    t.no_id &&
                    (delete d[r].id,
                    d[r].li_attr && d[r].li_attr.id && delete d[r].li_attr.id,
                    d[r].a_attr && d[r].a_attr.id && delete d[r].a_attr.id);
            else
              (d.type =
                (d.id || 0 === d.id) && a[d.id] && a[d.id].type
                  ? a[d.id].type
                  : "default"),
                t && t.no_id && (d = this._delete_ids(d));
            return d;
          }),
          (this._delete_ids = function (e) {
            if (E.vakata.is_array(e)) {
              for (var t = 0, i = e.length; t < i; t++)
                e[t] = this._delete_ids(e[t]);
              return e;
            }
            return (
              delete e.id,
              e.li_attr && e.li_attr.id && delete e.li_attr.id,
              e.a_attr && e.a_attr.id && delete e.a_attr.id,
              e.children &&
                E.vakata.is_array(e.children) &&
                (e.children = this._delete_ids(e.children)),
              e
            );
          }),
          (this.check = function (e, t, i, r, s) {
            if (!1 === l.check.call(this, e, t, i, r, s)) return !1;
            (t = t && (t.id || 0 === t.id) ? t : this.get_node(t)),
              (i = i && (i.id || 0 === i.id) ? i : this.get_node(i));
            var a,
              n,
              d,
              o,
              c,
              a =
                (a =
                  t && (t.id || 0 === t.id)
                    ? s && s.origin
                      ? s.origin
                      : E.jstree.reference(t.id)
                    : null) &&
                a._model &&
                a._model.data
                  ? a._model.data
                  : null;
            switch (e) {
              case "create_node":
              case "move_node":
              case "copy_node":
                if ("move_node" !== e || -1 === E.inArray(t.id, i.children)) {
                  if (
                    (n = this.get_rules(i)).max_children !== P &&
                    -1 !== n.max_children &&
                    n.max_children === i.children.length
                  )
                    return !(this._data.core.last_error = {
                      error: "check",
                      plugin: "types",
                      id: "types_01",
                      reason: "max_children prevents function: " + e,
                      data: JSON.stringify({
                        chk: e,
                        pos: r,
                        obj: !(!t || (!t.id && 0 !== t.id)) && t.id,
                        par: !(!i || (!i.id && 0 !== i.id)) && i.id,
                      }),
                    });
                  if (
                    n.valid_children !== P &&
                    -1 !== n.valid_children &&
                    -1 === E.inArray(t.type || "default", n.valid_children)
                  )
                    return !(this._data.core.last_error = {
                      error: "check",
                      plugin: "types",
                      id: "types_02",
                      reason: "valid_children prevents function: " + e,
                      data: JSON.stringify({
                        chk: e,
                        pos: r,
                        obj: !(!t || (!t.id && 0 !== t.id)) && t.id,
                        par: !(!i || (!i.id && 0 !== i.id)) && i.id,
                      }),
                    });
                  if (a && t.children_d && t.parents) {
                    for (o = d = 0, c = t.children_d.length; o < c; o++)
                      d = Math.max(d, a[t.children_d[o]].parents.length);
                    d = d - t.parents.length + 1;
                  }
                  (d <= 0 || d === P) && (d = 1);
                  do {
                    if (
                      n.max_depth !== P &&
                      -1 !== n.max_depth &&
                      n.max_depth < d
                    )
                      return !(this._data.core.last_error = {
                        error: "check",
                        plugin: "types",
                        id: "types_03",
                        reason: "max_depth prevents function: " + e,
                        data: JSON.stringify({
                          chk: e,
                          pos: r,
                          obj: !(!t || (!t.id && 0 !== t.id)) && t.id,
                          par: !(!i || (!i.id && 0 !== i.id)) && i.id,
                        }),
                      });
                    (i = this.get_node(i.parent)), (n = this.get_rules(i)), d++;
                  } while (i);
                }
            }
            return !0;
          }),
          (this.get_rules = function (e) {
            if (!(e = this.get_node(e))) return !1;
            var e = this.get_type(e, !0);
            return (
              e.max_depth === P && (e.max_depth = -1),
              e.max_children === P && (e.max_children = -1),
              e.valid_children === P && (e.valid_children = -1),
              e
            );
          }),
          (this.get_type = function (e, t) {
            return (
              !!(e = this.get_node(e)) &&
              (t
                ? E.extend({ type: e.type }, this.settings.types[e.type])
                : e.type)
            );
          }),
          (this.set_type = function (e, t) {
            var i = this._model.data,
              r,
              s,
              a,
              n,
              d,
              o,
              c,
              l;
            if (E.vakata.is_array(e)) {
              for (s = 0, a = (e = e.slice()).length; s < a; s++)
                this.set_type(e[s], t);
              return !0;
            }
            if (
              ((r = this.settings.types), (e = this.get_node(e)), !r[t] || !e)
            )
              return !1;
            if (
              ((c = this.get_node(e, !0)) &&
                c.length &&
                (l = c.children(".jstree-anchor")),
              (n = e.type),
              (d = this.get_icon(e)),
              (e.type = t),
              (!0 !== d && r[n] && (r[n].icon === P || d !== r[n].icon)) ||
                this.set_icon(e, r[t].icon === P || r[t].icon),
              r[n] && r[n].li_attr !== P && "object" == typeof r[n].li_attr)
            )
              for (o in r[n].li_attr)
                r[n].li_attr.hasOwnProperty(o) &&
                  "id" !== o &&
                  ("class" === o
                    ? ((i[e.id].li_attr.class = (
                        i[e.id].li_attr.class || ""
                      ).replace(r[n].li_attr[o], "")),
                      c && c.removeClass(r[n].li_attr[o]))
                    : i[e.id].li_attr[o] === r[n].li_attr[o] &&
                      ((i[e.id].li_attr[o] = null), c && c.removeAttr(o)));
            if (r[n] && r[n].a_attr !== P && "object" == typeof r[n].a_attr)
              for (o in r[n].a_attr)
                r[n].a_attr.hasOwnProperty(o) &&
                  "id" !== o &&
                  ("class" === o
                    ? ((i[e.id].a_attr.class = (
                        i[e.id].a_attr.class || ""
                      ).replace(r[n].a_attr[o], "")),
                      l && l.removeClass(r[n].a_attr[o]))
                    : i[e.id].a_attr[o] === r[n].a_attr[o] &&
                      ("href" === o
                        ? ((i[e.id].a_attr[o] = "#"), l && l.attr("href", "#"))
                        : (delete i[e.id].a_attr[o], l && l.removeAttr(o))));
            if (r[t].li_attr !== P && "object" == typeof r[t].li_attr)
              for (o in r[t].li_attr)
                r[t].li_attr.hasOwnProperty(o) &&
                  "id" !== o &&
                  (i[e.id].li_attr[o] === P
                    ? ((i[e.id].li_attr[o] = r[t].li_attr[o]),
                      c &&
                        ("class" === o
                          ? c.addClass(r[t].li_attr[o])
                          : c.attr(o, r[t].li_attr[o])))
                    : "class" === o &&
                      ((i[e.id].li_attr.class =
                        r[t].li_attr[o] + " " + i[e.id].li_attr.class),
                      c && c.addClass(r[t].li_attr[o])));
            if (r[t].a_attr !== P && "object" == typeof r[t].a_attr)
              for (o in r[t].a_attr)
                r[t].a_attr.hasOwnProperty(o) &&
                  "id" !== o &&
                  (i[e.id].a_attr[o] === P
                    ? ((i[e.id].a_attr[o] = r[t].a_attr[o]),
                      l &&
                        ("class" === o
                          ? l.addClass(r[t].a_attr[o])
                          : l.attr(o, r[t].a_attr[o])))
                    : "href" === o && "#" === i[e.id].a_attr[o]
                    ? ((i[e.id].a_attr.href = r[t].a_attr.href),
                      l && l.attr("href", r[t].a_attr.href))
                    : "class" === o &&
                      ((i[e.id].a_attr.class =
                        r[t].a_attr.class + " " + i[e.id].a_attr.class),
                      l && l.addClass(r[t].a_attr[o])));
            return !0;
          });
      }),
      (E.jstree.defaults.unique = {
        case_sensitive: !1,
        trim_whitespace: !1,
        duplicate: function (e, t) {
          return e + " (" + t + ")";
        },
      }),
      (E.jstree.plugins.unique = function (e, f) {
        (this.check = function (e, t, i, r, s) {
          if (!1 === f.check.call(this, e, t, i, r, s)) return !1;
          if (
            ((t = t && (t.id || 0 === t.id) ? t : this.get_node(t)),
            !(i = i && (i.id || 0 === i.id) ? i : this.get_node(i)) ||
              !i.children)
          )
            return !0;
          for (
            var a = "rename_node" === e ? r : t.text,
              n = [],
              d = this.settings.unique.case_sensitive,
              o = this.settings.unique.trim_whitespace,
              c = this._model.data,
              l,
              h,
              _,
              l = 0,
              h = i.children.length;
            l < h;
            l++
          )
            (_ = c[i.children[l]].text),
              d || (_ = _.toLowerCase()),
              o && (_ = _.replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, "")),
              n.push(_);
          switch (
            (d || (a = a.toLowerCase()),
            o && (a = a.replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, "")),
            e)
          ) {
            case "delete_node":
              return !0;
            case "rename_node":
              return (
                (_ = t.text || ""),
                d || (_ = _.toLowerCase()),
                o && (_ = _.replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, "")),
                (l = -1 === E.inArray(a, n) || (t.text && _ === a)) ||
                  (this._data.core.last_error = {
                    error: "check",
                    plugin: "unique",
                    id: "unique_01",
                    reason:
                      "Child with name " +
                      a +
                      " already exists. Preventing: " +
                      e,
                    data: JSON.stringify({
                      chk: e,
                      pos: r,
                      obj: !(!t || (!t.id && 0 !== t.id)) && t.id,
                      par: !(!i || (!i.id && 0 !== i.id)) && i.id,
                    }),
                  }),
                l
              );
            case "create_node":
              return (
                (l = -1 === E.inArray(a, n)) ||
                  (this._data.core.last_error = {
                    error: "check",
                    plugin: "unique",
                    id: "unique_04",
                    reason:
                      "Child with name " +
                      a +
                      " already exists. Preventing: " +
                      e,
                    data: JSON.stringify({
                      chk: e,
                      pos: r,
                      obj: !(!t || (!t.id && 0 !== t.id)) && t.id,
                      par: !(!i || (!i.id && 0 !== i.id)) && i.id,
                    }),
                  }),
                l
              );
            case "copy_node":
              return (
                (l = -1 === E.inArray(a, n)) ||
                  (this._data.core.last_error = {
                    error: "check",
                    plugin: "unique",
                    id: "unique_02",
                    reason:
                      "Child with name " +
                      a +
                      " already exists. Preventing: " +
                      e,
                    data: JSON.stringify({
                      chk: e,
                      pos: r,
                      obj: !(!t || (!t.id && 0 !== t.id)) && t.id,
                      par: !(!i || (!i.id && 0 !== i.id)) && i.id,
                    }),
                  }),
                l
              );
            case "move_node":
              return (
                (l =
                  (t.parent === i.id && (!s || !s.is_multi)) ||
                  -1 === E.inArray(a, n)) ||
                  (this._data.core.last_error = {
                    error: "check",
                    plugin: "unique",
                    id: "unique_03",
                    reason:
                      "Child with name " +
                      a +
                      " already exists. Preventing: " +
                      e,
                    data: JSON.stringify({
                      chk: e,
                      pos: r,
                      obj: !(!t || (!t.id && 0 !== t.id)) && t.id,
                      par: !(!i || (!i.id && 0 !== i.id)) && i.id,
                    }),
                  }),
                l
              );
          }
          return !0;
        }),
          (this.create_node = function (e, t, i, r, s) {
            if (!t || ("object" == typeof t && t.text === P)) {
              if ((null === e && (e = E.jstree.root), !(e = this.get_node(e))))
                return f.create_node.call(this, e, t, i, r, s);
              if (
                !(i = i === P ? "last" : i)
                  .toString()
                  .match(/^(before|after)$/) &&
                !s &&
                !this.is_loaded(e)
              )
                return f.create_node.call(this, e, t, i, r, s);
              t = t || {};
              for (
                var a,
                  n,
                  d,
                  o,
                  c,
                  l = this._model.data,
                  h = this.settings.unique.case_sensitive,
                  _ = this.settings.unique.trim_whitespace,
                  g = this.settings.unique.duplicate,
                  u,
                  n = (a = this.get_string("New node")),
                  d = [],
                  o = 0,
                  c = e.children.length;
                o < c;
                o++
              )
                (u = l[e.children[o]].text),
                  h || (u = u.toLowerCase()),
                  _ &&
                    (u = u.replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, "")),
                  d.push(u);
              (o = 1),
                (u = n),
                h || (u = u.toLowerCase()),
                _ && (u = u.replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, ""));
              while (-1 !== E.inArray(u, d))
                (u = n = g.call(this, a, ++o).toString()),
                  h || (u = u.toLowerCase()),
                  _ &&
                    (u = u.replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, ""));
              t.text = n;
            }
            return f.create_node.call(this, e, t, i, r, s);
          });
      });
    var j = b.createElement("DIV");
    if (
      (j.setAttribute("unselectable", "on"),
      j.setAttribute("role", "presentation"),
      (j.className = "jstree-wholerow"),
      (j.innerHTML = "&#160;"),
      (E.jstree.plugins.wholerow = function (e, a) {
        (this.bind = function () {
          a.bind.call(this),
            this.element
              .on(
                "ready.jstree set_state.jstree",
                function () {
                  this.hide_dots();
                }.bind(this)
              )
              .on(
                "init.jstree loading.jstree ready.jstree",
                function () {
                  this.get_container_ul().addClass("jstree-wholerow-ul");
                }.bind(this)
              )
              .on(
                "deselect_all.jstree",
                function (e, t) {
                  this.element
                    .find(".jstree-wholerow-clicked")
                    .removeClass("jstree-wholerow-clicked");
                }.bind(this)
              )
              .on(
                "changed.jstree",
                function (e, t) {
                  this.element
                    .find(".jstree-wholerow-clicked")
                    .removeClass("jstree-wholerow-clicked");
                  for (
                    var i = !1, r, s, r = 0, s = t.selected.length;
                    r < s;
                    r++
                  )
                    (i = this.get_node(t.selected[r], !0)) &&
                      i.length &&
                      i
                        .children(".jstree-wholerow")
                        .addClass("jstree-wholerow-clicked");
                }.bind(this)
              )
              .on(
                "open_node.jstree",
                function (e, t) {
                  this.get_node(t.node, !0)
                    .find(".jstree-clicked")
                    .parent()
                    .children(".jstree-wholerow")
                    .addClass("jstree-wholerow-clicked");
                }.bind(this)
              )
              .on(
                "hover_node.jstree dehover_node.jstree",
                function (e, t) {
                  ("hover_node" === e.type && this.is_disabled(t.node)) ||
                    this.get_node(t.node, !0)
                      .children(".jstree-wholerow")
                      ["hover_node" === e.type ? "addClass" : "removeClass"](
                        "jstree-wholerow-hovered"
                      );
                }.bind(this)
              )
              .on(
                "contextmenu.jstree",
                ".jstree-wholerow",
                function (e) {
                  var t;
                  this._data.contextmenu &&
                    (e.preventDefault(),
                    (t = E.Event("contextmenu", {
                      metaKey: e.metaKey,
                      ctrlKey: e.ctrlKey,
                      altKey: e.altKey,
                      shiftKey: e.shiftKey,
                      pageX: e.pageX,
                      pageY: e.pageY,
                    })),
                    E(e.currentTarget)
                      .closest(".jstree-node")
                      .children(".jstree-anchor")
                      .first()
                      .trigger(t));
                }.bind(this)
              )
              .on("click.jstree", ".jstree-wholerow", function (e) {
                e.stopImmediatePropagation();
                var t = E.Event("click", {
                  metaKey: e.metaKey,
                  ctrlKey: e.ctrlKey,
                  altKey: e.altKey,
                  shiftKey: e.shiftKey,
                });
                E(e.currentTarget)
                  .closest(".jstree-node")
                  .children(".jstree-anchor")
                  .first()
                  .trigger(t)
                  .trigger("focus");
              })
              .on("dblclick.jstree", ".jstree-wholerow", function (e) {
                e.stopImmediatePropagation();
                var t = E.Event("dblclick", {
                  metaKey: e.metaKey,
                  ctrlKey: e.ctrlKey,
                  altKey: e.altKey,
                  shiftKey: e.shiftKey,
                });
                E(e.currentTarget)
                  .closest(".jstree-node")
                  .children(".jstree-anchor")
                  .first()
                  .trigger(t)
                  .trigger("focus");
              })
              .on(
                "click.jstree",
                ".jstree-leaf > .jstree-ocl",
                function (e) {
                  e.stopImmediatePropagation();
                  var t = E.Event("click", {
                    metaKey: e.metaKey,
                    ctrlKey: e.ctrlKey,
                    altKey: e.altKey,
                    shiftKey: e.shiftKey,
                  });
                  E(e.currentTarget)
                    .closest(".jstree-node")
                    .children(".jstree-anchor")
                    .first()
                    .trigger(t)
                    .trigger("focus");
                }.bind(this)
              )
              .on(
                "mouseover.jstree",
                ".jstree-wholerow, .jstree-icon",
                function (e) {
                  return (
                    e.stopImmediatePropagation(),
                    this.is_disabled(e.currentTarget) ||
                      this.hover_node(e.currentTarget),
                    !1
                  );
                }.bind(this)
              )
              .on(
                "mouseleave.jstree",
                ".jstree-node",
                function (e) {
                  this.dehover_node(e.currentTarget);
                }.bind(this)
              );
        }),
          (this.teardown = function () {
            this.settings.wholerow &&
              this.element.find(".jstree-wholerow").remove(),
              a.teardown.call(this);
          }),
          (this.redraw_node = function (e, t, i, r) {
            var s;
            return (
              (e = a.redraw_node.apply(this, arguments)) &&
                ((s = j.cloneNode(!0)),
                -1 !== E.inArray(e.id, this._data.core.selected) &&
                  (s.className += " jstree-wholerow-clicked"),
                this._data.core.focused &&
                  this._data.core.focused === e.id &&
                  (s.className += " jstree-wholerow-hovered"),
                e.insertBefore(s, e.childNodes[0])),
              e
            );
          });
      }),
      window.customElements && Object && Object.create)
    ) {
      var e = Object.create(HTMLElement.prototype);
      e.createdCallback = function () {
        var e = { core: {}, plugins: [] },
          t;
        for (t in E.jstree.plugins)
          E.jstree.plugins.hasOwnProperty(t) &&
            this.attributes[t] &&
            (e.plugins.push(t),
            this.getAttribute(t) &&
              JSON.parse(this.getAttribute(t)) &&
              (e[t] = JSON.parse(this.getAttribute(t))));
        for (t in E.jstree.defaults.core)
          E.jstree.defaults.core.hasOwnProperty(t) &&
            this.attributes[t] &&
            (e.core[t] =
              JSON.parse(this.getAttribute(t)) || this.getAttribute(t));
        E(this).jstree(e);
      };
      try {
        window.customElements.define("vakata-jstree", function () {}, {
          prototype: e,
        });
      } catch (e) {}
    }
  }
});
