<%
    def patient = config.patient
    def appId  = config.appId

    def returnUrl = "/${contextPath}/registrationapp/registrationSummary.page?patientId=${patient.patient.id}"


%>
<div class="info-section">
    <div class="info-header">
        <i class="icon-user"></i>
        <h3>${ (section.id == 'demographics' ? ui.message("registrationapp.patient.demographics.label") : ui.message(section.label)).toUpperCase() }</h3>
        <i class="icon-pencil edit-action right" title="${ ui.message("coreapps.edit") }"
           onclick="location.href='${ui.pageLink("registrationapp", "editSection", [patientId: patient.patient.id, returnUrl: returnUrl, sectionId: section.id, appId: appId ])}#visits';"></i>

    </div>
    <div class="info-body">

        <ul>
            <!-- display basic baked-in demographics information if demographics section -->
            <% if (section.id == 'demographics') { %>
                <% nameTemplate.lines.each { line ->
                    // go through each line in the template and find the first name token; assumption is there is only one name token per line
                    def name = line.find({it['isToken'] == 'IS_NAME_TOKEN'})['codeName'];
                    def initialNameFieldValue = ""
                    if(patient.personName && patient.personName[name]){
                        initialNameFieldValue = patient.personName[name]
                    }
                %>
                <li class="clear">
                    <strong>${ ui.message(nameTemplate.nameMappings[name]) }: </strong>
                    <div class="summary-tag">
                        ${ ui.message(initialNameFieldValue) }
                    </div>
                </li>
                <% } %>

                <li class="clear">
                    <strong>${ ui.message("emr.gender") }:</strong>
                    <div class="summary-tag">
                        <span>${ui.message("coreapps.gender." + patient.gender)}&nbsp;</span>
                    </div>
                </li>

                <li class="clear">
                    <strong>${ui.message("registrationapp.patient.birthdate.label")}:</strong>
                    <div class="summary-tag">
                        <span>
                            <% if (patient.birthdate) { %>
                            <% if (patient.birthdateEstimated) { %>~<% } %>${ ui.formatDatePretty(patient.birthdate) }
                            <% } else { %>
                            ${ui.message("coreapps.unknownAge")}
                            <% } %>
                        </span>
                    </div>
                </li>
            <% } %>

            <!-- display other fields -->
            <% section.questions.each { question ->
                // TODO do we want to display any labels for questions?
                def fields = question.fields %>
                <li class="clear">
                    <strong>${ ui.message(question.legend) }:</strong>
                    <div class="summary-tag">
                        <span>
                        <% fields.each { field ->
                            def displayValue = "";
                            if (field.type == 'personAttribute') {
                                displayValue = uiUtils.getAttribute(patient.patient, field.uuid);
                            }
                            else if (field.type == 'personAddress') {
                                displayValue = ui.format(config.patient.personAddress).replace("\n", "<br />");
                            }
                            // TODO support other types besides personAttribute and personAddress
                        %>
                            ${ displayValue }&nbsp;
                        <% } %>
                        </span>
                    </div>
                </li>
            <% } %>
        </ul>
    </div>
</div>