app.directive('fixedTop',['$window', function ($window) {
    var $win = angular.element($window);
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            var topClass = attrs.fixedTop;
            var containerClass = 'container';
            var parent = element.parent();
            $win.on('scroll', function (e) {
                var offsetTop = parent.offset().top;
                if ($win.scrollTop() >= offsetTop) {
                    element.addClass(topClass);
                    element.children().first().addClass(containerClass);
                    parent.height(element.height());
                } else {
                    element.removeClass(topClass);
                    element.children().first().removeClass(containerClass);
                    parent.css("height", null);
                }
            });
        }
    }
}]);
