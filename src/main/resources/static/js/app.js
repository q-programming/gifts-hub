var app = angular.module('app', [
    'ngRoute'
    , 'ngAnimate'
    , 'ngSanitize'
    , 'LocalStorageModule'
    , 'pascalprecht.translate'
    , 'ngImgCrop'
    , 'ui.bootstrap'
    , 'ui.select'
    , 'AvatarService'
    , 'AuthService'
    , 'AlertService'
    , 'AppService']);
app.constant("MESSAGES", {
    SUCCESS: "success",
    ERROR: "danger",
    WARNING: "warning"
});
app.constant("GIFT_STATUS", {
    NEW: "NEW",
    REALISED: "REALISED",
    CLAIMED: "CLAIMED"
});
app.config(function ($routeProvider, $httpProvider, $locationProvider, $logProvider, localStorageServiceProvider, $translateProvider) {
    $routeProvider
        .when('/', {
            templateUrl: '/home.html',
            controller: 'home'
        })
        .when('/login', {
            templateUrl: '/user/login.html',
            controller: 'navigation'
        })
        .when('/register', {
            templateUrl: '/user/register.html',
            controller: 'register'
        })
        .when('/list/:username?', {
            templateUrl: '/gifts/list.html',
            controller: 'gift'
        })
        .when('/users', {
            templateUrl: '/user/list.html',
            controller: 'userlist'
        })
        .when('/settings', {
            templateUrl: '/user/settings.html',
            controller: 'settings'
        })
        .when('/manage', {
            templateUrl: '/app/manage.html',
            controller: 'manage'
        })
        .otherwise('/');
    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
    $locationProvider.hashPrefix('');
    $logProvider.debugEnabled(true);
    localStorageServiceProvider
        .setStorageType('sessionStorage')
        .setPrefix('gifts-hub');
    $translateProvider.useUrlLoader('/api/messages');
    $translateProvider.useStorage('UrlLanguageStorage');
    $translateProvider.preferredLanguage('pl');
    $translateProvider.fallbackLanguage('pl');
});
app.factory('avatarCache', function ($cacheFactory) {
    return $cacheFactory('avatarCache');
});
app.directive('showErrors', function () {
    return {
        restrict: 'A',
        require: '^form',
        link: function (scope, el, attrs, formCtrl) {
            var inputEl = el[0].querySelector("[name]");
            var inputNgEl = angular.element(inputEl);
            var inputName = inputNgEl.attr('name');
            inputNgEl.bind('blur', function () {
                el.toggleClass('has-error', formCtrl[inputName].$invalid);
            });
            scope.$watch(function () {
                return scope.showErrorsCheckValidity;
            }, function (newVal, oldVal) {
                if (!newVal) {
                    return;
                }
                el.toggleClass('has-error', formCtrl[inputName].$invalid);
            });
        }
    }
});
app.run(function ($rootScope) {
    $rootScope.alerts = [];
});

/**
 * Highlights text that matches $select.search.
 *
 * Taken from AngularUI Bootstrap Typeahead
 * See https://github.com/angular-ui/bootstrap/blob/0.10.0/src/typeahead/typeahead.js#L340
 */
app.filter('highlight', function () {
    function escapeRegexp(queryToEscape) {
        return ('' + queryToEscape).replace(/([.?*+^$[\]\\(){}|-])/g, '\\$1');
    }

    return function (matchItem, query) {
        return query && matchItem ? ('' + matchItem).replace(new RegExp(escapeRegexp(query), 'gi'), '<span class="ui-select-highlight">$&</span>') : matchItem;
    };
});
app.filter('propsFilter', function() {
    return function(items, props) {
        var out = [];
        if (angular.isArray(items)) {
            items.forEach(function(item) {
                var itemMatches = false;
                var keys = Object.keys(props);
                for (var i = 0; i < keys.length; i++) {
                    var prop = keys[i];
                    var text = props[prop].toLowerCase();
                    if (item && item[prop] && item[prop].toString().toLowerCase().indexOf(text) !== -1) {
                        itemMatches = true;
                        break;
                    }
                }
                if (itemMatches) {
                    out.push(item);
                }
            });
        } else {
            // Let the output be the input untouched
            out = items;
        }
        return out;
    };
});