var AppService = angular.module('AppService', []);

AppService.factory('AppService', function ($http) {
    var AppService = {};

    AppService.getLanguageList = function () {
        var url = 'api/app/languages';
        return $http.get(url);
    };
    return AppService;
});