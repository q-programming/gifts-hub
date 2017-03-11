app.controller('gift', function ($rootScope, $scope, $http, $log, $routeParams, $route, AlertService) {
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
    $scope.showAlert = function () {
        AlertService.addSuccess("test!");
    };

    $scope.reset = function () {
        $scope.giftForm = {};
        $scope.showAddNew = false;
    };

    $scope.create = function () {
        $scope.showAddNew = true;
        $http.post('api/gift/create', $scope.giftForm).then(
            function (response) {
                AlertService.addSuccess("New gift added to Your wish list");
                $scope.giftsList.push(response.data);
                $scope.reset();
            }).catch(function (response) {
            AlertService.addError("Something went wrong");
        });
    };

    function getGiftList(username) {
        var url;
        if (username) {
            $scope.listTitle = username + "'s gift list";
            url = 'api/gift/user/' + username;
        } else {
            $scope.listTitle = "My wish list";
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
            AlertService.addError("Something went wrong");
        });
    }
});