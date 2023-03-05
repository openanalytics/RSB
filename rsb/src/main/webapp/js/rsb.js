/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2023
 *
 *   ===========================================================================
 *
 *   This file is part of R Service Bus.
 *
 *   R Service Bus is free software: you can redistribute it and/or modify
 *   it under the terms of the Apache License as published by
 *   The Apache Software Foundation, either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   Apache License for more details.
 *
 *   You should have received a copy of the Apache License
 *   along with R Service Bus.  If not, see <http://www.apache.org/licenses/>.
 *
 *   @author rsb.development@openanalytics.eu
 */

var remoteDataRoot;
var remoteDataSelectionContext;
var selectedRemoteDataNode;

var urlParams = {};
(function () {
    var e,
        a = /\+/g,  // Regex for replacing addition symbol with a space
        r = /([^&;=]+)=?([^&;]*)/g,
        d = function (s) { return decodeURIComponent(s.replace(a, " ")); },
        q = window.location.search.substring(1);

    while (e = r.exec(q))
       urlParams[d(e[1])] = d(e[2]);
})();

function xmlTimeStampToDate(xmlDate)
{
    var dt = new Date();
    var dtS = xmlDate.slice(xmlDate.indexOf('T')+1, xmlDate.indexOf('.'))
    var TimeArray = dtS.split(":");
    dt.setUTCHours(TimeArray[0],TimeArray[1],TimeArray[2]);
    dtS = xmlDate.slice(0, xmlDate.indexOf('T'))
    TimeArray = dtS.split("-");
    dt.setUTCFullYear(TimeArray[0],TimeArray[1]-1,TimeArray[2]);
    return dt;
}

function loadApplicationResults(applicationName, highlightJobId) {
  $.ajax({ url: "api/rest/results/" + applicationName,
           cache: false,
           success: function(responseXML) {
             
    var tableBody = $("#resultsTableBody");
    tableBody.empty();
    
    $(responseXML).find('result').each(function(){
     var jobId = $(this).attr("jobId");
     var resultUri = $(this).attr("selfUri");
     var dataUri = $(this).attr("dataUri");
     var timestamp = $(this).attr("resultTime");
     
     tableBody.append("<tr id='res-" + jobId + "' class='" + (jobId === highlightJobId ? "highlighted" : "") + "'><td>"
                      + $(this).attr("applicationName")
                      + "</td><td>"
                      + xmlTimeStampToDate(timestamp)
                      + "</td><td>"
                      + jobId
                      + "</td><td>"
                      + "<img src='images/"
                      + (($(this).attr("success")==='true')?"success.gif":"failure.png")
                      + "' title='Status' border='0' />&nbsp;"
                      + $(this).attr("type")
                      + "</td><td>"
                      + "<a href='" + dataUri + "' target='_blank' id='getres-"+jobId+"'><img src='images/download.gif' title='Download' border='0' /></a>"
                      + "&nbsp;&nbsp;"
                      + "<a href='#' id='delres-"+jobId+"'><img src='images/delete.png' title='Delete' border='0' /></a>"
                      +"</td></tr>");
    
     $("#delres-"+jobId).click(function() {
       var shouldDelete = confirm("Are you sure you want to delete the result of job: " + jobId + "?");
       if (shouldDelete) {
         $.ajax({
           type       : 'DELETE',
           url        : resultUri,
           jobId      : jobId,
                     
           success: function(data, textStatus, xhr) {
             $("#res-"+jobId).hide();
           },
         
           error: function(jqXHR, textStatus, errorThrown) {
             // DELETE returns No Content, which may yield a parse issue -> if this happens, reload all the results
             loadApplicationResults(applicationName);
           }
         });
       }
     });
    });
    
    $('#resultsPanel').show(250);
  }});
}

function callCallback(cb) {
  if (cb && typeof(cb) === "function") {
    cb();
  }
}

