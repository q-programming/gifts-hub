app.controller('changelog', ['$rootScope', '$scope', '$uibModal', '$http', 'AlertService',
    function ($rootScope, $scope, $uibModal, $http, AlertService) {
        if ($rootScope.principal && !$rootScope.principal.seenChangelog) {
            $uibModal.open({
                templateUrl: 'changelog/' + $rootScope.principal.language + '.html',
                scope: $scope,
                controller: ['$uibModalInstance', '$scope', function ($uibModalInstance, $scope) {
                    $scope.markRead = function () {
                        $scope.seenChangelog();
                        $uibModalInstance.dismiss('cancel');
                    };
                }]
            });
        }
        $scope.seenChangelog = function () {
            var url = 'api/user/changelog';
            $http.post(url).then(
                function (response) {
                    $log.debug("[DEBUG] Changelog marked as seen");
                    $rootScope.principal.seenChangelog = true;
                }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        };
    }]);