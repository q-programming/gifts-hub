var AvatarService = angular.module('AvatarService', []);
AvatarService.factory('AvatarService', ['$http', '$log', '$rootScope', 'localStorageService',
    function ($http, $log, $rootScope, localStorageService) {
        var AvatarService = {};

        AvatarService.getUserAvatar = function (user) {
            var image;
            if (localStorageService.isSupported) {
                image = localStorageService.get("avatar:" + user.id);
            }
            if (!image) {
                $log.debug("[DEBUG] Getting avatar from DB for user " + user.id);
                $http.get('api/user/' + user.id + '/avatar').then(function (result) {
                    if (result.data) {
                        var datatype = "data:" + result.data.type + ";base64,";
                        image = datatype + result.data.image;
                        localStorageService.set("avatar:" + user.id, image);
                        user.avatar = image;
                    } else {
                        localStorageService.set("avatar:" + user.id, "NaN");
                    }
                }).catch(function (response) {
                    $log.error("Failed to get avatar");
                    $log.error(response);
                });
            } else {
                $log.debug("[DEBUG] Fetching avatar from localStorage for user " + user.id);
                user.avatar = image;
            }
        };

        AvatarService.uploadAvatar = function (avatarSource) {
            $http.put('api/user/avatar-upload', avatarSource).then(function () {
                localStorageService.clearAll("avatar:" + $rootScope.principal.id);
                AvatarService.getUserAvatar($rootScope.principal);
            }).catch(function (response) {
                $log.error("Failed to upload avatar");
                $log.error(response);
            })
        };

        AvatarService.clearCache = function () {
            localStorageService.clearAll("avatar:");
        };
        return AvatarService;
    }]);

