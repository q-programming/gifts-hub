var AlertService = angular.module('AlertService', []);
AlertService.factory('AlertService', function ($http, $log, $rootScope, $timeout, MESSAGES) {
    var AlertService = {};
    $rootScope.alerts = [];

    AlertService.addSuccess = function (message) {
        AlertService.addAlert(MESSAGES.SUCCESS, message);
    };
    AlertService.addWarning = function (message) {
        AlertService.addAlert(MESSAGES.WARNING, message);
    };
    AlertService.addError = function (message) {
        AlertService.addAlert(MESSAGES.ERROR, message);
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
