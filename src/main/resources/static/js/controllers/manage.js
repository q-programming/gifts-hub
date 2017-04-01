app.controller('manage', function ($rootScope, $scope, $http, $log, AlertService) {
    $scope.settings = {};
    $scope.searchEngine = {};
    $scope.searchEngineList = [];
    $scope.showSearchForm = null;
    $scope.editSearch = false;
    $scope.languages = {};
    if ($rootScope.authenticated && $rootScope.principal.role === 'ROLE_ADMIN') {
        getLanguages();
        getAppSettings();
        $scope.update = function () {
            $scope.showSearchForm = false;
            $scope.editSearch = false;
            $http.post('api/app/settings', $scope.settings).then(
                function () {
                    AlertService.addSuccess('app.manage.saved');
                }).catch(function (response) {
                AlertService.addError('error.general', response)
            });
        };

        $scope.show = function () {
            $scope.showSearchForm = true;
        };
        $scope.reset = function () {
            $scope.searchEngine = {};
            $scope.showSearchForm = false;
            $scope.editSearch = false;
        };

        $scope.updateSearchEngine = function () {
            if (!$scope.editSearch) {
                $scope.settings.searchEngines.push($scope.searchEngine);
                $scope.searchEngine = {};
                $scope.showSearchForm = false;
            } else {
                var indexes = $.map($scope.settings.searchEngines, function (engine, index) {
                    if (engine.id === $scope.searchEngine.id) {
                        return index;
                    }
                });
                $scope.settings.searchEngines[indexes[0]] = $scope.searchEngine;
            }
            //TODO validation
            $scope.update();
        };

        $scope.deleteSearchEngine = function (engine) {
            if ($scope.settings.searchEngines) {
                var index = $scope.settings.searchEngines.indexOf(engine);
                $scope.settings.searchEngines.splice(index, 1)
            }
            $scope.update();
        };

        $scope.editSearchEngine = function (engine) {
            $scope.searchEngine = $.extend({}, engine);
            $scope.showSearchForm = true;
            $scope.editSearch = true;
        }
    }
    //TODO move to service and eliminate duplication
    function getLanguages() {
        var url = 'api/app/languages';
        $http.get(url).then(
            function (response) {
                $log.debug("[DEBUG] Languages loaded");
                $scope.languages = response.data;
            }).catch(function (response) {
            AlertService.addError("error.general");
            $log.debug(response);
        });
    }

    function getAppSettings() {
        $http.get('api/app/settings').then(
            function (result) {
                $scope.settings = result.data;
            }).catch(function (response) {
            AlertService.addError('error.general', response)
        });
    }

});