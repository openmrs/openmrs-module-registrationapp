<%
    if (sessionContext.authenticated && !sessionContext.currentProvider) {
        throw new IllegalStateException("Logged-in user is not a Provider")
    }
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("registrationapp", "summary.css")

    def returnUrl = "/${contextPath}/registrationapp/registrationSummary.page?patientId=${patient.patient.id}"

%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.escapeJs(ui.format(patient.patient)) }" ,
            link: '${ui.pageLink("registrationapp", "registrationSummary", [patientId: patient.patient.id])}'}
    ]
    var patient = { id: ${ patient.id } };
</script>


${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient.patient, activeVisit: null, appContextModel: null ]) }

<div class="clear"></div>
<div class="container">
    <div class="dashboard clear">
        <div class="info-container column">
            ${ ui.includeFragment("registrationapp", "summary/demographics", [patient: patient, appId: "registrationapp.registerPatient"]) }

            <% if (registrationFragments) {
                registrationFragments.each { %>
                    ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, [patientId: patient.patient.id, app: it.appId, returnUrl: returnUrl ])}
            <% }
            } %>
        </div>

        <div class="info-container column">
            ${ ui.includeFragment("registrationapp", "summary/contactInfo", [patient: patient, appId: "registrationapp.registerPatient"]) }
        </div>
    </div>
</div>