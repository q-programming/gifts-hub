app.directive("progressbar", function () {
    return {
        restrict: "A",
        scope: {
            total: "=",
            current: "=",
            class: "="
        },
        link: function (scope, element) {
            scope.$watch("current", function (value) {
                var percentage = scope.current / scope.total * 100;
                element.css("width", percentage + "%");
                if (percentage < 50) {
                    element.addClass("progress-bar-danger");
                    element.removeClass("progress-bar-warning");
                    element.removeClass("progress-bar-success");
                } else if (percentage === 50) {
                    element.removeClass("progress-bar-danger");
                    element.addClass("progress-bar-warning");
                    element.removeClass("progress-bar-success");
                } else if (percentage > 50) {
                    element.removeClass("progress-bar-danger");
                    element.removeClass("progress-bar-warning");
                    element.addClass("progress-bar-success");
                }
            });
            scope.$watch("total", function (value) {
                element.css("width", scope.current / scope.total * 100 + "%");
            })
        }
    }
});