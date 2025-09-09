/**
 * Initializing subscription module.
 */
r.subscription.init = function () {
  // Actionbar: displaying subscriptions
  $(
    "#subscriptions-show-button, #subscriptions .close-button, #subscriptions-backdrop"
  ).click(function () {
    $("#subscriptions").fadeToggle("fast");
    $("#subscriptions-backdrop").fadeToggle("fast");
  });

  console.log("l11 subscription.js");
  // r.subscription.update();
  // Button for changing subscription tree unread state
  $('#subscription-unread-button').click(function() {
    console.log("here")
    console.log(r.user.isDisplayUnread())
    var unread = !r.user.isDisplayUnread();
    r.user.setDisplayUnread(unread);
    r.subscription.update();
  });

  // Tip for adding subscription
  $("#subscription-add-button").qtip({
    content: { text: $("#qtip-subscription-add") },
    position: {
      my: "top left",
      at: "bottom center",
      effect: false,
      viewport: $(window),
      adjust: { method: "shift" },
    },
    show: { event: "click" },
    hide: { event: "click unfocus" },
    style: { classes: "qtip-light qtip-shadow" },
    events: {
      visible: function () {
        $("#subscription-url-input").focus();
        $("#subscription-simulate-input").focus();
      },
    },
  });

  // Adding a subscription
  $("#subscription-submit-button").click(function () {
    var _this = $(this);
    var url = $("#subscription-url-input").val();
    var query = $("#subscription-simulate-input").val();
    // Validating form
    if (url != "") {
      // Disable button during the request to avoid double entries
      _this.attr("disabled", "disabled");
      $("#subscriptions .ajax-loader").removeClass("hidden");

      // Closing tip
      $("#subscription-add-button").qtip("hide");
      var data = { url: url ,query: query};
      // Calling API
      r.util.ajax({
        url: r.util.url.subscription_add,
        type: 'PUT',
        data: data,
        done: function (data) {
          // Reseting form
          $('#qtip-subscription-add form')[0].reset();
          // Refreshing the articles list on custom feed page (2nd tab)
          r.customfeed.loadArticles();
          // Open newly added feed
          window.location.hash = "#/feed/subscription/" + data.id;
        },
        fail: function (jqxhr) {
          var data = JSON.parse(jqxhr.responseText);
        },
        always: function () {
          // Enabing button
          _this.removeAttr("disabled");
          $("#subscriptions .ajax-loader").addClass("hidden");
        },
      });
    }

    // Prevent form submission
    return false;
  });

  //Simulating RSS feeds
  // $("#subscription-simulate-button").click(function () {
  //   var _this = $(this);
  //   var url = $("#subscription-url-input").val();

  //   // Validating form
  //   if (url != "") {
  //     // Disable button during the request to avoid double entries
  //     _this.attr("disabled", "disabled");
  //     $("#subscriptions .ajax-loader").removeClass("hidden");

  //     // Closing tip
  //     $("#subscription-add-button").qtip("hide");

  //     // Calling API
  //     r.util.ajax({
  //       url: r.util.url.subscription_simulate,
  //       type: "PUT",
  //       data: { url: url },
  //       done: function (data) {
  //         // Reseting form
  //         $("#qtip-subscription-add form")[0].reset();

  //         // Open newly added feed
  //         window.location.hash = "#/feed/subscription/" + data.id;
  //       },
  //       fail: function (jqxhr) {
  //         var data = JSON.parse(jqxhr.responseText);
  //         alert(data.message);
  //       },
  //       always: function () {
  //         // Enabing button
  //         _this.removeAttr("disabled");
  //         $("#subscriptions .ajax-loader").addClass("hidden");
  //       },
  //     });
  //   }

    // Prevent form submission
  //   return false;
  // });

  // Initializing toolbar subscriptions actions
  r.subscription.initToolbar();

  // Refresh subscription tree every minutes
  setInterval(function () {
    // Check if no category or subscription edit qtip is opened
    if (
      $("body .qtip").find(
        ".qtip-subscription-edit:visible, .qtip-category-edit:visible"
      ).length == 0
    ) {
      r.subscription.update();
    }
  }, 60000);

  $("#subscriptions").on("click", "li a", function () {
    // Force hashchange trigger if the user clicks on an already opened feed
    if (window.location.hash == $(this).attr("href")) {
      $.History.trigger();
    }

    // Hide subscriptions on mobile
    if(r.main.mobile) {
      $('#subscriptions').fadeOut('fast');
      $('#subscriptions-backdrop').fadeOut('fast');
    }
  });

  // [PATCH] Add your "Submit Selection" button logic
  $('#selection-submit-button').click(function () {
    const selectedIds = [];
    
    // Process all checked categories to include their nested subscriptions
    $('.select-checkbox[data-type="category"]:checked').each(function () {
      const categoryId = $(this).data('id');
      // Add the category itself
      selectedIds.push('categoryId:' + categoryId);
      
      // Find all nested subscriptions at any level
      // This is a change from the original code which only included direct children
      $('#category-' + categoryId).find('li.subscription').each(function() {
        const subId = $(this).data('subscription-id');
        if (subId && !selectedIds.includes('subscriptionId:' + subId)) {
          selectedIds.push('subscriptionId:' + subId);
        }
      });
    });
    
    // Add individually selected subscriptions
    $('.select-checkbox[data-type="subscription"]:checked').each(function () {
      const subId = $(this).data('id');
      if (!selectedIds.includes('subscriptionId:' + subId)) {
        selectedIds.push('subscriptionId:' + subId);
      }
    });
  
    // Navigate
    if (selectedIds.length > 0) {
      window.location.hash = '#/feed/selection/mixed/' + selectedIds.join(',');
    } else {
      window.location.hash = '#/feed/unread';
    }
  
    // Reset checkboxes
    $('.select-checkbox').prop('checked', false);
  });
  
  


  $('#filter-feed-button').click(function() {
    r.subscription.renderFilterList('feed');
  });
  
  $('#filter-category-button').click(function() {
    r.subscription.renderFilterList('category');
  });
  

};

