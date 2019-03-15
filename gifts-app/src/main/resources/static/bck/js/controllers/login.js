app.controller('login', ['$rootScope', '$scope', '$http', '$log', '$location', '$httpParamSerializerJQLike', 'AlertService', 'AuthService',
    function ($rootScope, $scope, $http, $log, $location, $httpParamSerializerJQLike, AlertService, AuthService) {
        $scope.credentials = {};
        if ($rootScope.authenticated) {
            $location.path("#/");
        }
        $scope.login = function () {
            $http({
                url: 'login',
                method: 'POST',
                data: $httpParamSerializerJQLike($scope.credentials),
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            }).then(function (result) {
                $rootScope.authenticated = true;
                AlertService.clearAlerts();
                AuthService.getUser();
            }).catch(function (result) {
                $rootScope.authenticated = false;
                AlertService.addError('user.login.failed');
                $log.debug(result);
                $location.path("/login");

            });
        };
    }]);
app.controller('logout', ['$scope', '$rootScope', 'AuthService',
    function ($scope, $rootScope, AuthService) {
        AuthService.logout();
    }]);