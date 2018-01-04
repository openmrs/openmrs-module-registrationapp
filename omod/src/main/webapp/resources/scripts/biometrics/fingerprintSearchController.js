angular.module('openmrs-module-registrationapp-fingerprint-search', ['pascalprecht.translate', 'openmrs-module-registrationapp-fingerprint-service'])

    .constant('errorMessages', {
        'SERVICE_NOT_RUNNING': 'registrationapp.biometrics.serviceNotRunning',
        'SERVICE_NOT_ENABLED': 'registrationapp.biometrics.serviceNotEnabled',
        'DEVICE_NOT_FOUND': 'registrationapp.biometrics.scannerNotFound',
        'UNKNOWN_ERROR': 'registrationapp.biometrics.scannerError',
        'BAD_SCAN': 'registrationapp.biometrics.badscan',
        'DEVICE_TIMEOUT': ''
    })

    .config(function ($translateProvider) {
        $translateProvider
            .useUrlLoader('/' +  OPENMRS_CONTEXT_PATH + '/module/uicommons/messages/messages.json',  {
                queryParameter : 'localeKey'
            })
            .useSanitizeValueStrategy('escape');
    })

    .controller('FingerprintSearchController', ['$scope', '$q', 'FingerprintService', '$translate', '$timeout', 'errorMessages',

        function($scope, $q, FingerprintService, $translate, $timeout, errorMessages) {

            var TIMEOUT = 10000;

            var scannerStatus;
            var engineStatus;

            $scope.init = function(config, locale) {

                $scope.errorMessage = "";

                $scope.config = config;

                // we don't do the rest of initializatio until $translate.use is complete, because otherwise the translate
                // call to display the placeholder message may fail if the transaltions have not been loaded
                $translate.use(locale)
                    .then(function() {
                        $q.all([
                            FingerprintService.getScannerStatus($scope.config).then(function (scannerStatus) {
                                $scope.scannerStatus = scannerStatus;
                            }),

                            FingerprintService.getEngineStatus().then(function (engineStatus) {
                                $scope.engineStatus = engineStatus.results;
                            })
                        ])
                            .then(function() {
                                if ($scope.scannerStatus.enabled && $scope.engineStatus.enabled) {
                                    $translate('registrationapp.biometrics.search.placeholder').then(function (translation) {
                                        jq('#patient-search-form').trigger('search:placeholder', translation);
                                    });
                                    $scope.scanFinger();
                                }
                            })
                })


            }

            $scope.scanFinger = function() {

                FingerprintService.scanFinger({ type: null, format: $scope.config.templateFormat }, $scope.config).then(function(data) {

                    // handle errors
                    if (!data) {
                        $timeout($scope.scanFinger, TIMEOUT);
                    }
                    else if (data.status == 'SERVICE_NOT_RUNNING' || data.status == 'SERVICE_NOT_ENABLED' ||
                            data.status == 'DEVICE_NOT_FOUND' || data.status == 'UNKNOWN_ERROR') {
                        $scope.errorMessage = errorMessages[data.status];
                        // try again after timeout
                        $timeout($scope.scanFinger, TIMEOUT);
                    }
                    else if (data.status == 'BAD_SCAN' || data.status == 'DEVICE_TIMEOUT') {
                        $scope.errorMessage = errorMessages[data.status];
                        // try again immediately
                        $scope.scanFinger()
                    }
                    else if (data.status == 'OK' && data.template) {

                        $scope.errorMessage = "";

                        jq('#patient-search-form').trigger('search:clear');

                        FingerprintService.matchFinger(data.template).then(function (data) {

                            if (data && data.length > 0) {
                                // TODO sort by match score
                                var identifiers = [];
                                angular.forEach(data.reverse(), function (match) {
                                    identifiers.push(match.subjectId)
                                });
                                // TODO better way to do this than access jquery directly? how do we assure jquery is present?
                                jq('#patient-search-form').trigger('search:identifiers', identifiers);
                            }
                            else {
                                jq('#patient-search-form').trigger('search:no-matches');
                            }

                            // try again in all case if scan was successful, regardlesss of whether or not match was found
                            $scope.scanFinger();
                        }, function (err) {
                            // there was a problem contacting the matching engine, scan again
                            $scope.errorMessage = 'registrationapp.biometrics.errorContactingBiometricsServer';
                            $scope.scanFinger();
                        });
                    }
                    else {
                        // final fallback, try again after timeout
                        $timeout($scope.scanFinger, TIMEOUT);
                    }
                });
            };

        }

    ]);

