/**
 * Reset duplicate detector related context.
 */
r.aiduplicatedetector.reset = function () {
    // Hiding duplicate detector container
    $('#ai-duplicate-detector-container').hide();
    
    // Reset selections
    r.aiduplicatedetector.selectedLeft = null;
    r.aiduplicatedetector.selectedRight = null;
    
    // Reset radio buttons
    $('input[name="article-left"]').prop('checked', false);
    $('input[name="article-right"]').prop('checked', false);
    
    // Reset result area
    $('#duplicate-result').html('').hide();
    
    // Disable compare button
    $('#detect-duplicate-button').attr('disabled', 'disabled');
};

/**
 * Initializing AI duplicate detector module.
 */
r.aiduplicatedetector.init = function() {
    // Initialize selection trackers
    r.aiduplicatedetector.selectedLeft = null;
    r.aiduplicatedetector.selectedRight = null;

    // Listening hash changes on #/duplicate-detector/
    $.History.bind('/aiduplicatedetector/', function(state, target) {
        // Resetting page context
        r.main.reset();
        
        // Showing duplicate detector container
        $('#ai-duplicate-detector-container').show();
        
        // Configuring contextual toolbar
        $('#toolbar > .duplicate-detector').removeClass('hidden');
        
        // Load article lists
        r.aiduplicatedetector.loadArticles();
        
        // Apply i18n to newly shown elements
        $('#ai-duplicate-detector-container').i18n();
    });
    
    // Detect Duplicity button click event
    $('#detect-duplicate-button').on('click', function() {
        if (r.aiduplicatedetector.selectedLeft && r.aiduplicatedetector.selectedRight) {
            r.aiduplicatedetector.compareArticles();
        }
    });
    
    // Event delegation for radio button changes
    $('#left-articles-container').on('change', 'input[name="article-left"]', function() {
        r.aiduplicatedetector.selectedLeft = $(this).val();
        r.aiduplicatedetector.checkButtonState();
    });
    
    $('#right-articles-container').on('change', 'input[name="article-right"]', function() {
        r.aiduplicatedetector.selectedRight = $(this).val();
        r.aiduplicatedetector.checkButtonState();
    });
};

/**
 * Check if both articles are selected and enable/disable button accordingly.
 */
r.aiduplicatedetector.checkButtonState = function() {
    if (r.aiduplicatedetector.selectedLeft && r.aiduplicatedetector.selectedRight) {
        $('#detect-duplicate-button').removeAttr('disabled');
    } else {
        $('#detect-duplicate-button').attr('disabled', 'disabled');
    }
};

/**
 * Fetch articles from the server with pagination support.
 * @param {Function} onComplete - Callback when all articles are fetched
 * @param {Function} onError - Callback when an error occurs
 */
