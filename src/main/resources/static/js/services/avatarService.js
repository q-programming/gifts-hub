var avatarService = angular.module('avatarService', []);
avatarService.factory('avatarService', function ($http, $log, avatarCache, $rootScope) {
    var avatarService = {};
    var avatars = {};

    avatarService.getAvatar = function (id) {
        var image = avatarCache.get(id);
        if (!image) {
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
    return avatarService;
});

