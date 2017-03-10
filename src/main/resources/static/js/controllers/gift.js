app.controller('gift', function ($rootScope, $scope, $http, $log) {
    $scope.giftForm = {};
    $scope.giftsList = [];
    $scope.error = null;
    $scope.success_added = null;
    $scope.addNew = false;
    if ($rootScope.authenticated) {
        getMyGifts();
    }

    $scope.reset = function () {
        $scope.giftForm = {};
        $scope.addNew = false;
        $log.debug($scope.addNew);
    };

    $scope.create = function () {
        $http.post('api/gift/create', $scope.giftForm).then(
            function (response) {
                $scope.success_added = true;
                $scope.giftsList.push(response.data);
                $scope.reset();
                $scope.$digest();
            }).catch(function (response) {
            $scope.success_added = null;
            $scope.error = 'ERROR';
        });
    };

    function getMyGifts() {
        $http.get('api/gift/mine', $scope.giftForm).then(
            function (response) {
                $scope.giftsList = [];
                $log.debug("[DEBUG] User gifts loaded");
                angular.forEach(response.data, function (value, key) {
                    $scope.giftsList.push(value);
                });
            }).catch(function (response) {
            $scope.error = 'ERROR';
        });
    }
});