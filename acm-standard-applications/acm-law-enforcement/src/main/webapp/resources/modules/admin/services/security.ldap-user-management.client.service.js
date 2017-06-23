'use strict';

angular.module('admin').factory('Admin.LdapUserManagementService', ['$resource', '$http', '$q', 'Acm.StoreService',
    function ($resource, $http, $q, Store) {
        return ({
            queryGroupsByDirectory: queryGroupsByDirectory,
            addGroupsToUser: addGroupsToUser,
            removeGroupsFromUser: removeGroupsFromUser,
            cloneUser: cloneUser
        });

        function queryGroupsByDirectory(directory) {
            return $http({
                method: 'GET',
                url: 'api/latest/users/directory/groups?directory=' + directory
            });
        };

        function addGroupsToUser(user, groups, directory) {
            var url = 'api/latest/ldap/' + directory + '/manage/' + user +'/groups';
            return $http({
                method: 'PUT',
                url: url,
                data: groups
            });
        };

        function removeGroupsFromUser(user, groups, directory) {
            var groupNames = {};
            groupNames['groupNames'] = groups;
            var url = 'api/latest/ldap/' + directory + '/manage/' + user +'/groups';
            return $http({
                method: 'DELETE',
                url: url,
                params: groupNames
            });
        };

        function cloneUser(user) {
            var url = 'api/latest/ldap/' + user.selectedUser.directory + '/users/' + user.selectedUser.key;
            return $http({
                method: 'POST',
                url: url,
                data: {
                    acmUser: user.acmUser,
                    password: user.password
                }
            });
        };
    }
]);