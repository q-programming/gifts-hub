var app = angular.module('app', [
    'ngRoute'
    , 'ngAnimate'
    , 'ngSanitize'
    , 'LocalStorageModule'
    , 'pascalprecht.translate'
    , 'ngImgCrop'
    , 'ui.bootstrap'
    , 'ui.select'
    , 'ngCookies'
    , 'AvatarService'
    , 'AuthService'
    , 'AlertService'
    , 'AppService']);
app.constant("MESSAGES", {
    SUCCESS: "success",
    ERROR: "danger",
    WARNING: "warning"
});
app.constant("GIFT_STATUS", {
    NEW: "NEW",
    REALISED: "REALISED",
    CLAIMED: "CLAIMED"
});
app.config(function ($routeProvider, $httpProvider, $locationProvider, $logProvider, localStorageServiceProvider, $translateProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'home.html',
            controller: 'home'
        })
        .when('/login', {
            templateUrl: 'user/login.html',
            controller: 'login'
        })
        .when('/register', {
            templateUrl: 'user/register.html',
            controller: 'register'
        })
        .when('/list/:username?', {
            templateUrl: 'gifts/list.html',
            controller: 'gift'
        })
        .when('/public/:userid?', {
            templateUrl: 'gifts/publicList.html',
            controller: 'gift'
        })
        .when('/users', {
            templateUrl: 'user/list.html',
            controller: 'userlist'
        })
        .when('/settings', {
            templateUrl: 'user/settings.html',
            controller: 'settings'
        })
        .when('/manage', {
            templateUrl: 'app/manage.html',
            controller: 'manage'
        })
        .when('/404', {
            templateUrl: 'error/404.html',
            controller: 'error'
        })
        .otherwise('/');
    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
    $locationProvider.hashPrefix('');
    $logProvider.debugEnabled(true);
    localStorageServiceProvider
        .setStorageType('sessionStorage')
        .setPrefix('gifts-hub');
    $translateProvider.useUrlLoader('api/messages');
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
app.run(function ($rootScope) {
    $rootScope.alerts = [];
});

