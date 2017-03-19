app.controller('manage', function ($rootScope, $scope, $http, AlertService) {
    if ($rootScope.authenticated && $rootScope.principal.role === 'ROLE_ADMIN') {
        $scope.appLang = "en";
        $scope.update = function () {
            $http.post('api/manage/language', angular.toJson({
                "app.language": $scope.appLang
            })).then(
                function () {
                    AlertService.addSuccess('user.settings.language.set');
                }).catch(function (response) {
                AlertService.addError('user.settings.language.error')
            });
        }
    }
});