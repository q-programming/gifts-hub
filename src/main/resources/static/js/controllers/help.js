app.controller('help', ['$rootScope', '$scope', '$http', '$log', '$translate', 'AlertService',
    function ($rootScope, $scope, $http, $log, $translate, AlertService) {
        $translate("gift.status.claimed").then(function (translation) {
            $scope.gift_tooltip = translation + " " + $rootScope.principal.fullname;
        });
        $scope.tourReset = function () {
            var url = 'api/user/tour-reset';
            $http.post(url).then(
                function () {
                    $log.debug("[DEBUG] Tour marked as incomplete");
                    $rootScope.principal.tourComplete = false;
                }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        };
    }]);