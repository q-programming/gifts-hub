app.controller('help', ['$rootScope', '$scope', '$http', '$log', 'AlertService',
    function ($rootScope, $scope, $http, $log, AlertService) {
        $scope.tourReset = function () {
            var url = 'api/user/tour-reset';
            $http.post(url).then(
                function (response) {
                    $log.debug("[DEBUG] Tour marked as incomplete");
                    $rootScope.principal.tourComplete = false;
                }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        };
    }]);