var remoteDataNode = function(remoteNode, type) {
  var name;
  var attr = {};
  var empty = true;
  var children = [];
  var fetched = false;
  
  if (remoteNode) {
    name = remoteNode.name;
    attr = {path: remoteNode.path, uri: remoteNode.uri, type: type, baseType: type=='drive'?'directory':type};
    if (remoteNode.empty != undefined) empty = remoteNode.empty;
  }
  
  var o = $({});
  
  o.extend({
    markFetched : function() {
      fetched = true;
    },
    openNode: function(cb) {
      if (attr.uri) {
        if (!fetched) {
          $.ajax({
            type       : 'GET',
            url        : attr.uri,
            dataType   : 'json',
                      
            success: function(data) {
              var newChildren = [];
              
              // add child directories
              var childDirectoryOrIes = data.directory.directory;
              if (childDirectoryOrIes) {
                var childDirectories = childDirectoryOrIes instanceof Array ? childDirectoryOrIes : [childDirectoryOrIes];
                for(var key in childDirectories) {
                  var childDirectory = childDirectories[key];
                  var newChild = remoteDataNode(childDirectory, o.getType()=='network'?'drive':'directory'); 
                  newChildren.push(newChild);
                }
              }
              
              // add child files
              var childFileOrFiles = data.directory.file;
              if (childFileOrFiles) {
                var childFiles = childFileOrFiles instanceof Array ? childFileOrFiles : [childFileOrFiles];
                for(var key in childFiles) {
                  var childFile = childFiles[key];
                  var newChild = remoteDataNode(childFile, 'file'); 
                  newChildren.push(newChild);
                }
              }
              
              o.markFetched();
              remoteDataRoot.trigger("addChildren.jstree",[o,newChildren]);
              callCallback(cb);
            }
          });
        } else {
          callCallback(cb);
        }
      } else {
        remoteDataRoot.trigger("addChildren.jstree",[this,children]);
        callCallback(cb);
      }
    },
    closeNode: function() {
      // NOOP
    },
    hasChildren : function() {
      return attr.path == '/' || !empty || children.length > 0;
    },
    addChild : function(child) {
      children.push(child);
      return this;
    },
    removeChild : function(i) {
      var child;
      if (i>=children.length) {
        child = children.pop();
      } else {
        child = children[i];
        delete children[i];
      }
      remoteDataRoot.trigger("removeChildren.jstree",[this,child]);               
      return this;
    },
    getAttr : function() {
      return attr;
    },
    getName : function() {
      return {title: name};
    },
    getProps : function() {
      return attr;
    },
    getType : function() {
      return type;
    }
  });
  
  return o;
};

function initializeRemoteDataTree(data) {
  $('#remoteDataTree').jstree({
    themes : {
      theme : "classic",
      url : "./css/jstree-classic/style.css",
      dots : true,
      icons : true
    },
    model_data: {
      data: function(){return(remoteDataRoot);}, 
      progressive_render: true, 
      type_attr: 'rel'
    },
    ui : {
      select_limit : 1
    },
    types : {
      max_depth : -2,
      max_children : -2,
      valid_children : ['network'],
      types : {
        network : {
          valid_children : ['drive'],
          icon : {
            image : 'images/network.png'
          }
        },
        drive : {
          valid_children : ['file', 'directory'],
          icon : {
            image : 'images/drive.png'
          }
        },
        directory : {
          valid_children : ['file', 'directory'],
          icon : {
            image : 'images/directory.png'
          }
        },
        file : {
          valid_children : 'none',
          icon : {
            image : 'images/file.png'
          }
        }
      }      
    },
    plugins : [ 'themes', 'types', 'ui', 'model_data' ]
  }).bind('select_node.jstree', function (event, data) {
    selectedRemoteDataNode = data.rslt.obj;
  });
  
  remoteDataRoot = remoteDataNode({name:'Remote Data',uri:'api/rest/data',path:'/'}, 'network');
  remoteDataRoot = remoteDataNode().addChild(remoteDataRoot);
  log.info('Remote Data Browser Initialized');
}

function showRemoteFileSelector(selectionType, targetInput) {
  if ((selectionType != 'file') && (selectionType != 'directory')) {
    alert("Unsupported section type: " + selectionType);
    return false;
  }
  $('#remoteDataSelectorType').text(selectionType);
  remoteDataSelectionContext = {selectionType: selectionType, targetInput: targetInput};
  $('#remoteDataSelector').dialog('open');
  return false;
}

