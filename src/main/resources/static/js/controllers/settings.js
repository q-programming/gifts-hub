app.controller('settings', ['$rootScope', '$scope', '$http', '$location', '$translate', '$log', '$window', 'AlertService', 'AvatarService', 'AppService', 'UtilsService', 'AuthService',
    function ($rootScope, $scope, $http, $location, $translate, $log, $window, AlertService, AvatarService, AppService, UtilsService, AuthService) {
        $scope.avatarUploadInProgress = false;
        $scope.avatarImage = '';
        $scope.croppedAvatar = '';
        $scope.languages = {};
        if ($rootScope.authenticated) {
            AppService.getLanguageList().then(function (response) {
                $scope.languages = response.data
            }).catch(function (response) {
                AlertService.addError("error.general");
                $log.debug(response);
            });
        } else {
            $location.path("/login");
        }

        $scope.handleFileSelect = function (evt) {
            $scope.avatarUploadInProgress = true;
            var file = evt.files[0];
            var reader = new FileReader();
            reader.onload = function (evt) {
                $scope.$apply(function ($scope) {
                    $scope.avatarImage = evt.target.result;
                });
            };
            reader.readAsDataURL(file);
        };

        $scope.uploadAvatarFile = function () {
            var el = document.getElementById("croppedAvatar");
            var source = UtilsService.getBase64Image(el);
            if (source) {
                AvatarService.uploadAvatar(JSON.stringify(source));
                $scope.avatarUploadInProgress = false;
                document.getElementById("avatarFileInput").value = "";
                AlertService.addSuccess('user.settings.avatar.success');
            }
        };

        $scope.update = function () {
            $http.post('api/user/settings', angular.toJson({
                id: $rootScope.principal.id,
                language: $rootScope.principal.language,
                publicList: $rootScope.principal.publicList
            })).then(
                function () {
                    $translate.use($rootScope.principal.language);
                    $location.search('lang', $rootScope.principal.language);
                    AlertService.addSuccess('user.settings.updated');
                    $scope.lang_success = true;
                }).catch(function (response) {
                AlertService.addError('user.settings.updated.error')
            });
        };

        $scope.getPublicUrl = function (user) {
            return UtilsService.getPublicUrl($rootScope.principal);
        };

        $scope.copyLink = function () {
            UtilsService.copyLink();
        };

        $scope.deleteAccount = function () {
            var url = 'api/user/delete/' + $rootScope.principal.id;
            $http.delete(url).then(
                function (response) {
                    $log.debug("[DEBUG] Account deleted");
                    AlertService.addSuccessMessage(response.data.message);
                    AuthService.logout();
                }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        };

    }]);