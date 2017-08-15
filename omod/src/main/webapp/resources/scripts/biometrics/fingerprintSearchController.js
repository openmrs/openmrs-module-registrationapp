angular.module('openmrs-module-registrationapp-fingerprint-search', ['pascalprecht.translate', 'openmrs-module-registrationapp-fingerprint-service'])


    .config(function ($translateProvider) {
        $translateProvider
            .useUrlLoader('/' +  OPENMRS_CONTEXT_PATH + '/module/uicommons/messages/messages.json',  {
                queryParameter : 'localeKey'
            })
            .useSanitizeValueStrategy('escape');
    })

    .controller('FingerprintSearchController', ['$scope', '$q', 'FingerprintService', '$translate',


        function($scope, $q, FingerprintService, $translate) {

            var scannerStatus;
            var engineStatus;
            var selectedScanner = null;

            $scope.message = "";

            $scope.init = function(config, locale) {

                $scope.config = config;

                $translate.use(locale);

                $q.all([
                    FingerprintService.getScannerStatus($scope.config).then(function (scannerStatus) {
                        $scope.scannerStatus = scannerStatus;
                        if (scannerStatus.scanners.length) {
                            $scope.selectedScanner = scannerStatus.scanners[0];
                        }
                    }),

                    FingerprintService.getEngineStatus().then(function (engineStatus) {
                        $scope.engineStatus = engineStatus.results;
                    })
                ])
                .then(function() {
                    if ($scope.scannerStatus.enabled && $scope.engineStatus.enabled) {
                        $scope.message = "registrationapp.biometrics.scannerAvailable";
                        $scope.scanFinger();
                    }
                })
            }

            $scope.scanFinger = function() {

                FingerprintService.scanFinger($scope.selectedScanner, { type: null, format: $scope.config.templateFormat }, $scope.config).then(function(data) {
                    if (data && data.template) {

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

                            $scope.message = "registrationapp.biometrics.scannerAvailable";
                            $scope.scanFinger();
                        },
                        function (error) {
                            $scope.message = "registrationapp.biometrics.badscan";
                            $scope.scanFinger();
                        });
                    }
                    else {
                        $scope.message = "registrationapp.biometrics.badscan";
                        $scope.scanFinger();
                    }
                },
                function (error) {
                    $scope.message = "registrationapp.biometrics.badscan";
                    $scope.scanFinger();
                });
            };

        }

    ]);

