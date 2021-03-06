'use strict';

angular.module('admin').controller('Admin.AuditHistoryController',
        [ '$scope', '$q', '$modal', '$translate', 'UtilService', 'Admin.ApplicationSettingsService', 'Dialog.BootboxService', 'MessageService', function($scope, $q, $modal, $translate, Util, ApplicationSettingsService, DialogService, messageService) {
            var saved = {};
            ApplicationSettingsService.getProperty(ApplicationSettingsService.PROPERTIES.HISTORY_DAYS).then(function(response) {
                $scope.historyDays = Util.goodValue(response.data[ApplicationSettingsService.PROPERTIES.HISTORY_DAYS], 30);
                saved.historyDays = $scope.historyDays;
            });

            $scope.applyChanges = function() {
                if (saved.historyDays != $scope.historyDays) {
                    ApplicationSettingsService.setProperty(ApplicationSettingsService.PROPERTIES.HISTORY_DAYS, $scope.historyDays);
                    saved.historyDays = $scope.historyDays;


                    //change for AFDP-6803 change ok button content
                    bootbox.alert({
                        message: $translate.instant("admin.application.auditHistory.config.inform"),
                        buttons: {
                            ok:{
                                label: $translate.instant("admin.application.auditHistory.config.dialog.OKBtn")
                            },
                        },
                        callback: function(result) {
                            messageService.succsessAction();
                        }
                    });
                }
            }
        } ]);
