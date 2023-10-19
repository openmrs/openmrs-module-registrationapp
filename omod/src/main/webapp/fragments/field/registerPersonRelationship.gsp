<%
    ui.includeJavascript("registrationapp", "field/registerPersonRelationship.js")
    ui.includeCss("registrationapp","registerPatient.css")

    def multipleValues = config.multipleValues ? config.multipleValues : false
%>

<div id="${ config.id }-container">
    <p>
        <input type="hidden" id="${ config.id }-relationship_type" name="relationship_type" class="rel_type" value="" data-display-value="-"/>
    </p>
    <p>
        <label for="${ config.id }-field">
            ${ config.label } <% if (config.classes && config.classes.join(' ').contains("required")) { %><span>(${ ui.message("emr.formValidation.messages.requiredField.label") })</span><% } %>
        </label>

        <input type="text"  id="${ config.id }-field" class="searchablePerson" size="50" placeholder="${ui.message(ui.encodeHtmlAttribute('registrationapp.person.name'))}"/>
        <input type="hidden" name="other_person_uuid" id="${ config.id }-other_person_uuid"/>
    </p>
</div>

<script type="text/javascript">
  var patientRelationship = {
    id: null,
    relationshipType: null,
    relationshipDirection: null,
    container: null,
    multipleValues: null,
    gender: null
  }

  patientRelationship.id = '${ config.id }';
  patientRelationship.gender = '${ config.gender ? config.gender : null }';
  patientRelationship.multipleValues = '${ multipleValues }';
  patientRelationship.relationshipType = '${ config.relationshipType }';
  patientRelationship.relationshipDirection = '${ config.relationshipDirection }';
  patientRelationship.container = jq('#${ config.id }-container');

  RegisterPatientRelationship(patientRelationship);
</script>
