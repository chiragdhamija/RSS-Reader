/**
 * Reset report bug related context.
 */
r.reportbug.reset = function () {
    // Hiding reportbug container
    $('#reportbug-container').hide();
    // Reset form values
    $('#bug-description').val('');
    
    // // Destroy and reset tabs to prevent state issues
    // if ($('#reportbug-tabs').hasClass('ui-tabs')) {
    //     $('#reportbug-tabs').tabs('destroy');
    // }
};

/**
 * Initializing report bug module.
 */
r.reportbug.init = function() {
    // Listening hash changes on #/reportbug/
    $.History.bind('/reportbug/', function(state, target) {
      // Resetting page context
      r.main.reset();
  
      // Showing reportbug container
      $('#reportbug-container').show();
  
      // Configuring contextual toolbar
      $('#toolbar > .reportbug').removeClass('hidden');
  
      // Initialize tabs
      $('#reportbug-tabs').tabs({
        create: function(e, ui) {
          r.reportbug.onTabActivated(ui.panel);
        },
        activate: function(e, ui) {
          r.reportbug.onTabActivated(ui.newPanel);
        }
      });
    });
  
    // Bug report form submission
    $('#reportbug-form').submit(function(e) {
      e.preventDefault();
  
      var _this = $(this);
      var descriptionInput = _this.find('#bug-description');
      
      // Validate form
      if (!descriptionInput.val().trim()) {
        alert($.t('reportbug.form.error.description'));
        return false;
      }
  
      // Disable button during request
      var submitButton = _this.find('input[type="submit"]');
      submitButton.attr('disabled', 'disabled');
      $('#reportbug-form .ajax-loader').removeClass('hidden');
      
      // Calling API using POST method
      r.util.ajax({
        url: r.util.url.report_bug,
        type: 'POST',
        data: {
          description: descriptionInput.val()
        },
        done: function(data) {
          alert($.t('reportbug.form.success'));
  
          // Clear the form
          descriptionInput.val('');
  
          // Refresh the bug list
          r.reportbug.loadBugReports();
        },
        fail: function(data) {
          alert($.t('reportbug.form.error'));
        },
        always: function() {
          // Enabling button
          submitButton.removeAttr('disabled');
          $('#reportbug-form .ajax-loader').addClass('hidden');
        }
      });
  
      // Prevent form submission
      return false;
    });
  };

/**
 * Triggers when a tab is visible.
 */
r.reportbug.onTabActivated = function (panel) {
    var initialized = panel.data('initialized');
    panel.data('initialized', true);

    switch (panel.attr('id')) {
        case 'reportbug-tab-form':
            r.reportbug.onTabForm(panel, !initialized);
            break;
        case 'reportbug-tab-dashboard':
            r.reportbug.onTabDashboard(panel, !initialized);
            break;
    }
};

/**
 * Triggers when report form tab is activated.
 */
r.reportbug.onTabForm = function (panel, initialize) {
    if (initialize) {
        // Any specific initialization for the report form
        // For example, prefill user information
        var userInfoText = panel.find('.user-info');
        userInfoText.text($.t('reportbug.form.reporting_as') + ' ' + r.user.userInfo.username);
    }
};

/**
 * Triggers when dashboard tab is activated.
 */
r.reportbug.onTabDashboard = function (panel, initialize) {
    if (initialize) {
        // Add refresh button functionality
        panel.find('#refresh-bugs-button').click(function () {
            r.reportbug.loadBugReports();
        });
    }

    // Load bug reports
    r.reportbug.loadBugReports();
};

/**
 * Load bug reports from the server.
 */
