app.controller('navigation', function ($scope, $rootScope, $http, $location, $route, $log, AvatarService, AuthService, AlertService) {
    $scope.tab = function (route) {
        return $route.current && route === $route.current.controller;
    };

    AuthService.authenticate();
    $scope.credentials = {};
    //LOGIN
    $scope.login = function () {
        AuthService.authenticate($scope.credentials, function () {
            if ($rootScope.authenticated) {
                $location.path("/");
                AlertService.clearAlerts();
            } else {
                $location.path("/login");
                AlertService.addError('user.login.failed');
            }
        });
    };

    //LOGOUT
    $scope.logout = function () {
        $http.post('logout', {}).then(
            function successCallback() {
                AvatarService.clearCache();
                $rootScope.authenticated = false;
                $location.path("/");
            },
            function errorCallback() {
                $rootScope.authenticated = false;
            });
    };

    $scope.dismissAlert = function (index) {
        AlertService.dismissAlert(index)
    }
});