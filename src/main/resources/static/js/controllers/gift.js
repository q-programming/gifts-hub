app.controller('gift', function ($rootScope, $scope, $http, $log, $routeParams, $route, $translate,AlertService) {
    $scope.giftForm = {};
    $scope.giftsList = [];

    $scope.showAddNew = false;
    $scope.userList = false;
    $scope.listTitle = "";

    if ($rootScope.authenticated) {
        $scope.userList = !$routeParams.username || $routeParams.username === $rootScope.principal.username;
        getGiftList($routeParams.username);
    }

    $scope.show = function () {
        $scope.showAddNew = true;
    };

    $scope.getSearchGiftLink = function (gift) {
        var serachEngine = "http://www.google.com/search?q=";
        return serachEngine + gift.name;
    };
    $scope.getGiftDate = function (gift) {
        var date = new Date(gift.created);
        var dateString = ('0' + date.getDate()).slice(-2) + '-' + ('0' + (date.getMonth() + 1)).slice(-2) + '-' + date.getFullYear() + ' ' + date.getHours() + ':' + date.getMinutes();
        return dateString;
    };

    $scope.getGiftStatus = function (gift) {
        var newGift = '<span class="gift-status new-gift" data-toggle="tooltip" data-placement="top" title="New">' +
            '<i class="fa fa-fw fa-certificate" aria-hidden="true"></i>' +
            '<i class="fa fa-fw fa-2x fa-gift" aria-hidden="true"></i>' +
            '</span>';
        var claimedGift = '<span class="gift-status claimed-gift" data-toggle="tooltip" data-placement="top" title="New">' +
            '<i class="fa fa-fw fa-exclamation" aria-hidden="true"></i>' +
            '<i class="fa fa-fw fa-2x fa-gift" aria-hidden="true"></i>' +
            '</span>';
        var realisedGift = '<span class="gift-status realised-gift" data-toggle="tooltip" data-placement="top" title="New">' +
            '<i class="fa fa-fw fa-check" aria-hidden="true"></i>' +
            '<i class="fa fa-fw fa-2x fa-gift" aria-hidden="true"></i>' +
            '</span>';
        var other = '<span class="gift-status" data-toggle="tooltip" data-placement="top" title="New">' +
            '<i class="fa-spacer">&nbsp;</i>' +
            '<i class="fa fa-fw fa-2x fa-gift" aria-hidden="true"></i>' +
            '</span>';

        if (gift.status === 'NEW') {
            return newGift;
        } else if (gift.status === 'CLAIMED' && gift.userId !== $rootScope.principal.id) {
            return claimedGift;
        } else if (gift.status === 'REALISED') {
            return realisedGift;
        } else {
            return other;
        }
    };


    $scope.reset = function () {
        $scope.giftForm = {};
        $scope.showAddNew = false;
    };

    $scope.create = function () {
        $scope.showAddNew = true;
        $http.post('api/gift/create', $scope.giftForm).then(
            function (response) {
                $scope.giftsList.unshift(response.data);
                $scope.reset();
                AlertService.addSuccess("gift.new.added");
            }).catch(function (response) {
            AlertService.addError("error.general");
            $log.debug(response);
        });
    };


    function getGiftList(username) {
        var url;
        if (username) {
            $translate("gift.list").then(function (translation) {
                $scope.listTitle = translation + username;

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
                $scope.giftsList = [];
                $log.debug("[DEBUG] User gifts loaded");
                angular.forEach(response.data, function (value) {
                    $scope.giftsList.push(value);
                });
            }).catch(function (response) {
            AlertService.addError("error.general");
            $log.debug(response);
        });
    }
});