r.reportbug.loadBugReports = function() {
  var dashboardTab = $('#reportbug-tab-dashboard');
  
  // Clear existing content
  dashboardTab.empty();
  
  // Create tables structure
  var isAdmin = r.user.hasBaseFunction('ADMIN');
  var activeColumnCount = isAdmin ? 5 : 4; // 5 columns for admin, 4 for normal users
  
  // Create active bugs table
  dashboardTab.append('<h1 data-i18n="reportbug.dashboard.active_title">Active Bug Reports</h1>');
  dashboardTab.append(
    '<table id="active-bugs-table" class="data-table">' +
      '<thead>' +
        '<tr>' +
          '<th data-i18n="reportbug.dashboard.number">#</th>' +
          '<th data-i18n="reportbug.dashboard.date">Date</th>' +
          '<th data-i18n="reportbug.dashboard.user">User</th>' +
          '<th data-i18n="reportbug.dashboard.description">Description</th>' +
          (isAdmin ? '<th data-i18n="reportbug.dashboard.actions">Actions</th>' : '<th data-i18n="reportbug.dashboard.actions">Actions</th>') +
        '</tr>' +
      '</thead>' +
      '<tbody>' +
        '<tr><td colspan="' + activeColumnCount + '">' + $.t('reportbug.dashboard.loading') + '</td></tr>' +
      '</tbody>' +
    '</table>'
  );
  
  // Create resolved bugs table
  dashboardTab.append('<h1 data-i18n="reportbug.dashboard.resolved_title">Resolved Bug Reports</h1>');
  dashboardTab.append(
    '<table id="resolved-bugs-table" class="data-table">' +
      '<thead>' +
        '<tr>' +
          '<th data-i18n="reportbug.dashboard.number">#</th>' +
          '<th data-i18n="reportbug.dashboard.date">Date</th>' +
          '<th data-i18n="reportbug.dashboard.user">User</th>' +
          '<th data-i18n="reportbug.dashboard.description">Description</th>' +
        '</tr>' +
      '</thead>' +
      '<tbody>' +
        '<tr><td colspan="4">' + $.t('reportbug.dashboard.loading') + '</td></tr>' +
      '</tbody>' +
    '</table>'
  );
  
  // Now get references to the tables after they've been created
  var activeBugsTable = $('#active-bugs-table');
  var resolvedBugsTable = $('#resolved-bugs-table');
  var activeBugsBody = activeBugsTable.find('tbody');
  var resolvedBugsBody = resolvedBugsTable.find('tbody');
  
  r.util.ajax({
      url: r.util.url.bug_list,
      type: 'GET',
      data: { offset: 0, limit: 100 },
      done: function(data) {
          if (!data.bugs || data.bugs.length === 0) {
              activeBugsBody.html('<tr><td colspan="' + activeColumnCount + '">' + $.t('reportbug.dashboard.no_bugs') + '</td></tr>');
              resolvedBugsBody.html('<tr><td colspan="4">' + $.t('reportbug.dashboard.no_bugs') + '</td></tr>');
              return;
          }
          
          var activeHtml = '';
          var resolvedHtml = '';
          var activeCount = 0;
          var resolvedCount = 0;
          
          $(data.bugs).each(function(i, bug) {
              var date = new Date(bug.create_date);
              var formattedDate = date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
              
              if (bug.resolved) {
                  // For resolved bugs - no action buttons
                  resolvedCount++;
                  resolvedHtml += '<tr data-bug-id="' + bug.id + '">' +
                      '<td>' + resolvedCount + '</td>' +
                      '<td>' + formattedDate + '</td>' +
                      '<td>' + bug.username + '</td>' +
                      '<td>' + bug.description + '</td>' +
                      '</tr>';
              } else {
                  // For active bugs - include buttons based on user role
                  activeCount++;
                  var actionCell = '';
                  
                  if (isAdmin) {
                      // Admin gets both resolve and delete buttons
                      actionCell = '<td>' +
                          '<button class="resolve-bug-button" data-bug-id="' + bug.id + '">Resolve</button> ' +
                          '<button class="delete-bug-button" data-bug-id="' + bug.id + '">Delete</button>' +
                          '</td>';
                  } else {
                      // Normal user only gets delete button
                      actionCell = '<td>' +
                          '<button class="delete-bug-button" data-bug-id="' + bug.id + '">Delete</button>' +
                          '</td>';
                  }
                  activeHtml += '<tr data-bug-id="' + bug.id + '">' +
                      '<td>' + activeCount + '</td>' +
                      '<td>' + formattedDate + '</td>' +
                      '<td>' + bug.username + '</td>' +
                      '<td>' + bug.description + '</td>' +
                      actionCell +
                      '</tr>';
              }
          });
          
          // Update the tables
          if (activeCount > 0) {
              activeBugsBody.html(activeHtml);
          } else {
              activeBugsBody.html('<tr><td colspan="' + activeColumnCount + '">' + $.t('reportbug.dashboard.no_bugs') + '</td></tr>');
          }
          
          if (resolvedCount > 0) {
              resolvedBugsBody.html(resolvedHtml);
          } else {
              resolvedBugsBody.html('<tr><td colspan="4">' + $.t('reportbug.dashboard.no_bugs') + '</td></tr>');
          }
          
          // Apply i18n to newly added elements
          dashboardTab.i18n();
          
          // Attach event listeners to buttons
          $('.resolve-bug-button').on('click', function() {
              var bugId = $(this).data('bug-id');
              resolveBug(bugId);
          });
          
          $('.delete-bug-button').on('click', function() {
              var bugId = $(this).data('bug-id');
              deleteBug(bugId);
          });
      },
      fail: function() {
          activeBugsBody.html('<tr><td colspan="' + activeColumnCount + '">' + $.t('reportbug.dashboard.error') + '</td></tr>');
          resolvedBugsBody.html('<tr><td colspan="4">' + $.t('reportbug.dashboard.error') + '</td></tr>');
      }
  });
}
// r.reportbug.loadBugReports = function () {
//   var activeBugsTable = $('#active-bugs-table');
//   var resolvedBugsTable = $('#resolved-bugs-table');
//   var activeBugsBody = activeBugsTable.find('tbody');
//   var resolvedBugsBody = resolvedBugsTable.find('tbody');