function showErrorDialog(message) {
  $('#errorDialogText').text(message);
  $('#errorDialog').dialog('open');
}

$(document).ready(function() {
  // Dialogs
  $('#remoteDataSelector').dialog({
    modal: true,
    autoOpen: false,
    width: 600,
    buttons: {
      "Select": function() { 
        if ((!selectedRemoteDataNode) || (selectedRemoteDataNode.attr('baseType') != remoteDataSelectionContext.selectionType)) {
          showErrorDialog("You must select a "+remoteDataSelectionContext.selectionType);
          return false;
        }
        $('#'+remoteDataSelectionContext.targetInput).val(selectedRemoteDataNode.attr('path'));
        $(this).dialog("close"); 
      }, 
      "Cancel": function() { 
        $(this).dialog("close"); 
      } 
    }
  });
  
  $('#errorDialog').dialog({
    modal: true,
    autoOpen: false,
    buttons: {
      Ok: function() {
        $(this).dialog("close");
      }
    }
  });
  
  // Panels
  $('#requiredParamsPanel').panel({
      collapsible:false,
      collapsed:true
  });
  $('#optionalParamsPanel').panel({
      collapsed:true
  });
  
  $('#runningJobsPanel').panel({
      collapsed:false
  });
  $('#runningJobsPanel').hide();
  
  $('#resultsPanel').panel({
      collapsed:false
  });
  $('#resultsPanel').hide();
  
  // Application Results Loading
  $('#applicationResultsButton').click(function() {
      var applicationName = $('#applicationName').val();
      
      if (applicationName.length == 0) {
        alert("You must provide an application name for retrieving its results!");
      } else {
        loadApplicationResults(applicationName);
      }
    });
 
  // Upload Form and Ajax Job Progress Monitor
  $('#jobUploadForm').validate();
  
  $('#jobUploadForm').ajaxForm({
      dataType: 'xml',
      success: function(responseXML, textStatus, xhr) {
          if(RSB_FORM_TITLE != 'RSB - qPCR') {
              // reset the file selector only
              $('#jobFileSelector').attr({ value: '' });
              $('#jobFileSelector').MultiFile('reset');
          }
          
          var response = $('jobToken', responseXML)
          
          var jobId = response.attr('jobId');
          var appName = response.attr('applicationName');
          var resultUri = response.attr('resultUri');

          if (!jobId) {
            if ($.browser.msie) {
              var xml = new ActiveXObject("Microsoft.XMLDOM");
              xml.async = "false";
              xml.loadXML($(responseXML).text());
              var attributes = xml.documentElement.attributes;
              jobId = attributes.getNamedItem('jobId').value;
              appName = attributes.getNamedItem('applicationName').value;
              resultUri = attributes.getNamedItem('resultUri').value;
            } else {
              // something went wrong: display the server response as-is
              alert($(responseXML).text());
              return;
            }
          }
          
          $('#runningJobsPanel').show(250);
           
          $('#runningJobsTableBody').append("<tr id='run-" + jobId + "'><td>"
              + appName
              + "</td><td>"
              + Date.today().setTimeToNow()
              + "</td><td>"
              + jobId
              +"</td></tr>");

          $.ajax({
              type       : 'GET',
              url        : resultUri,
              appName    : appName,
              jobId      : jobId,
                        
              success: function(data, textStatus, xhr) {
                $('#run-' + jobId).hide();
                loadApplicationResults(appName, jobId);
              },

              error: function(xhr, textStatus, errorThrown) {
                // try again after 5 secs
                var reAjax = this;
                $.doTimeout(5000, function() { $.ajax(reAjax) });
              }
          });
      }
  });
  
  // Setup page appearance
  document.title = RSB_FORM_TITLE;
  $('#formTitle').html(RSB_FORM_TITLE);

  // Remote data browser
  $.ajax({
    type       : 'GET',
    url        : 'api/rest/data',
    dataType   : 'json',
              
    success: function(data, textStatus, xhr) {
      initializeRemoteDataTree(data);
    }
  });
  
  log.info('RSB UI Ready');
});     
