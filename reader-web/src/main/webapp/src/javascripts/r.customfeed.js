/**
 * Reset custom feed related context.
 */
r.customfeed.reset = function () {
    // Hiding custom feed container
    $('#custom-feeds-container').hide();
    
    // Reset selections
    r.customfeed.selectedFeed = null;
    r.customfeed.selectedArticle = null;
    r.customfeed.selectedFeedForDeletion = null;
    
    // Reset radio buttons
    $('input[name="feed-selection"]').prop('checked', false);
    $('input[name="article-selection"]').prop('checked', false);
    $('input[name="feed-deletion"]').prop('checked', false);
    
    // tab 1 buttons
    $('#delete-custom-feed-btn').attr('disabled', 'disabled');

    // tab 2 buttons
    $('#add-to-feed-button').attr('disabled', 'disabled');
    $('#remove-from-feed-button').attr('disabled', 'disabled');
    
    // Reset show tab selections and states
    r.customfeed.selectedFeedForDisplay = null;
    // r.customfeed.selectedFeedSubscriptionStatus = false;
    
    // Reset radio buttons
    $('input[name="feed-display"]').prop('checked', false);
    
    // Disable buttons tab
    $('#show-custom-feed-btn').attr('disabled', 'disabled');
    $('#subscribe-custom-feed-btn').attr('disabled', 'disabled');
    $('#unsubscribe-custom-feed-btn').attr('disabled', 'disabled');
    
    // Clear feed articles
    $('#selected-feed-title').text('');
    $('#selected-feed-description').text('');
    $('#feed-articles-list').empty();

    // Tab 4
    // Reset the subscribed feeds section
    $('#subscribed-feeds-list').html('<p class="loading-message" data-i18n="custom_feeds.loading">Loading your subscribed feeds...</p>');
    $('#all-subscribed-articles-list').html('<p class="loading-message" data-i18n="custom_feeds.loading">Loading articles from your subscribed feeds...</p>');

};

/**
 * Initializing custom feed module.
 */
r.customfeed.init = function() {
    // Initialize selection trackers
    r.customfeed.selectedFeed = null;
    r.customfeed.selectedArticle = null;
    r.customfeed.selectedFeedForDeletion = null;

    // Initialize refresh tracker
    if (!r.customfeed.pendingRefresh) {
        r.customfeed.pendingRefresh = {};
    }

    // Listening hash changes on #/customfeed/
    $.History.bind('/customfeed/', function(state, target) {
        // Resetting page context
        r.main.reset();
        
        // Showing custom feed container
        $('#custom-feeds-container').show();
        
        // Configuring contextual toolbar
        $('#toolbar > .custom-feed').removeClass('hidden');
        
        // Initialize tabs
        $('#custom-feeds-tabs').tabs({
            create: function(e, ui) {
                r.customfeed.onTabActivated(ui.panel);
                r.customfeed.loadFeedsForDeletion();
            },
            activate: function(e, ui) {
                r.customfeed.onTabActivated(ui.newPanel);
            }
        });
        
        // Apply i18n to newly shown elements
        $('#custom-feeds-container').i18n();
    });
    // Add to Feed button click event
    $('#add-to-feed-button').on('click', function() {
        if (r.customfeed.selectedFeed && r.customfeed.selectedArticle) {
            r.customfeed.addArticleToFeed();
        }
    });

    $('#remove-from-feed-button').on('click', function() {
        if (r.customfeed.selectedFeed && r.customfeed.selectedArticle) {
            r.customfeed.removeArticleFromFeed();
        }
    });
    
    // Delete Feed button click event
    $('#delete-custom-feed-btn').on('click', function() {
        if (r.customfeed.selectedFeedForDeletion) {
            r.customfeed.deleteCustomFeed();
        }
    });
    
    // Event delegation for feed radio button changes
    $('#user-feeds-container').on('change', 'input[name="feed-selection"]', function() {
        r.customfeed.selectedFeed = $(this).val();
        r.customfeed.checkButtonState();
    });
    
    // Event delegation for article radio button changes
    $('#user-articles-container').on('change', 'input[name="article-selection"]', function() {
        // Ensure only one article is selected at a time
        $('input[name="article-selection"]').not(this).prop('checked', false);
        
        r.customfeed.selectedArticle = $(this).val();
        r.customfeed.checkButtonState();
    });
    
    // Event delegation for feed deletion radio button changes
    $('#delete-feeds-list').on('change', 'input[name="feed-deletion"]', function() {
        r.customfeed.selectedFeedForDeletion = $(this).val();
        r.customfeed.checkDeleteButtonState();
    });
    
    // Custom feed form submission
    $('#custom-feed-form').submit(function(e) {
        e.preventDefault();
        r.customfeed.createNewFeed();
        return false;
    });
    
     // Initialize selection trackers for all tabs
     r.customfeed.selectedFeed = null;
     r.customfeed.selectedArticle = null;
     r.customfeed.selectedFeedForDeletion = null;
     r.customfeed.selectedFeedForDisplay = null;
 
     // Initialize the Show tab buttons
     $('#show-custom-feed-btn').on('click', function(e) {
         e.preventDefault();
         if (r.customfeed.selectedFeedForDisplay) {
             r.customfeed.showFeedArticles();
         }
     });
     
     $('#subscribe-custom-feed-btn').on('click', function(e) {
         e.preventDefault();
         console.log("l131 subscribetofeed")
         // Tab 4 refresh
         r.customfeed.loadSubscribedFeeds();
         r.customfeed.loadAllSubscribedArticles();

         if (r.customfeed.selectedFeedForDisplay) {
             r.customfeed.subscribeToFeed();
         }
     });
     
     $('#unsubscribe-custom-feed-btn').on('click', function(e) {
         e.preventDefault();
         // Tab 4 refresh
         console.log("l166 triggering custom feed")
         r.customfeed.loadSubscribedFeeds();
         r.customfeed.loadAllSubscribedArticles();

         if (r.customfeed.selectedFeedForDisplay) {
             r.customfeed.unsubscribeFromFeed();
         }
     });
     
     // Event delegation for feed selection in the show tab
     $('#show-feeds-list').on('change', 'input[name="feed-display"]', function() {
         // Update selected feed
         r.customfeed.selectedFeedForDisplay = $(this).val();
         
         // Update subscription status
        //  r.customfeed.selectedFeedSubscriptionStatus = $(this).data('subscribed') === true || $(this).data('subscribed') === 'true';
         
         // Update button states
         r.customfeed.checkShowButtonsState();
     });

    // // Tab 4
    // r.customfeed.loadSubscribedFeeds();
    // r.loadAllSubscribedArticles();

    // View feed articles button
    $('.view-feed-articles-btn').on('click', function() {
    var feedId = $(this).data('feed-id');
    
    // Select the corresponding feed in the main feed list if it exists
    var feedRadio = $('#feed-display-' + feedId);
    if (feedRadio.length > 0) {
        feedRadio.prop('checked', true);
        r.customfeed.showSelectedFeed();
    } else {
        // If the feed is not in the main list, load it directly
        r.customfeed.loadFeedArticles(feedId);
    }
    
    // Scroll to the articles section
    $('html, body').animate({
        scrollTop: $('#feed-articles-container').offset().top
    }, 500);
    });
    
    // Unsubscribe button
    $('.unsubscribe-feed-btn').on('click', function() {
    var feedId = $(this).data('feed-id');
    var feedTitle = $(this).closest('.subscribed-feed-item').find('h4').text();
    
    if (confirm($.t('custom_feeds.unsubscribe.confirm', { feedTitle: feedTitle }))) {
        r.customfeed.unsubscribeFromFeedById(feedId);
    }
    });
};

