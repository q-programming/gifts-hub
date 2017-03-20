app.controller('manage', function ($rootScope, $scope, $http, $log, AlertService) {
    if ($rootScope.authenticated && $rootScope.principal.role === 'ROLE_ADMIN') {
        $scope.settings = {};
        $http.get('api/manage/settings').then(
            function (result) {
                $scope.settings = result.data;
                $log.debug($scope.settings);
            }).catch(function (response) {
            AlertService.addError('error.general', response)
        });
        // $scope.appLang = "en";
        $scope.update = function () {
            $http.post('api/manage/settings', angular.toJson({
                language: $scope.settings.language
            })).then(
                function () {
                    AlertService.addSuccess('user.settings.language.set');
                }).catch(function (response) {
                AlertService.addError('user.settings.language.error')
            });
        }
    }
});