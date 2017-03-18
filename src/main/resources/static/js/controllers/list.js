app.controller('list', function ($rootScope, $scope, $http) {
    if ($rootScope.authenticated) {
        //TODO something
    }
    $scope.testit = function () {
        var test = /*[[testtext]]*/ '';
        alert("some alert test contets:" + test);
    };
    $scope.testEmail = function () {
        var testEmail = "test@email.com";
        $http.get('api/user/validate-email?email=' + testEmail).then(
            function successCallback() {
                console.log("Got it")
            },
            function errorCallback() {
                console.log("error")
            });
    };
    $scope.getLocale = function () {
        $http.get('api/user/validate-email?email=' + testEmail).then(
            function successCallback() {
                console.log("Got it")
            },
            function errorCallback() {
                console.log("error")
            });
    }
});