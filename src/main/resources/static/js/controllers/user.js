app.controller('navigation', function ($scope, $rootScope, $http, $location, $route, AvatarService, AuthService, AlertService) {
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
                AlertService.addError('There was a problem logging in. Please try again.');
            }
        });
    };

    //LOGOUT
    $scope.logout = function () {
        $http.post('/logout', {}).then(
            function successCallback() {
                AvatarService.clearCache();
                $rootScope.authenticated = false;
                $location.path("/");
            },
            function errorCallback() {
                $rootScope.authenticated = false;
            });
    };
});

app.controller('register', function ($scope, $rootScope, $http, AlertService) {
    $scope.formData = {};
    $scope.success = false;

    $scope.register = function () {
        $scope.showErrorsCheckValidity = true;
        if ($scope.formData.$invalid) {
            return;
        }
        $http.post('api/user/register', $scope.formData).then(
            function () {
                AlertService.addSuccess('<strong>Successfully registered.</strong> Please check your email for confirmation.');
                $scope.success = true;
            }).catch(function (response) {
            AlertService.addError('There was a problem registering. Please try again.');
            // if (response.status === 400 && response.data === 'login already in use') {
            //     $scope.errorUserExists = 'ERROR';
            // } else if (response.status === 400 && response.data === 'e-mail address already in use') {
            //     $scope.errorEmailExists = 'ERROR';
            // } else {
            //     $scope.error = 'ERROR';
            // }
        });
    };

    $scope.createUsername = function () {
        if (!$scope.formData.username && !$scope.formData.email.$invalid) {
            $scope.formData.username = $scope.formData.email.split("@")[0];
        }
    }
});

app.controller('userlist', function ($scope, $rootScope, $http, $log, AlertService, AvatarService) {
    $scope.users = [];
    if ($rootScope.authenticated) {
        $http.get('api/user/users').then(
            function (response) {
                $scope.users = [];
                $log.debug("[DEBUG] Loaded users");
                angular.forEach(response.data, function (value) {
                    var user = value;
                    AvatarService.getUserAvatar(user);
                    //TODO get avatars
                    $scope.users.push(user);
                });
            }).catch(function (response) {
            AlertService.addError("Something went wrong: " + response.data);
        });
    }
});

