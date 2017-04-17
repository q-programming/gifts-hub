app.controller('navigation', ['$scope', '$rootScope', '$http', '$location', '$route', 'AvatarService', 'AuthService',
    function ($scope, $rootScope, $http, $location, $route, AvatarService, AuthService) {
        $scope.tab = function (route) {
            return $route.current && route === $route.current.controller;
        };
        $rootScope.authenticated = AuthService.isAuthenticated();
        //LOGOUT
        $scope.logout = function () {
            AuthService.logout();
        };
}]);