var avatarService = angular.module('avatarService', []);
avatarService.factory('avatarService', function ($http, $log, avatarCache) {
    var avatarService = {};
    var avatars = {};

    avatarService.getAvatar = function (id) {
        var data = avatarCache.get(id);
        if (!data) {
            return $http.get('api/user/' + id + '/avatar').then(function (result) {
                data = result.data.image;
                avatarCache.put(id, data);
                return data;
            }).catch(function (response) {
                $log.error("Failed to get avatar");
                $log.error(response);
            });
        }
        $log.debug("[DEBUG] Fetching avatar from cache");
        return data;
    };


    function _arrayBufferToBase64(buffer) {
        var binary = '';
        var bytes = new Uint8Array(buffer);
        var len = bytes.byteLength;
        for (var i = 0; i < len; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        return window.btoa(binary);
    }

    return avatarService;
});