//   // Show loading indicators
//   activeBugsBody.html('<tr><td colspan="6">' + $.t('reportbug.dashboard.loading') + '</td></tr>');
//   resolvedBugsBody.html('<tr><td colspan="4">' + $.t('reportbug.dashboard.loading') + '</td></tr>');

//   // Calling API using GET method
//   r.util.ajax({
//       url: r.util.url.bug_list,
//       type: 'GET',
//       data: { offset: 0, limit: 100 },
//       done: function (data) {
//           if (!data.bugs || data.bugs.length === 0) {
//               activeBugsBody.html('<tr><td colspan="6">' + $.t('reportbug.dashboard.no_bugs') + '</td></tr>');
//               resolvedBugsBody.html('<tr><td colspan="4">' + $.t('reportbug.dashboard.no_bugs') + '</td></tr>');
//               return;
//           }

//           var activeHtml = '';
//           var resolvedHtml = '';
//           var activeCount = 0;
//           var resolvedCount = 0;

//           $(data.bugs).each(function (i, bug) {
//               var date = new Date(bug.create_date);
//               var formattedDate = date.toLocaleDateString() + ' ' + date.toLocaleTimeString();

//               if (bug.resolved) {
//                   // For resolved bugs - no action buttons
//                   resolvedCount++;
//                   resolvedHtml += '<tr data-bug-id="' + bug.id + '">' +
//                       '<td>' + resolvedCount + '</td>' +
//                       '<td>' + formattedDate + '</td>' +
//                       '<td>' + bug.username + '</td>' +
//                       '<td>' + bug.description + '</td>' +
//                       '</tr>';
//               } else {
//                   // For active bugs - include resolve and delete buttons
//                   activeCount++;
//                   activeHtml += '<tr data-bug-id="' + bug.id + '">' +
//                       '<td>' + activeCount + '</td>' +
//                       '<td>' + formattedDate + '</td>' +
//                       '<td>' + bug.username + '</td>' +
//                       '<td>' + bug.description + '</td>' +
//                       '<td><button class="resolve-bug-button" data-bug-id="' + bug.id + '">Resolve</button></td>' +
//                       '<td><button class="delete-bug-button" data-bug-id="' + bug.id + '">Delete</button></td>' +
//                       '</tr>';
//               }
//           });

//           // Update the tables
//           if (activeCount > 0) {
//               activeBugsBody.html(activeHtml);
//           } else {
//               activeBugsBody.html('<tr><td colspan="6">' + $.t('reportbug.dashboard.no_bugs') + '</td></tr>');
//           }

//           if (resolvedCount > 0) {
//               resolvedBugsBody.html(resolvedHtml);
//           } else {
//               resolvedBugsBody.html('<tr><td colspan="4">' + $.t('reportbug.dashboard.no_bugs') + '</td></tr>');
//           }

//           // Attach event listeners to buttons
//           $('.resolve-bug-button').on('click', function () {
//               var bugId = $(this).data('bug-id');
//               resolveBug(bugId);
//           });

//           $('.delete-bug-button').on('click', function () {
//               var bugId = $(this).data('bug-id');
//               deleteBug(bugId);
//           });
//       },
//       fail: function () {
//           activeBugsBody.html('<tr><td colspan="6">' + $.t('reportbug.dashboard.error') + '</td></tr>');
//           resolvedBugsBody.html('<tr><td colspan="4">' + $.t('reportbug.dashboard.error') + '</td></tr>');
//       }
//   });
// };

// Function to call API for bug deletion
function deleteBug(bugId) {
    if (confirm("Are you sure you want to delete this bug?")) {
      var url = r.util.url.bug_delete;

      r.util.ajax({
          url: url.replace('{id}', bugId),
          type: 'DELETE',
          done: function () {
              alert("Bug deleted successfully!");
              r.reportbug.loadBugReports(); // Refresh table
          },
          fail: function () {
              alert("Failed to delete bug.");
          }
      });
    }
}

// Function to call API for bug resolution
function resolveBug(bugId) {
  if (confirm("Are you sure you want to mark this bug as resolved?")) {
    var url = r.util.url.bug_resolve;
    r.util.ajax({
      url: url.replace('{id}', bugId),
      type: 'PUT',
      data: { id: bugId },
      done: function () {
        alert("Bug marked as resolved!");
        r.reportbug.loadBugReports(); // Refresh table
      },
      fail: function () {
        alert("Failed to resolve bug.");
      }
    });
  }
}