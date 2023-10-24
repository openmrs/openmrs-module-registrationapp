function RegisterPatientRelationship(patientRelationship) {

  let selectedPersons = [];

  function updatePersonNames() {
    let confirmNames = '';
    for (const [key, value] of Object.entries(selectedPersons)) {
      if (value && value.length > 0) {
        if (confirmNames.length > 0) {
          confirmNames = confirmNames + "; " + value;
        } else {
          confirmNames = value;
        }
      }
    }
    if (confirmNames.length > 0 ) {
      $('#' + patientRelationship.id + "-selected_persons").attr('data-display-value', confirmNames);
    }
  }
  function getInputElementFor(fieldId) {
    // this is to handle integration with HFE, when the id is set on the parent span,
    // not the input element itself
    return $('#' + fieldId).is('input') ?
      $('#' + fieldId) :
      $('#' + fieldId).find('input');
  }

  function getValue(fieldId) {
    return getInputElementFor(fieldId).val();
  }
  function setValue(fieldId, value) {
    getInputElementFor(fieldId).val(value);
  }

  patientRelationship.container.find(".searchablePerson").autocomplete({
        autoFocus: true,
        source: function (request, response) {
          if (request.term.length < 3) {
            return;
          }
          let url = '/' + OPENMRS_CONTEXT_PATH + '/ws/rest/v1/person/';
          $.getJSON(url, {
            limit: 10,
            q: request.term,
            v: 'custom:(id,uuid,display,gender,age)'
          }, function (result) {
              let items = null;
              if ( patientRelationship.gender && patientRelationship.gender != 'null') {
                items = _.filter(result.results, function(person) {
                  return (person.gender === patientRelationship.gender);
                });
              }
              let persons = _.map( items != null ? items : result.results, function (item) {
                return {
                  label: item.display + ", " + item.age + ", " + item.gender,
                  data: item.uuid
                }
              });
              response(persons);
          });
        },
        select: function (event, ui) {
              setValue(patientRelationship.id + "-other_person_uuid", ui.item.data);
              if (ui.item.data) {
                setValue(patientRelationship.id + "-relationship_type",  patientRelationship.relationshipType + "-" + patientRelationship.relationshipDirection);
              } else {
                setValue(patientRelationship.id + "-relationship_type", '');
              }
        },
        change: function (event, ui) {
          if ( !ui.item ) {
            setValue(patientRelationship.id + "-relationship_type", '');
            getInputElementFor(patientRelationship.id + "-field").focus();
          }
        }
    });

    patientRelationship.container.find(".addRelationship").on("click", function() {

      let otherPersonName = $('#' + patientRelationship.id + '-field').val();
      let otherPersonUuid = $('#' + patientRelationship.id + '-other_person_uuid').val();
      if (otherPersonName && otherPersonUuid) {
        $('<div>' +
          '<p><input type="hidden" name="relationship_type" class="rel_type" ' +
          'value="' + patientRelationship.relationshipType + '-' + patientRelationship.relationshipDirection
          + '" data-display-value="-"></p>'
          + '<p><input type="text" value="' + otherPersonName + '" data-display-value="' + otherPersonName + '" size="40" readonly>' +
          '<input type="hidden" name="other_person_uuid" value="' + otherPersonUuid + '">'
          + '<a class="removeRelationship" style="padding-left: 20px;"><i class="icon-minus-sign edit-action"></i></a>'
          + '</p>'
          + '</div>').insertBefore('#' + patientRelationship.id + '-fields-div');

        $(".removeRelationship").click(function(event) {
          event.stopImmediatePropagation();
          $(this).closest('div').remove();
          delete selectedPersons[otherPersonUuid];
          updatePersonNames();
        });

        setValue(patientRelationship.id + "-relationship_type", '');
        setValue(patientRelationship.id + "-field", '');
        setValue(patientRelationship.id + "-other_person_uuid", '');
        selectedPersons[otherPersonUuid] = otherPersonName;
        updatePersonNames();
      }
    });

}
