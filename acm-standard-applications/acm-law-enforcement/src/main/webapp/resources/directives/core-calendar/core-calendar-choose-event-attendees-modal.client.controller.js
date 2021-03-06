'use strict';

angular.module('directives').controller('Directives.CoreCalendarChooseEventAttendeesController', [ '$scope', '$modalInstance', '$config', 'attendeeType', 'attendees', function($scope, $modalInstance, $config, attendeeType, attendees) {
    $scope.config = $config;
    $scope.attendees = angular.copy(attendees);
    $scope.attendeeType = attendeeType;

    $scope.onSelectAttendee = function(selectedItems, lastSelectedItems, isSelected) {
        var selectedAttendeeEmail = lastSelectedItems[0].email_lcs;
        var isAttendeeSelected = _.find($scope.attendees, function(attendee) {
            return attendee.email === selectedAttendeeEmail;
        });

        var selectedAttendeeDataModel = {
            email: lastSelectedItems[0].email_lcs,
            displayName: lastSelectedItems[0].name,
            type: $scope.attendeeType
        };

        if (isAttendeeSelected) {
            if (!isSelected) {
                _.remove($scope.attendees, function(attendee) {
                    return attendee.email === isAttendeeSelected.email;
                });
            }
        } else {
            if (isSelected) {
                $scope.attendees.push(selectedAttendeeDataModel);
            }
        }
    };

    $scope.addAdditionalAttendees = function(tag) {
        var selectedAttendeeDataModel = {
            email: tag.email,
            displayName: '',
            type: $scope.attendeeType
        };

        $scope.attendees[$scope.attendees.length - 1] = selectedAttendeeDataModel;
    };

    $scope.addAttendees = function() {
        $modalInstance.close($scope.attendees);
    };

    $scope.cancel = function() {
        $modalInstance.dismiss();
    };
} ]);