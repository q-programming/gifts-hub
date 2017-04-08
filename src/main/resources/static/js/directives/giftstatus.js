app.directive("giftStatus", ['$rootScope', '$translate', '$sce', 'GIFT_STATUS', function ($rootScope, $translate, $sce, GIFT_STATUS) {
    return {
        restrict: "E",
        scope: {
            gift: "="
        },
        template: '<span class="{{gift_status}}" uib-tooltip-html="gift_tooltip">' +
        '<i class="{{gift_state_class}}" aria-hidden="true"></i>' +
        '<i class="fa fa-fw fa-2x fa-gift" aria-hidden="true"></i>' +
        '</span>',
        link: function (scope, element) {
            if (scope.gift.status === GIFT_STATUS.REALISED) {
                scope.gift_status = "gift-status realised-gift";
                $translate("gift.status.realised").then(function (translation) {
                    scope.gift_tooltip = translation;
                });
                scope.gift_state_class = "fa fa-fw fa-check";
            } else {
                if (scope.gift.claimed && scope.gift.userId !== $rootScope.principal.id) {
                    scope.gift_status = "gift-status claimed-gift";
                    $translate("gift.status.claimed").then(function (translation) {
                        scope.gift_tooltip = $sce.trustAsHtml(translation + "</br>" + scope.gift.claimed.fullname);
                    });
                    scope.gift_state_class = "fa fa-fw fa-hand-paper-o";
                } else if (scope.gift.status === GIFT_STATUS.NEW) {
                    scope.gift_status = "gift-status new-gift";
                    $translate("gift.status.new").then(function (translation) {
                        scope.gift_tooltip = translation;
                    });
                    scope.gift_state_class = "fa fa-fw fa-certificate";
                }
                else {
                    scope.gift_status = "gift-status";
                    $translate("gift").then(function (translation) {
                        scope.gift_tooltip = translation;
                    });
                    scope.gift_state_class = "fa fa-fw fa-minus fa-spacer";
                }
            }
        }
    }
}]);