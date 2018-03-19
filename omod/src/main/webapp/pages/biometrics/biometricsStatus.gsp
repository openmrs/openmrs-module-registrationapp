<%
    ui.includeJavascript("uicommons", "angular.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-common.js")
    ui.includeJavascript("uicommons", "angular-app.js")
    ui.includeJavascript("uicommons", "angular-translate.min.js")
    ui.includeJavascript("uicommons", "angular-translate-loader-url.min.js")
    ui.includeJavascript("registrationapp", "biometrics/fingerprintStatusController.js")
    ui.includeJavascript("registrationapp", "biometrics/fingerprintService.js")

    ui.decorateWith("appui", "standardEmrPage")

%>

<script type="text/javascript">

    // TODO fix
    var breadcrumbs = _.compact(_.flatten([
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("coreapps.app.systemAdministration.label")}", link: '${ui.pageLink("coreapps", "systemadministration/systemAdministration")}' },
        { label: '${ ui.message("registrationapp.biometrics.status") }' }
    ]))



</script>


<div id="fingerprint-status"
        ng-controller="FingerprintStatusController"
        ng-init='init(${ ui.toJson(config) }, "${ ui.locale }")'>

    <div>
        <h3>${ ui.message('registrationapp.biometrics.scannerStatus') }</h3>
        <p>{{ scannerName }} {{ scannerStatusMessage | translate }}</p>
    </div>



</div>

<script>
    angular.bootstrap("#fingerprint-status", ["openmrs-module-registrationapp-fingerprint-status"])
</script>