/**
 * Triggers when create form tab is activated.
 */
r.customfeed.onTabCreate = function (panel, initialize) {
    if (initialize) {
        // Any specific initialization for the create form
        // For example, prefill user information if needed
        var userInfoText = panel.find('.user-info');
        if (userInfoText.length > 0) {
            userInfoText.text($.t('custom_feeds.form.creating_as') + ' ' + r.user.userInfo.username);
        }
    }
};

/**
 * Triggers when feed manager tab is activated.
 */
r.customfeed.onTabManager = function (panel, initialize) {
    if (initialize || (r.customfeed.pendingRefresh && r.customfeed.pendingRefresh.articles)) {
        // Load feeds and articles
        r.customfeed.loadFeeds();
        r.customfeed.loadArticles();
        r.customfeed.loadFeedsForDeletion();
        // Reset the flag
        if (r.customfeed.pendingRefresh) {
            r.customfeed.pendingRefresh.articles = false;
        }
    }
};

// Functions for the first tab
/**
 * Create a new custom feed using the form data.
 */
r.customfeed.createNewFeed = function() {
    var titleInput = $('#feed-title');
    var descriptionInput = $('#feed-description');
    
    // Validate form - require both title and description
    if (!titleInput.val().trim()) {
        alert($.t('custom_feeds.form.error.title'));
        return;
    }
    
    if (!descriptionInput.val().trim()) {
        alert($.t('custom_feeds.form.error.description'));
        return;
    }
    
    // Disable button during request
    var submitButton = $('#new-custom-feed-btn');
    submitButton.attr('disabled', 'disabled');
    $('#custom-feed-form .ajax-loader').removeClass('hidden');
    console.log("l197 title = ", titleInput.val());
    console.log("description = ", descriptionInput.val());

    // Calling API using POST method
    r.util.ajax({
        url: r.util.url.create_custom_feed,
        type: 'POST',
        data: {
            title: titleInput.val(),
            description: descriptionInput.val()
        },
        done: function(data) {
            alert($.t('custom_feeds.form.success'));
            
            // Clear the form
            titleInput.val('');
            descriptionInput.val('');
            
            // Refresh the feed list and switch to manager tab
            r.customfeed.loadFeeds();
            r.customfeed.loadFeedsForDeletion();
            // $('#custom-feeds-tabs').tabs('option', 'active', 1);
        },
        fail: function(data) {
            alert($.t('custom_feeds.form.error'));
        },
        always: function() {
            // Enabling button
            submitButton.removeAttr('disabled');
            $('#custom-feed-form .ajax-loader').addClass('hidden');
        }
    });
};

/**
 * Check if a feed is selected for deletion, then enable/disable delete button accordingly.
 */
r.customfeed.checkDeleteButtonState = function() {
    if (r.customfeed.selectedFeedForDeletion) {
        $('#delete-custom-feed-btn').removeAttr('disabled');
    } else {
        $('#delete-custom-feed-btn').attr('disabled', 'disabled');
    }
};

