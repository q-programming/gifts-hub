app.controller('settings', function ($rootScope, $scope, $http, $location, $translate, $log, $window, AlertService, AvatarService, AppService) {
    $scope.avatarUploadInProgress = false;
    $scope.avatarImage = '';
    $scope.croppedAvatar = '';
    $scope.languages = {};
    if ($rootScope.authenticated) {
        getLanguages()
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
        var source = getBase64Image(el);
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

    $scope.getPublicUrl = function () {
        var url = $location.absUrl().split("#")[0];
        return url + "#/public/" + $rootScope.principal.id
    };

    $scope.copyLink = function () {
        var el = document.getElementById('public-link');
        var range = document.createRange();
        range.selectNode(el);
        window.getSelection().removeAllRanges();
        window.getSelection().addRange(range);
        document.execCommand('copy');
        window.getSelection().removeAllRanges();
        AlertService.addSuccess("user.settings.public.copy.success");
    };
    /**
     * Get all available languages for application
     */
    function getLanguages() {
        var url = 'api/app/languages';
        $http.get(url).then(
            function (response) {
                $log.debug("[DEBUG] Languages loaded");
                $scope.languages = response.data;
            }).catch(function (response) {
            AlertService.addError("error.general");
            $log.debug(response);
        });
    }

});