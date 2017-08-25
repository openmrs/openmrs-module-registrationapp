
<div class="info-section">
    <div class="info-header">
        <i class="icon-user"></i>
        <h3>${ ui.message('registrationapp.biometrics.summary').toUpperCase() }</h3>
        <i id="biometrics-edit-link" class="icon-pencil edit-action right" title="${ ui.message("coreapps.edit") }"
           onclick="location.href='${ui.pageLink("registrationapp", "biometrics/editBiometrics", [patientId: patient.patient.id, registrationAppId: registrationAppId ])}'"></i>
    </div>
    <div class="info-body summary-section">
        ${ ui.message(status) }

        <ul>
            <% if (identifierToSubjectMap) {
                identifierToSubjectMap.each { identifier, subject ->
                    def fingers = []
                    subject.fingerprints.each {
                        fingers.push(ui.message('registrationapp.biometrics.' + it.type) != 'registrationapp.biometrics.' + it.type ? ui.message('registrationapp.biometrics.' + it.type) : it.type)
                    }
            %>
                    <li>
                        ${ fingers.join(", ") }
                        <div class="tag">${ ui.format(identifier.dateCreated) }</div>
                    </li>
            <% }
            } %>
        </ul>
    </div>
</div>
