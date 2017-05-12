app.controller('tour', ['$rootScope', '$scope', '$location', '$log', '$translate', '$http', 'tourConfig',
    function ($rootScope, $scope, $location, $log, $translate, $http, tourConfig) {
        if ($rootScope.principal && !$rootScope.principal.tourComplete) {
            $scope.currentStep =0;
            $translate("main.next").then(function (translation) {
                tourConfig.nextLabel = translation;
            });
        }
        /**
         * @return {string}
         */
        $scope.goToPage = function (page) {
            $location.path(page);
        };

        $scope.tourCompleted = function () {
            var url = 'api/user/tour-complete';
            $http.post(url).then(
                function (response) {
                    $log.debug("[DEBUG] Tour marked as completed");
                    $rootScope.principal.tourComplete = true;
                    $location.path("/");
                }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        };
    }]);
