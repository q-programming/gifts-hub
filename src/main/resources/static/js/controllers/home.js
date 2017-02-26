app.controller('home', function ($rootScope, $scope, $http) {
    if ($rootScope.authenticated) {
        $http.get('api/resource').then(
            function successCallback(response) {
                $scope.greeting = response.data;
            },
            function errorCallback(response) {
                console.log("Error while fetching resource: " + response);
            });
    }
    $scope.testit = function () {
        var test = /*[[testtext]]*/ '';
        alert("some alert test contets:" + test);
    }
});