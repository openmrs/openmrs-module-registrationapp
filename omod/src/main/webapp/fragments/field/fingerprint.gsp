<%
    ui.includeJavascript("uicommons", "angular.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-common.js")
    ui.includeJavascript("uicommons", "angular-app.js")
    ui.includeJavascript("uicommons", "ngDialog/ngDialog.js")
    ui.includeCss("uicommons", "ngDialog/ngDialog.min.css")
    ui.includeJavascript("registrationapp", "field/fingerprint.js")
    ui.includeJavascript("uicommons", "handlebars/handlebars.js")
%>

<style>
    .fingerprint-status { color:red; padding: 20px; text-align: center; }
    .engine-enabled-section { padding-top: 10px; }
    .fingerprint-section { padding: 20px; }
    .fingerprint-capture-section { display: inline-block; }
    .finger-label {font-weight: bold; padding-bottom: 5px; text-align: center;}
    .fingerprints-field {display:none;}
</style>

<div id="fingerprint-enrollment-section" ng-controller="FingerprintScanningController" ng-init='init(${ ui.toJson(config) })'>

    <div class="fingerprint-status" ng-show="scannerStatus.statusMessage">{{ scannerStatus.statusMessage }}</div>

    <div class="scanner-enabled-section" ng-show="scannerStatus.enabled">
        <b>Scanner: </b>
        <span ng-show="scannerStatus.scanners.length === 0">
            None Found
        </span>
        <span ng-show="scannerStatus.scanners.length > 0">
            <select ng-options="scanner as scanner.displayName for scanner in scannerStatus.scanners track by scanner.id" ng-model="selectedScanner"></select>
        </span>
        <button class="task" style="min-width: 30px;" type="button" ng-click="refreshScannerStatus()" ng-disabled="refreshingScannerStatus">
            <i class="icon-refresh"></i>
        </button>

        <i ng-show="refreshingScannerStatus" class="icon-spinner icon-spin"></i>
    </div>

    <div class="engine-enabled-section" ng-show="engineStatus.enabled && scannerStatus.enabled && scannerStatus.scanners.length > 0">

        <div class="fingerprint-section fingerprint-capture-section" ng-repeat="finger in fingersToScan">
            <div class="finger-label">{{finger.label}}</div>
            <img data-ng-src="data:image/PNG;base64,{{ scannedData[finger.index].image }}" width="120" height="120"><br/>
            <button class="scan-finger-button task" style="min-width: 120px;" type="button" ng-click="scanFinger(finger)" ng-disabled="scanningFingerInProgress">
                {{scannedData[finger.index].buttonLabel}}
            </button>
            <p class="fingerprints-field">
                <input type="text" size="40" name="{{ finger.formFieldName }}" value="{{scannedData[finger.index].template}}"/>
            </p>
        </div>

    </div>

</div>

<script>
    angular.bootstrap("#fingerprint-enrollment-section", ["openmrs-module-registrationapp-fingerprint"])
</script>