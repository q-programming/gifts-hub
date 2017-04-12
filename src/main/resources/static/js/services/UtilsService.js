var UtilsService = angular.module('UtilsService', []);

UtilsService.factory('UtilsService', ['$http', '$location', 'AlertService',
    function ($http, $location, AlertService) {
        var UtilsService = {};

        UtilsService.copyLink = function (noConfirm) {
            var el = document.getElementById('public-link');
            var range = document.createRange();
            range.selectNode(el);
            window.getSelection().removeAllRanges();
            window.getSelection().addRange(range);
            document.execCommand('copy');
            window.getSelection().removeAllRanges();
            if (!noConfirm) {
                AlertService.addSuccess("user.settings.public.copy.success");
            }
        };
        /**
         * Convert image element into base64 image
         * @param imgElem element to be converted
         * @returns {string}
         */
        UtilsService.getBase64Image = function (imgElem) {
            var canvas = document.createElement("canvas");
            canvas.width = imgElem.clientWidth;
            canvas.height = imgElem.clientHeight;
            var ctx = canvas.getContext("2d");
            ctx.drawImage(imgElem, 0, 0);
            var dataURL = canvas.toDataURL("image/png");
            return dataURL.replace(/^data:image\/(png|jpg);base64,/, "");
        };

        UtilsService.getPublicUrl = function (user) {
            var url = $location.absUrl().split("#")[0];
            return url + "#/public/" + user.id
        };

        return UtilsService;
    }]);