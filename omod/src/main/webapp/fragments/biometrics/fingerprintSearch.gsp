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

<button id="fingerprint-search-icon"
        class="scan-finger-button"
        type="button"
        ng-controller="FingerprintSearchController"
        ng-click="scanFinger()"
        ng-init='init(${ ui.toJson(config) }, "${ ui.locale }")'
        ng-show="scannerStatus.enabled && engineStatus.enabled"
        ng-disabled="scanningFingerInProgress">
    {{ buttonLabel | translate }}
</button>


<script>
    angular.bootstrap("#fingerprint-search-icon", ["openmrs-module-registrationapp-fingerprint-search"])
</script>