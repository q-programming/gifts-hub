var AlertService = angular.module('AlertService', []);
AlertService.factory('AlertService', function ($http, $log, $rootScope, $timeout, $translate, MESSAGES) {
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
    AlertService.addError = function (code, message) {
        $translate(code).then(function (translation) {
            AlertService.addAlert(MESSAGES.ERROR, translation + "</br>" + message);
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
});
