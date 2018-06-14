angular.module('openmrs-module-registrationapp-fingerprint-service', [])
    .constant('statusCodes', {
        '200': 'OK',
        '-1': 'SERVICE_NOT_RUNNING',
        '403': 'SERVICE_NOT_ENABLED',
        '404': 'DEVICE_NOT_FOUND',
        '500': 'UNKNOWN_ERROR',
        '502': 'BAD_SCAN',
        '504': 'DEVICE_TIMEOUT'
    })
    .service('FingerprintService', ['$q', '$http', 'statusCodes',

        function($q, $http, statusCodes) {

            // TODO do we want to change this and/or match finger to return error codes like we do for the scanner-side services?
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

                        var scannerStatus = {};
                        scannerStatus.enabled = true;
                        scannerStatus.scanners = [];
                        scannerStatus.status = statusCodes[response.status];

                        if (scannerStatus.status === 'OK') {
                            scannerStatus.scanners = response.data;
                        }

                        return scannerStatus;
                    },
                    function (response) {
                        var scannerStatus = {};

                        scannerStatus.enabled = false;
                        scannerStatus.scanners = [];
                        scannerStatus.status = statusCodes[response.status];
                        return scannerStatus;
                    }
                );
            };

            // note that scanning service currently doesn't support specifying device id, so this is ignored
            this.scanFinger = function(finger, config) {
                return $http.get(config.scanUrl, {"params" : {"type": finger.type, "format": finger.format}}).then(function(response) {
                    if (response.status === 200) {
                        var data = response.data ? response.data : {};
                        data.type = data.type || finger.type;
                        data.format = data.format || finger.format;
                        data.status = statusCodes[response.status];
                        return data;
                    }
                    else {
                        return {
                            status: statusCodes[response.status]
                        };
                    }
                },
                function(response) {
                    return {
                        status: statusCodes[response.status]
                    };
                });
            };

          this.matchFinger = function(template) {
            var biometricUrl = '/' + OPENMRS_CONTEXT_PATH + '/registrationapp/biometrics/biometrics/search.action';
            var formData = "template="+ encodeURIComponent(template);
            // TODO why jquery here? angular doesn't seem to work
            return jq.post(biometricUrl, formData, null, "json");
          }
        }
    ])