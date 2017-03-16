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

    $scope.passwordStrength = function () {
        var strRegexp = [/[0-9]/, /[a-z]/, /[A-Z]/, /[^A-Z-0-9]/i];
        var pass = $scope.formData.password;
        var confirmPass = $scope.formData.confirmpassword;
        if (!pass) {
            return -1;
        }
        var s = 0;
        if (pass.length < 8)
            return 0;
        for (var i in strRegexp) {
            if (strRegexp[i].test(pass)) {
                s++;
            }
        }
        return s;

    };

    $scope.register = function () {
        $scope.showErrorsCheckValidity = true;
        //call all checks once more
        $scope.checkEmail();
        $scope.checkPasswords();
        $scope.checkUsername();
        if ($scope.userForm.$invalid) {
            return;
        }
        $http.post('api/user/register', $scope.formData).then(
            function (response) {
                if (response.data.body.code === 'ERROR') {
                    AlertService.addError("Unable to register user : " + response.data.body.message);
                } else {
                    AlertService.addSuccess('<strong>Successfully registered.</strong> Please check your email for confirmation.');
                    $scope.success = true;
                }
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

    $scope.checkEmail = function () {
        $http.post('api/user/validate-email', $scope.formData.email).then(
            function (response) {
                if (response.data.body.code === 'ERROR') {
                    $scope.userForm.email.$setValidity("exists", false);
                } else {
                    $scope.userForm.email.$setValidity("exists", true);
                }
            }).catch(function (response) {
            AlertService.addError("Something went wrong: " + response.data);
        });
    };
    $scope.checkUsername = function () {
        $http.post('api/user/validate-username', $scope.formData.username).then(
            function (response) {
                if (response.data.body.code === 'ERROR') {
                    $scope.userForm.username.$setValidity("exists", false);
                } else {
                    $scope.userForm.username.$setValidity("exists", true);
                }
            }).catch(function (response) {
            AlertService.addError("Something went wrong: " + response.data);
        });
    };
    $scope.checkPasswords = function () {
        if ($scope.formData.password !== $scope.formData.confirmpassword) {
            $scope.userForm.confirmpassword.$setValidity("passDontMatch", false);
        } else {
            $scope.userForm.confirmpassword.$setValidity("passDontMatch", true);
        }
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

