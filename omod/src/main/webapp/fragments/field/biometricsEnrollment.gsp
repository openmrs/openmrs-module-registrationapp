<%
    ui.includeJavascript("uicommons", "angular.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-common.js")
    ui.includeJavascript("uicommons", "angular-app.js")
    ui.includeJavascript("uicommons", "ngDialog/ngDialog.js")
    ui.includeCss("uicommons", "ngDialog/ngDialog.min.css")
    ui.includeJavascript("registrationapp", "field/biometrics.js")
    ui.includeJavascript("uicommons", "handlebars/handlebars.js")
%>

<style>
    .biometric-status { color:red; padding: 20px; text-align: center; }
    .service-enabled-section { padding-top: 10px; }
    .biometric-section { padding: 20px; }
    .fingerprint-capture-section { display: inline-block; }
    .finger-label {font-weight: bold; padding-bottom: 5px; text-align: center;}
    .biometrics-enrollment-field {display:none;}
</style>

<div id="fingerprint-enrollment-section" ng-controller="FingerprintScanningController" ng-init='init(${ ui.toJson(config) })'>

    <div class="biometric-status" ng-show="scannerInfo.statusMessage">{{ scannerInfo.statusMessage }}</div>

    <div class="scanner-enabled-section" ng-show="scannerInfo.enabled">
        <b>Scanner: </b>
        <span ng-show="scannerInfo.scanners.length === 0">
            None Found
        </span>
        <span ng-show="scannerInfo.scanners.length > 0">
            <select ng-options="scanner as scanner.displayName for scanner in scannerInfo.scanners track by scanner.id" ng-model="selectedScanner"></select>
        </span>
        <button class="task" style="min-width: 30px;" type="button" ng-click="refreshScannerInfo()" ng-disabled="refreshingScannerInfo">
            <i class="icon-refresh"></i>
        </button>

        <i ng-show="refreshingScannerInfo" class="icon-spinner icon-spin"></i>
    </div>

    <div class="biometric-status" ng-show="serverStatus.statusMessage">{{ serverStatus.statusMessage }}</div>

    <div class="service-enabled-section" ng-show="serverStatus.enabled && scannerInfo.enabled && scannerInfo.scanners.length > 0">

        <div class="biometric-section fingerprint-capture-section" ng-repeat="finger in fingersToScan track by finger.type">
            <div class="finger-label">{{finger.label}}</div>
            <img data-ng-src="data:image/PNG;base64,{{ scannedData[finger.type].image }}" width="120" height="120"><br/>
            <button class="scan-finger-button task" style="min-width: 120px;" type="button" ng-click="scanFinger(finger)" ng-disabled="scanningFingerInProgress">
                {{scannedData[finger.type].buttonLabel}}
            </button>
        </div>

        <div class="biometric-section fingerprint-match-section">

            <button class="match-finger-button task" style="min-width: 120px;" type="button" ng-click="matchFingers()" ng-disabled="matchingFingersInProgress">
                Search for matching fingerprints
            </button>

            <div ng-show="!matchingFingersInProgress">
                <div ng-show="matchesFound.length == 0">
                    No matches Found
                </div>
                <div ng-show="matchesFound.length > 0">
                    <br/>
                    <div ng-repeat="match in matchesFound">
                        <b>{{ match.display }}</b>
                    </div>
                </div>
            </div>

        </div>

        <div class="biometric-section fingerprint-enroll-section">

            <button class="enroll-finger-button task" style="min-width: 120px;" type="button" ng-click="enrollTemplate()" ng-disabled="enrollingTemplateInProgress || enrolledTemplate">
                Enroll fingerprints
            </button>

        </div>

        <p id="biometrics-enrollment-field" class="biometrics-enrollment-field">
            <input id="biometrics-enrollment" type="text" size="40" name="{{ config.formFieldName }}" value="{{enrolledTemplate.subjectId}}"/>
        </p>

    </div>

</div>

<script>
    angular.bootstrap("#fingerprint-enrollment-section", ["openmrs-module-registrationapp-biometrics"])
</script>