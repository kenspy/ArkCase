'use strict';

/**
 * @ngdoc service
 * @name services:Object.ObjectService
 *
 * @description
 *
 * {@link https://gitlab.armedia.com/arkcase/ACM3/tree/develop/acm-standard-applications/acm-law-enforcement/src/main/webapp/resources/services/object/object.client.service.js services/object/object.client.service.js}
 *
 * Basic object service
 */

angular.module('services').factory('ObjectService', ['$state', '$window', 'UtilService', 'Object.LookupService'
    , function ($state, $window, Util, ObjectLookupService) {
        return {
            ObjectTypes: {
                CASE_FILE: "CASE_FILE"
                , COMPLAINT: "COMPLAINT"
                , TASK: "TASK"
                , ADHOC_TASK: "ADHOC"
                , TIMESHEET: "TIMESHEET"
                , COSTSHEET: "COSTSHEET"
                , DOCUMENT: "DOCUMENT"
                , FILE: "FILE"
            }

            /**
             * @ngdoc method
             * @name gotoUrl
             * @methodOf services:Object.ObjectService
             *
             * @param {String} objType ArkCase Object type
             * @param {String} objId ArkCase Object ID
             *
             * @description
             * Go to a page via URL that show the specified ArkCase Object (Case, Complaint, Document, etc.)
             */
            , gotoUrl: function (objType, objId) {
                ObjectLookupService.getObjectTypes().then(
                    function (objectTypes) {
                        var found = _.find(objectTypes, {type: objType});
                        if (found) {
                            var url = Util.goodValue(found.url);
                            url = url.replace(":id", objId);
                            url = url.replace(":type", objType);
                            $window.location.href = url;
                        }
                        return objectTypes;
                    }
                );
            }

            /**
             * @ngdoc method
             * @name gotoState
             * @methodOf services:Object.ObjectService
             *
             * @param {String} objType ArkCase Object type
             * @param {String} objId ArkCase Object ID
             *
             * @description
             * Go to a page by Angular route that show the specified ArkCase Object (Case, Complaint, Document, etc.)
             */
            , gotoState: function (objType, objId) {
                ObjectLookupService.getObjectTypes().then(
                    function (objectTypes) {
                        var found = _.find(objectTypes, {type: objType});
                        if (found) {
                            var state = Util.goodValue(found.state);
                            state = state.replace(":id", objId);
                            $state.go(state);
                        }
                        return objectTypes;
                    }
                );
            }

        };
    }
]);