// Functions for the second tab
/**
 * Check if both a feed and an article are selected, then enable/disable button accordingly.
 */
r.customfeed.checkButtonState = function() {
    if (r.customfeed.selectedFeed && r.customfeed.selectedArticle) {
        $('#add-to-feed-button').removeAttr('disabled');
        $('#remove-from-feed-button').removeAttr('disabled');
    } else {
        $('#add-to-feed-button').attr('disabled', 'disabled');
        $('#remove-from-feed-button').attr('disabled', 'disabled');
    }
};


/**
 * Fetch user's custom feeds from the server.
 * @param {Function} onComplete - Callback when all feeds are fetched
 * @param {Function} onError - Callback when an error occurs
 */
r.customfeed.fetchFeeds = function(onComplete, onError) {
    // Calling API using GET method
    r.util.ajax({
        url: r.util.url.my_custom_feeds,
        type: 'GET',
        done: function(data) {
            if (data.custom_feeds && data.custom_feeds.length > 0) {
                onComplete(data.custom_feeds);
            } else {
                onComplete([]);
            }
            console.log("l290 data.custom_feed = ", data.custom_feeds);
        },
        fail: function(error) {
            console.error("Failed to load feeds:", error);
            onError(error);
        }
    });
};

/**
 * Fetch articles from the server with pagination support.
 * @param {Function} onComplete - Callback when all articles are fetched
 * @param {Function} onError - Callback when an error occurs
 */
r.customfeed.fetchArticles = function(onComplete, onError) {
    // Calling API using GET method
    r.util.ajax({
        url: r.util.url.all,
        type: 'GET',
        done: function(data) {
            if (data.articles && data.articles.length > 0) {
                onComplete(data.articles);
            } else {
                onComplete([]);
            }
        },
        fail: function(error) {
            console.error("Failed to load articles:", error);
            onError(error);
        }
    });
};

/**
 * Display custom feeds in the left container.
 * @param {Array} feeds - List of feeds to display
 */
r.customfeed.displayFeeds = function(feeds) {
    if (!feeds || feeds.length === 0) {
        var noFeedsMsg = '<p class="no-feeds">' + $.t('custom_feeds.no_feeds') + '</p>';
        $('#user-feeds-container').html(noFeedsMsg);
        return;
    }
    
    // Sort feeds alphabetically
    feeds.sort(function(a, b) {
        return a.title.localeCompare(b.title);
    });
    
    // Build HTML for feed list
    var feedsHtml = '';
    $(feeds).each(function(i, feed) {
        feedsHtml += '<div class="feed-item">' +
            '<input type="radio" id="feed-' + feed.id + '" name="feed-selection" value="' + feed.id + '">' +
            '<label for="feed-' + feed.id + '">' + feed.title + '</label>' +
            '</div>';
    });
    
    // Update feeds container
    $('#user-feeds-container').html(feedsHtml);
};

/**
 * Display feeds for deletion in the delete feeds list.
 * @param {Array} feeds - List of feeds to display
 */
r.customfeed.displayFeedsForDeletion = function(feeds) {
    if (!feeds || feeds.length === 0) {
        var noFeedsMsg = '<p class="no-feeds">' + $.t('custom_feeds.no_feeds') + '</p>';
        $('#delete-feeds-list').html(noFeedsMsg);
        return;
    }
    
    // Sort feeds alphabetically
    feeds.sort(function(a, b) {
        return a.title.localeCompare(b.title);
    });
    
    // Build HTML for feed list
    var feedsHtml = '';
    $(feeds).each(function(i, feed) {
        feedsHtml += '<div class="feed-item">' +
            '<input type="radio" id="delete-feed-' + feed.id + '" name="feed-deletion" value="' + feed.id + '">' +
            '<label for="delete-feed-' + feed.id + '">' + feed.title + '</label>' +
            '</div>';
    });
    
    // Update feeds container and re-bind event handlers
    $('#delete-feeds-list').html(feedsHtml);
};

/**
 * Display articles in the right container.
 * @param {Array} articles - List of articles to display
 */
r.customfeed.displayArticles = function(articles) {
    if (!articles || articles.length === 0) {
        var noArticlesMsg = '<p class="no-articles">' + $.t('custom_feeds.no_articles') + '</p>';
        $('#user-articles-container').html(noArticlesMsg);
        return;
    }
    
    // Sort articles by publication date (newest first)
    articles.sort(function(a, b) {
        // Handle different date formats - they could be strings or timestamps
        var dateA = new Date(a.date);
        var dateB = new Date(b.date);
        return dateB - dateA;
    });
    
    // Build HTML for article list
    var articlesHtml = '';
    $(articles).each(function(i, article) {
        articlesHtml += '<div class="article-item">' +
            '<input type="radio" id="article-' + article.article_id + '" name="article-selection" value="' + article.article_id + '">' +
            '<label for="article-' + article.article_id + '">' + article.title + '</label>' +
            '<div class="article-date">' + new Date(article.date).toLocaleDateString() + '</div>' +
            '</div>';
    });
    
    // Update articles container
    $('#user-articles-container').html(articlesHtml);
};

/**
 * Load user's custom feeds from the server.
 */
