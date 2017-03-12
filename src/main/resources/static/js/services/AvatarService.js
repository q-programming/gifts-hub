var AvatarService = angular.module('AvatarService', []);
AvatarService.factory('AvatarService', function ($http, $log, localStorageService) {
    var AvatarService = {};

    AvatarService.getUserAvatar = function (user) {
        var image;
        if (localStorageService.isSupported) {
            image = localStorageService.get("avatar:" + user.id);
        }
        // var image = avatarCache.get(user.id);
        if (!image) {
            $log.debug("[DEBUG] Getting avatar from DB");
            $http.get('api/user/' + user.id + '/avatar').then(function (result) {
                var datatype = "data:" + result.data.type + ";base64,";
                image = datatype + result.data.image;
                localStorageService.set("avatar:" + user.id, image);
                user.avatar = image;
            }).catch(function (response) {
                $log.error("Failed to get avatar");
                $log.error(response);
            });
        } else {
            $log.debug("[DEBUG] Fetching avatar from localStorage");
            user.avatar = image;
        }
    };

    AvatarService.clearCache = function () {
        localStorageService.clearAll("avatar:");
    };
    return AvatarService;
});

