
angular.module('openmrs-module-registrationapp-fingerprint-field', ['ngDialog', 'pascalprecht.translate', 'openmrs-module-registrationapp-fingerprint-service'])

    .config(function ($translateProvider) {
        $translateProvider
            .useUrlLoader('/' +  OPENMRS_CONTEXT_PATH + '/module/uicommons/messages/messages.json',  {
                queryParameter : 'localeKey'
            })
            .useSanitizeValueStrategy('escape');
    })

    .controller('FingerprintScanningController', ['$scope', '$interval', 'FingerprintService', 'ngDialog', '$translate', '$filter',

        function($scope, $interval, FingerprintService, ngDialog, $translate, $filter) {

            $scope.refreshScannerStatus = function() {
                $scope.refreshingScannerStatus = true;
                FingerprintService.getScannerStatus($scope.config).then(function (scannerStatus) {
                    $scope.scannerStatus = scannerStatus;
                    if (scannerStatus.scanners.length > 0 && !$scope.selectedScanner) {
                        $scope.selectedScanner = scannerStatus.scanners[0];
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
                $scope.fingersToScan.forEach(function(finger) {
                    biometricData += "&" + finger.formFieldName + "=" + encodeURIComponent($scope.scannedData[finger.index].template);
                });
                getBiometricMatches(biometricData);
            }

            $scope.scanFinger = function(finger) {
                $scope.scanningFingerInProgress = true;
                $scope.scannedData[finger.index] = {"currentlyScanning": true, "buttonLabel": "registrationapp.biometrics.scanning"};
                FingerprintService.scanFinger($scope.selectedScanner, finger, $scope.config).then(function(data) {
                    $scope.scanningFingerInProgress = false;
                    data.currentlyScanning = false;
                    if (data.template) {
                        data.buttonLabel = "registrationapp.biometrics.rescan";
                        //NavigatorController.stepForward(); // TODO: Decide if and when we want to do this
                    }
                    else {
                        data.buttonLabel = "registrationapp.biometrics.scan";
                    }
                    $scope.scannedData[finger.index] = data;
                    $scope.updateBiometricMatches();
                });
            };

            $scope.init = function(config, locale) {
                $translate.use(locale);

                $scope.config = config;
                $scope.refreshScannerStatus();
                $scope.refreshEngineStatus();

                $scope.fingersToScan = [];
                $scope.scannedData = [];
                $scope.config.fingers.forEach(function(finger, index) {
                    finger.index = index;
                    $scope.fingersToScan[index] = finger;
                    $scope.scannedData[index] = {"currentlyScanning": false, "buttonLabel": "registrationapp.biometrics.scan"};
                });
            }
        }

    ]);

