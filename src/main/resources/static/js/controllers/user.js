app.controller('navigation', function ($scope, $rootScope, $http, $location, $route, AvatarService, AuthService, MESSAGES) {
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
                $rootScope.clearAlerts();
            } else {
                $location.path("/login");
                $rootScope.addAlert(MESSAGES.ERROR, 'There was a problem logging in. Please try again.');
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

app.controller('register', function ($scope, $rootScope, $http, MESSAGES) {
    $scope.formData = {};

    $scope.register = function () {
        $scope.showErrorsCheckValidity = true;
        if ($scope.formData.$invalid) {
            return;
        }
        $http.post('api/user/register', $scope.formData).then(
            function () {
                $rootScope.addAlert(MESSAGES.SUCCESS, '<strong>Successfully registered.</strong> Please check your email for confirmation.');
            }).catch(function (response) {
            $rootScope.addAlert(MESSAGES.ERROR, 'There was a problem registering. Please try again.');
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