/**
 * Updating subscriptions tree.
 */
r.subscription.update = function () {
  // Unread state
  var unread = r.user.isDisplayUnread();
  console.log('Updating subscriptions tree with unread=' + unread)
  // Getting subscriptions
  r.util.ajax({
    url: r.util.url.subscription_list,
    data: { unread: unread },
    type: 'GET',
    done: function(data) {
      console.log(data.categories[0].categories)
      if ($(data.categories[0].categories).size() > 0 || $(data.categories[0].subscriptions).size() > 0) {
        // Building HTML tree
        r.subscription.applyArticleCounts(data);
        var html = '<ul id="category-root" data-category-id="' + data.categories[0].id + '">';
        $(data.categories[0].categories).each(function(i, category) {
          // Adding sub-category
          var subscriptionsHtml =
            "<ul " + (category.folded ? 'style="display: none;"' : "") + ">";
          if ($(category.subscriptions).length > 0) {
            // Adding subscriptions
            $(category.subscriptions).each(function (i, subscription) {
              subscriptionsHtml +=
                r.subscription.buildSubscriptionItem(subscription);
            });
          }
          subscriptionsHtml += "</ul>";
          html += r.subscription.buildCategoryItem(category, subscriptionsHtml);
        });

        // Adding remaining subscriptions
        $(data.categories[0].subscriptions).each(function (i, subscription) {
          html += r.subscription.buildSubscriptionItem(subscription);
        });
        html += "&nbsp;</ul>";

        // Updating HTML and force redraw
        $("#subscription-list").html(html).redraw();
      } else {
        // Empty placeholder
        var html = "<p>" + $.t("subscription.empty") + "</p>";
        if (unread) {
          html =
            "<p>" +
            $.t("subscription.emptyunread") +
            "</p>" +
            '<p><a href="#">' +
            $.t("subscription.showall") +
            "</a></p>";
        }
        $("#subscription-list").html(html);
        $("#subscription-list p a").click(function () {
          r.user.setDisplayUnread(false);
          r.subscription.update();
        });
      }

      // Updating main unread item and title
      var unreadItem = $("#unread-feed-button");
      r.subscription.updateUnreadCount(unreadItem, data.unread_count);
      r.subscription.updateTitle(data.unread_count);

      // Initializing tree features
      r.subscription.initSorting(data.categories[0].id);
      r.subscription.initCollapsing();
      r.subscription.initEditing();
    },
  });
    // [PATCH] Auto-check boxes in selection mode
    var currentHash = window.location.hash;
    if (currentHash.indexOf('/feed/selection/') !== -1) {
      // e.g., "#/feed/selection/mixed/categoryId:xxx,subscriptionId:yyy"
      var parts = currentHash.split('/');
      var idsPart = parts[parts.length - 1]; // e.g., "categoryId:xxx,subscriptionId:yyy"
      var ids = idsPart.split(',');
      $('.select-checkbox').each(function() {
        var $chk = $(this);
        var val = $chk.data('type') + ":" + $chk.data('id');
        if (ids.indexOf(val) !== -1) {
          $chk.prop('checked', true);
        }
      });
    }
};

