angular.module('personRelationships', ['personService', 'relationshipService', 'ui.bootstrap'])
    .controller('PersonRelationshipController', ['$scope', 'PersonService', 'RelationshipService', function ($scope, PersonService, RelationshipService) {
            	
        $scope.relationships = [{uuid: '', name: '', type: ''}];
        
    	var patientUuid = '';
    	if (jq('#patientUuid')) {
    		patientUuid = jq('#patientUuid').val();
    	}
    	if (patientUuid != '') {
    		RelationshipService.getRelationships({'person': patientUuid,
    			'v': 'custom:(uuid,personA:(uuid,display,personName,birthdate,isPatient,personId),personB:(uuid,display,personName,birthdate,isPatient,personId),relationshipType)'}).then(function(patientRelationships) {
                    if (patientRelationships) {
                        for (relationship of patientRelationships) {
                            var rel = {};
                            if(relationship.personA.uuid != patientUuid){
                                rel.uuid = relationship.personA.uuid;
                                rel.name = relationship.personA.personName.givenName + ' ' + relationship.personA.personName.familyName;
                                rel.type = relationship.relationshipType.uuid + '-A';
                            } else {
                                rel.uuid = relationship.personB.uuid;
                                rel.name = relationship.personB.personName.givenName + ' ' + relationship.personB.personName.familyName;
                                rel.type = relationship.relationshipType.uuid + '-B';

                            }
                            if ($scope.relationships[0].uuid == '') {
                                $scope.relationships[0].uuid = rel.uuid;
                                $scope.relationships[0].name = rel.name;
                                $scope.relationships[0].type = rel.type;
                            } else {
                                $scope.relationships.push(rel);
                            }
                        }
                    }
                });
        }
    	
        $scope.getPersons = function (searchString) {
            return PersonService.getPersons({'q': searchString, 'v': 'full'});
        };

        $scope.addNewRelationship = function () {
            $scope.relationships.push({uuid: '', name: '', type: ''});
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

        // this is a (hack?) that provides integration with the one-question-per-screen navigator, since the navigator doesn't play well with angular
        // specifically, we override the "displayValue" function on the relationship_type field within the navigator so that:
        // 1) the checkmark in the left-hand navigation of the is properly rendered when data is filled out
        // 2) the confirmation screen at the end of the workflow properly displays the relationships that have been entered
        if (typeof(NavigatorController) != 'undefined') {
            var field = NavigatorController.getFieldById("relationship_type");
            field.displayValue = function() {
                return $scope.relationships.map(function(r) {
                    return r.name +  " - " + jq('.rel_type:first').children("[value='" + r.type + "']").text();
                }).join(', ');
            }
        }
    }]);