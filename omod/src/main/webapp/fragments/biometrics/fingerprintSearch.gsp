<%
    ui.includeJavascript("uicommons", "angular.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-common.js")
    ui.includeJavascript("uicommons", "angular-app.js")
    ui.includeJavascript("uicommons", "angular-translate.min.js")
    ui.includeJavascript("uicommons", "angular-translate-loader-url.min.js")
    ui.includeJavascript("registrationapp", "biometrics/fingerprintSearchController.js")
    ui.includeJavascript("registrationapp", "biometrics/fingerprintService.js")
%>

<div id="fingerprint-search"
        ng-controller="FingerprintSearchController"
        ng-init='init(${ ui.toJson(config) }, "${ ui.locale }")'>
    <!-- TODO: use a style -->
    <span style="color: red">{{ errorMessage | translate }}</span>
</div>

<script>
    angular.bootstrap("#fingerprint-search", ["openmrs-module-registrationapp-fingerprint-search"])
</script>