/**
 * Building subscription li.
 */
r.subscription.buildSubscriptionItem = function(subscription) {
  var unread = '<span class="unread-count" ' + (subscription.unread_count == 0 ? 'style="display: none;"' : '') + '>&nbsp;(<span class="count">' + subscription.unread_count + '</span>)</span>';
  var total = '<span class="total-count">&nbsp;[<span class="total">' + (subscription.total_count || 0) + '</span>]</span>';
  var title = r.util.escape(subscription.title);

// [PATCH] Add checkbox for selection
return '' +
'<li id="subscription-' + subscription.id + '" data-subscription-id="' + subscription.id + '"' +
' data-subscription-url="' + subscription.url + '"' +
' class="subscription' + (r.feed.context.subscriptionId == subscription.id ? ' active' : '') +
(subscription.unread_count > 0 ? ' unread' : '') + '">' +
  // '<input type="checkbox" class="select-checkbox" data-type="subscription" data-id="' + subscription.id + '"> ' +
  '<a href="#/feed/subscription/' + subscription.id + '" title="' + title + '">' +
    '<img src="' + r.util.url.subscription_favicon.replace('{id}', subscription.id) + '" /> ' +
    (subscription.sync_fail_count >= 5 ? '<img src="images/warning.png" title="' + $.t('subscription.syncfail') + '" />' : '') +
    '<span class="title">' + title + '</span>' + unread + total +
  '</a>' +
  '<div class="edit"></div>' +
'</li>';
};


r.subscription.renderFilterList = function(type) {
  if (type === 'feed') {
    const list = $('#filter-list-feed');
    list.empty();
    $('#subscription-list li.subscription').each(function () {
      const id = $(this).data('subscription-id');
      const title = $(this).find('.title').text().trim();
      list.append('<li><label><input type="checkbox" class="select-checkbox" data-type="subscription" data-id="' + id + '"> ' + title + '</label></li>');
    });
  } else if (type === 'category') {
    const list = $('#filter-list-category');
    list.empty();
    $('#subscription-list li.category').each(function () {
      const id = $(this).data('category-id');
      // Fix: Get only the direct name text without children's text
      const title = $(this).find('> a .name').text().trim();
      list.append('<li><label><input type="checkbox" class="select-checkbox" data-type="category" data-id="' + id + '"> ' + title + '</label></li>');
    });
  }
};





/**
 * Building category li.
 */
r.subscription.buildCategoryItem = function(category, subscriptionsHtml) {
  var unread = '<span class="unread-count" ' +
    (category.unread_count === 0 ? 'style="display: none;"' : '') +
    '>&nbsp;(<span class="count">' + category.unread_count + '</span>)</span>';
  var total = '<span class="total-count">&nbsp;[<span class="total">' + (category.total_count || 0) + '</span>]</span>';
  var name = r.util.escape(category.name);

  // [PATCH] Add checkbox for selection
  var html = '' +
    '<li id="category-' + category.id + '" data-category-id="' + category.id + '"' +
    ' class="category' + (r.feed.context.categoryId == category.id ? ' active' : '') +
    (category.unread_count > 0 ? ' unread' : '') + '">' +
      // '<input type="checkbox" class="select-checkbox" data-type="category" data-id="' + category.id + '"> ' +
      '<div class="collapse ' + (category.folded ? 'closed' : 'opened') + '"></div>' +
      '<a href="#/feed/category/' + category.id + '" title="' + name + '">' +
        '<img src="images/category.png" /> ' +
        '<span class="name">' + name + '</span>' + unread + total +
      '</a>' +
      '<div class="edit"></div>';

  html += '<ul' + (category.folded ? ' style="display: none;"' : '') + '>';

  if (category.subscriptions && category.subscriptions.length > 0) {
    $.each(category.subscriptions, function(i, sub) {
      html += r.subscription.buildSubscriptionItem(sub);
    });
  }
  if (category.categories && category.categories.length > 0) {
    $.each(category.categories, function(i, subcat) {
      html += r.subscription.buildCategoryItem(subcat);
    });
  }
  html += '</ul></li>';
  return html;
};



