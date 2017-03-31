app.controller('gift', function ($rootScope, $scope, $http, $log, $routeParams, $route, $translate, AlertService, GIFT_STATUS) {
    $scope.giftForm = {};
    $scope.giftsList = [];
    $scope.searchEngines = [];

    $scope.showAddNew = false;
    $scope.userList = false;
    $scope.listTitle = "";
    $scope.categoryOther = "";

    $scope.searchWith = '';

    $translate("gift.category.other").then(function (translation) {
        $scope.categoryOther = translation;
    });
    //init
    if ($rootScope.authenticated) {
        $scope.userList = !$routeParams.username || $routeParams.username === $rootScope.principal.username;
        $translate("gift.search").then(function (translation) {
            $scope.searchWith = translation;
        });
        getGiftList($routeParams.username);
        getSearchEngines();
        getCategories();
    }

    $scope.show = function () {
        $scope.showAddNew = true;
        $scope.giftForm.searchEngines = {};
        angular.forEach($scope.searchEngines, function (engine) {
            $scope.giftForm.searchEngines[engine.id] = true;
        });
    };

    $scope.getGiftDate = function (gift) {
        var date = new Date(gift.created);
        var dateString = ('0' + date.getDate()).slice(-2) + '-' + ('0' + (date.getMonth() + 1)).slice(-2) + '-' + date.getFullYear();
        return dateString;
    };
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
    $scope.reset = function () {
        $scope.giftForm = {};
        $scope.showAddNew = false;
    };

    $scope.create = function () {
        $scope.showAddNew = true;
        $scope.newCreateGift = $.extend({}, $scope.giftForm);
        $scope.newCreateGift.searchEngines = [];
        angular.forEach($scope.giftForm.searchEngines, function (val, key) {
            if (val === true) {
                $scope.newCreateGift.searchEngines.push(key)
            }
        });
        if ($scope.giftForm.category) {
            $scope.newCreateGift.category = $scope.giftForm.category.name;
        }
        $http.post('api/gift/create', angular.toJson($scope.newCreateGift)).then(
            function () {
                $scope.reset();
                getGiftList();
                getCategories();
                AlertService.addSuccess("gift.new.added");
            }).catch(function (response) {
            AlertService.addError("error.general", response);
            $log.debug(response);
        });
    };

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