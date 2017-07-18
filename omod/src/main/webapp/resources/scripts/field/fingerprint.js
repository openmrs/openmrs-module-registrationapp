
angular.module('openmrs-module-registrationapp-fingerprint', ['ngDialog'])

    .service('FingerprintScanningService', ['$q', '$http',

        function($q, $http) {

            this.getEngineStatus = function() {
                var url = "/" + OPENMRS_CONTEXT_PATH + "/ws/rest/v1/registrationcore/biometrics/enginestatus";
                return $http.get(url, {}).then(function (response) {
                    if (response.status === 200) {
                        return response.data;
                    }
                    else {
                        return {
                            "enabled": false,
                            "statusMessage": "Error retrieving server status: " + response.status
                        };
                    }
                }, function (error) {
                    return {
                        "enabled": false,
                        "statusMessage": "Error retrieving server status: " + error
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
                            scannerStatus.statusMessage = "Unable to retrieve available scanners: " + response.status;
                            return scannerStatus;
                        }
                    },
                    function (error) {
                        var scannerStatus = {};
                        scannerStatus.enabled = false;
                        scannerStatus.scanners = [];
                        scannerStatus.statusMessage = "Unable to retrieve available scanners.  Is the scanning service started?";
                        return scannerStatus;
                    }
                );
            };

            this.scanFinger = function(scanner, finger, config) {
                return $http.get(config.scanUrl, {"params" : {"deviceId": scanner.id, "type": finger.type, "format": finger.format}}).then(function(response) {
                    if (response.status === 200) {
                        return response.data;
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

    .controller('FingerprintScanningController', ['$scope', '$interval', 'FingerprintScanningService', 'ngDialog',

        function($scope, $interval, FingerprintScanningService, ngDialog) {

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

            $scope.scanFinger = function(finger) {
                $scope.scanningFingerInProgress = true;
                $scope.scannedData[finger.index] = {"currentlyScanning": true, "buttonLabel": "Scanning"};
                FingerprintScanningService.scanFinger($scope.selectedScanner, finger, $scope.config).then(function(data) {
                    $scope.scanningFingerInProgress = false;
                    data.currentlyScanning = false;
                    data.buttonLabel = "Re-Scan";
                    $scope.scannedData[finger.index] = data;
                    NavigatorController.stepForward();
                    getBiometricMatches();  // TODO: This should be done better.
                });
            };

            $scope.init = function(config) {
                $scope.config = config;
                $scope.refreshScannerStatus();
                $scope.refreshEngineStatus();

                $scope.fingersToScan = [];
                $scope.scannedData = [];
                $scope.config.fingers.forEach(function(finger, index) {
                    finger.index = index;
                    $scope.fingersToScan[index] = finger;
                    $scope.scannedData[index] = {"currentlyScanning": false, "buttonLabel": "Scan"};
                });
            }
        }

    ]);

