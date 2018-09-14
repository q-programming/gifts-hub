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
    , 'ng.httpLoader'
    , 'angular-confirm'
    , 'angular-tour'
    , 'AvatarService'
    , 'AuthService'
    , 'AlertService'
    , 'AppService'
    , 'UtilsService']);
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
app.config(['$routeProvider', '$httpProvider', '$locationProvider', '$logProvider', 'localStorageServiceProvider', '$translateProvider', 'httpMethodInterceptorProvider',
    function ($routeProvider, $httpProvider, $locationProvider, $logProvider, localStorageServiceProvider, $translateProvider, httpMethodInterceptorProvider) {
        var DEFAULT_LANGUAGE = 'pl';
        $routeProvider
            .when('/', {
                templateUrl: 'home.html',
                controller: 'home'
            })
            .when('/login', {
                templateUrl: 'user/login.html',
                controller: 'login'
            })
            .when('/logout', {
                templateUrl: 'user/login.html',
                controller: 'logout'
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
            .when('/confirm/:token?', {
                templateUrl: 'home.html',
                controller: 'confirm'
            })
            .when('/help/:language?', {
                templateUrl: function (params) {
                    if (params.language) {
                        return 'help/' + params.language + '.html'
                    } else {
                        return 'help/' + DEFAULT_LANGUAGE + '.html'
                    }
                },
                controller: 'help'
            })
            .when('/404', {
                templateUrl: 'error/404.html',
                controller: 'error'
            })
            .otherwise('/');
        //add correct headers
        $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
        //location
        $locationProvider.hashPrefix('');
        //logs
        $logProvider.debugEnabled(true);
        //locale
        localStorageServiceProvider
            .setStorageType('sessionStorage')
            .setPrefix('gifts-hub');
        $translateProvider.useUrlLoader('api/messages');
        $translateProvider.useStorage('UrlLanguageStorage');


        $translateProvider.preferredLanguage(DEFAULT_LANGUAGE);
        $translateProvider.fallbackLanguage(DEFAULT_LANGUAGE);
        //http loader
        httpMethodInterceptorProvider.whitelistLocalRequests();

    }]);
app.factory('avatarCache', ['$cacheFactory',
    function ($cacheFactory) {
        return $cacheFactory('avatarCache');
    }]);

//TODO move to directive js
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
app.run(['$rootScope', '$confirmModalDefaults', '$translate', '$log', '$location', 'AuthService', function ($rootScope, $confirmModalDefaults, $translate, $log, $location, AuthService) {
    $rootScope.alerts = [];
    $rootScope.$on('$routeChangeStart', function (event, current, pre) {
        var path = $location.path();
        if (!$rootScope.authenticated
            && path.indexOf("help") === -1
            && path.indexOf("login") === -1
            && path.indexOf("public") === -1) {
            $log.debug('[DEBUG] Redirecting to Login');
            $location.path('/login');
        }
    });
    //confirm
    $translate("main.confirm.yes").then(function (translation) {
        $confirmModalDefaults.defaultLabels.ok = translation;
    });
    $translate("main.confirm.no").then(function (translation) {
        $confirmModalDefaults.defaultLabels.cancel = translation;
    });
    $translate("main.confirm").then(function (translation) {
        $confirmModalDefaults.defaultLabels.title = translation;
    });
}]);

