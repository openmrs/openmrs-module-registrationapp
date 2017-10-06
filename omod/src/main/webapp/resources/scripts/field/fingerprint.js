
angular.module('openmrs-module-registrationapp-fingerprint-field', ['ngDialog', 'pascalprecht.translate', 'openmrs-module-registrationapp-fingerprint-service'])

    .constant('errorMessages', {
        'SERVICE_NOT_RUNNING': 'registrationapp.biometrics.serviceNotRunning',
        'SERVICE_NOT_ENABLED': 'registrationapp.biometrics.serviceNotEnabled',
        'DEVICE_NOT_FOUND': 'registrationapp.biometrics.scannerNotFound',
        'UNKNOWN_ERROR': 'registrationapp.biometrics.scannerError',
        'BAD_SCAN': 'registrationapp.biometrics.badscan'
    })

    .config(function ($translateProvider) {
        $translateProvider
            .useUrlLoader('/' +  OPENMRS_CONTEXT_PATH + '/module/uicommons/messages/messages.json',  {
                queryParameter : 'localeKey'
            })
            .useSanitizeValueStrategy('escape');
    })

    .controller('FingerprintScanningController', ['$scope', '$interval', 'FingerprintService', 'ngDialog', '$translate', '$filter', 'errorMessages',

        function($scope, $interval, FingerprintService, ngDialog, $translate, $filter, errorMessages) {

            $scope.refreshScannerStatus = function() {
                $scope.refreshingScannerStatus = true;
                FingerprintService.getScannerStatus($scope.config).then(function (scannerStatus) {
                    $scope.scannerStatus = scannerStatus;

                    // currently we don't support selecting multiple scanners, s
                    /* if (scannerStatus.scanners.length > 0 && !$scope.selectedScanner) {
                        $scope.selectedScanner = scannerStatus.scanners[0];
                    }*/
                    if ($scope.scannerStatus.status === 'OK') {
                        $scope.scannerStatus.statusMessage = "";
                        $scope.scannerStatus.errorDetails = "";
                    }
                    else {
                        $scope.scannerStatus.statusMessage = "registrationapp.biometrics.unableToRetrieveScanners";
                        if ($scope.scannerStatus.status in errorMessages) {
                            $scope.scannerStatus.errorDetails = errorMessages[$scope.scannerStatus.status]
                        }
                    }

                    $scope.refreshingScannerStatus = false;
                });
            };

            $scope.refreshEngineStatus = function() {
                $scope.refreshingEngineStatus = true;
                FingerprintService.getEngineStatus().then(function (engineStatus) {
                    $scope.engineStatus = engineStatus.results;
                    $scope.refreshingEngineStatus = false;
                });
            };

            // TODO: This seems hacky and should be improved
            $scope.updateBiometricMatches = function() {
                var biometricData = ""
                $scope.fingersToScan.forEach(function (finger) {
                    if ($scope.scannedData[finger.index].template) {
                        biometricData += "&" + finger.formFieldName + "=" + encodeURIComponent($scope.scannedData[finger.index].template);
                    }
                });

                // this depends on getBiometricMatches being available--it's defined in global scope in registerPatient.js
                // it will *not* we available in the 'editSection' use case
                if (biometricData && angular.isDefined(window.getBiometricMatches)) {
                    getBiometricMatches(biometricData);
                }
            }

            $scope.scanFinger = function(finger) {

                // if we are currently in a scan, the scan finger button acts as a cancel button
                if ($scope.scanningFingerInProgress) {
                    $scope.cancelScan = true;
                }
                else {
                    $scope.scanningFingerInProgress = true;
                    $scope.cancelScan = false;
                    $scope.scanErrorDetails = "";
                    $scope.scannedData[finger.index] = {};
                    $scope.scanStatus[finger.index] = {
                        "currentlyScanning": true,
                        "buttonLabel": "registrationapp.biometrics.cancel"
                    };
                    scanFingerHelper(finger);
                }
            };

            var scanFingerHelper = function(finger) {
                FingerprintService.scanFinger(finger, $scope.config).then(function (data) {

                    if (data.status in errorMessages) {
                        stopScan(finger);
                        $scope.scanErrorDetails = errorMessages[data.status]

                    }
                    else if (data.template) {
                        stopScan(finger);
                        $scope.scannedData[finger.index] = data;
                        $scope.updateBiometricMatches();
                        //NavigatorController.stepForward(); // TODO: Decide if and when we want to do this
                    }
                    else if ($scope.cancelScan) {
                        stopScan(finger);
                    }
                    else {
                      scanFingerHelper(finger);
                    }

                });
            }

            var stopScan = function (finger) {
                $scope.scanningFingerInProgress = false;
                $scope.scanStatus[finger.index] = {
                    currentlyScanning: false,
                    buttonLabel: "registrationapp.biometrics.rescan"
                }
            }

            $scope.init = function(config, locale) {
                $translate.use(locale);

                $scope.config = config;
                $scope.refreshScannerStatus();
                $scope.refreshEngineStatus();

                $scope.fingersToScan = [];
                $scope.scannedData = [];
                $scope.scanStatus = [];

                $scope.config.fingers.forEach(function(finger, index) {
                    finger.index = index;
                    $scope.fingersToScan[index] = finger;
                    $scope.scanStatus[index] = {"currentlyScanning": false, "buttonLabel": "registrationapp.biometrics.scan"};
                    $scope.scannedData[index] = {};
                });
            }
        }

    ]);

