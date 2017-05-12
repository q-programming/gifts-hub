app.controller('tour', ['$rootScope', '$scope', '$location', '$log',
    function ($rootScope, $scope, $location, $log, tourConfig) {
        if (true) {//TODO based on principal
            $scope.currentStep = 10;
        }
        /**
         * @return {string}
         */
        $scope.goToPage = function (page) {
            $location.path(page);
        }
        $scope.onShow = function () {
            $log.debug("show");
        }
        $scope.onProceed = function () {
            $log.debug("proceed");
        }
    }]);
