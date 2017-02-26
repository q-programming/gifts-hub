var app = angular.module('app', ['ngRoute']);
app.config(function ($routeProvider, $httpProvider, $locationProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'home.html',
            controller: 'home'
        })
        .when('/login', {
            templateUrl: 'login.html',
            controller: 'navigation'
        })
        .when('/list', {
            templateUrl: 'list.html',
            controller: 'home'
        })
        .otherwise('/');
    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
    $locationProvider.hashPrefix('');
});