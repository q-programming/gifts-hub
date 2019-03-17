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
            var url = UtilsService.getAppUrl();
            return url + "#/public/" + user.id
        };

        UtilsService.getAppUrl = function () {
            return $location.absUrl().split("#")[0];
        };

        UtilsService.toID = function (string) {
            if (string) {
                return string.replace(/[^a-zA-Z0-9]/g, '_');
            }
            return Math.floor((Math.random() * 6) + 1);
        };

        UtilsService.toggleRow = function (target, event) {
            $(event.target).toggleClass("fa-caret-down");
            $(event.target).toggleClass("fa-caret-right");
            angular.element(document.getElementById(target)).toggle();
        };


        return UtilsService;
    }]);