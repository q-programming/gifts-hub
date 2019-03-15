app.controller('help', ['$rootScope', '$scope', '$http', '$log', '$translate', '$location', '$anchorScroll', 'AlertService', 'AvatarService',
    function ($rootScope, $scope, $http, $log, $translate, $location, $anchorScroll, AlertService, AvatarService) {
        $translate("gift.status.claimed").then(function (translation) {
            if ($rootScope.principal) {
                $scope.admins = [];
                $scope.gift_tooltip = translation + " " + $rootScope.principal.fullname;
                getAdmins();
            }
            else {
                $scope.gift_tooltip = translation + " Jakub Romaniszyn";
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

        /**
         * Get all admins , sorted by names
         * Read theirs avatar in the process
         */
        function getAdmins() {
            $http.get('api/user/admins').then(
                function (response) {
                    $scope.admins.length = 0;
                    $log.debug("[DEBUG] Loaded administrators");
                    angular.forEach(response.data, function (user) {
                        AvatarService.getUserAvatar(user);
                        $scope.admins.push(user);
                    });
                }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        }
    }]);
app.run(['$anchorScroll', function ($anchorScroll) {
    $anchorScroll.yOffset = 60;   // always scroll by 50 extra pixels
}]);