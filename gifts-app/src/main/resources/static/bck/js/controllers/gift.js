app.controller('gift', [
    '$rootScope', '$scope', '$http', '$log', '$routeParams', '$route', '$location', '$window', '$translate', '$uibModal', '$sce', 'AlertService', 'AvatarService', 'UtilsService', 'GIFT_STATUS',
    function ($rootScope, $scope, $http, $log, $routeParams, $route, $location, $window, $translate, $uibModal, $sce, AlertService, AvatarService, UtilsService, GIFT_STATUS) {
        $scope.utils = UtilsService;
        $scope.giftForm = {};
        $scope.giftsList = {};
        $scope.giftsListAll = {};
        $scope.searchEngines = [];
        $scope.editInProgress = false;
        $scope.userList = false;
        $scope.userGiftList = {};
        $scope.listTitle = "";

        $scope.importedFile = null;


        $scope.filterShow = false;
        $scope.categoriesFilter = [];
        $scope.filteredCategory = null;
        $scope.categoryOther = "";
        $scope.categoryRealised = "";

        $scope.searchWith = '';

        $translate("gift.category.other").then(function (translation) {
            $scope.categoryOther = translation;
        });
        $translate("gift.category.realised").then(function (translation) {
            $scope.categoryRealised = translation;
        });

        //INIT
        getGiftList();
        if ($rootScope.authenticated) {
            if (!$rootScope.principal) {
                $window.location.reload();//in case of loosing session, principal fetch is delayed. Reload location to prevent errors
            }
            $scope.userList = !$routeParams.username || $routeParams.username === $rootScope.principal.username;
            $translate("gift.search").then(function (translation) {
                $scope.searchWith = translation + " ";
            });
            getSearchEngines();
            getCategories();
            getFamily();
            getUsers();
        }
        //End init

        $scope.goToMemberList = function (user) {
            $location.path('/list/' + user.username);

        };
        /**
         * Filter all already read gift lists per category
         * @param filteredCategory passed category from dropdown select
         */
        $scope.filterCategory = function (filteredCategory) {
            $scope.giftsList = $.extend({}, $scope.giftsListAll);//reset any potential filters
            $scope.filteredCategory = null;
            if (filteredCategory) {
                $scope.filteredCategory = filteredCategory;
                $scope.giftsListAll = $.extend({}, $scope.giftsList);
                $scope.giftsList = {};
                angular.forEach($scope.giftsListAll, function (contents, category) {
                    if (filteredCategory.name === category) {
                        $scope.giftsList[category] = contents;
                    }
                });
            }
        };

        $scope.addNewLinkField = function () {
            var link = {};
            link.id = '';
            link.url = '';
            $scope.giftForm.links.push(link);
        };

        $scope.removeLinkField = function (index) {
            $scope.giftForm.links.splice(index, 1);
        };

        /**
         * Show gift form, pre-filled with search engines selected
         */
        $scope.showCreate = function () {
            $scope.editInProgress = false;
            $scope.giftForm.searchEngines = {};
            $scope.giftForm.links = [];
            angular.forEach($scope.searchEngines, function (engine) {
                $scope.giftForm.searchEngines[engine.id] = true;
            });
            $scope.giftForm.username = $routeParams.username;
            $scope.openModalForm();
        };
        /**
         * Show gift form, pre-filled with search engines selected
         */
        $scope.showEdit = function (gift) {
            $scope.editInProgress = true;
            $scope.giftForm = $.extend({}, gift);
            $scope.giftForm.searchEngines = {};
            angular.forEach(gift.engines, function (engine) {
                $scope.giftForm.searchEngines[engine.id] = true;
            });
            $scope.giftForm.username = $routeParams.username;
            $scope.openModalForm();
        };

        $scope.openModalForm = function () {
            $uibModal.open({
                templateUrl: 'modals/gift.html',
                scope: $scope,
                controller: ['$uibModalInstance', '$scope', function ($uibModalInstance, $scope) {
                    if ($scope.giftForm.links.length === 0) {
                        $scope.addNewLinkField();
                    }
                    $scope.cancel = function () {
                        $scope.reloadGiftsAndCategories();
                        $uibModalInstance.dismiss('cancel');

                    };
                    $scope.action = function () {
                        angular.forEach($scope.giftForm.links, function (link) {
                            link.invalid = !validUrl(link.url);
                        });
                        if ($scope.mainGiftForm.$valid) {
                            $log.debug("[DEBUG] sending gift data");
                            $scope.sendGiftData();
                            $uibModalInstance.close()
                        }
                    };
                }]
            });
        };
        $scope.showImport = function () {
            $uibModal.open({
                templateUrl: 'modals/giftImport.html',
                scope: $scope,
                controller: ['$uibModalInstance', '$scope', function ($uibModalInstance, $scope) {
                    $scope.fileName = "";
                    $scope.gotFile = null;
                    $scope.logmessage = "";
                    $scope.importFinished = null;
                    $scope.importInProgress = null;
                    $scope.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };
                    $scope.close = function () {
                        getGiftList();
                        getCategories();
                        $uibModalInstance.close();
                    };
                    $scope.action = function () {
                        $log.debug($scope.importedFile);
                        var fd = new FormData();
                        fd.append("file", $scope.importedFile);
                        if ($routeParams.username) {
                            fd.append("user", $routeParams.username);
                        }
                        var url = 'api/gift/import';
                        $scope.importInProgress = true;
                        $http.post(url, fd, {
                            transformRequest: angular.identity,
                            headers: {'Content-Type': undefined}
                        }).then(function (response) {
                            $scope.logmessage = $sce.trustAsHtml(response.data.message);
                            $scope.importInProgress = false;
                            $scope.importFinished = true
                        }).catch(function (response) {
                            $uibModalInstance.close();
                            AlertService.addError("error.general", response);
                            $log.debug(response);
                        });
                    };
                    $scope.handleFileSelect = function (evt) {
                        $scope.importedFile = evt.files[0];
                        $scope.fileName = $scope.importedFile.name;
                        $scope.gotFile = true;
                        $scope.$apply();
                    };
                }]
            });
        };


        /**
         * Reset gift form
         */
        $scope.resetForm = function () {
            $scope.giftForm = {};
            $scope.editInProgress = false;
        };
        /**
         * Reloads all gifts and categories. Additionally resets form
         */
        $scope.reloadGiftsAndCategories = function () {
            getGiftList();
            getCategories();
            $scope.resetForm();
        };

        /**
         * Send gift data from form. It can be either new or update of existing
         */
        $scope.sendGiftData = function () {
            //clone original data that will be sent
            $scope.apiSendGift = $.extend({}, $scope.giftForm);
            $scope.apiSendGift.searchEngines = [];
            angular.forEach($scope.giftForm.searchEngines, function (val, key) {
                if (val === true) {
                    $scope.apiSendGift.searchEngines.push(key)
                }
            });
            if ($scope.giftForm.category) {
                $scope.apiSendGift.category = $scope.giftForm.category.name;
            }
            //existing gift was chosen to be sent ( edit )
            var url;
            if ($scope.apiSendGift.id && $scope.editInProgress) {
                url = 'api/gift/edit';
            }
            else if (!$scope.editInProgress) {
                url = 'api/gift/create';
            }
            else {
                //Should not happen
                AlertService.addError("error.general");
                return
            }
            $http.post(url, angular.toJson($scope.apiSendGift)).then(
                function () {
                    $scope.reloadGiftsAndCategories();
                    if ($scope.editInProgress) {
                        AlertService.addSuccess("gift.edit.success");
                    } else {
                        AlertService.addSuccess("gift.new.added");
                    }
                }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        };
        /**
         * Check if gift can be edited by currently logged in user.
         * In order to be able to edit he user to be either owner or be family admin
         * @param gift gift to be checked
         * @returns {boolean|*|null}
         */
        $scope.canBeEdited = function (gift) {
            return gift.status !== GIFT_STATUS.REALISED && (gift.userId === $rootScope.principal.id || ($scope.family && $scope.family.familyAdmin));
        };

        /**
         * Refresh autofill result of category search
         * @param $select
         */
        $scope.refreshResults = function ($select) {
            var search = $select.search,
                list = angular.copy($select.items),
                FLAG = -1;
            //remove last user input
            list = list.filter(function (item) {
                return item.id !== FLAG;
            });

            if (!search) {
                //use the predefined list
                $select.items = list;
            }
            else {
                //manually add user input and set selection
                var userInputItem = {
                    id: FLAG,
                    name: search
                };
                $select.items = [userInputItem].concat(list);
                $select.selected = userInputItem;
            }
        };

        // CLAIM
        $scope.canBeClaimed = function (gift) {
            return (gift.status !== GIFT_STATUS.REALISED && !gift.claimed && gift.userId !== $rootScope.principal.id)
        };
        $scope.canBeUnClaimed = function (gift) {
            return (gift.status !== GIFT_STATUS.REALISED && (gift.claimed && gift.claimed.id === $rootScope.principal.id))
        };
        $scope.claimGift = function (gift) {
            var url = 'api/gift/claim/' + gift.id;
            $http.put(url).then(
                function (response) {
                    $log.debug("[DEBUG] Gift claimed");
                    AlertService.addSuccessMessage(response.data.message);
                    getGiftList();
                }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        };
        $scope.unClaimGift = function (gift) {
            var url = 'api/gift/unclaim/' + gift.id;
            $http.put(url).then(
                function (response) {
                    $log.debug("[DEBUG] Gift unclaimed");
                    AlertService.addSuccessMessage(response.data.message);
                    getGiftList();
                }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        };

        //Realised

        /**
         * Check if gift can be marked as not realised by currently logged in user.
         * In order to be able to do so ,user has to be either owner or be family admin
         * @param gift gift to be checked
         * @returns {boolean|*|null}
         */

        $scope.canBeUndoRealised = function (gift) {
            return gift.status === GIFT_STATUS.REALISED && (gift.userId === $rootScope.principal.id || ($scope.family && $scope.family.familyAdmin));
        };

        $scope.realiseGift = function (gift) {
            var url = 'api/gift/complete/' + gift.id;
            $http.put(url).then(
                function (response) {
                    $log.debug("[DEBUG] Gift realised");
                    AlertService.addSuccessMessage(response.data.message);
                    getGiftList();
                }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        };
        $scope.undoRealiseGift = function (gift) {
            var url = 'api/gift/undo-complete/' + gift.id;
            $http.put(url).then(
                function (response) {
                    $log.debug("[DEBUG] Gift undo realised");
                    AlertService.addSuccessMessage(response.data.message);
                    getGiftList();
                }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        };

        // Delete
        $scope.deleteGift = function (gift) {
            var url = 'api/gift/delete/' + gift.id;
            $http.delete(url).then(
                function (response) {
                    $log.debug("[DEBUG] Gift deleted");
                    AlertService.addSuccessMessage(response.data.message);
                    getGiftList();
                }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        };
        $scope.checkCategory = function (item, form) {
            var url = 'api/gift/allowed-category';
            form.categoryError = null;
            if (item) {
                $http.get(url, {params: {category: item.name}}).then(
                    function (response) {
                        if (response.data && response.data.code === 'ERROR') {
                            form.categoryError = $sce.trustAsHtml(response.data.message);
                            form.category = null;
                        }
                    }).catch(function (response) {
                    AlertService.addError("error.general", response);
                    $log.debug(response);
                });
            }
        };


        // HELPER FUNCTIONS

        $scope.getGiftClass = function (gift) {
            if (gift.status === GIFT_STATUS.REALISED) {
                return 'gift-realised';
            } else if (gift.claimed && gift.userId !== $rootScope.principal.id) {
                return 'gift-claimed';
            }
            return '';
        };

        function getGiftList() {
            var username;
            var url;
            if ($routeParams.username || $routeParams.userid) {
                if ($routeParams.username) {
                    username = $routeParams.username;
                    getUsernDataByUsernameOrId(username);
                    url = 'api/gift/user/' + username;
                } else if ($routeParams.userid) {
                    username = $routeParams.userid;
                    getUsernDataByUsernameOrId(username);
                    url = 'api/gift/user/' + $routeParams.userid;
                }
                $translate("gift.list").then(function (translation) {
                    $scope.listTitle = translation + " " + username;
                });
            } else {
                $translate("gift.list.mine").then(function (translation) {
                    $scope.listTitle = translation;
                });
                url = 'api/gift/mine';
                $scope.userGiftList = $rootScope.principal;
            }
            $http.get(url).then(
                function (response) {
                    $scope.isPublicList = true;
                    $scope.giftsList = {};
                    $log.debug("[DEBUG] User gifts loaded");
                    angular.forEach(response.data, function (value, key) {
                        if (key === '') {
                            key = $scope.categoryOther;
                        } else if (key === 'REALISED') {
                            key = $scope.categoryRealised;
                        }
                        $scope.giftsList[key] = value;
                    });
                    $scope.giftsListAll = $.extend({}, $scope.giftsList);//clone list for listing purpose
                    $scope.filterCategory($scope.filteredCategory);
                }).catch(function (response) {
                if (response.status === 404) {
                    $location.url('/404');
                } else if (response.status === 400) {
                    $scope.isPublicList = false;
                }
                $log.debug(response);
            });
        }

        function getSearchEngines() {
            var url = 'api/app/search-engines';
            $http.get(url).then(
                function (response) {
                    $log.debug("[DEBUG] Search engines loaded");
                    $scope.searchEngines = response.data;
                }).catch(function (response) {
                AlertService.addError("error.general");
                $log.debug(response);
            });
        }

        function getUsernDataByUsernameOrId(usernameOrId) {
            var url = 'api/user?identification=' + usernameOrId;
            $http.get(url).then(
                function (response) {
                    $log.debug("[DEBUG] User data loaded");
                    $scope.userGiftList = response.data;
                    AvatarService.getUserAvatar($scope.userGiftList);
                }).catch(function (response) {
                AlertService.addError("error.general");
                $log.debug(response);
            });
        }

        function getCategories() {
            var url = 'api/gift/categories';
            $http.get(url).then(
                function (response) {
                    $scope.categories = [];
                    $log.debug("[DEBUG] Categories loaded");
                    angular.forEach(response.data, function (cat) {
                        $scope.categories.push({id: cat.id, name: cat.name})
                    });
                    angular.copy($scope.categories, $scope.categoriesFilter);
                    $scope.categoriesFilter.push({id: Math.pow(2, 53) - 2, name: $scope.categoryOther});
                    $scope.categoriesFilter.push({id: Math.pow(2, 53) - 1, name: $scope.categoryRealised});
                }).catch(function (response) {
                AlertService.addError("error.general");
                $log.debug(response);
            });
        }

        //TODO move to family service?
        function isFamilyAdmin() {
            angular.forEach($scope.family.admins, function (user) {
                AvatarService.getUserAvatar(user);
                if (user.id === $rootScope.principal.id) {
                    $scope.family.familyAdmin = true;
                }
            });
        }

        function getUsersAvatars(users) {
            angular.forEach(users, function (user) {
                AvatarService.getUserAvatar(user);
            });
        }


        function getUsers() {
            var url;
            if ($routeParams.username) {
                url = 'api/user/userList?username=' + $routeParams.username;
            }
            else {
                url = 'api/user/userList';
            }
            $http.get(url).then(
                function (response) {
                    if (response.data) {
                        $scope.users = response.data;
                        getUsersAvatars($scope.users);
                    }
                }
            );
        }

        function getFamily() {
            var url;
            if ($routeParams.username) {
                url = 'api/user/family?username=' + $routeParams.username;
            }
            else {
                url = 'api/user/family';
            }
            $http.get(url).then(
                function (response) {
                    if (response.data) {
                        $scope.family = response.data;
                        isFamilyAdmin();
                        getUsersAvatars($scope.family.members);
                    }
                }
            );
        }

        function validUrl(url) {
            var pattern = /^(http[s]?:\/\/){0,1}(www\.){0,1}[a-zA-Z0-9\.\-]+\.[a-zA-Z]{2,5}[\.]{0,1}/;
            return pattern.test(url);
        }
    }]);