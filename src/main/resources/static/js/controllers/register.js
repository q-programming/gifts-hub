app.controller('register', ['$scope', '$rootScope', '$http', '$log', 'AlertService',
    function ($scope, $rootScope, $http, $log, AlertService) {
        $scope.formData = {};
        $scope.success = false;

        $scope.passwordStrength = function () {
            var strRegexp = [/[0-9]/, /[a-z]/, /[A-Z]/, /[^A-Z-0-9]/i];
            var pass = $scope.formData.password;
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
                    if (response.data.code === 'ERROR') {
                        $log.debug(response.data);
                        AlertService.addError("user.register.failed", response);
                    } else {
                        AlertService.addSuccess('user.register.success');
                        $scope.success = true;
                    }
                }).catch(function (response) {
                AlertService.addError('user.register.failed');
                $log.debug(response);
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
                AlertService.addError("error.general", response);
            });
        };
        $scope.checkUsername = function () {
            if ($scope.formData.username) {
                $http.post('api/user/validate-username', $scope.formData.username).then(
                    function (response) {
                        if (response.data.body.code === 'ERROR') {
                            $scope.userForm.username.$setValidity("exists", false);
                        } else {
                            $scope.userForm.username.$setValidity("exists", true);
                        }
                    }).catch(function (response) {
                    AlertService.addError("error.general", response);
                });
            }
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
    }]);