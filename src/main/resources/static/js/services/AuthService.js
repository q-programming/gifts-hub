var AuthService = angular.module('AuthService', []);
AuthService.factory('AuthService', ['$http', '$log', 'avatarCache', '$rootScope', '$translate', '$cookies', '$location', 'AvatarService',
    function ($http, $log, avatarCache, $rootScope, $translate, $cookies, $location, AvatarService) {
        var AuthService = {};

        AuthService.isAuthenticated = function () {
            if (!!$cookies.get('c_user')) {
                AuthService.getUser();
                return true;
            }
        };

        AuthService.getUser = function (callback) {
            $http.get('api/user').then(
                function (response) {
                    var data = response.data;
                    if (data.id) {
                        $rootScope.principal = data;
                        $translate.use($rootScope.principal.language);
                        $location.search('lang', $rootScope.principal.language);
                        AvatarService.getUserAvatar($rootScope.principal);
                    } else {
                        $rootScope.authenticated = false;
                    }
                }
            );
        };
        /**
         * @Depreciated
         *
         * @param credentials
         * @param callback
         */
        AuthService.authenticate = function (credentials, callback) {
            var headers = credentials ? {
                authorization: "Basic "
                + btoa(credentials.username + ":" + credentials.password)
            } : {};

            $http.get('api/user', {headers: headers}, {timeout: 5000}).then(
                function (response) {
                    var data = response.data;
                    if (data.id) {
                        $rootScope.authenticated = true;
                        $rootScope.principal = data;
                        $translate.use($rootScope.principal.language);
                        $location.search('lang', $rootScope.principal.language);
                        AvatarService.getUserAvatar($rootScope.principal);
                        $log.debug("[DEBUG] User logged in " + $rootScope.principal.id);
                    } else {
                        $rootScope.authenticated = false;
                    }
                    callback && callback();
                },
                function () {
                    $rootScope.authenticated = false;
                    callback && callback();
                });
        };
        return AuthService;
    }]);

