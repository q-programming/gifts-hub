app.controller('confirm', ['$rootScope', '$scope', '$http', '$log', '$translate', '$location', '$routeParams', 'AlertService',
    function ($rootScope, $scope, $http, $log, $translate, $location, $routeParams, AlertService) {
        if (!$rootScope.authenticated) {
            AlertService.addWarning("user.confirm.token.tryagain");
            $location.path("/");
        } else {
            if ($routeParams.token) {
                var url = 'api/user/confirm';
                $http.post(url, $routeParams.token).then(
                    function (response) {
                        $log.debug("[DEBUG] Operation confirmed");
                        AlertService.addSuccessMessage(response.data.message);
                        $location.path("/");
                    }).catch(function (response) {
                    AlertService.addError("user.confirm.token.error.missing ", response);
                    $log.debug(response);
                });
            } else {
                AlertService.addError('user.confirm.token.error.missing');
                $location.path("/");
            }
        }


    }]);