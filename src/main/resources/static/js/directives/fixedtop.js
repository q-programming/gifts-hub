app.directive('fixedTop', function ($window, $log) {
    var $win = angular.element($window);
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            var topClass = attrs.fixedTop;
            var containerClass = 'container';
            // $log.debug("fixedTop init");
            var offsetTop = element.offset().top;
            $win.on('scroll', function (e) {
                // $log.debug(offsetTop);
                // $log.debug($win.scrollTop());
                if ($win.scrollTop() >= offsetTop) {
                    // $log.debug("fixedTop add topClass");
                    element.addClass(topClass);
                    element.children().first().addClass(containerClass);
                } else {
                    element.removeClass(topClass);
                    // $log.debug("fixedTop remove topClass");
                    element.children().first().removeClass(containerClass);
                }
            });
        }
    }
});