/**
 * Adding sorting feature.
 */
r.subscription.initSorting = function (rootCategoryId) {
  $("#subscription-list ul")
    .sortable({
      connectWith: "#subscription-list ul", // Can move items between lists
      revert: 100, // 100ms revert animation duration
      items: "li", // Only li can be moved
      distance: 15, // Drag only after 15px mouse distance
      placeholder: "placeholder", // Placeholder CSS class
      forcePlaceholderSize: true, // Otherwise placeholder is 1px height
      stop: function (event, ui) {
        // Category or subscription moved
        if (ui.item.hasClass("subscription")) {
          // Getting contextual parameters
          var subscriptionId = ui.item.attr("data-subscription-id");
          var order = ui.item.index() - ui.item.prevAll("li.category").length; // Substract categories, which are not part of the order
          var categoryId = ui.item.parent().parent().attr("data-category-id");
          if (ui.item.parent().attr("id") == "category-root") {
            categoryId = rootCategoryId;
          }

        // Calling API
        r.util.ajax({
          url: r.util.url.subscription_update.replace('{id}', subscriptionId),
          data: { category: categoryId, order: order },
          type: 'POST',
          always: function() {
            // Full tree update needed to update unread counts
            r.subscription.update();
          }
        });
      } else if (ui.item.hasClass('category')) {
        var categoryId = ui.item.attr('data-category-id');
        var parentCategoryId = ui.item.parent().closest('li.category').attr('data-category-id') || rootCategoryId;
        var order = ui.item.index();
    

    
        r.util.ajax({
            url: r.util.url.category_update.replace('{id}', categoryId),
            type: 'POST',
            data: {
                parent_id: parentCategoryId,
                order: order
            },
            fail: function(jqxhr) {
                var response = JSON.parse(jqxhr.responseText);
                alert(response.message || $.t("error.unknown"));
                r.subscription.update();
            }
        });
    }
    
    
      
    }
  }).disableSelection();
};

/**
 * Initializing collapsing feature.
 */
r.subscription.initCollapsing = function () {
  $("#subscription-list .collapse").click(function () {
    var parent = $(this).parent();
    var children = parent.find("> ul");
    var categoryId = parent.attr("data-category-id");
    children.toggle();
    $(this).toggleClass("opened").toggleClass("closed");

    // Calling API
    r.util.ajax({
      url: r.util.url.category_update.replace("{id}", categoryId),
      data: { folded: !children.is(":visible") },
      type: "POST",
      fail: function (jqxhr) {
        // In case of error, client is no more synced with server, perform full update
        r.subscription.update();
      },
    });
  });
};

/**
 * Initializing editing feature.
 */
