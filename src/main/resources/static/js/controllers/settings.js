app.controller('settings', function ($rootScope, $scope, $http, $rootScope) {
    $scope.lang_success = null;
    $scope.lang_error = null;

    $scope.update = function () {
        $http.post('api/user/language', angular.toJson({
            id: $rootScope.principal.id,
            language: $rootScope.principal.language
        })).then(
            function () {
                $scope.lang_success = true;
            }).catch(function (response) {
            $scope.lang_success = null;
            $scope.lang_error = true
        });
    }
});