app.controller('userlist', ['$scope', '$rootScope', '$http', '$log', '$uibModal', '$filter', '$translate', '$location', 'AlertService', 'AvatarService',
    function ($scope, $rootScope, $http, $log, $uibModal, $filter, $translate, $location, AlertService, AvatarService) {
        $scope.users = [];
        $scope.familyUsers = [];
        $scope.family = {};
        $scope.hasFamily = null;
        $scope.familyAdmin = null;
        if ($rootScope.authenticated) {
            getUsers();
            getFamily();
        } else {
            $location.path("/login");
        }

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
            $scope.familyAdmin = true;
            var modalInstance = $uibModal.open({
                templateUrl: 'modals/family.html',
                scope: $scope,
                controller: function ($uibModalInstance, $scope) {
                    getFamilyAvailableUsers();
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
                    getFamilyAvailableUsers();
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
            $http.post(url, dataToSend).then(
                function (response) {
                    if (create) {
                        AlertService.addSuccess("user.family.create.success");
                    } else {
                        AlertService.addSuccess("user.family.edit.success");
                    }
                    getUsers();
                    getFamily();
                }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        }

        function getFamilyAvailableUsers() {
            $http.get('api/user/users?family=true').then(
                function (response) {
                    $scope.familyUsers = [];
                    $log.debug("[DEBUG] Loaded family users");
                    angular.forEach(response.data, function (user) {
                        if (user.id !== $rootScope.principal.id) {
                            AvatarService.getUserAvatar(user);
                            $scope.familyUsers.push(user);
                        }
                    });
                }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        }

        function isFamilyAdmin() {
            angular.forEach($scope.family.admins, function (user) {
                if (user.id === $rootScope.principal.id) {
                    $scope.familyAdmin = true;
                }
            });
        }

        function getFamily() {
            $http.get('api/user/family').then(
                function (response) {
                    if (response.data) {
                        $scope.family = response.data;
                        $scope.hasFamily = true;
                        isFamilyAdmin();
                    }
                }
            );
        }

        // KID
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
                        var source = getBase64Image(el);
                        if (source) {
                            $scope.formData.avatar = el.currentSrc;
                            $scope.formData.avatarSource = JSON.stringify(source);
                            $scope.avatarUploadInProgress = false;
                            document.getElementById("avatarFileInput").value = "";
                        }
                    };

                    $scope.action = function () {
                        $log.debug("[DEBUG] Creating family");
                        sendChildData($scope.formData, true);
                        $uibModalInstance.close()
                    };
                }
            });
        };
        $scope.checkUsername = function () {
            $scope.usernameExists = false;
            if ($scope.formData.username) {
                $http.post('api/user/validate-username', $scope.formData.username).then(
                    function (response) {
                        if (response.data.body.code === 'ERROR') {
                            $scope.usernameExists = true;
                        } else {
                            $scope.usernameExists = false;
                        }
                    }).catch(function (response) {
                    AlertService.addError("error.general", response);
                });
            }
        };


        function sendChildData(formData, create) {
            var url;
            if (create) {
                url = 'api/user/kid-add';
            } else {
                url = 'api/user/kid-update';
            }
            var dataToSend = {};
            dataToSend.name = formData.name;
            dataToSend.surname = formData.surname;
            dataToSend.username = formData.username;
            dataToSend.avatar = formData.avatarSource;
            $http.post(url, dataToSend).then(
                function (response) {
                    if (create) {
                        AlertService.addSuccess("user.family.add.kid.success");
                    } else {
                        AlertService.addSuccess("user.family.edit.kid.success");
                    }
                    getUsers();
                    getFamily();
                }).catch(function (response) {
                AlertService.addError("error.general", response);
                $log.debug(response);
            });
        }


        function getUsers() {
            $http.get('api/user/users').then(
                function (response) {
                    $scope.users = [];
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

