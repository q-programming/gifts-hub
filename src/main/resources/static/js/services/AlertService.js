var AlertService = angular.module('AlertService', []);
AlertService.factory('AlertService', ['$http', '$log', '$rootScope', '$timeout', '$translate', 'MESSAGES',
    function ($http, $log, $rootScope, $timeout, $translate, MESSAGES) {
        var AlertService = {};
        $rootScope.alerts = [];
        /**
         * Adds success message but with key instead of message. Translation srv. will be called for it
         * @param code message code
         */
        AlertService.addSuccess = function (code) {
            $translate(code).then(function (translation) {
                AlertService.addAlert(MESSAGES.SUCCESS, translation);
            });
        };
        /**
         * Add success message without translation key
         * @param message message to be printed
         */
        AlertService.addSuccessMessage = function (message) {
            AlertService.addAlert(MESSAGES.SUCCESS, message);
        };

        AlertService.addWarning = function (code) {
            $translate(code).then(function (translation) {
                AlertService.addAlert(MESSAGES.WARNING, translation);
            });
        };

        AlertService.addError = function (code) {
            $translate(code).then(function (translation) {
                AlertService.addAlert(MESSAGES.ERROR, translation);
            });
        };
        AlertService.addError = function (code, response) {
            $translate(code).then(function (translation) {
                var msg = translation;
                if (response) {
                    if (response.message) {
                        msg += "</br>" + response.message;
                    } else if (response.data.message) {
                        msg += "</br>" + response.data.message;
                    }
                }
                AlertService.addAlert(MESSAGES.ERROR, msg);
            });
        };

        AlertService.addAlert = function (type, message) {
            var alert = {};
            var exists = false;
            alert.type = type;
            alert.msg = message;
            angular.forEach($rootScope.alerts, function (value) {
                if (value.msg = alert.msg) {
                    exists = true;
                    return false;
                }
            });
            if (!exists) {
                var index = $rootScope.alerts.push(alert) - 1;
                $timeout(function () {
                    AlertService.dismissAlert(index)
                }, 5000);
            }
        };
        AlertService.clearAlerts = function () {
            $rootScope.alerts = [];
        };

        AlertService.dismissAlert = function ($index) {
            $rootScope.alerts.splice($index, 1)
        };

        return AlertService;
    }]);
