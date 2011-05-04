/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2011
 *
 *   ===========================================================================
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   @author rsb.development@openanalytics.eu
 */

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
    dt.setUTCFullYear(TimeArray[0],TimeArray[1],TimeArray[2]);
    return dt;
}

function loadApplicationResults(applicationName, highlightJobId) {
  $.ajax({ url: "api/rest/results/" + applicationName,
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

$(document).ready(function() {
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
        alert('You must provide an application name for retrieving its results!');
      } else {
        loadApplicationResults(applicationName);
      }
    });
 
  // Upload Form and Ajax Job Progress Monitor
  $("#jobUploadForm").validate();
  
  $('#jobUploadForm').ajaxForm({
      beforeSubmit: function(a,f,o) {
        o.dataType = 'xml';
      },
      
      success: function(responseXML, textStatus, xhr) {
          // reset the file selector only
          $('#jobFileSelector').attr({ value: '' });
          $('#jobFileSelector').MultiFile('reset');
          
          var response = $('jobToken', responseXML)
          var jobId = response.attr('jobId');

          if (!jobId) {
            // something went wrong: display the server response as-is
            alert($(responseXML).text());
            return;
          }
          
          var appName = response.attr('applicationName');
          var resultUri = response.attr('resultUri');
          
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
      },
  });
  
  // load URL parameters into specific field values
  $('#applicationName').val(urlParams['appName']);
  
});     
