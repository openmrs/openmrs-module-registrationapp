angular.module('openmrs-module-registrationapp-fingerprint-status', ['pascalprecht.translate', 'openmrs-module-registrationapp-fingerprint-service'])

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

    .controller('FingerprintStatusController', ['$scope', '$q', 'FingerprintService', '$translate', 'errorMessages',

        function($scope, $q, FingerprintService, $translate, errorMessages) {

            var scannerStatus;
            var engineStatus;

            $scope.init = function (config, locale) {

                $scope.errorMessage = "";

                $scope.config = config;

                // we don't do the rest of initialization until $translate.use is complete, because otherwise the translate
                // call to display the placeholder message may fail if the transaltions have not been loaded
                $translate.use(locale)
                    .then(function () {
                        $q.all([
                            FingerprintService.getScannerStatus($scope.config).then(function (scannerStatus) {
                                $scope.scannerStatus = scannerStatus;
                            }),
                        ])
                            .then(function() {
                                if ($scope.scannerStatus.status == 'OK') {
                                    if ($scope.scannerStatus.scanners.length > 0) {
                                        $scope.scannerName = $scope.scannerStatus.scanners[0].displayName;
                                        $scope.scannerStatusMessage =  'registrationapp.biometrics.connected';
                                    }
                                    else {
                                        $scope.scannerStatusMessage =  'registrationapp.biometrics.scannerNotFound';
                                    }
                                }
                                else {
                                    $scope.scannerStatusMessage = errorMessages[$scope.scannerStatus.status];
                                }

                            })

                    })


            }
        }
    ]);