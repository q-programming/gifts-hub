app.controller('navigation', function ($scope, $rootScope, $http, $location, $route, avatarService) {
    $scope.tab = function (route) {
        return $route.current && route === $route.current.controller;
    };

    var authenticate = function (credentials, callback) {
        var headers = credentials ? {
            authorization: "Basic "
            + btoa(credentials.username + ":" + credentials.password)
        } : {};

        $http.get('api/user/', {headers: headers}).then(
            function (response) {
                var data = response.data;
                if (data.id) {
                    $rootScope.authenticated = true;
                    $rootScope.principal = data;
                    avatarService.getAvatar($rootScope.principal.id);
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
        $http.post('/logout', {}).then(
            function successCallback() {
                $rootScope.authenticated = false;
                $location.path("/");
            },
            function errorCallback() {
                $rootScope.authenticated = false;
            });
    };
});

app.controller('register', function ($scope, $rootScope, $http) {
    $scope.formData = {};
    $scope.success = null;
    $scope.doNotMatch = null;
    $scope.error = null;
    $scope.errorUserExists = null;

    $scope.register = function () {
        $http.post('api/user/register', $scope.formData).then(
            function () {
                $scope.success = true;
            }).catch(function (response) {
            $scope.success = null;
            if (response.status === 400 && response.data === 'login already in use') {
                $scope.errorUserExists = 'ERROR';
            } else if (response.status === 400 && response.data === 'e-mail address already in use') {
                $scope.errorEmailExists = 'ERROR';
            } else {
                $scope.error = 'ERROR';
            }
        });
    }
});
