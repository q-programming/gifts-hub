var AppService = angular.module('AppService', []);

AppService.factory('AppService', function ($http, $log) {
    var AppService = {};

    AppService.fillLanguageList = function (langList) {
        var url = 'api/app/languages';
        $http.get(url).then(
            function (response) {
                $log.debug("[DEBUG] Languages loaded");
                langList = response.data;
            }).catch(function (response) {
            AlertService.addError("error.general");
            $log.debug(response);
        });
    };
    return AppService;
});