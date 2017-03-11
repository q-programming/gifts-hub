app.controller('settings', function ($rootScope, $scope, $http, $rootScope, AlertService) {
    $scope.update = function () {
        $http.post('api/user/language', angular.toJson({
            id: $rootScope.principal.id,
            language: $rootScope.principal.language
        })).then(
            function () {
                AlertService.addSuccess('Successfully updated user language.')
                $scope.lang_success = true;
            }).catch(function (response) {
            AlertService.addError('Failed to update user language.')
        });
    }
});