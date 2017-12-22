app.controller('manage', ['$rootScope', '$scope', '$http', '$log', '$uibModal', 'AlertService', 'AppService', 'UtilsService',
    function ($rootScope, $scope, $http, $log, $uibModal, AlertService, AppService, UtilsService) {
        $scope.settings = {};
        $scope.searchEngine = {};
        $scope.searchEngineList = [];
        $scope.showSearchForm = null;
        $scope.editSearch = false;
        $scope.languages = {};
        if ($rootScope.authenticated) {
            if ($rootScope.principal.role === 'ROLE_ADMIN') {
                AppService.getLanguageList().then(function (response) {
                    $scope.languages = response.data;
                }).catch(function (response) {
                    AlertService.addError("error.general");
                    $log.debug(response);
                });
                getAppSettings();
                $scope.update = function () {
                    $scope.editSearch = false;
                    $http.post('api/app/settings', $scope.settings).then(
                        function () {
                            AlertService.addSuccess('app.manage.saved');
                            $scope.reset();
                        }).catch(function (response) {
                        AlertService.addError('error.general', response)
                    });
                };
                $scope.updateEmail = function () {
                    $http.post('api/app/settings/email', $scope.settings.email).then(
                        function (response) {
                            if (response.data.code === 'WARNING') {
                                AlertService.addError('app.manage.email.error', response);
                            } else {
                                AlertService.addSuccess('app.manage.saved');
                            }
                        }).catch(function (response) {
                        AlertService.addError('error.general', response)
                    });
                };
                $scope.saveAppUrl = function () {
                    $scope.settings.appUrl = UtilsService.getAppUrl();
                    $scope.update();
                };
                $scope.openSearchModalForm = function () {
                    $uibModal.open({
                        templateUrl: 'modals/search.html',
                        scope: $scope,
                        controller: ['$uibModalInstance', '$scope', function ($uibModalInstance, $scope) {
                            $scope.cancel = function () {
                                $scope.reset();
                                $uibModalInstance.dismiss('cancel');
                            };
                            $scope.action = function () {
                                if ($scope.searchForm.$valid) {
                                    $log.debug("[DEBUG] sending search engine data");
                                    $scope.updateSearchEngine();
                                    $scope.reset();
                                    $uibModalInstance.close()
                                }
                            };
                        }]
                    });
                };
                $scope.openCategoryModalForm = function () {
                    $uibModal.open({
                        templateUrl: 'modals/category.html',
                        scope: $scope,
                        controller: ['$uibModalInstance', '$scope', function ($uibModalInstance, $scope) {
                            $scope.cancel = function () {
                                $scope.reset();
                                $uibModalInstance.dismiss('cancel');
                            };
                            $scope.action = function () {
                                if ($scope.categoryForm.$valid) {
                                    $log.debug("[DEBUG] sending category data");
                                    $http.post('api/app/update-category', $scope.categoryEdit).then(
                                        function () {
                                            AlertService.addSuccess('app.manage.categories.updated');
                                            getAppSettings();
                                        }).catch(function (response) {
                                        AlertService.addError('error.general', response)
                                    });
                                    $uibModalInstance.close()
                                }
                            };
                        }]
                    });
                };

                $scope.reset = function () {
                    $scope.searchEngine = {};
                    $scope.editSearch = false;
                    $scope.priorityChanged = false;
                };

                $scope.updateSearchEngine = function () {
                    if (!$scope.editSearch) {
                        $scope.settings.searchEngines.push($scope.searchEngine);
                        $scope.searchEngine = {};
                    } else {
                        var indexes = $.map($scope.settings.searchEngines, function (engine, index) {
                            if (engine.id === $scope.searchEngine.id) {
                                return index;
                            }
                        });
                        $scope.settings.searchEngines[indexes[0]] = $scope.searchEngine;
                    }
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
                    $scope.editSearch = true;
                    $scope.openSearchModalForm()
                };

                $scope.moveUp = function (index) {
                    $log.debug("[DEBUG] Increasing category priority " + index);
                    if (index > 0) {
                        var tmp = $scope.settings.categories[index - 1];
                        $scope.settings.categories[index - 1] = $scope.settings.categories[index];
                        $scope.settings.categories[index] = tmp;
                        $scope.priorityChanged = true;
                    }
                };
                $scope.moveDown = function (index) {
                    $log.debug("[DEBUG] Decreasing category priority " + index);
                    if (index < $scope.settings.categories.length) {
                        var tmp = $scope.settings.categories[index + 1];
                        $scope.settings.categories[index + 1] = $scope.settings.categories[index];
                        $scope.settings.categories[index] = tmp;
                        $scope.priorityChanged = true;
                    }
                };

                $scope.deleteCategory = function (category, index) {
                    $log.debug("[DEBUG] Remove category " + category);
                    $http.post('api/app/remove-category', category).then(
                        function () {
                            $scope.settings.categories.splice(index, 1);
                            AlertService.addSuccess('app.manage.categories.removed');
                        }).catch(function (response) {
                        AlertService.addError('error.general', response)
                    });
                }

                $scope.editCategory = function (category, index) {
                    $log.debug("[DEBUG] Edit category " + category);
                    $scope.categoryEdit = $.extend({}, category);
                    $scope.openCategoryModalForm()

                }

            }
        } else {
            $location.path("/login");
        }

        function getAppSettings() {
            $http.get('api/app/settings').then(
                function (result) {
                    $scope.settings = result.data;
                }).catch(function (response) {
                AlertService.addError('error.general', response)
            });
        }

    }]);