r.subscription.initEditing = function () {
  //Subscriptions editing
  $("#subscription-list li.subscription > .edit").each(function () {
    // Initializing edit popup
    var parent = $(this).parent();
    var _this = $(this);
    var subscriptionId = parent.attr("data-subscription-id");
    var content = $("#template .qtip-subscription-edit").clone();
    var infoContent = $("#template .qtip-subscription-edit-info").clone();
    var titleInput = content.find(".subscription-edit-title-input");
    titleInput.val(parent.find("> a .title").text().trim());

    // Calling API delete
    $(".subscription-edit-delete-button", content).click(function () {
      if (confirm($.t("subscription.edit.deleteconfirm"))) {
        r.util.ajax({
          url: r.util.url.subscription_delete.replace("{id}", subscriptionId),
          type: "DELETE",
          always: function () {
            // Full tree refresh
            r.subscription.update();

            // Refresh the list of articles in the custom feed page (2nd tab)
            if (!r.customfeed.pendingRefresh) {
              r.customfeed.pendingRefresh = {};
            }
            r.customfeed.pendingRefresh.articles = true;
            console.log("l339 Set articles refresh flag");

            // Go to home
            window.location.hash = "#/feed/unread";
          },
        });
      }
    });

    // Calling API edit
    $(".subscription-edit-submit-button", content).click(function () {
      var title = titleInput.val();

      if (title != '') {
        r.util.ajax({
          url: r.util.url.subscription_update.replace("{id}", subscriptionId),
          data: { title: title },
          type: "POST",
          always: function () {
            // Full tree refresh
            r.subscription.update();

            // Refresh the list of articles in the custom feed page (2nd tab)
            if (!r.customfeed.pendingRefresh) {
              r.customfeed.pendingRefresh = {};
            }
            r.customfeed.pendingRefresh.articles = true;
            console.log("l366 Set articles refresh flag");
          }
        });
      }

      // Prevent form submission
      return false;
    });

    // Opening informations popup
    $(".subscription-edit-info-button", content).click(function () {
      _this.qtip("hide");

      // Get feed informations
      r.util.ajax({
        url: r.util.url.subscription_get.replace("{id}", subscriptionId),
        data: { limit: 0 },
        type: "GET",
        done: function (data) {
          var table = $(".subscription-edit-info-table tbody", infoContent);
          $(".title", table).html(r.util.escape(data.subscription.title));
          $(".feed_title", table).html(
            r.util.escape(data.subscription.feed_title)
          );
          $(".url", table)
            .attr("href", data.subscription.url)
            .html(data.subscription.url);
          $(".rss_url", table)
            .attr("href", data.subscription.rss_url)
            .html(data.subscription.rss_url);
          $(".category_name", table).html(data.subscription.category_name);
          var date = moment(data.subscription.create_date);
          $(".create_date", table)
            .attr("title", date.format("L LT"))
            .html(date.fromNow());
        },
      });

      // Get latest synchronizations
      r.util.ajax({
        url: r.util.url.subscription_sync.replace("{id}", subscriptionId),
        type: "GET",
        done: function (data) {
          var html = "";
          $(data.synchronizations).each(function (i, sync) {
            var date = moment(sync.create_date);
            html +=
              "<tr>" +
              '<td title="' +
              (sync.message ? sync.message : "") +
              '">' +
              (sync.success
                ? $.t("feed.info.syncok")
                : $.t("feed.info.syncfail")) +
              "</td>" +
              '<td title="' +
              date.format("L LT") +
              '">' +
              date.fromNow() +
              "</td>" +
              "<td>" +
              sync.duration +
              "ms</td>" +
              "</tr>";
          });
          $(".subscription-edit-info-synctable tbody", infoContent).html(html);
        },
      });
    });

    // Creating edit popup
    $(this).qtip({
      content: { text: content },
      position: {
        my: "top right",
        at: "bottom center",
        effect: false,
        viewport: $(window),
      },
      show: { event: "click" },
      hide: { event: "click unfocus" },
      style: { classes: "qtip-light qtip-shadow" },
    });

    // Creation informations popup
    $(".subscription-edit-info-button", content).qtip({
      content: { text: infoContent },
      position: {
        my: "center",
        at: "center",
        target: $(document.body),
      },
      show: {
        modal: {
          on: true,
          blur: true,
          escape: true,
        },
        event: "click",
      },
      hide: { event: "" },
      style: { classes: "qtip-light qtip-shadow" },
    });
  });

  // Categories editing
  $("#subscription-list li.category > .edit").each(function () {
    // Initializing edit popup
    var parent = $(this).parent();
    var categoryId = parent.attr("data-category-id");
    var content = $("#template .qtip-category-edit").clone();
    var nameInput = content.find(".category-edit-name-input");
    nameInput.val(parent.find("> a .name").text().trim());

    // Calling API delete
    $(".category-edit-delete-button", content).click(function () {
      if (confirm($.t("category.edit.deleteconfirm"))) {
        r.util.ajax({
          url: r.util.url.category_delete.replace("{id}", categoryId),
          type: "DELETE",
          always: function () {
            // Full tree refresh
            r.subscription.update();
            // Go to home
            window.location.hash = "#/feed/unread";
          },
        });
      }
    });

    // Calling API edit
    $(".category-edit-submit-button", content).click(function () {
      var name = nameInput.val();

      if (name != "") {
        r.util.ajax({
          url: r.util.url.category_update.replace("{id}", categoryId),
          data: { name: name },
          type: "POST",
          always: function () {
            // Full tree refresh
            r.subscription.update();
          },
        });
      }

      // Prevent form submission
      return false;
    });

    // Creating edit popup
    $(this).qtip({
      content: { text: content },
      position: {
        my: "top right",
        at: "bottom center",
        effect: false,
        viewport: $(window),
      },
      show: { event: "click" },
      hide: { event: "click unfocus" },
      style: { classes: "qtip-light qtip-shadow" },
    });
  });
};

