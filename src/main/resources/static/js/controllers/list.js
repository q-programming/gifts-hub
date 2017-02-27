app.controller('list', function ($rootScope, $scope, $http) {
    if ($rootScope.authenticated) {
        //TODO something
    }
    $scope.testit = function () {
        var test = /*[[testtext]]*/ '';
        alert("some alert test contets:" + test);
    }
});