r.customfeed.loadFeeds = function() {
    // Show loading indicator
    $('#user-feeds-container').html('<p class="loading">' + $.t('custom_feeds.loading') + '</p>');
    
    // Fetch and display feeds
    r.customfeed.fetchFeeds(
        // Success callback
        function(feeds) {
            r.customfeed.displayFeeds(feeds);
        },
        // Error callback
        function(error) {
            var errorMsg = '<p class="error">' + $.t('custom_feeds.error_loading_feeds') + '</p>';
            $('#user-feeds-container').html(errorMsg);
        }
    );
};

/**
 * Load user's custom feeds for deletion from the server.
 */
r.customfeed.loadFeedsForDeletion = function() {
    // Show loading indicator
    $('#delete-feeds-list').html('<p class="loading">' + $.t('custom_feeds.loading') + '</p>');
    
    // Fetch and display feeds
    r.customfeed.fetchFeeds(
        // Success callback
        function(feeds) {
            r.customfeed.displayFeedsForDeletion(feeds);
        },
        // Error callback
        function(error) {
            var errorMsg = '<p class="error">' + $.t('custom_feeds.error_loading_feeds') + '</p>';
            $('#delete-feeds-list').html(errorMsg);
        }
    );
};

/**
 * Load articles from the server.
 */
r.customfeed.loadArticles = function() {
    // Show loading indicator
    $('#user-articles-container').html('<p class="loading">' + $.t('custom_feeds.loading') + '</p>');
    
    // Fetch and display articles
    r.customfeed.fetchArticles(
        // Success callback
        function(articles) {
            r.customfeed.displayArticles(articles);
        },
        // Error callback
        function(error) {
            var errorMsg = '<p class="error">' + $.t('custom_feeds.error_loading_articles') + '</p>';
            $('#user-articles-container').html(errorMsg);
        }
    );
};

/**
 * Add selected article to the selected feed.
 */
r.customfeed.addArticleToFeed = function() {
    // Disable button during request
    $('#add-to-feed-button').attr('disabled', 'disabled');
    $('#custom-feeds-actions .ajax-loader').removeClass('hidden');
    
    // Get selected feed ID and article ID
    var feedId = r.customfeed.selectedFeed;
    var articleId = r.customfeed.selectedArticle;
    
    // Calling API using POST method
    r.util.ajax({
        url: r.util.url.add_to_customfeed,
        type: 'POST',
        data: {
            feed_id: feedId,
            article_id: articleId
        },
        done: function(data) {
            // Show success notification
            alert($.t('custom_feeds.success_added'), 'success');
            
            // Reset article selection but keep feed selected
            r.customfeed.selectedArticle = null;
            $('input[name="article-selection"]').prop('checked', false);
        },
        fail: function(error) {
            // Show error notification
            if (error.responseJSON.type === 'AlreadyExists') {
                alert($.t('custom_feeds.article_already_added'), 'warning');
            } else {
                alert($.t('custom_feeds.error_adding'), 'error');
                console.error("Failed to add article to feed:", error);
            }
        },
        always: function() {
            // Re-check button state and hide loading indicator
            r.customfeed.checkButtonState();
            $('#custom-feeds-actions .ajax-loader').addClass('hidden');
        }
    });
};

/**
 * Remove selected article from the selected feed.
 */
r.customfeed.removeArticleFromFeed = function() {
    // Disable button during request
    $('#remove-from-feed-button').attr('disabled', 'disabled');
    $('#custom-feeds-actions .ajax-loader').removeClass('hidden');

    // Get selected feed ID and article ID
    var feedId = r.customfeed.selectedFeed;
    var articleId = r.customfeed.selectedArticle;
    
    // Calling API using POST method
    r.util.ajax({
        url: r.util.url.remove_from_customfeed, // You'll need to define this URL in your configuration
        type: 'POST',
        data: {
            feed_id: feedId,
            article_id: articleId
        },
        done: function(data) {
            // Show success notification
            alert($.t('custom_feeds.success_removed'), 'success');
            // Reset article selection but keep feed selected
            r.customfeed.selectedArticle = null;
            $('input[name="article-selection"]').prop('checked', false);
        },
        fail: function(error) {
            if (error.responseJSON.type === "NotFound") {
                // Show a specific notification for this case
                alert($.t('custom_feeds.article_not_in_custom_feed'), 'warning');
            } else {
                // Show general error notification for other errors
                alert($.t('custom_feeds.error_removing'), 'error');
            }
        },
        always: function() {
            // Re-check button state and hide loading indicator
            r.customfeed.checkButtonState();
            $('#custom-feeds-actions .ajax-loader').addClass('hidden');
        }
    });
};


/**
 * Delete a custom feed.
 */
