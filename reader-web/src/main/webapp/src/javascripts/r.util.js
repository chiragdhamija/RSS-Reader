/**
 * API URLs.
 */
r.util.url = {
  user_info: '../api/user',
  user_login: '../api/user/login',
  user_update: '../api/user',
  user_logout: '../api/user/logout',
  user_list: '../api/user/list',
  user_register: '../api/user',
  user_username_info: '../api/user/{username}',
  user_username_update: '../api/user/{username}',
  user_username_delete: '../api/user/{username}',
  job_delete: '../api/job/{id}',
  subscription_list: '../api/subscription',
  subscription_add: '../api/subscription',
  subscription_simulate: '../api/subscription/simulate',
  subscription_update: '../api/subscription/{id}',
  subscription_delete: '../api/subscription/{id}',
  subscription_get: '../api/subscription/{id}',
  subscription_import: '../api/subscription/import',
  subscription_export: '../api/subscription/export',
  subscription_favicon: '../api/subscription/{id}/favicon',
  subscription_sync: '../api/subscription/{id}/sync',
  category_update: '../api/category/{id}',
  category_delete: '../api/category/{id}',
  category_add: '../api/category',
  category_list: '../api/category',
  all: '../api/all',
  starred: '../api/starred',
  trending: '../api/starred/trending',
  starred_star: '../api/starred/{id}',
  article_read: '../api/article/{id}/read',
  article_unread: '../api/article/{id}/unread',
  articles_read: '../api/article/read',
  search: '../api/search/{query}',
  locale_list: '../api/locale',
  theme_list: '../api/theme',
  app_batch_reindex: '../api/app/batch/reindex',
  app_log: '../api/app/log',
  app_version: '../api/app',
  app_map_port: '../api/app/map_port',
  github_tags: 'https://api.github.com/repos/sismics/reader/tags',
  // report bugs
  report_bug: '../api/bugreport',
  bug_list: '../api/bugreport/list',
  bug_delete: '../api/bugreport/{id}/delete',
  bug_resolve: '../api/bugreport/{id}/resolve',
  // custom feed
  my_custom_feeds: '../api/customfeed/my_custom_feeds',
  add_to_customfeed: '../api/customfeed/add_article',
  remove_from_customfeed: '../api/customfeed/remove_article',
  create_custom_feed: '../api/customfeed/create',
  delete_custom_feed: '../api/customfeed/{id}/delete',
  // custom feed subscription
  all_custom_feeds: '../api/customfeedsubscription/all_custom_feeds',
  show_customfeed_articles: '../api/customfeedsubscription/show',
  subscribe_to_customfeed: '../api/customfeedsubscription/subscribe',
  unsubscribe_from_customfeed: '../api/customfeedsubscription/unsubscribe',
  // 4th tab - gives all feeds and articles
  get_subscribed_customfeeds: '../api/customfeedsubscription/subscribed_feeds',
  get_customfeed_articles: '../api/customfeedsubscription/user_articles',
  aisummary: '../api/aisummary',
  compare_articles: '../api/ai_duplicate_detector/compare_articles',
};

/**
 * Initialize utility module.
 */
r.util.init = function() {
  // Initialize toastmessage
  $().toastmessage({
    sticky: false,
    position : 'top-center'
  });
  
  // Initialize show/replace pattern
  $('body').on('click', '.show-pattern-button', function() {
    var show = $(this).attr('data-show');
    $(show, this).show();
    $(show + ' input[type="text"]:first', this).focus();
    $(this).hide();
  });
};

/**
 * Wrapper around $.ajax().
 */
r.util.ajax = function(args) {
  args.dataType = 'json';
  
  args.cache = false;
  if (!args.fail) {
    args.fail = function(jqxhr) {
      if (jqxhr.responseText) {
        console.log(jqxhr.responseText);
      }
      $().toastmessage('showErrorToast', $.t('error.unknown'));
    }
  }
  
  return $.ajax(args)
    .done(args.done)
    .fail(args.fail)
    .always(args.always);
};

/**
 * Returns animated CSS3 loader.
 */
r.util.buildLoader = function() {
  return '<div class="loader"><div id="bowlG"><div id="bowl_ringG"><div class="ball_holderG"><div class="ballG"></div></div></div></div></div>';
};

/**
 * Escape HTML.
 */
r.util.escape = function(str) {
  return $('<div />').text(str).html();
};

/**
 * Redraw an element (WebKit workaround).
 */
jQuery.fn.redraw = function() {
  var _this = this;
  setTimeout(function() {
    _this.hide(0, function() {
      $(this).show();
    });
  }, 10);
  return this;
};
