app.controller('gift', ['$rootScope', '$scope', '$http', '$log', '$routeParams', '$route', '$location', '$window', '$translate', 'AlertService', 'AvatarService', 'GIFT_STATUS', function ($rootScope, $scope, $http, $log, $routeParams, $route, $location, $window, $translate, AlertService, AvatarService, GIFT_STATUS) {
    $scope.giftForm = {};
    $scope.giftsList = [];
    $scope.searchEngines = [];

    $scope.showGiftForm = false;
    $scope.editInProgress = false;
    $scope.userList = false;
    $scope.userGiftList = {};
    $scope.listTitle = "";
    $scope.categoryOther = "";

    $scope.searchWith = '';

    $translate("gift.category.other").then(function (translation) {
        $scope.categoryOther = translation;
    });
    //INIT
    getGiftList();
    if ($rootScope.authenticated) {
        $translate("gift.search").then(function (translation) {
            $scope.searchWith = translation;
        });
        getSearchEngines();
        getCategories();
        getFamily();
        if (!$rootScope.principal) {
            $window.location.reload();//in case of loosing session, principal fetch is delayed. Reload location to prevent errors
        }
        $scope.userList = !$routeParams.username || $routeParams.username === $rootScope.principal.username;
    }
    $scope.goToMemberList = function (user) {
        $location.path('/list/' + user.username);

    };


    /**
     * Show gift form, pre-filled with search engines selected
     */
    $scope.showCreate = function () {
        $scope.showGiftForm = true;
        $scope.editInProgress = false;
        $scope.giftForm.searchEngines = {};
        angular.forEach($scope.searchEngines, function (engine) {
            $scope.giftForm.searchEngines[engine.id] = true;
        });
        $scope.giftForm.username = $routeParams.username;
    };
    /**
     * Show gift form, pre-filled with search engines selected
     */
    $scope.showEdit = function (gift) {
        $scope.showGiftForm = true;
        $scope.editInProgress = true;
        $scope.giftForm = $.extend({}, gift);
        $scope.giftForm.searchEngines = {};
        angular.forEach(gift.engines, function (engine) {
            $scope.giftForm.searchEngines[engine.id] = true;
        });
        $scope.giftForm.username = $routeParams.username;
    };

    /**
     * Reset gift form
     */
    $scope.reset = function () {
        $scope.giftForm = {};
        $scope.showGiftForm = false;
        $scope.editInProgress = false;
    };

    /**
     * Send gift data from form. It can be either new or update of existing
     */
    $scope.sendGiftData = function () {
        $scope.showGiftForm = true;
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
                getGiftList();
                getCategories();
                if ($scope.editInProgress) {
                    AlertService.addSuccess("gift.edit.success");
                } else {
                    AlertService.addSuccess("gift.new.added");
                }
                $scope.reset();
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
        return gift.status !== GIFT_STATUS.REALISED && (gift.userId === $rootScope.principal.id || ($scope.family && $scope.family.familyAdmin ));
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
        var url = 'api/gift/claim?gift=' + gift.id;
        $http.get(url).then(
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
        var url = 'api/gift/unclaim?gift=' + gift.id;
        $http.get(url).then(
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
        var url = 'api/gift/complete?gift=' + gift.id;
        $http.get(url).then(
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
        var url = 'api/gift/undo-complete?gift=' + gift.id;
        $http.get(url).then(
            function (response) {
                $log.debug("[DEBUG] Gift undo realised");
                AlertService.addSuccessMessage(response.data.message);
                getGiftList();
            }).catch(function (response) {
            AlertService.addError("error.general", response);
            $log.debug(response);
        });
    };


    // HELPER FUNCTIONS
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
                    }
                    $scope.giftsList[key] = value;
                });
            }).catch(function (response) {
            if (response.status === 404) {
                // AlertService.addError('error.page.notfound');
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

    function getFamilyAvatars() {
        angular.forEach($scope.family.members, function (user) {
            AvatarService.getUserAvatar(user);
        });
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
                    getFamilyAvatars();
                }
            }
        );
    }
}]);