<%
    config.require("identifierTypeName")

    // the identifier type's format is validated in the browser by the patient-identifier-format validator
    def identifierFormat = config.identifierFormat
    def identifierFormatMessage = identifierFormat ?
            ui.message("registrationapp.error.identifier.invalidFormat", config.identifierFormatDescription ?: identifierFormat) : null
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
        <% if (identifierFormat) { %>
            <input id="patient-identifier" name="patientIdentifier" class="patient-identifier-format"
                   data-identifier-format="${ui.escapeAttribute(identifierFormat)}"
                   data-identifier-format-message="${ui.escapeAttribute(identifierFormatMessage)}"/>
        <% } else { %>
            <input id="patient-identifier" name="patientIdentifier"/>
        <% } %>
        ${ ui.includeFragment("uicommons", "fieldErrors", [ fieldName: "patientIdentifier" ]) }
    </p>
</fieldset>
