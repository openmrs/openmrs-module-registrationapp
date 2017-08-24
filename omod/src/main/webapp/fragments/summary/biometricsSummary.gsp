
<div class="info-section">
    <div class="info-header">
        <i class="icon-user"></i>
        <h3>${ ui.message('registrationapp.biometrics.summary').toUpperCase() }</h3>
        <i id="biometrics-edit-link" class="icon-pencil edit-action right" title="${ ui.message("coreapps.edit") }"
           onclick=""></i>
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
                        <div class="tag">${ ui.formatDatePretty(identifier.dateCreated) }</div>
                    </li>
            <% }
            } %>
        </ul>

    </div>
</div>
