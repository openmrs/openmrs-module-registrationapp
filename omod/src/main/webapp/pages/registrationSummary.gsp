<%
    if (sessionContext.authenticated && !sessionContext.currentProvider) {
        throw new IllegalStateException("Logged-in user is not a Provider")
    }
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("registrationapp", "summary.css")

%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.format(patient.patient.familyName) }, ${ ui.format(patient.patient.givenName) }" ,
            link: '${ui.pageLink("registrationapp", "registrationSummary", [patientId: patient.patient.id])}'}
    ]
    var patient = { id: ${ patient.id } };
</script>


${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient.patient, activeVisit: null, appContextModel: null ]) }

<div class="clear"></div>
<div class="container">
    <div class="dashboard clear">
        <div class="info-container column">
            <% if (registrationFragments) {
                registrationFragments.each { %>
                    ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, [patientId: patient.patient.id, app: it.appId ])}
            <% }
            } %>
            ${ ui.includeFragment("registrationapp", "summary/demographics", [patient: patient, appId: "registrationapp.registerPatient"]) }
        </div>

        <div class="info-container column">
            ${ ui.includeFragment("registrationapp", "summary/contactInfo", [patient: patient, appId: "registrationapp.registerPatient"]) }
        </div>
    </div>
</div>