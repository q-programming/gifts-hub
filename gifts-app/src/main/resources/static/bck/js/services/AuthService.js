var AuthService = angular.module('AuthService', []);
AuthService.factory('AuthService', ['$http', '$log', 'avatarCache', '$rootScope', '$translate', '$cookies', '$location','$window', 'AvatarService', 'AlertService',
    function ($http, $log, avatarCache, $rootScope, $translate, $cookies, $location, $window,AvatarService, AlertService) {
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
                        if ($rootScope.principal && $rootScope.principal.role === 'ROLE_ADMIN') {
                            $http.get("api/app/setup").then(
                                function (response) {
                                    if (response.data) {
                                        AlertService.addWarning('app.manage.setup');
                                        $location.path("/manage");
                                    } else {
                                        // $location.path("#/");
                                        // $location.path("/");
                                    }
                                }).catch(function (response) {
                                AlertService.addError("error.general", response);
                                $log.debug(response);
                            });
                        } else {
                            // $location.path("#/");
                            // $location.path("/");
                        }
                    } else {
                        $rootScope.authenticated = false;
                    }
                }
            );
        };
        AuthService.logout = function () {
            $http.post('logout', {}).then(
                function successCallback() {
                    AvatarService.clearCache();
                    //Nuke cookies to be sure
                    var context = $window.location.pathname.substring(0, window.location.pathname.indexOf("/",2));
                    $cookies.remove("AUTH-TOKEN",context);
                    $cookies.remove("JSESSIONID");
                    $cookies.remove("XSRF-TOKEN");
                    $rootScope.authenticated = false;
                    $location.path("/login");
                    $rootScope.principal = undefined;
                },
                function errorCallback() {
                    $rootScope.authenticated = false;
                });
        };
        return AuthService;
    }]);