r.customfeed.deleteCustomFeed = function() {
    // Confirm deletion
    if (!confirm($.t('custom_feeds.delete.confirm'))) {
        return;
    }
    
    // Disable button during request
    var deleteButton = $('#delete-custom-feed-btn');
    deleteButton.attr('disabled', 'disabled');
    $('.delete-feed-actions .ajax-loader').removeClass('hidden');
    
    // Get selected feed ID for deletion
    var feedId = r.customfeed.selectedFeedForDeletion;

    // Calling API using POST method
    r.util.ajax({
        url: r.util.url.delete_custom_feed.replace('{id}', feedId),
        type: 'POST',
        done: function(data) {
            // Show success notification
            alert($.t('custom_feeds.delete.success'), 'success');
            
            // Reset feed selection for deletion
            r.customfeed.selectedFeedForDeletion = null;
            
            // Refresh the feed lists
            r.customfeed.loadFeeds();
            r.customfeed.loadFeedsForDeletion();
        },
        fail: function(error) {
            // Show error notification
            alert($.t('custom_feeds.delete.error'), 'error');
            console.error("Failed to delete feed:", error);
        },
        always: function() {
            // Re-check button state and hide loading indicator
            r.customfeed.checkDeleteButtonState();
            $('.delete-feed-actions .ajax-loader').addClass('hidden');
        }
    });
};

/**
 * SHOW FEEDS TAB FUNCTIONALITY
 * This extends the r.customfeed.js with functions for the Show tab
 */

/**
 * Initialize Show Feeds tab
 * @param {jQuery} panel - The panel element
 * @param {boolean} initialize - Whether this is the first initialization
 */
r.customfeed.onTabShow = function(panel, initialize) {
    if (initialize || (r.customfeed.pendingRefresh && r.customfeed.pendingRefresh.showFeed)) {
        // Load feeds for display
        r.customfeed.loadFeedsForDisplay();
        
        // Reset the flag
        if (r.customfeed.pendingRefresh) {
            r.customfeed.pendingRefresh.showFeed = false;
        }
    }
};

/**
 * Track selected feed for display
 */
r.customfeed.selectedFeedForDisplay = null;

/**
 * Track subscription status of selected feed
 */
// r.customfeed.selectedFeedSubscriptionStatus = false;

/**
 * Fetch user's custom feeds from the server.
 * @param {Function} onComplete - Callback when all feeds are fetched
 * @param {Function} onError - Callback when an error occurs
 */
r.customfeed.fetchAllFeeds = function(onComplete, onError) {
    // Calling API using GET method
    r.util.ajax({
        url: r.util.url.all_custom_feeds,
        type: 'GET',
        done: function(data) {
            if (data.custom_feeds && data.custom_feeds.length > 0) {
                onComplete(data.custom_feeds);
            } else {
                onComplete([]);
            }
            console.log("l290 data.custom_feed = ", data.custom_feeds);
        },
        fail: function(error) {
            console.error("Failed to load feeds:", error);
            onError(error);
        }
    });
};

/**
 * Load all custom feeds for display in the 3rd tab 
 * so that user can choose to show, subscribe or unsubscribe.
 */
r.customfeed.loadFeedsForDisplay = function() {
    // Show loading indicator
    $('#show-feeds-list').html('<p class="loading">' + $.t('custom_feeds.loading') + '</p>');
    
    // Fetch and display feeds
    r.customfeed.fetchAllFeeds(
        // Success callback
        function(feeds) {
            r.customfeed.displayFeedsForShow(feeds);
        },
        // Error callback
        function(error) {
            var errorMsg = '<p class="error">' + $.t('custom_feeds.error_loading_feeds') + '</p>';
            $('#show-feeds-list').html(errorMsg);
        }
    );
};

/**
 * Display feeds for the Show Feeds tab.
 * @param {Array} feeds - List of feeds to display
 */
r.customfeed.displayFeedsForShow = function(feeds) {
    if (!feeds || feeds.length === 0) {
        var noFeedsMsg = '<p class="no-feeds">' + $.t('custom_feeds.no_feeds') + '</p>';
        $('#show-feeds-list').html(noFeedsMsg);
        // Disable all buttons
        $('#show-custom-feed-btn').attr('disabled', 'disabled');
        $('#subscribe-custom-feed-btn').attr('disabled', 'disabled');
        $('#unsubscribe-custom-feed-btn').attr('disabled', 'disabled');
        return;
    }
    
    // Sort feeds alphabetically
    feeds.sort(function(a, b) {
        return a.title.localeCompare(b.title);
    });
    
    // Build HTML for feed list
    var feedsHtml = '';
    $(feeds).each(function(i, feed) {
        // Include subscription status as a data attribute
        feedsHtml += '<div class="feed-item">' +
            '<input type="radio" id="show-feed-' + feed.id + '" name="feed-display" value="' + feed.id + '" ' + 
            'data-title="' + feed.title + '" data-description="' + feed.description + '" ' +
            'data-subscribed="' + (feed.subscribed ? 'true' : 'false') + '">' +
            '<label for="show-feed-' + feed.id + '">' + feed.title + '</label>' +
            (feed.subscribed ? '<span class="subscription-status">' + $.t('custom_feeds.subscribed') + '</span>' : '') +
            '</div>';
    });
    
    // Update feeds container
    $('#show-feeds-list').html(feedsHtml);
};

/**
 * Check if a feed is selected for display, then enable/disable buttons accordingly
 */
