app.directive("formatDate", function () {
    return {
        restrict: "A",
        scope: {
            date: "="
        },
        template: '{{dateString}}',
        link: function (scope) {
            var date = new Date(scope.date);
            scope.dateString = ('0' + date.getDate()).slice(-2) + '-' + ('0' + (date.getMonth() + 1)).slice(-2) + '-' + date.getFullYear();
        }
    }
});