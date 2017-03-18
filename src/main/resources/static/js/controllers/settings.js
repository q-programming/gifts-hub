app.controller('settings', function ($rootScope, $scope, $http, $location, $translate, AlertService, AvatarService) {
    $scope.avatarUploadInProgress = false;
    $scope.avatarImage = ''
    $scope.croppedAvatar = '';
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
        }
    };
    function getBase64Image(imgElem) {
        var canvas = document.createElement("canvas");
        canvas.width = imgElem.clientWidth;
        canvas.height = imgElem.clientHeight;
        var ctx = canvas.getContext("2d");
        ctx.drawImage(imgElem, 0, 0);
        var dataURL = canvas.toDataURL("image/png");
        return dataURL.replace(/^data:image\/(png|jpg);base64,/, "");
    }

    $scope.update = function () {
        $http.post('api/user/language', angular.toJson({
            id: $rootScope.principal.id,
            language: $rootScope.principal.language
        })).then(
            function () {
                $translate.use($rootScope.principal.language);
                $location.search('lang', $rootScope.principal.language);
                AlertService.addSuccess('user.settings.language.set');
                $scope.lang_success = true;
            }).catch(function (response) {
            AlertService.addError('user.settings.language.error')
        });
    }

});