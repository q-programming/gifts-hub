var app = angular.module('app', ['ngRoute', 'ngAnimate', 'AvatarService', 'AuthService']);
app.constant("MESSAGES", {
    SUCCESS: "success",
    ERROR: "danger",
    WARNING: "warning"
});
app.config(function ($routeProvider, $httpProvider, $locationProvider, $logProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'home.html',
            controller: 'home'
        })
        .when('/login', {
            templateUrl: 'login.html',
            controller: 'navigation'
        })
        .when('/register', {
            templateUrl: 'register.html',
            controller: 'register'
        })
        .when('/list', {
            templateUrl: 'list.html',
            controller: 'gift'
        })
        .when('/settings', {
            templateUrl: 'settings.html',
            controller: 'settings'
        })
        .otherwise('/');
    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
    $locationProvider.hashPrefix('');
    $logProvider.debugEnabled(true);
});
app.factory('avatarCache', function ($cacheFactory) {
    return $cacheFactory('avatarCache');
});
app.directive('showErrors', function () {
    return {
        restrict: 'A',
        require: '^form',
        link: function (scope, el, attrs, formCtrl) {
            var inputEl = el[0].querySelector("[name]");
            var inputNgEl = angular.element(inputEl);
            var inputName = inputNgEl.attr('name');
            inputNgEl.bind('blur', function () {
                el.toggleClass('has-error', formCtrl[inputName].$invalid);
            });
            scope.$watch(function () {
                return scope.showErrorsCheckValidity;
            }, function (newVal, oldVal) {
                if (!newVal) {
                    return;
                }
                el.toggleClass('has-error', formCtrl[inputName].$invalid);
            });
        }
    }
});
app.run(function ($rootScope) {
    $rootScope.alerts = [];
    $rootScope.addAlert = function (type, message) {
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
            $rootScope.alerts.push(alert);
        }
    };
    $rootScope.clearAlerts = function () {
        $rootScope.alerts = [];
    };

    $rootScope.dismissAlert = function ($index) {
        $rootScope.alerts.splice($index, 1)
    }
});
