<%
    def patient = config.patient
    def appId  = config.appId

    def returnUrl = "/${contextPath}/registrationapp/registrationSummary.page?patientId=${patient.patient.id}"


%>
<div class="info-section">
    <div class="info-header">
        <i class="icon-home"></i>
        <h3>${ ui.message("coreapps.patientDashBoard.contactinfo").toUpperCase() }</h3>
        <i class="icon-pencil edit-action right" title="${ ui.message("coreapps.edit") }" onclick="location.href='${ui.pageLink("registrationapp", "editPatientContactInfo", [patientId: patient.patient.id, appId: appId, returnUrl: returnUrl])}#visits';"></i>

    </div>
    <div class="info-body">
        <ul>
            <li class="clear">
                <strong>${ ui.message("coreapps.person.address")}: </strong><br />
                <div class="left-margin">
                    ${ ui.format(config.patient.personAddress).replace("\n", "<br />") }
                </div>
            </li>
            <li class="clear">
                <strong>${ ui.message("coreapps.person.telephoneNumber")}: </strong>
                    <span class="left-margin">
                        ${config.patient.telephoneNumber ?: ''}
                    </span>
            </li>
        </ul>
    </div>
</div>