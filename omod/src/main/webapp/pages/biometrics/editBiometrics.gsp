<%
    if (sessionContext.authenticated && !sessionContext.currentProvider) {
        throw new IllegalStateException("Logged-in user is not a Provider")
    }

    ui.decorateWith("appui", "standardEmrPage")

%>

<script type="text/javascript">

    <!-- TODO figure out breadcrumbs -->

    var breadcrumbs = _.compact(_.flatten([
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.escapeJs(ui.format(patient)) }" ,
            link: '${ui.pageLink("registrationapp", "registrationSummary", [patientId: patient.id, appId: registrationAppId])}'},
        { label: ${ ui.message("registrationapp.biometrics.edit") }
    ]))

    var patient = { id: ${ patient.id } };

</script>


${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient, activeVisit: null, appContextModel: null ]) }

<div class="info-body summary-section">
    ${ ui.message(status) }

    <table>
        <tr>
            <th>${ ui.message('registrationapp.biometrics.label') }</th>
            <th>${ ui.message('registrationapp.biometrics.dateCollected') }</th>
            <th>&nbsp;</th>
        </tr>

        <% if (identifierToSubjectMap) {
            identifierToSubjectMap.each { identifier, subject ->
                def fingers = []
                subject.fingerprints.each {
                    fingers.push(ui.message('registrationapp.biometrics.' + it.type) != 'registrationapp.biometrics.' + it.type ? ui.message('registrationapp.biometrics.' + it.type) : it.type)
                }
        %>
        <tr>
            <td>${ fingers.join(", ") }</td>
            <td>${ ui.format(identifier.dateCreated) }</td>
            <td><button class="cancel" id="${ identifier.identifier }">${ ui.message('coreapps.delete') }</button></td>
        </tr>
        <% }
        } %>
    </table>
</div>