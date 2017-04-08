app.factory('UrlLanguageStorage', ['$location', function ($location) {
    var UrlLanguageStorage = {};
    UrlLanguageStorage.get = function (name) {
        return $location.search()['lang']
    };
    UrlLanguageStorage.put = function (name, value) {
    };
    return UrlLanguageStorage;
}]);