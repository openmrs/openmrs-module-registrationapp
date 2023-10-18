function RegisterPatientRelationship(patientRelationship) {

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
            let persons = _.map(result.results, function (item) {
              return {
                label: item.display + ", " + item.age + ", " + item.gender,
                data: item.uuid
              }
            });
            response(persons);
          });
        },
        select: function (event, ui) {
              setValue("other_person_uuid", ui.item.data);
        }
    });
}
