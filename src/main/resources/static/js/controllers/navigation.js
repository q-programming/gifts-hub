app.controller('navigation', function ($scope, $rootScope, $http, $location, $route) {
    $scope.tab = function (route) {
        return $route.current && route === $route.current.controller;
    };

    var authenticate = function (credentials, callback) {
        var headers = credentials ? {
            authorization: "Basic "
            + btoa(credentials.username + ":" + credentials.password)
        } : {};

        $http.get('api/user', {headers: headers}).then(
            function (response) {
                var data = response.data;
                if (data.name) {
                    $rootScope.authenticated = true;
                } else {
                    $rootScope.authenticated = false;
                }
                callback && callback();
            },
            function () {
                $rootScope.authenticated = false;
                callback && callback();
            });
    };

    authenticate();
    $scope.credentials = {};
    //LOGIN
    $scope.login = function () {
        authenticate($scope.credentials, function () {
            if ($rootScope.authenticated) {
                $location.path("/");
                $scope.error = false;
            } else {
                $location.path("/login");
                $scope.error = true;
            }
        });
    };
    //LOGOUT
    $scope.logout = function () {
        $http.post('logout', {}).then(
            function successCallback() {
                $rootScope.authenticated = false;
                $location.path("/");
            },
            function errorCallback() {
                $rootScope.authenticated = false;
            });
    };
});
