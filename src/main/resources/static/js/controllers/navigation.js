app.controller('navigation', function ($scope, $rootScope, $http, $location, $route, $log, AvatarService, AuthService) {
    $scope.tab = function (route) {
        return $route.current && route === $route.current.controller;
    };
    $rootScope.authenticated = AuthService.isAuthenticated();

    //LOGOUT
    $scope.logout = function () {
        $http.post('logout', {}).then(
            function successCallback() {
                AvatarService.clearCache();
                $rootScope.authenticated = false;
                $location.path("/login");
            },
            function errorCallback() {
                $rootScope.authenticated = false;
            });
    };
});