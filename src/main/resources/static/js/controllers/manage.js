app.controller('manage', function ($rootScope, $scope, $http, $log, AlertService) {
    if ($rootScope.authenticated && $rootScope.principal.role === 'ROLE_ADMIN') {
        $scope.settings = {};
        $scope.newsearch = {};
        $scope.searchEngineList = [];

        $http.get('api/manage/settings').then(
            function (result) {
                $scope.settings = result.data;
            }).catch(function (response) {
            AlertService.addError('error.general', response)
        });


        $scope.update = function () {
            $http.post('api/manage/settings', $scope.settings).then(
                function () {
                    AlertService.addSuccess('user.settings.language.set');
                }).catch(function (response) {
                AlertService.addError('user.settings.language.error')
            });
        }

        $scope.show = function () {
            $scope.showAddNewSearch = true;
        };
        $scope.reset = function () {
            $scope.newsearch = {};
            $scope.showAddNewSearch = false;
        };
        $scope.createNewSearchEngine = function () {
            //TODO validation
            $scope.settings.searchEngines.unshift($scope.newsearch);
            $scope.newsearch = {};
            $scope.showAddNewSearch = false;
        }
    }
});