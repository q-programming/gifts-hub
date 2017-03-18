var app = angular.module('app', [
    'ngRoute'
    , 'ngAnimate'
    , 'ngSanitize'
    , 'LocalStorageModule'
    , 'pascalprecht.translate'
    , 'ngImgCrop'
    , 'AvatarService'
    , 'AuthService'
    , 'AlertService']);
app.constant("MESSAGES", {
    SUCCESS: "success",
    ERROR: "danger",
    WARNING: "warning"
});
app.config(function ($routeProvider, $httpProvider, $locationProvider, $logProvider, localStorageServiceProvider, $translateProvider) {
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
        .when('/list/:username?', {
            templateUrl: 'list.html',
            controller: 'gift'
        })
        .when('/users', {
            templateUrl: 'userlits.html',
            controller: 'userlist'
        })
        .when('/settings', {
            templateUrl: 'settings.html',
            controller: 'settings'
        })
        .otherwise('/');
    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
    $locationProvider.hashPrefix('');
    $logProvider.debugEnabled(true);
    localStorageServiceProvider
        .setStorageType('sessionStorage')
        .setPrefix('gifts-hub');
    $translateProvider.useUrlLoader('/api/messages');
    $translateProvider.useStorage('UrlLanguageStorage');
    $translateProvider.preferredLanguage('pl');
    $translateProvider.fallbackLanguage('pl');
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


app.directive("progressbar", function () {
    return {
        restrict: "A",
        scope: {
            total: "=",
            current: "=",
            class: "="
        },
        link: function (scope, element) {
            scope.$watch("current", function (value) {
                var percentage = scope.current / scope.total * 100;
                element.css("width", percentage + "%");
                if (percentage < 50) {
                    element.addClass("progress-bar-danger");
                    element.removeClass("progress-bar-warning");
                    element.removeClass("progress-bar-success");
                } else if (percentage === 50) {
                    element.removeClass("progress-bar-danger");
                    element.addClass("progress-bar-warning");
                    element.removeClass("progress-bar-success");
                } else if (percentage > 50) {
                    element.removeClass("progress-bar-danger");
                    element.removeClass("progress-bar-warning");
                    element.addClass("progress-bar-success");
                }
            });
            scope.$watch("total", function (value) {
                element.css("width", scope.current / scope.total * 100 + "%");
            })
        }
    }
});
app.run(function ($rootScope) {
    $rootScope.alerts = [];
});
