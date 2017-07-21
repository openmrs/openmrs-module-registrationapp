
angular.module('openmrs-module-registrationapp-fingerprint', ['ngDialog', 'pascalprecht.translate'])

    .config(function ($translateProvider) {
        $translateProvider
            .useUrlLoader('/' +  OPENMRS_CONTEXT_PATH + '/module/uicommons/messages/messages.json',  {
                queryParameter : 'localeKey'
            })
            .useSanitizeValueStrategy('escape');
    })

    .service('FingerprintScanningService', ['$q', '$http', '$translate', '$filter',

        function($q, $http, $translate, $filter) {

            this.getEngineStatus = function() {
                var url = "/" + OPENMRS_CONTEXT_PATH + "/ws/rest/v1/registrationcore/biometrics/enginestatus";
                return $http.get(url, {}).then(function (response) {
                    if (response.status === 200) {
                        return response.data;
                    }
                    else {
                        return {
                            "enabled": false,
                            "statusMessage": "registrationapp.biometrics.errorRetrievingServerStatus",
                            "errorDetails": response.status
                        };
                    }
                }, function (error) {
                    return {
                        "enabled": false,
                        "statusMessage": "registrationapp.biometrics.errorRetrievingServerStatus"
                    };
                });
            };

            this.getScannerStatus = function (config) {
                return $http.get(config.devicesUrl, {}).then(
                    function (response) {
                        if (response.status === 200) {
                           var scannerStatus = {};
                           scannerStatus.enabled = true;
                           scannerStatus.scanners = response.data;
                           scannerStatus.statusMessage = '';
                           return scannerStatus;
                        }
                        else {
                            var scannerStatus = {};
                            scannerStatus.enabled = true;
                            scannerStatus.scanners = [];
                            scannerStatus.statusMessage = "registrationapp.biometrics.unableToRetrieveScanners";
                            scannerStatus.errorDetails = response.status;
                            return scannerStatus;
                        }
                    },
                    function (error) {
                        var scannerStatus = {};
                        scannerStatus.enabled = false;
                        scannerStatus.scanners = [];
                        scannerStatus.statusMessage = "registrationapp.biometrics.unableToRetrieveScanners";
                        scannerStatus.errorDetails = "registrationapp.biometrics.isScanningServiceRunning";
                        return scannerStatus;
                    }
                );
            };

            this.scanFinger = function(scanner, finger, config) {
                return $http.get(config.scanUrl, {"params" : {"deviceId": scanner.id, "type": finger.type, "format": finger.format}}).then(function(response) {
                    if (response.status === 200) {
                        var data = response.data;
                        data.type = data.type || finger.type;
                        data.format = data.format || finger.format;
                        return data;
                    }
                    else {
                        console.log('Error scanning fingerprint: ' + response.status);
                        return {};
                    }
                }, function (error) {
                    console.log("Unable to connect to " + config.scanUrl + ". Please ensure this is running.");
                    return {};
                });
            };

        }
    ])

    .controller('FingerprintScanningController', ['$scope', '$interval', 'FingerprintScanningService', 'ngDialog', '$translate', '$filter',

        function($scope, $interval, FingerprintScanningService, ngDialog, $translate, $filter) {

            $scope.refreshScannerStatus = function() {
                $scope.refreshingScannerStatus = true;
                FingerprintScanningService.getScannerStatus($scope.config).then(function (scannerStatus) {
                    $scope.scannerStatus = scannerStatus;
                    if (scannerStatus.scanners.length > 0 && !$scope.selectedScanner) {
                        $scope.selectedScanner = scannerStatus.scanners[0];
                    }
                    $scope.refreshingScannerStatus = false;
                });
            };

            $scope.refreshEngineStatus = function() {
                $scope.refreshingEngineStatus = true;
                FingerprintScanningService.getEngineStatus().then(function (engineStatus) {
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
                FingerprintScanningService.scanFinger($scope.selectedScanner, finger, $scope.config).then(function(data) {
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