r.customfeed.checkShowButtonsState = function() {
    if (r.customfeed.selectedFeedForDisplay) {
        // Enable show button
        $('#show-custom-feed-btn').removeAttr('disabled');
        
        // Enable/disable subscribe/unsubscribe buttons based on subscription status
        // if (r.customfeed.selectedFeedSubscriptionStatus) {
        //     $('#subscribe-custom-feed-btn').attr('disabled', 'disabled');
        //     $('#unsubscribe-custom-feed-btn').removeAttr('disabled');
        // } else {
        $('#subscribe-custom-feed-btn').removeAttr('disabled');
        $('#unsubscribe-custom-feed-btn').removeAttr('disabled', 'disabled');
        // }
    } else {
        // Disable all buttons
        $('#show-custom-feed-btn').attr('disabled', 'disabled');
        $('#subscribe-custom-feed-btn').attr('disabled', 'disabled');
        $('#unsubscribe-custom-feed-btn').attr('disabled', 'disabled');
    }
};

/**
 * Fetch articles in a custom feed in response to the Show button in the 3rd tab.
 * @param {string} feedId - ID of the feed to fetch articles from
 * @param {Function} onComplete - Callback when all feed articles are fetched
 * @param {Function} onError - Callback when an error occurs
 */
r.customfeed.fetchFeedArticles = function(feedId, onComplete, onError) {
    // Calling API using GET method
    r.util.ajax({
        url: r.util.url.show_customfeed_articles,
        type: 'POST',
        data: {
            feed_id: feedId
        },
        done: function(data) {
            console.log("l739 data = ", data);
            if (data.articles && data.articles.length > 0) {
                onComplete(data.articles);
            } else {
                onComplete([]);
            }
        },
        fail: function(error) {
            console.error("Failed to load feed articles:", error);
            data("l748 error");
            onError(error);
        }
    });
};

/**
 * Display articles from a feed in the feed articles container
 * @param {Array} articles - List of articles to display
 * @param {string} feedTitle - Title of the feed
 * @param {string} feedDescription - Description of the feed
 */
r.customfeed.displayFeedArticles = function(articles, feedTitle, feedDescription) {
    // Update feed title and description
    $('#selected-feed-title').text(feedTitle);
    $('#selected-feed-description').text(feedDescription);
    console.log("l764");
    console.log("articles.length = ", articles.length);
    if (!articles || articles.length === 0) {
        var noArticlesMsg = '<p class="no-articles">' + $.t('custom_feeds.no_articles_in_feed') + '</p>';
        $('#feed-articles-list').html(noArticlesMsg);
        return;
    }
    
    // Sort articles by publication date (newest first)
    articles.sort(function(a, b) {
        var dateA = new Date(a.date);
        var dateB = new Date(b.date);
        return dateB - dateA;
    });
    console.log("l777");
    // Build HTML for article list
    var articlesHtml = '';
    $(articles).each(function(i, article) {
        articlesHtml += '<div class="feed-article-item">' +
        '<h3><a href="' + article.url + '" target="_blank">' +
        article.title + '</a></h3>' +
        '<div class="article-excerpt">' + (article.description || '') + '</div>' +
        '</div>';
    });
    // Update articles container
    $('#feed-articles-list').html(articlesHtml);
};

/**
 * Show articles from the selected feed
 */
r.customfeed.showFeedArticles = function() {
    if (!r.customfeed.selectedFeedForDisplay) {
        return;
    }
    
    // Get selected feed information
    var feedElement = $('input[name="feed-display"]:checked');
    var feedId = feedElement.val();
    var feedTitle = feedElement.data('title');
    var feedDescription = feedElement.data('description');
    
    // Show loading indicator
    $('#feed-articles-list').html('<p class="loading">' + $.t('custom_feeds.loading') + '</p>');
    $('.show-feed-actions .ajax-loader').removeClass('hidden');
    
    // // Check if user is subscribed (only show articles if subscribed)
    // if (!r.customfeed.selectedFeedSubscriptionStatus) {
    //     // Not subscribed - show message
    //     $('#selected-feed-title').text(feedTitle);
    //     $('#selected-feed-description').text(feedDescription);
    //     $('#feed-articles-list').html('<p class="not-subscribed">' + $.t('custom_feeds.not_subscribed') + '</p>');
    //     $('.show-feed-actions .ajax-loader').addClass('hidden');
    //     return;
    // }
    console.log("l820");
    // Fetch and display feed articles
    r.customfeed.fetchFeedArticles(
        feedId,
        // Success callback
        function(articles) {
            console.log("l826 data = ", articles);
            r.customfeed.displayFeedArticles(articles, feedTitle, feedDescription);
            $('.show-feed-actions .ajax-loader').addClass('hidden');
        },
        // Error callback
        function(error) {
            var errorMsg = '<p class="error">' + $.t('custom_feeds.error_loading_articles') + '</p>';
            $('#feed-articles-list').html(errorMsg);
            $('.show-feed-actions .ajax-loader').addClass('hidden');
        }
    );
};

/**
 * Subscribe to the selected feed
 */
