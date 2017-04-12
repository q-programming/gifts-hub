app.controller('login', ['$rootScope', '$scope', '$http', '$log', '$location', '$httpParamSerializerJQLike', 'AlertService', 'AuthService',
    function ($rootScope, $scope, $http, $log, $location, $httpParamSerializerJQLike, AlertService, AuthService) {
        $scope.credentials = {};
        $scope.login = function () {
            $http({
                url: 'login',
                method: 'POST',
                data: $httpParamSerializerJQLike($scope.credentials),
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            })
                .then(function (result) {
                    $rootScope.authenticated = true;
                    $location.path("#/");
                    AlertService.clearAlerts();
                    AuthService.getUser();
                })
                .catch(function (result) {
                    $rootScope.authenticated = false;
                    AlertService.addError('user.login.failed');
                    $log.debug(result);
                    $location.path("/login");

                });
        };
    }]);