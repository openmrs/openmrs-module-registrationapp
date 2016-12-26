angular.module('personRelationships', ['patientService', 'ui.bootstrap'])
    .controller('PersonRelationshipController', ['$scope', 'PatientService', function ($scope, PatientService) {

        $scope.relationships = [{uuid: '', name: ''}];

        var patientCustomRep = 'custom:(person:(uuid,display))';

        $scope.getPatients = function (searchString) {
            return PatientService.getPatients({'q': searchString, 'v': patientCustomRep}).then(function (result) {
                var patients = [];
                angular.forEach(result, function (patient) {
                    patients.push({
                        uuid: patient.person.uuid,
                        name: patient.person.display
                    });
                });
                return patients;
            });
        };

        $scope.addNewRelationship = function () {
            $scope.relationships.push({uuid: '', name: ''});
        };

        $scope.removeRelationship = function (relationship) {
            if ($scope.relationships.length > 1) {
                $scope.relationships.splice($scope.relationships.indexOf(relationship), 1);
            } else {
                jq(function () {
                    jq(".rel_type").val('');
                    jq(".person-typeahead").val('');
                    jq(".person-typeahead").removeClass('ng-touched');
                    jq(".person-typeahead").removeClass('ng-invalid');
                    jq("[name='other_person_uuid']").val('');
                });
            }
        };

        $scope.selectPatient = function (person, index) {
            $scope.relationships[index].uuid = person.uuid;
            $scope.relationships[index].name = person.name;
        };
    }]);