r.customfeed.subscribeToFeed = function() {
    // Get selected feed ID
    var feedId = r.customfeed.selectedFeedForDisplay;
    console.log("l914 feedId = ", feedId);
    // Disable buttons during request
    $('#subscribe-custom-feed-btn').attr('disabled', 'disabled');
    $('.show-feed-actions .ajax-loader').removeClass('hidden');
    
    // Calling API using POST method
    r.util.ajax({
        url: r.util.url.subscribe_to_customfeed,
        type: 'POST',
        data: {
           custom_feed_id: feedId
        },
        done: function(data) {
            // Show success notification
            alert($.t('custom_feeds.subscribe.success'), 'success');

            // Update UI to reflect subscription status
            var feedElement = $('input[name="feed-display"]:checked');
            var label = $('label[for="' + feedElement.attr('id') + '"]');
            
            // Add subscription status indicator if not exists
            // if (feedElement.closest('.feed-item').find('.subscription-status').length === 0) {
            //     feedElement.closest('.feed-item').append(
            //         '<span class="subscription-status">' + $.t('custom_feeds.subscribed') + '</span>'
            //     );
            // }
            feedElement.closest('.feed-item').find('.subscription-status').remove();
            feedElement.closest('.feed-item').append(
            '<span class="subscription-status">' + $.t('custom_feeds.subscribed') + '</span>'
            );
            feedElement.attr('data-subscribed', 'true');
            
            // Refresh button states
            r.customfeed.checkShowButtonsState();
        },
        fail: function(error) {
            // Show error notification
            if (error.responseJSON.type === 'AlreadyExists') {
                alert($.t('custom_feeds.already_subscribed'), 'error');
                console.log("Already subscribed to feed", error);
                // Update UI to reflect subscription status
                var feedElement = $('input[name="feed-display"]:checked');
                var label = $('label[for="' + feedElement.attr('id') + '"]');
                
                // Add subscription status indicator if not exists
                // if (feedElement.closest('.feed-item').find('.subscription-status').length === 0) {
                //     feedElement.closest('.feed-item').append(
                //         '<span class="subscription-status">' + $.t('custom_feeds.already_subscribed') + '</span>'
                //     );
                // }
                feedElement.closest('.feed-item').find('.subscription-status').remove(); // Remove existing if any
                feedElement.closest('.feed-item').append(
                    '<span class="subscription-status">' + $.t('custom_feeds.already_subscribed') + '</span>'
                );
                feedElement.attr('data-subscribed', 'true');

            } else {
                alert($.t('custom_feeds.subscribe.error'), 'error');
                console.error("Failed to subscribe to feed:", error);
            }
        },
        always: function() {
            // Hide loading indicator
            $('.show-feed-actions .ajax-loader').addClass('hidden');
            $('#subscribe-custom-feed-btn').removeAttr('disabled');

        }
    });
};

/**
 * Unsubscribe from the selected feed
 */
r.customfeed.unsubscribeFromFeed = function() {
    // Get selected feed ID
    var feedId = r.customfeed.selectedFeedForDisplay;

    
    // Disable buttons during request
    $('#unsubscribe-custom-feed-btn').attr('disabled', 'disabled');
    $('.show-feed-actions .ajax-loader').removeClass('hidden');
    
    // Calling API using POST method
    r.util.ajax({
        url: r.util.url.unsubscribe_from_customfeed,
        type: 'POST',
        data: {
            custom_feed_id: feedId
        },
        done: function(data) {
            // Show success notification
            alert($.t('custom_feeds.unsubscribe.success'), 'success');

            // Update UI to reflect subscription status
            var feedElement = $('input[name="feed-display"]:checked');
            var label = $('label[for="' + feedElement.attr('id') + '"]');
            
            // Remove subscription status indicator
            feedElement.closest('.feed-item').find('.subscription-status').remove();
            feedElement.closest('.feed-item').append(
                '<span class="subscription-status">' + $.t('custom_feeds.unsubscribed') + '</span>'
                );
            // if (feedElement.closest('.feed-item').find('.subscription-status').length === 0) {
            //     feedElement.closest('.feed-item').append(
            //     '<span class="subscription-status">' + $.t('custom_feeds.unsubscribed') + '</span>'
            //     );
            // }
            feedElement.attr('data-subscribed', 'false');
            
            // Refresh button states
            r.customfeed.checkShowButtonsState();
        },
        fail: function(error) {
            // Show error notification
            if (error.responseJSON.type === 'AlreadyExists') {
                alert($.t('custom_feeds.not_subscribed_yet'), 'error');
                console.log("Not subscribed to feed", error);
                // Update UI to reflect subscription status
                var feedElement = $('input[name="feed-display"]:checked');
                var label = $('label[for="' + feedElement.attr('id') + '"]');

                // Remove subscription status indicator
                feedElement.closest('.feed-item').find('.subscription-status').remove();
                feedElement.closest('.feed-item').append(
                    '<span class="subscription-status">' + $.t('custom_feeds.not_subscribed') + '</span>'
                );
                // if (feedElement.closest('.feed-item').find('.subscription-status').length === 0) {
                //     feedElement.closest('.feed-item').append(
                //         '<span class="subscription-status">' + $.t('custom_feeds.not_subscribed') + '</span>'
                //     );
                // }
                feedElement.attr('data-subscribed', 'false');

                // Refresh button states
                r.customfeed.checkShowButtonsState();

                // Clear articles if they were displayed
                $('#feed-articles-list').html('<p class="not-subscribed">' + $.t('custom_feeds.not_subscribed') + '</p>');
            } else {
                alert($.t('custom_feeds.unsubscribe.error'), 'error');
                console.error("Failed to unsubscribe from feed:", error);
            }
        },
        always: function() {
            // Hide loading indicator
            $('.show-feed-actions .ajax-loader').addClass('hidden');
            $('#unsubscribe-custom-feed-btn').removeAttr('disabled');
        }
    });
};


r.customfeed._originalOnTabActivated = r.customfeed.onTabActivated;

