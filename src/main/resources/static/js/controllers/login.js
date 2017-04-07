app.controller('login', function ($rootScope, $scope, $http, $location, $httpParamSerializerJQLike, AlertService, AuthService) {
    $scope.credentials = {};
    $scope.login = function () {
        // We are using formLogin in our backend, so here we need to serialize our form data
        $http({
            url: 'login',
            method: 'POST',
            data: $httpParamSerializerJQLike($scope.credentials),
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        })
            .then(function (res) {
                $rootScope.authenticated = true;
                $location.path("#/");
                AlertService.clearAlerts();
                AuthService.getUser();
            })
            .catch(function () {
                $rootScope.authenticated = false;
                $location.path("/login");
                AlertService.addError('user.login.failed');
            });
    };
});