r.aiduplicatedetector.fetchArticles = function(onComplete, onError) {
    // Calling API using GET method
    r.util.ajax({
        url: r.util.url.all,
        type: 'GET',
        done: function(data) {
            console.log("l90 data = ", data);
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
 * Display articles in both left and right containers.
 * @param {Array} articles - List of articles to display
 */
r.aiduplicatedetector.displayArticles = function(articles) {
    if (!articles || articles.length === 0) {
        var noArticlesMsg = '<p class="no-articles">' + $.t('duplicate_detector.no_articles') + '</p>';
        $('#left-articles-container').html(noArticlesMsg);
        $('#right-articles-container').html(noArticlesMsg);
        return;
    }
    
    // Sort articles by publication date (newest first)
    articles.sort(function(a, b) {
        return new Date(b.date) - new Date(a.date);
    });
    
    // Build HTML for left article list
    var leftArticlesHtml = '';
    $(articles).each(function(i, article) {
        leftArticlesHtml += '<div class="article-item">' +
            '<input type="radio" id="left-' + article.id + '" name="article-left" value="' + article.id + '">' +
            '<label for="left-' + article.id + '">' + article.title + '</label>' +
            '</div>';
    });
    
    // Update left container
    $('#left-articles-container').html(leftArticlesHtml);
    
    // Build HTML for right article list
    var rightArticlesHtml = '';
    $(articles).each(function(i, article) {
        rightArticlesHtml += '<div class="article-item">' +
            '<input type="radio" id="right-' + article.id + '" name="article-right" value="' + article.id + '">' +
            '<label for="right-' + article.id + '">' + article.title + '</label>' +
            '</div>';
    });
    
    // Update right container
    $('#right-articles-container').html(rightArticlesHtml);
};

/**
 * Load articles from the server.
 * This function retrieves all articles (both read and unread) for the current user
 * and populates the left and right article selection containers.
 */
r.aiduplicatedetector.loadArticles = function() {
    // Show loading indicators
    $('#left-articles-container').html('<p class="loading">' + $.t('duplicate_detector.loading') + '</p>');
    $('#right-articles-container').html('<p class="loading">' + $.t('duplicate_detector.loading') + '</p>');
    
    // Fetch and display articles
    r.aiduplicatedetector.fetchArticles(
        // Success callback
        function(articles) {
            r.aiduplicatedetector.displayArticles(articles);
        },
        // Error callback
        function(error) {
            var errorMsg = '<p class="error">' + $.t('duplicate_detector.error_loading') + '</p>';
            $('#left-articles-container').html(errorMsg);
            $('#right-articles-container').html(errorMsg);
        }
    );
};

/**
 * Compare selected articles for duplicity.
 */
r.aiduplicatedetector.compareArticles = function() {
    // Show loading in results area
    $('#duplicate-result').html('<p class="loading">' + $.t('duplicate_detector.comparing') + '</p>').show();
    
    // Disable button during request
    $('#detect-duplicate-button').attr('disabled', 'disabled');
    $('#duplicate-detector-actions .ajax-loader').removeClass('hidden');
    
    // Get selected article IDs
    var article1Id = r.aiduplicatedetector.selectedLeft;
    var article2Id = r.aiduplicatedetector.selectedRight;
    
    // Calling API using GET method
    r.util.ajax({
        url: r.util.url.compare_articles,
        type: 'GET',
        data: {
            article1Id: article1Id,
            article2Id: article2Id
        },
        done: function(data) {
            // Check if status is ok
            if (data.status !== 'ok') {
                $('#duplicate-result').html('<p class="error">' + $.t('duplicate_detector.error_comparing') + '</p>');
                return;
            }
            
            // Display the comparison result
            var resultHtml = '';
            
            if (data.isDuplicate) {
                resultHtml = '<div class="duplicate-detected">' +
                    '<h3>' + $.t('duplicate_detector.duplicate_found') + '</h3>' +
                    '<p>' + $.t('duplicate_detector.similarity_score') + ': ' + data.similarityScore + '%</p>';
                    '<p>' + $.t('duplicate_detector.is_duplicate') + ': ' + data.isDuplicate + '%</p>';
                
                // Add details section only if details are provided
                if (data.details) {
                    resultHtml += '<div class="similarity-details">' + data.details + '</div>';
                }
                
                resultHtml += '</div>';
            } else {
                resultHtml = '<div class="no-duplicate">' +
                    '<h3>' + $.t('duplicate_detector.no_duplicate') + '</h3>' +
                    '<p>' + $.t('duplicate_detector.similarity_score') + ': ' + data.similarityScore + '%</p>' +
                    '<p>' + $.t('duplicate_detector.is_duplicate') + ': ' + data.isDuplicate + '%</p>' +
                    '</div>';
            }
            
            $('#duplicate-result').html(resultHtml);
        },
        fail: function(error) {
            // Show error message
            $('#duplicate-result').html('<p class="error">' + $.t('duplicate_detector.error_comparing') + '</p>');
            console.error("Comparison failed:", error);
        },
        always: function() {
            // Re-enable button and hide loading indicator
            $('#detect-duplicate-button').removeAttr('disabled');
            $('#duplicate-detector-actions .ajax-loader').addClass('hidden');
        }
    });
};