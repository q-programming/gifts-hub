var app = angular.module('app', ['ngRoute', 'avatarService']);
app.config(function ($routeProvider, $httpProvider, $locationProvider,$logProvider) {
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
            controller: 'list'
        })
        .otherwise('/');
    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
    $locationProvider.hashPrefix('');
    $logProvider.debugEnabled(true);
});
app.factory('avatarCache', function ($cacheFactory) {
    return $cacheFactory('avatarCache');
});