/**
 * Initialize subscription related toolbar actions.
 */
r.subscription.initToolbar = function () {
  // Toolbar action: change category
  var content = $('#template .qtip-change-category');
  content.on('click', 'li', function() {
    var rawCategoryId = $(this).attr('data-category-id');
    var subscriptionId = r.feed.context.subscriptionId;
    var categoryId = rawCategoryId === '' ? null : rawCategoryId;
    
    // Calling API
    r.util.ajax({
      url: r.util.url.subscription_update.replace("{id}", subscriptionId),
      data: { category: categoryId },
      type: "POST",
      always: function () {
        $("#toolbar .category-button").qtip("hide");
        r.subscription.update();
      },
    });
  });

  // Configuring change category tooltip

$('#toolbar .category-button').qtip({
  content: { text: content },
  position: { my: 'top middle', at: 'bottom center', effect: false, viewport: $(window) },
  show: { event: 'click' },
  hide: { event: 'click unfocus' },
  style: { classes: 'qtip-light qtip-shadow' },
  events: {
    show: function(e, api) {
      // 1) Figure out which category the feed is using
      var subscriptionId = r.feed.context.subscriptionId;
      var $subsItem = $('#subscription-list .subscription[data-subscription-id="' + subscriptionId + '"]');
  
  // If it’s under #category-root (and not a sub <li.category>), treat that as no category
  var $closestCategory = $subsItem.closest('li.category');
  
  var categoryId = $closestCategory.attr('data-category-id');
  if (!categoryId) {
    // Means we found nothing above, or the feed has no assigned category
    categoryId = ''; // Indicate "no category"
  }

      // Display loading spinner
      content.html('<img src="images/loader.gif" />');

      // 2) Call /category to get the full tree
      r.util.ajax({
        url: r.util.url.category_list,
        type: 'GET',
        done: function(data) {
          var rootCat = data.categories[0];
          var html = '<ul>';
      
          var isNoCategoryActive = (categoryId === '') ? ' class="active"' : '';
          html += '<li data-category-id=""' + isNoCategoryActive + ' style="padding-left:0px">No Category</li>';
      
          $.each(rootCat.categories, function(i, child) {
            html += r.subscription.buildCategoryTreeHtml(child, categoryId, 1);
          });
      
          html += '</ul>';
          content.html(html);
        }
      });
      
    }
  }
});
  
};

/**
 * Update unread count of a tree item.
 * If count == -1, substract 1, if count == -2, add 1,
 * otherwise, force at this count.
 */
r.subscription.updateUnreadCount = function (item, count) {
  var countItem = item.find("> a .count");
  var current = parseInt(countItem.text());
  if (count == -1) {
    count = current - 1;
  } else if (count == -2) {
    count = current + 1;
  }

  if (count > 0) {
    item.addClass("unread").find("> a .unread-count").show();
  } else {
    item.removeClass("unread").find("> a .unread-count").hide();
  }

  countItem.html(count);
  return count;
};

/**
 * Update application title.
 */
r.subscription.updateTitle = function (count) {
  var title = $.t("app");
  if (count > 0) {
    title = "(" + count + ") " + title;
  }
  $("title").html(title);
};

