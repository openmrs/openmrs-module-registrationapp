angular.module('openmrs-module-registrationapp-fingerprint-service', [])

    .service('FingerprintService', ['$q', '$http',

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
                return $http.get(config.scanUrl, {"params" : {"deviceId": scanner != null ? scanner.id : null, "type": finger.type, "format": finger.format}}).then(function(response) {
                    if (response.status === 200) {
                        var data = response.data ? response.data : {};
                        data.type = data.type || finger.type;
                        data.format = data.format || finger.format;
                        return data;
                    }
                    else {
                        console.log('Error scanning fingerprint: ' + response.status);
                        return {};
                    }
                });
            };

          this.matchFinger = function(template) {
                var biometricUrl = '/' + OPENMRS_CONTEXT_PATH + '/registrationapp/biometrics/biometrics/search.action';
                var formData = "template="+ encodeURIComponent(template);
                // TODO why jquery here? angular doesn't seem to work
                return jq.post(biometricUrl, formData, function (data) {
                    return data;
                }, "json");
            }
        }
    ])