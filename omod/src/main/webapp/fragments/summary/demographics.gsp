<%
    def patient = config.patient
    def appId  = config.appId

    def  nameTemplate = config.nameTemplate


    def returnUrl = "/${contextPath}/registrationapp/registrationSummary.page?patientId=${patient.patient.id}"


%>
<div class="info-section">
    <div class="info-header">
        <i class="icon-user"></i>
        <h3>${ ui.message("registrationapp.patient.demographics.label").toUpperCase() }</h3>
        <i class="icon-pencil edit-action right" title="${ ui.message("coreapps.edit") }"
           onclick="location.href='${ui.pageLink("registrationapp", "editSection", [patientId: patient.patient.id, returnUrl: returnUrl, sectionId: 'demographics', appId: appId ])}#visits';"></i>

    </div>
    <div class="info-body">

        <ul>
            <% nameTemplate.lineByLineFormat.each { name ->
                def initialNameFieldValue = ""
                if (patient.personName && patient.personName[name]) {
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
                    <strong>${ ui.message("emr.gender") }</strong>
                    <div class="summary-tag">
                        <span>${ui.message("coreapps.gender." + patient.gender)}&nbsp;</span>
                    </div>
                </li>
                <li class="clear">
                    <strong>${ui.message("registrationapp.patient.birthdate.label")}</strong>
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
        </ul>
    </div>
</div>