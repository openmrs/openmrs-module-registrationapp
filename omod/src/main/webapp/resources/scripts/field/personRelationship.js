angular.module('personRelationships', ['personService', 'ui.bootstrap'])
    .controller('PersonRelationshipController', ['$scope', 'PersonService', function ($scope, PersonService) {

        $scope.relationships = [{uuid: '', name: ''}];

        $scope.getPersons = function (searchString) {
            return PersonService.getPersons({'q': searchString, 'v': 'full'});
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

        $scope.selectPerson = function (person, index) {
            $scope.relationships[index].uuid = person.uuid;
            $scope.relationships[index].name = person.display;
        };
    }]);