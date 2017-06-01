app.controller('confirm', ['$rootScope', '$scope', '$http', '$log', '$translate', '$location', '$routeParams', 'AlertService',
    function ($rootScope, $scope, $http, $log, $translate, $location, $routeParams, AlertService) {
        if ($routeParams.uuid) {
            var url = 'api/user/confirm';
            var fd = new FormData();
            fd.append("token", $routeParams.uuid);
            $http.post(url, fd).then(
                function () {
                    $log.debug("[DEBUG] Operation confirmed");
                    //TODO show success
                }).catch(function (response) {
                AlertService.addError("user.confirm.uuid.error.missing ", response);
                $log.debug(response);
            });
        } else {
            AlertService.addError('user.confirm.uuid.error.missing');
            $location.path("/");
        }


    }]);