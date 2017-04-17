app.directive('fixedTop', function ($window) {
    var $win = angular.element($window);
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            var topClass = attrs.fixedTop;
            var containerClass = 'container';

            var offsetTop = element.offset().top;
            $win.on('scroll', function (e) {
                if ($win.scrollTop() >= offsetTop) {
                    element.addClass(topClass);
                    element.children().first().addClass(containerClass);
                } else {
                    element.removeClass(topClass);
                    element.children().first().removeClass(containerClass);
                }
            });
        }
    }
});
