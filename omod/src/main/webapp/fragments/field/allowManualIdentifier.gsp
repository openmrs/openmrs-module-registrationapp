<%
    config.require("identifierTypeName")
%>
<fieldset id="patient-identifier-question">
    <legend id="patientIdentifierLabel">${config.identifierTypeName}</legend>
    <h3>${ui.message("registrationapp.patient.identifier.question", config.identifierTypeName)}</h3>

    <p>
        <input id="checkbox-autogenerate-identifier" type="checkbox" checked/>
        <label for="checkbox-autogenerate-identifier">${ui.message("registrationapp.patient.identifier.autogenerate.label")}</label>
    </p>

    <p>
        <label for="patient-identifier">${ui.message("registrationapp.patient.identifier.label")}</label>
        <input id="patient-identifier" name="patientIdentifier"/>
    </p>
</fieldset>