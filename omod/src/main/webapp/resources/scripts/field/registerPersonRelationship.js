function RegisterPatientRelationship(patientRelationship) {

  let selectedPersons = [];

  var relationshipExitHandler = {
    handleExit: function(field) {
                  let fieldId = patientRelationship.id + "-field";
                  let element = getInputElementFor(fieldId);
                  // clear any previous errors
                  $(element).next(".field-error").remove();
                  if (!field.value()) {
                    return true;
                  } else {
                    let selectedVal = getValue(patientRelationship.id + "-relationship_type");
                    if ( !selectedVal ) {
                      // no person from the search results list was selected
                      $('<span class="field-error" style="">Invalid entry</span>').insertAfter('#' + fieldId);
                      return false;
                    } else {
                      return true;
                    }
                  }
                  return false;
                }
  }
  ExitHandlers['relationships-' + patientRelationship.id] = relationshipExitHandler;
  //returns Date in the String format YYYY-MM-DD
  function formatDate(inputDate) {
    if (inputDate) {
      if ( inputDate.length > 23 ) {
        //remove the timezone suffix
        inputDate = inputDate.substring(0,23);
      }
      let date = new Date(inputDate);
      let year = date.toLocaleString("default", { year: "numeric" });
      let month = date.toLocaleString("default", { month: "short" });
      let day = date.toLocaleString("default", { day: "2-digit" });
      return year + "-" + month + "-" + day;
    }
    return '';
  }
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

  $('#' + patientRelationship.id + '-field').autocomplete({
        autoFocus: true,
        source: function (request, response) {
          if (request.term.length < 3) {
            return;
          }
          let url = '/' + OPENMRS_CONTEXT_PATH + '/ws/rest/v1/patient/';
          $.getJSON(url, {
            limit: 10,
            q: request.term,
            v: 'custom:(id,uuid,display,gender,age,person:(birthdate))'
          }, function (result) {
              let items = null;
              if ( patientRelationship.gender && patientRelationship.gender != 'null') {
                items = _.filter(result.results, function(person) {
                  return (person.gender === patientRelationship.gender);
                });
              }
              let persons = _.map( items != null ? items : result.results, function (item) {
                return {
                  label: item.display + ", " + item.age + ", " + item.gender + ", " +  formatDate(item.person.birthdate),
                  data: item.uuid
                }
              });
              response(persons);
          });
        },
        select: function (event, ui) {
              setValue(patientRelationship.id + "-other_person_uuid", ui.item.data);
              $(getInputElementFor(patientRelationship.id + "-field")).next(".field-error").remove();
              if (ui.item.data) {
                setValue(patientRelationship.id + "-relationship_type",  patientRelationship.relationshipType + "-" + patientRelationship.relationshipDirection);
              } else {
                setValue(patientRelationship.id + "-relationship_type", '');
              }
        },
        change: function (event, ui) {
          let fieldId = patientRelationship.id + "-field";
          let element = getInputElementFor(fieldId);
          if ( !ui.item ) {
            setValue(patientRelationship.id + "-relationship_type", '');
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
