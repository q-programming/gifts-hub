app.controller('alert', function ($rootScope, $scope, AlertService) {
    $scope.dismissAlert = function (index) {
        AlertService.dismissAlert(index)
    }

});