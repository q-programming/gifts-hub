app.controller('tour', ['$rootScope', '$scope', '$location', '$http',
    function ($rootScope, $scope, $location) {
        if (true) {//TODO based on principal
            $scope.currentStep = 0;
        }
        /**
         * @return {string}
         */
        $scope.goToPage = function (page) {
            $location.path(page);
        }
        $scope.test = function () {
            alert($scope.currentStep);
        }
    }]);
