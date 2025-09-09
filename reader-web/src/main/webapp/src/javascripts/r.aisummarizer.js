/**
 * AI Summarizer module
 */
r.aisummarizer = {};

/**
 * Reset summarizer related context.
 */
r.aisummarizer.reset = function () {
    // Hiding summarizer container
    $('#ai-summarizer-container').hide();
    // Reset the summary output
//     $('#summary-output').addClass('empty-summary')
//         .html('<p data-i18n="summarizer.empty">Your AI summary will appear here. Select an article and click the "Get Summary" button below.</p>');
};

/**
 * Initializing AI summarizer module.
 */
r.aisummarizer.init = function() {
    // Listening hash changes on #/summarizer/
    $.History.bind('/aisummarizer/', function(state, target) {
        // Resetting page context
        r.main.reset();
        
        // Showing summarizer container
        $('#ai-summarizer-container').show();
        
        // Configuring contextual toolbar
        $('#toolbar > .aisummarizer').removeClass('hidden');
        
        // Apply i18n to newly shown elements
        $('#ai-summarizer-container').i18n();
    });
    
    // Get Summary button click event
    $('#get-summary-button').on('click', function() {
        r.aisummarizer.getSummary();
    });
};

/**
 * Get AI summary from the server.
 */
r.aisummarizer.getSummary = function() {
    // Get the summary output container
    var summaryOutput = $('#summary-output');
    
    // Show loading indicator
    summaryOutput.removeClass('empty-summary').html('<p>' + $.t('summarizer.loading') + '</p>');
    $('#get-summary-button').attr('disabled', 'disabled');
    $('#ai-summarizer-container .ajax-loader').removeClass('hidden');
    
    // Calling API using GET method with no data parameters
    r.util.ajax({
        url: r.util.url.aisummary,
        type: 'GET',
        done: function(data) {
            // Display the summary
            summaryOutput.removeClass('empty-summary').html(data.summary);
        },
        fail: function(error) {
            // Show error message
            summaryOutput.addClass('empty-summary')
                .html('<p class="error">' + $.t('summarizer.error') + '</p>');
            console.error("Summary fetch failed:", error);
        },
        always: function() {
            // Re-enable button and hide loading indicator
            $('#get-summary-button').removeAttr('disabled');
            $('#ai-summarizer-container .ajax-loader').addClass('hidden');
        }
    });
};

/**
 * Clear the current summary.
 */
r.aisummarizer.clearSummary = function() {
    $('#summary-output').addClass('empty-summary')
        .html('<p data-i18n="summarizer.empty">Your AI summary will appear here. Select an article and click the "Get Summary" button below.</p>');
    $('#summary-output').i18n();
};