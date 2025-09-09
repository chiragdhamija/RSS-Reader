/**
 * Application modules.
 */
var r = {
  main: {
    mobile: false // True if mobile context
  },
  user: {},
  subscription: {},
  category: {},
  feed: {},
  article: {},
  search: {},
  settings: {},
  about: {},
  wizard: {},
  theme: {},
  shortcuts: {},
  reportbug: {},
  util: {},
  customfeed: {},
  aisummarizer: {},
  aiduplicatedetector: {}
};

/**
 * Application entry point.
 */
$(document).ready(function() {
  r.main.mobile = $('#subscriptions-show-button').is(':visible');
  
  // Displaying login if necessary
  r.util.init();
  r.user.init();
  r.user.signup();
  r.user.boot();
});

/**
 * Modules initialization.
 */
r.main.initModules = function() {
  // Load modules together
  r.subscription.init();
  r.feed.init();
  r.category.init();
  r.article.init();
  r.search.init();
  r.settings.init();
  r.about.init();
  r.wizard.init();
  r.theme.init();
  r.shortcuts.init();
  r.reportbug.init();
  r.customfeed.init();
  r.aisummarizer.init();
  r.aiduplicatedetector.init();

  // First page routing
  if (r.user.hasBaseFunction('ADMIN') && r.user.userInfo.first_connection) {
    window.location.hash = '#/wizard/';
  } else {
    window.location.hash = '#/feed/unread';
  }
};

/**
 * Reset current page context to show a new view.
 */
r.main.reset = function() {
  $('#toolbar > *').addClass('hidden');
  
  r.feed.reset();
  r.settings.reset();
  r.about.reset();
  r.reportbug.reset();
  r.aisummarizer.reset();
  r.aiduplicatedetector.reset();
  r.wizard.reset();
  r.customfeed.reset();
};