// Override with new function that includes the show tab
r.customfeed.onTabActivated = function(panel) {
    var initialized = panel.data('initialized');
    panel.data('initialized', true);

    switch (panel.attr('id')) {
        case 'custom-feeds-tab-create':
            r.customfeed.onTabCreate(panel, !initialized);
            break;
        case 'custom-feeds-tab-manager':
            r.customfeed.onTabManager(panel, !initialized);
            break;
        case 'custom-feeds-tab-show':
            r.customfeed.onTabShow(panel, !initialized);
            break;
    }
};

  // GET and display all subscribed feeds
  r.customfeed.loadSubscribedFeeds = function() {
    $('#subscribed-feeds-list').html('<p class="loading-message" data-i18n="custom_feeds.loading">Loading your subscribed feeds...</p>');
    
    r.util.ajax({
      url: r.util.url.get_subscribed_customfeeds,
      type: 'GET',
      done: function(data) {
        console.log("l1111, data = ", data);
        if (data.subscribed_feeds && data.subscribed_feeds.length > 0) {
          var feedsHtml = '';
          $.each(data.subscribed_feeds, function(index, feed) {
            feedsHtml += '<div class="subscribed-feed-item" data-feed-id="' + feed.feed_id + '">';
            feedsHtml += '<h4>' + r.util.escapeHtml(feed.feed_title) + '</h4>';
            feedsHtml += '<p class="feed-description">' + r.util.escapeHtml(feed.feed_description) + '</p>';
            feedsHtml += '<button class="view-feed-articles-btn" data-feed-id="' + feed.feed_id + '" data-i18n="custom_feeds.view_articles">View Articles</button>';
            feedsHtml += '<button class="unsubscribe-feed-btn warning" data-feed-id="' + feed.feed_id + '" data-i18n="custom_feeds.unsubscribe">Unsubscribe</button>';
            feedsHtml += '</div>';
          });
          
          $('#subscribed-feeds-list').html(feedsHtml);
        } else {
          $('#subscribed-feeds-list').html('<p class="no-feeds" data-i18n="custom_feeds.no_subscribed_feeds">You haven\'t subscribed to any custom feeds yet.</p>');
          $('#all-subscribed-articles-list').html('<p class="no-feeds" data-i18n="custom_feeds.no_subscribed_feeds">You haven\'t subscribed to any custom feeds yet.</p>');
        }
      },
      fail: function(error) {
        console.error("Failed to load subscribed feeds:", error);
        $('#subscribed-feeds-list').html('<p class="error" data-i18n="custom_feeds.load_error">Failed to load subscribed feeds. Please try again.</p>');
      }
    });
  };
  
  // GET and display all articles from all subscribed feeds
  r.customfeed.loadAllSubscribedArticles = function() {
        $('#all-subscribed-articles-list').html('<p class="loading-message" data-i18n="custom_feeds.loading">Loading articles from your subscribed feeds...</p>');
        
        r.util.ajax({
        url: r.util.url.get_customfeed_articles,
        type: 'GET',
        done: function(data) {
            console.log("l1183, data = ", data);
            if (data.articles && data.articles.length > 0) {
                var articlesHtml = '';
            
                // Create a map of feed IDs to feed titles
                var feedTitles = {};
                $('#subscribed-feeds-list .subscribed-feed-item').each(function() {
                var feedId = $(this).data('feed-id');
                var feedTitle = $(this).find('h4').text();
                feedTitles[feedId] = feedTitle;
                });

            $.each(data.articles, function(index, article) {
                // Format the article display
                articlesHtml += '<div class="article-item">';
                articlesHtml += '<h4><a href="' + article.url + '" target="_blank">' + r.util.escapeHtml(article.title) + '</a></h4>';
                articlesHtml += '<p class="article-meta">';
                
                // Get feed title if available, otherwise just show the feed ID
                var feedTitle = feedTitles[article.feed_id] || 'Feed #' + article.feed_id;
                articlesHtml += '<span class="article-feed">' + r.util.escapeHtml(feedTitle) + '</span>';
                articlesHtml += '</p>';
                articlesHtml += '<p class="article-description">' + r.util.escapeHtml(article.description) + '</p>';
                articlesHtml += '</div>';
            });
          
          $('#all-subscribed-articles-list').html(articlesHtml);
        } else {
          $('#all-subscribed-articles-list').html('<p class="no-articles" data-i18n="custom_feeds.no_articles">No articles found in your subscribed feeds.</p>');
        }
      },
      fail: function(error) {
        console.error("Failed to load articles from subscribed feeds:", error);
        $('#all-subscribed-articles-list').html('<p class="error" data-i18n="custom_feeds.load_articles_error">Failed to load articles. Please try again.</p>');
      }
    });
  };



document.addEventListener('DOMContentLoaded', function() {
    r.customfeed.loadSubscribedFeeds();
    r.customfeed.loadAllSubscribedArticles();
});
document.getElementById('subscribe-custom-feed-btn').addEventListener('click', function() {
    r.customfeed.loadSubscribedFeeds();
    r.customfeed.loadAllSubscribedArticles();
});

document.getElementById('unsubscribe-custom-feed-btn').addEventListener('click', function() {
    r.customfeed.loadSubscribedFeeds();
    r.customfeed.loadAllSubscribedArticles();
});