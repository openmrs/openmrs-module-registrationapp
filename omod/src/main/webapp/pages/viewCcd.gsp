<%
    if (sessionContext.authenticated && !sessionContext.currentProvider) {
        throw new IllegalStateException("Logged-in user is not a Provider")
    }

    def breadcrumbMiddle = breadcrumbOverride ?: '';

    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("registrationapp", "ccd.css")

%>

<script type="text/javascript">

    var breadcrumbs = _.compact(_.flatten([
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
 ,      ${ breadcrumbMiddle },
        { label: "${ ui.escapeJs(ui.format(patient.patient)) }" ,
            link: '${ui.pageLink("registrationapp", "viewCcd", [patientId: patient.patient.id, appId: appId])}'}
    ]))

    var patient = { id: ${ patient.id } };

</script>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient.patient, activeVisit: null, appContextModel: null ]) }

<div id="ccdDisplay">${ ccdHtml }</div>