/**
 * Builds nested <li> for a category and its children.
 * @param {Object} category
 * @param {String} currentCategoryId
 * @param {Number} level - depth level for indentation
 */
r.subscription.buildCategoryTreeHtml = function(category, currentCategoryId, level) {
  var name = category.name || "(Unnamed)";
  var activeClass = (category.id === currentCategoryId) ? ' class="active"' : '';

  // Add left padding for visual indentation
  var padding = level * 15; // 15px per level of nesting

  var html = '<li data-category-id="' + category.id + '"' + activeClass + ' style="padding-left:' + padding + 'px">' + r.util.escape(name) + '</li>';

  // Add children recursively
  if (category.categories && category.categories.length > 0) {
    $.each(category.categories, function(i, child) {
      html += r.subscription.buildCategoryTreeHtml(child, currentCategoryId, level + 1);
    });
  }

  return html;
};

r.subscription.applyArticleCounts = function(data) {
  function updateSubscriptionCounts(categories) {
    if (!categories) return;

    $.each(categories, function(i, category) {
      if (category.subscriptions) {
        $.each(category.subscriptions, function(j, sub) {
          sub.total_count = r.feed.subscriptionTotalCounts[sub.id] || 0;
          sub.unread_count = sub.unread_count || 0; // Ensure it's a number
        });
      }
      if (category.categories) {
        updateSubscriptionCounts(category.categories);
      }
    });
  }

  function calculateCategoryCounts(category) {
    var total = 0;
    var unread = 0;

    if (category.subscriptions) {
      $.each(category.subscriptions, function(i, sub) {
        total += sub.total_count || 0;
        unread += sub.unread_count || 0;
      });
    }

    if (category.categories) {
      $.each(category.categories, function(i, subcat) {
        var childCounts = calculateCategoryCounts(subcat);
        total += childCounts.total;
        unread += childCounts.unread;
      });
    }

    category.total_count = total;
    category.unread_count = unread;
    return { total: total, unread: unread };
  }

  if (data && data.categories && data.categories.length > 0) {
    var root = data.categories[0];
    updateSubscriptionCounts(root.categories);
    calculateCategoryCounts(root);

    if (root.subscriptions) {
      $.each(root.subscriptions, function(i, sub) {
        sub.total_count = r.feed.subscriptionTotalCounts[sub.id] || 0;
        sub.unread_count = sub.unread_count || 0;
      });

      var totalRoot = 0, unreadRoot = 0;
      $.each(root.subscriptions, function(i, sub) {
        totalRoot += sub.total_count;
        unreadRoot += sub.unread_count;
      });

      root.total_count = totalRoot;
      root.unread_count = unreadRoot;
    }
  }

  return data;
};

/**
 * Handle (un)checking category or subscription in selection mode
 */
$(document).on('change', '.select-checkbox', function () {
  const $checkbox = $(this);
  const isChecked = $checkbox.prop('checked');

  // Recursive function to check/uncheck all children of a category
  function checkAllNested($categoryLi, checkedState) {
    // All descendant checkboxes within this category
    $categoryLi.find('input.select-checkbox').each(function () {
      $(this).prop('checked', checkedState);
    });
  }

  // Function to check/uncheck parent checkbox based on children state
  function updateParentCheckbox($childCheckbox) {
    const $categoryLi = $childCheckbox.closest('ul').closest('li.category');
    if ($categoryLi.length > 0) {
      const $parentCheckbox = $categoryLi.children('label').find('input.select-checkbox[data-type="category"]');
      const $childCheckboxes = $categoryLi.find('> ul > li input.select-checkbox[data-type="subscription"], > ul > li input.select-checkbox[data-type="category"]');

      const allChecked = $childCheckboxes.length > 0 && $childCheckboxes.filter(':checked').length === $childCheckboxes.length;
      $parentCheckbox.prop('checked', allChecked);

      // Recursively update further parents
      updateParentCheckbox($parentCheckbox);
    }
  }

  if ($checkbox.data('type') === 'category') {
    const $categoryLi = $checkbox.closest('li.category');
    checkAllNested($categoryLi, isChecked);
  }

  if ($checkbox.data('type') === 'subscription' || $checkbox.data('type') === 'category') {
    updateParentCheckbox($checkbox);
  }
});

