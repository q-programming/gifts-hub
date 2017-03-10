app.controller('navigation', function ($scope, $rootScope, $http, $location, $route, AvatarService, AuthService) {
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
        $scope.showErrorsCheckValidity = true;
        if ($scope.formData.$invalid) {
            return;
        }
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

    $scope.createUsername = function () {
        if (!$scope.formData.email.$invalid) {
            $scope.formData.username = $scope.formData.email.split("@")[0];

        }

    }
});
app.directive('showErrors', function () {
    return {
        restrict: 'A',
        require: '^form',
        link: function (scope, el, attrs, formCtrl) {
            var inputEl = el[0].querySelector("[name]");
            var inputNgEl = angular.element(inputEl);
            var inputName = inputNgEl.attr('name');
            inputNgEl.bind('blur', function () {
                el.toggleClass('has-error', formCtrl[inputName].$invalid);
            });
            scope.$watch(function () {
                return scope.showErrorsCheckValidity;
            }, function (newVal, oldVal) {
                if (!newVal) {
                    return;
                }
                el.toggleClass('has-error', formCtrl[inputName].$invalid);
            });
        }
    }
});
