app.controller('help', ['$rootScope', '$scope', '$http', '$log', '$translate', '$location', '$anchorScroll', 'AlertService',
    function ($rootScope, $scope, $http, $log, $translate, $location, $anchorScroll, AlertService) {
        $translate("gift.status.claimed").then(function (translation) {
            if ($rootScope.principal) {
                $scope.gift_tooltip = translation + " " + $rootScope.principal.fullname;
            }
            else {
                $scope.gift_tooltip = translation;
            }
        });

        $scope.anchor = function (anchor) {
            if ($location.hash() !== anchor) {
                $location.hash(anchor);
            } else {
                $anchorScroll();
            }
        };

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
app.run(['$anchorScroll', function ($anchorScroll) {
    $anchorScroll.yOffset = 60;   // always scroll by 50 extra pixels
}]);