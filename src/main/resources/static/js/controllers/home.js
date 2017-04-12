app.controller('home', ['$rootScope', '$scope', '$http', '$location',
    function ($rootScope, $scope, $http, $location) {
        if ($rootScope.authenticated) {
            $location.path("/list");
        } else {
            $location.path("/login");
        }

        $scope.getLocale = function () {
            $http.get('api/messages', {
                params: {
                    lang: $rootScope.principal.language
                }
            }).then(
                function successCallback() {
                    console.log("Got it")
                },
                function errorCallback() {
                    console.log("error")
                });
        }
    }]);