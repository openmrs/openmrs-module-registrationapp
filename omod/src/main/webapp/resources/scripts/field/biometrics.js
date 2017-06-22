
angular.module('openmrs-module-registrationapp-biometrics', ['ngDialog'])

    .service('FingerprintScanningService', ['$q', '$http',

        function($q, $http) {

            var CONFIG = {
                URLS: {
                    GET_AVAILABLE_SCANNERS: "http://localhost:9000/fingerprint/devices",
                    GET_SERVER_STATUS: "http://localhost:9000/status",
                    SCAN_FINGER: "http://localhost:9000/fingerprint/scan",
                    MATCH_TEMPLATE: "http://localhost:9000/match",
                    GENERATE_TEMPLATE_FROM_FINGERS: "http://localhost:9000/fingerprint/template",
                    ENROLL_TEMPLATE: "http://localhost:9000/template",
                    PATIENT_QUERY: "/" + OPENMRS_CONTEXT_PATH + "/ws/rest/v1/patient"
                }
            };

            this.getScannerInfo = function () {
                return $http.get(CONFIG.URLS.GET_AVAILABLE_SCANNERS, {}).then(
                    function (response) {
                        if (response.status === 200) {
                           var scannerInfo = {};
                           scannerInfo.enabled = true;
                           scannerInfo.scanners = response.data;
                           scannerInfo.statusMessage = '';
                           return scannerInfo;
                        }
                        else {
                            var scannerInfo = {};
                            scannerInfo.enabled = true;
                            scannerInfo.scanners = [];
                            scannerInfo.statusMessage = "Unable to retrieve available scanners: " + response.status;
                            return scannerInfo;
                        }
                    },
                    function (error) {
                        var scannerInfo = {};
                        scannerInfo.enabled = false;
                        scannerInfo.scanners = [];
                        scannerInfo.statusMessage = "Unable to retrieve available scanners.  Please check browser console for errors.";
                        return scannerInfo;
                    }
                );
            };

            this.getServerStatus = function() {
                return $http.get(CONFIG.URLS.GET_SERVER_STATUS, {}).then(function (response) {
                    if (response.status === 200) {
                        return response.data;
                    }
                    else {
                        console.log('Error retrieving server status: ' + response.status);
                        return [];
                    }
                }, function (error) {
                    //   TODO: Even though we catch this,an ERR_CONNECTION_REFUSED is logged to the JS console continuously.  Try to suppress this.
                    console.log("Unable to connect to " + CONFIG.URLS.GET_SERVER_STATUS + ". Please ensure this is running.");
                    return [];
                });
            };

            this.scanFinger = function(scanner) {
                return $http.get(CONFIG.URLS.SCAN_FINGER, {"deviceId": scanner.id}).then(function(response) {
                    if (response.status === 200) {
                        return response.data;
                    }
                    else {
                        console.log('Error scanning fingerprint: ' + response.status);
                        return {};
                    }
                }, function (error) {
                    //   TODO: Even though we catch this,an ERR_CONNECTION_REFUSED is logged to the JS console continuously.  Try to suppress this.
                    console.log("Unable to connect to " + CONFIG.URLS.SCAN_FINGER + ". Please ensure this is running.");
                    return {};
                });
            };

            this.createTemplate = function(scannedFingers) {
                var scans = [];
                scannedFingers.forEach(function(finger) {
                    if (finger.template) {
                        scans.push(finger);
                    }
                });

                var deferred = $q.defer();
                if (scans.length == 0) {
                    deferred.resolve({});
                }
                else if (scans.length == 1) {
                    deferred.resolve(scannedFingers[0]);
                }
                else {
                    $http.post(CONFIG.URLS.GENERATE_TEMPLATE_FROM_FINGERS, JSON.stringify(scans)).then(function(response) {
                        if (response.status === 200) {
                            deferred.resolve(response.data);
                        }
                        else {
                            deferred.resolve({});
                        }
                    }, function (error) {
                        deferred.reject(error);
                    });
                }
                return deferred.promise;
            };

            this.matchFingers = function(scannedFingers) {
                return this.createTemplate(scannedFingers).then(function(template) {
                    return $http.post(CONFIG.URLS.MATCH_TEMPLATE, JSON.stringify(template)).then(function(response) {
                        if (response.status === 200) {

                            // TODO: Unsure if we should ignore matches if no patients are found for them, but also not sure what we should do
                            var ret = [];
                            response.data.forEach(function(item) {
                                $http.get(CONFIG.URLS.PATIENT_QUERY + "?identifier="+item.subjectId).then(function(response) {
                                    response.data.results.forEach(function(result) {
                                        ret.push(result);
                                    });
                                });
                            });

                            return ret;
                        }
                        else {
                            console.log('Error scanning fingerprint: ' + response.status);
                            return [];
                        }
                    }, function (error) {
                        //   TODO: Even though we catch this,an ERR_CONNECTION_REFUSED is logged to the JS console continuously.  Try to suppress this.
                        console.log("Unable to connect to " + CONFIG.URLS.MATCH_TEMPLATE + ". Please ensure this is running.");
                        return [];
                    });
                })
            };

            this.enrollTemplate = function(scannedFingers) {
                return this.createTemplate(scannedFingers).then(function(template) {
                    return $http.post(CONFIG.URLS.ENROLL_TEMPLATE, template).then(function(response) {
                        if (response.status === 201) {
                            return response.data;
                        }
                        else {
                            console.log('Error enrolling template: ' + response.status);
                            return {};
                        }
                    }, function (error) {
                        //   TODO: Even though we catch this,an ERR_CONNECTION_REFUSED is logged to the JS console continuously.  Try to suppress this.
                        console.log("Unable to connect to " + CONFIG.URLS.ENROLL_TEMPLATE + ". Please ensure this is running.");
                        return {};
                    });
                })
            };
        }
    ])

    .controller('FingerprintScanningController', ['$scope', '$interval', 'FingerprintScanningService', 'ngDialog',

        function($scope, $interval, FingerprintScanningService, ngDialog) {

            $scope.refreshScannerInfo = function() {
                $scope.refreshingScannerInfo = true;
                FingerprintScanningService.getScannerInfo().then(function (scannerInfo) {
                    $scope.scannerInfo = scannerInfo;
                    if (scannerInfo.scanners.length > 0 && !$scope.selectedScanner) {
                        $scope.selectedScanner = scannerInfo.scanners[0];
                    }
                    $scope.refreshingScannerInfo = false;
                });
            };

            $scope.refreshServerStatus = function() {
                $scope.refreshingServerStatus = true;
                FingerprintScanningService.getServerStatus().then(function (serverStatus) {
                    $scope.serverStatus = serverStatus;
                    $scope.refreshingServerStatus = false;
                });
            };

            $scope.scanFinger = function(finger) {
                $scope.scanningFingerInProgress = true;
                $scope.scannedData[finger.type] = {"currentlyScanning": true, "buttonLabel": "Scanning"};
                FingerprintScanningService.scanFinger($scope.selectedScanner).then(function(data) {
                    $scope.scanningFingerInProgress = false;
                    data.currentlyScanning = false;
                    data.buttonLabel = "Re-Scan";
                    $scope.scannedData[finger.type] = data;
                });
            };

            $scope.matchFingers = function() {
                $scope.matchesFound = [];
                $scope.matchingFingersInProgress = true;
                FingerprintScanningService.matchFingers($scope.scannedData).then(function(data) {
                    $scope.matchingFingersInProgress = false;
                    $scope.matchesFound = data;
                });
            };

            $scope.enrollTemplate = function() {
                $scope.enrollingTemplateInProgress = true;
                FingerprintScanningService.enrollTemplate($scope.scannedData).then(function(data) {
                    $scope.enrollingTemplateInProgress = false;
                    $scope.enrolledTemplate = data;
                    NavigatorController.stepForward();
                    NavigatorController.stepBackward();
                    $("#biometrics-enrollment").focus();
                    NavigatorController.stepForward();
                });
            }

            $scope.init = function(config) {
                $scope.config = config;
                $scope.refreshScannerInfo();
                $interval(function () {$scope.refreshScannerInfo();}, 5000);

                $scope.refreshServerStatus();
                $interval(function () {$scope.refreshServerStatus();}, 5000);

                $scope.fingersToScan = $scope.config.fingers;
                $scope.scannedData = [];
                $scope.fingersToScan.forEach(function(finger) {
                    $scope.scannedData[finger.type] = {"currentlyScanning": false, "buttonLabel": "Scan"};
                });
            }
        }

    ]);

