var AvatarService = angular.module('AvatarService', []);
AvatarService.factory('AvatarService', function ($http, $log, avatarCache, $rootScope) {
    var AvatarService = {};
    var avatars = {};

    AvatarService.getAvatar = function (id) {
        var image = avatarCache.get(id);
        if (!image) {
            $log.debug("[DEBUG] Getting avatar from DB");
            $http.get('api/user/' + id + '/avatar').then(function (result) {
                image = result.data.image;
                avatarCache.put(id, image);
                $rootScope.principal.avatar = image;
            }).catch(function (response) {
                $log.error("Failed to get avatar");
                $log.error(response);
            });
        } else {
            $log.debug("[DEBUG] Fetching avatar from cache");
            $rootScope.principal.avatar = image;
        }
    };
    return AvatarService;
});

