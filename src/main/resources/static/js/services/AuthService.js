var AuthService = angular.module('AuthService', []);
AuthService.factory('AuthService', function ($http, $log, avatarCache, $rootScope, AvatarService) {
    var AuthService = {};

    AuthService.authenticate = function (credentials, callback) {
        var headers = credentials ? {
            authorization: "Basic "
            + btoa(credentials.username + ":" + credentials.password)
        } : {};

        $http.get('api/user/', {headers: headers}).then(
            function (response) {
                var data = response.data;
                if (data.id) {
                    $rootScope.authenticated = true;
                    $rootScope.principal = data;
                    AvatarService.getAvatar($rootScope.principal.id);
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
});

