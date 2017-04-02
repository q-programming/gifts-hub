app.controller('gift', function ($rootScope, $scope, $http, $log, $routeParams, $route, $translate, AlertService, GIFT_STATUS) {
    $scope.giftForm = {};
    $scope.giftsList = [];
    $scope.searchEngines = [];

    $scope.showGiftForm = false;
    $scope.editInProgress = false;
    $scope.userList = false;
    $scope.listTitle = "";
    $scope.categoryOther = "";

    $scope.searchWith = '';

    $translate("gift.category.other").then(function (translation) {
        $scope.categoryOther = translation;
    });
    //INIT
    if ($rootScope.authenticated) {
        $scope.userList = !$routeParams.username || $routeParams.username === $rootScope.principal.username;
        $translate("gift.search").then(function (translation) {
            $scope.searchWith = translation;
        });
        getGiftList($routeParams.username);
        getSearchEngines();
        getCategories();
    }

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

    $scope.canBeEdited = function (gift) {
        return gift.status !== GIFT_STATUS.REALISED && gift.userId === $rootScope.principal.id
    };

    /**
     * Refresh autofill result of category searc
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
                AlertService.addSuccessMessage(response.data.body.message);
                getGiftList($routeParams.username);
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
                AlertService.addSuccessMessage(response.data.body.message);
                getGiftList($routeParams.username);
            }).catch(function (response) {
            AlertService.addError("error.general", response);
            $log.debug(response);
        });
    };

    // HELPER FUNCTIONS

    function getGiftList(username) {
        var url;
        if (username) {
            $translate("gift.list").then(function (translation) {
                $scope.listTitle = translation + " " + username;

            });
            url = 'api/gift/user/' + username;
        } else {
            $translate("gift.list.mine").then(function (translation) {
                $scope.listTitle = translation;

            });
            url = 'api/gift/mine';
        }
        $http.get(url).then(
            function (response) {
                $scope.giftsList = {};
                $log.debug("[DEBUG] User gifts loaded");
                angular.forEach(response.data, function (value, key) {
                    if (key === '') {
                        key = $scope.categoryOther;
                    }
                    $scope.giftsList[key] = value;
                });
            }).catch(function (response) {
            AlertService.addError("error.general");
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
});