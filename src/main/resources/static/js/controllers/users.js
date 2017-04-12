app.controller('userlist', ['$scope', '$rootScope', '$http', '$log', '$uibModal', '$filter', '$translate', '$location','$timeout', 'AlertService', 'AvatarService', 'AppService', 'UtilsService',
    function ($scope, $rootScope, $http, $log, $uibModal, $filter, $translate, $location,$timeout, AlertService, AvatarService, AppService, UtilsService) {
        //lists
        $scope.users = [];
        $scope.families = [];
        $scope.usersWithoutFamily = [];

        //current user props.
        $scope.family = {};
        $scope.hasFamily = null;
        $scope.familyAdmin = null;

        //triggers
        $scope.sortByName = null;
        $scope.sortByFamily = null;

        if ($rootScope.authenticated) {
            showUsersWithDefaultSorting();
        } else {
            $location.path("/login");
        }

        //************************SORT BY********************************
        function showUsersWithDefaultSorting() {
            AppService.getDefaultSort().then(function (response) {
                switch (response.data) {
                    case 'FAMILY':
                        $scope.sortByFamilies();
                        break;
                    default://default is sort by name
                        $scope.sortByNames();
                }
                // $scope.languages = response.data;
            }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        }

        $scope.sortByNames = function () {
            $scope.families.length = 0;
            getUsers();
            getFamily();
            $scope.sortByName = true;
            $scope.sortByFamily = false;
        };

        $scope.sortByFamilies = function () {
            $scope.users.length = 0;
            getAllFamilies();
            getUsersWithoutFamily();
            getFamily();
            $scope.sortByName = false;
            $scope.sortByFamily = true;
        };

        // ***********************FAMILIES********************************
        /**
         * Creates new family
         */
        $scope.createFamily = function () {
            $scope.family = {};
            $scope.family.members = [];
            $scope.family.admins = [];
            $translate("user.family.create").then(function (translation) {
                $scope.modalTitle = translation;
            });
            $translate("user.family.create.help").then(function (translation) {
                $scope.modalHelp = translation;
            });
            var modalInstance = $uibModal.open({
                templateUrl: 'modals/family.html',
                scope: $scope,
                controller: function ($uibModalInstance, $scope) {
                    getUsersWithoutFamily();
                    $scope.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };
                    $scope.action = function () {
                        $log.debug("[DEBUG] Creating family");
                        sendFamilyData($scope.family, true);
                        $uibModalInstance.close()
                    };
                }
            });
        };

        $scope.editFamily = function () {
            $translate("user.family.edit").then(function (translation) {
                $scope.modalTitle = translation;
            });
            $translate("user.family.edit.help").then(function (translation) {
                $scope.modalHelp = translation;
            });
            isFamilyAdmin();
            filterAndAddAvatar($scope.family.members);
            filterAndAddAvatar($scope.family.admins);
            var modalInstance = $uibModal.open({
                templateUrl: 'modals/family.html',
                scope: $scope,
                controller: function ($uibModalInstance, $scope) {
                    getUsersWithoutFamily();
                    $scope.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };
                    $scope.action = function () {
                        $log.debug("[DEBUG] Creating family");
                        sendFamilyData($scope.family, false);
                        $uibModalInstance.close()
                    };
                }
            });
        };

        $scope.removeUserFromAdmin = function (userToRemove) {
            angular.forEach($scope.family.admins, function (user, index) {
                if (user.id === userToRemove.id) {
                    $scope.family.admins.splice(index, 1);
                    return
                }
            });
        };

        $scope.removeUserFromFamily = function (array, index) {
            var user = array[index];
            array.splice(index, 1);
            $scope.removeUserFromAdmin(user);
        };

        /**
         * Add avatars to all users and remove currently logged in user from array in the process
         * @param userArray
         */
        function filterAndAddAvatar(userArray) {
            var i = userArray.length;
            while (i--) {
                var user = userArray[i];
                if (user.id === $rootScope.principal.id) {
                    userArray.splice(i, 1);
                } else {
                    AvatarService.getUserAvatar(user)
                }
            }
        }

        /**
         * Send family data for creation or edition
         * @param familyForm
         * @param create
         */
        function sendFamilyData(familyForm, create) {
            var url;
            if (create) {
                url = 'api/user/family-create';
            } else {
                url = 'api/user/family-update';
            }
            var dataToSend = {};
            dataToSend.members = [];
            dataToSend.admins = [];
            angular.forEach(familyForm.members, function (member) {
                dataToSend.members.push(member.id);
            });
            angular.forEach(familyForm.admins, function (member) {
                dataToSend.admins.push(member.id);
            });
            if (!familyForm.name) {
                familyForm.name = $rootScope.principal.surname;
            }
            dataToSend.name = familyForm.name;
            $scope.family = {};
            $http.post(url, dataToSend).then(
                function (response) {
                    if (create) {
                        AlertService.addSuccess("user.family.create.success");
                    } else {
                        AlertService.addSuccess("user.family.edit.success");
                    }
                    showUsersWithDefaultSorting()
                }).catch(function (response) {
                showUsersWithDefaultSorting()
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        }

        /**
         * Get all users that currently are not part of any family
         */
        function getUsersWithoutFamily() {
            $http.get('api/user/users?family=true').then(
                function (response) {
                    $scope.usersWithoutFamily.length = 0;
                    $log.debug("[DEBUG] Loaded family users");
                    angular.forEach(response.data, function (user) {
                        if (user.id !== $rootScope.principal.id) {
                            AvatarService.getUserAvatar(user);
                            $scope.usersWithoutFamily.push(user);
                        }
                    });
                }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        }

        /**
         * Check if currently logged in user is admin of his family
         */
        function isFamilyAdmin() {
            angular.forEach($scope.family.admins, function (user) {
                if (user.id === $rootScope.principal.id) {
                    $scope.familyAdmin = true;
                }
            });
        }

        /**
         * Get currently logged in user family
         */
        function getFamily() {
            $http.get('api/user/family').then(
                function (response) {
                    if (response.data) {
                        $scope.family.length = 0;
                        $scope.family = response.data;
                        $scope.hasFamily = true;
                        isFamilyAdmin();
                    }
                }
            );
        }

        /**
         * Get currently logged in user family
         */
        function getAllFamilies() {
            $http.get('api/user/families').then(
                function (response) {
                    if (response.data) {
                        $scope.families.length = 0;
                        $scope.families = response.data;
                        if ($scope.families) {
                            angular.forEach($scope.families, function (family) {
                                angular.forEach(family.members, function (user) {
                                    AvatarService.getUserAvatar(user);
                                });
                            });
                        }
                    }
                }
            );
        }


        // *******************************KID***************************
        /**
         * Call modal for creation of kid for family
         */
        $scope.addKid = function () {
            $scope.avatarUploadInProgress = false;
            $scope.avatarImage = '';
            $scope.croppedAvatar = '';
            $scope.formData = {};
            $scope.usernameExists = null;
            $translate("user.family.add.kid").then(function (translation) {
                $scope.modalTitle = translation;
            });
            $translate("user.family.add.kid.help").then(function (translation) {
                $scope.modalHelp = translation;
            });
            var modalInstance = $uibModal.open({
                templateUrl: 'modals/kid.html',
                scope: $scope,
                controller: function ($uibModalInstance, $scope) {
                    $scope.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };


                    $scope.action = function () {
                        $log.debug("[DEBUG] Creating kid");
                        sendChildData($scope.formData, true);
                        $uibModalInstance.close()
                    };
                }
            });
        };

        $scope.editKid = function (kid) {
            $scope.avatarUploadInProgress = false;
            $scope.avatarImage = kid.avatar;
            $scope.croppedAvatar = '';
            $scope.editKidInProgress = true;
            $scope.formData = $.extend({}, kid);
            $translate("user.family.edit.kid").then(function (translation) {
                $scope.modalTitle = translation;
            });
            $scope.modalHelp = '';
            var modalInstance = $uibModal.open({
                templateUrl: 'modals/kid.html',
                scope: $scope,
                controller: function ($uibModalInstance, $scope) {
                    $scope.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };
                    $scope.action = function () {
                        $log.debug("[DEBUG] edit family");
                        sendChildData($scope.formData, false);
                        $scope.editKidInProgress = false;
                        $uibModalInstance.close()
                    };
                }
            });
        };

        $scope.handleFileSelect = function (evt) {
            $scope.avatarUploadInProgress = true;
            var file = evt.files[0];
            var reader = new FileReader();
            reader.onload = function (evt) {
                $scope.$apply(function ($scope) {
                    $scope.avatarImage = evt.target.result;
                });
            };
            reader.readAsDataURL(file);
        };
        $scope.saveAvatarFile = function () {
            var el = document.getElementById("croppedAvatar");
            var source = UtilsService.getBase64Image(el);
            if (source) {
                $scope.formData.avatar = el.currentSrc;
                $scope.formData.avatarSource = JSON.stringify(source);
                $scope.avatarUploadInProgress = false;
                document.getElementById("avatarFileInput").value = "";
            }
        };

        /**
         * Check if username is free
         */
        $scope.checkUsername = function () {
            $scope.usernameExists = false;
            if ($scope.formData.username) {
                $http.post('api/user/validate-username', $scope.formData.username).then(
                    function (response) {
                        $scope.usernameExists = response.data.body.code === 'ERROR';
                    }).catch(function (response) {
                    AlertService.addError("error.general", response);
                });
            }
        };

        /**
         * Check if kid belongs to currently logged in user family
         * @param kid
         * @returns {boolean}
         */
        $scope.isUsersFamilyKid = function (kid) {
            if ($scope.family && $scope.family.members) {
                return $scope.family.members.map(function (e) {
                        return e.id;
                    }).indexOf(kid.id) > -1
            }
            return false;
        };

        /**
         * Send child data for creation/update
         * @param formData
         * @param create
         */
        function sendChildData(formData, create) {
            var url;
            if (create) {
                url = 'api/user/kid-add';
            } else {
                url = 'api/user/kid-update';
            }
            var dataToSend = {};
            dataToSend.id = formData.id;
            dataToSend.name = formData.name;
            dataToSend.surname = formData.surname;
            dataToSend.username = formData.username;
            dataToSend.avatar = formData.avatarSource;
            dataToSend.publicList = formData.publicList;
            $http.post(url, dataToSend).then(
                function (response) {
                    if (create) {
                        AlertService.addSuccess("user.family.add.kid.success");
                    } else {
                        AlertService.addSuccess("user.family.edit.kid.success");
                    }
                    showUsersWithDefaultSorting();
                }).catch(function (response) {
                showUsersWithDefaultSorting();
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        }

        $scope.getPublicUrl = function (user) {
            return UtilsService.getPublicUrl(user);
        };
        $scope.copyLink = function () {
            $scope.showCopiedMsg = true;
            $timeout(function () {
                $scope.showCopiedMsg = false;
            }, 5000);
            UtilsService.copyLink(true);

        };

        // ***********************USERS********************************

        /**
         * Get all users , sorted by names
         * Read theirs avatar in the process
         */
        function getUsers() {
            $http.get('api/user/users').then(
                function (response) {
                    $scope.users.length = 0;
                    $log.debug("[DEBUG] Loaded users");
                    angular.forEach(response.data, function (user) {
                        AvatarService.getUserAvatar(user);
                        $scope.users.push(user);
                    });
                }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        }
    }]);

