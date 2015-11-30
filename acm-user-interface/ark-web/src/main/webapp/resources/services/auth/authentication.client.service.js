'use strict';

/**
 * @ngdoc service
 * @name services.service:Authentication
 *
 * @description
 * {@link https://github.com/Armedia/ACM3/blob/develop/acm-user-interface/ark-web/src/main/webapp/resources/services/auth/authentication.client.service.js services/auth/authentication.client.service.js}
 *
 * The Authentication service retrieves user information from server
 */
angular.module('services').factory('Authentication', ['$resource', 'StoreService', 'UtilService',
    function ($resource, Store, Util) {
        var Service = $resource('proxy/arkcase/api/v1/users/info', {}, {
            /**
             * @ngdoc method
             * @name queryUserInfo
             * @methodOf services.service:Authentication
             *
             * @description
             * Returns User info object
             *
             * @returns {HttpPromise} Future user info object
             */
            queryUserInfo: {
                method: 'GET',
                url: 'proxy/arkcase/api/v1/users/info',
                cache: true
            }
            , queryUserInfo_tmp: {
                method: 'GET',
                url: 'proxy/arkcase/api/v1/users/info',
                cache: true
            }
        });


        Service.SessionCacheNames = {
            USER_INFO: "AcmUserInfo"
        };

        /**
         * @ngdoc method
         * @name getCaseInfo
         * @methodOf services.service:Authentication
         *
         * @description
         * Query current login user info
         *
         * @returns {Object} Promise
         */
        Service.queryUserInfoNew = function () {
            var cacheUserInfo = new Store.SessionData(Service.SessionCacheNames.USER_INFO);
            var userInfo = cacheUserInfo.get();
            return Util.serviceCall({
                service: Service.queryUserInfo
                , result: userInfo
                , onSuccess: function (data) {
                    if (Service.validateUserInfo(data)) {
                        userInfo = data;
                        cacheUserInfo.set(userInfo);
                        return userInfo;
                    }
                }
            });
        };

        /**
         * @ngdoc method
         * @name validateUserInfo
         * @methodOf services.service:Authentication
         *
         * @description
         * Validate case data
         *
         * @param {Object} data  Data to be validated
         *
         * @returns {Boolean} Return true if data is valid
         */
        Service.validateUserInfo = function (data) {
            if (Util.isEmpty(data)) {
                return false;
            }
            if (Util.isEmpty(data.userId)) {
                return false;
            }
            if (Util.isEmpty(data.fullName)) {
                return false;
            }
            if (Util.isEmpty(data.mail)) {
                return false;
            }
            if (Util.isEmpty(data.firstName)) {
                return false;
            }
            if (Util.isEmpty(data.lastName)) {
                return false;
            }
            return true;
        };
        return Service;
    }
]);