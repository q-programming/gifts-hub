app.controller('alert',['$rootScope', '$scope', 'AlertService', function ($rootScope, $scope, AlertService) {
    $scope.dismissAlert = function (index) {
        AlertService.dismissAlert(index)
    }
}]);