angular.module('openmrs-module-registrationapp-fingerprint-service', [])
    .constant('errors', {
        '200': 'OK',
        '-1': 'SERVICE_NOT_RUNNING',
        '403': 'SERVICE_NOT_ENABLED',
        '404': 'DEVICE_NOT_FOUND',
        '500': 'UNKNOWN_ERROR',
        '502': 'BAD_SCAN',
        '504': 'DEVICE_TIMEOUT'
    })
    .service('FingerprintService', ['$q', '$http', 'errors',

        // TODO fix the getEngineStatus and getScannerStatus and matchFinger to be consist/use the new error messaging?
        // TODO fix the fingerprint.js/registration flow to use the new error mappings?

        function($q, $http, errors) {

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
                        data.status = errors[response.status];
                        return data;
                    }
                    else {
                        return {
                            status: errors[response.status]
                        };
                    }
                },
                function(response) {
                    return {
                        status: errors[response.status]
                    };
                });
            };

            // TODO add error codes here?

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