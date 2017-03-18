app.controller('settings', function ($rootScope, $scope, $http, $location, $translate, AlertService) {
    $scope.update = function () {
        $http.post('api/user/language', angular.toJson({
            id: $rootScope.principal.id,
            language: $rootScope.principal.language
        })).then(
            function () {
                $translate.use($rootScope.principal.language);
                $location.search('lang', $rootScope.principal.language);
                AlertService.addSuccess('user.settings.language.set');
                $scope.lang_success = true;
            }).catch(function (response) {
            AlertService.addError('user.settings.language.error')
        });
    }
});