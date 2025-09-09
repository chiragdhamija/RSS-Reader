/**
 * Initialize theme module.
 */
r.theme.init = function() {
  // give js code to alert the type of r.user.userInfo.theme
  r.theme.update("default");
};

/**
 * Update current theme.
 */
r.theme.update = function(theme) {
  if (theme) {
    // Add custom stylesheet
    var isLess = typeof less != 'undefined';
    if (isLess) {
      // Remove previous custom theme
      less.sheets = $.grep(less.sheets, function(sheet) {
        return !$(sheet).hasClass('custom');
      });
      $('html > style').remove();
        
      // Add custom stylesheet
      less.sheets.push($('<link class="custom" rel="stylesheet/less" href="stylesheets/theme/' + theme + '.less" type="text/css" />')[0]);
      less.refresh();
    } else {
      // Remove previous custom theme
      $('head > link.theme').remove();
      
      // Just add the stylesheet, the browser handle it
      $('head').append($('<link class="theme" rel="stylesheet" href="stylesheets/theme/' + theme + '.css" type="text/css" />')[0]);
    }
  }
};