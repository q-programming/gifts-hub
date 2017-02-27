app.controller('home', function ($rootScope, $scope, $http, $location) {
    if ($rootScope.authenticated) {
        $http.get('api/resource').then(
            function successCallback(response) {
                $scope.greeting = response.data;
            },
            function errorCallback(response) {
                console.log("Error while fetching resource: " + response);
            });
    } else {
        $location.path("/login");
    }
});