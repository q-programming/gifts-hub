app.directive('fixedTop', function ($window, $log) {
    var $win = angular.element($window);
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            var topClass = attrs.fixedTop;
            var containerClass = 'container';
            var parent = element.parent();
            // $log.debug("fixedTop init");
            $win.on('scroll', function (e) {
                var offsetTop = parent.offset().top;
                // $log.debug(offsetTop);
                // $log.debug($win.scrollTop());
                if ($win.scrollTop() >= offsetTop) {
                    // $log.debug("fixedTop add topClass");
                    element.addClass(topClass);
                    element.children().first().addClass(containerClass);
                    parent.height(element.height());
                } else {
                    element.removeClass(topClass);
                    // $log.debug("fixedTop remove topClass");
                    element.children().first().removeClass(containerClass);
                    parent.css("height", null);
                }
            });
        }
    }
});
