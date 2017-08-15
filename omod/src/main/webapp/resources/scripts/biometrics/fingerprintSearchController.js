angular.module('openmrs-module-registrationapp-fingerprint-search', ['pascalprecht.translate', 'openmrs-module-registrationapp-fingerprint-service'])


    .config(function ($translateProvider) {
        $translateProvider
            .useUrlLoader('/' +  OPENMRS_CONTEXT_PATH + '/module/uicommons/messages/messages.json',  {
                queryParameter : 'localeKey'
            })
            .useSanitizeValueStrategy('escape');
    })

    .controller('FingerprintSearchController', ['$scope', '$q', 'FingerprintService', '$translate', '$timeout',

        function($scope, $q, FingerprintService, $translate, $timeout) {

            var TIMEOUT = 10000;

            var scannerStatus;
            var engineStatus;

            $scope.init = function(config, locale) {

                $scope.config = config;

                $translate.use(locale);

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
                        if ($scope.scannerStatus.scanners && $scope.scannerStatus.scanners.length > 0) {
                            $translate('registrationapp.biometrics.search.placeholder').then(function (translation) {
                                jq('#patient-search-form').trigger('search:placeholder', translation);
                            });
                        }
                        $scope.scanFinger();
                    }
                })
            }

            $scope.scanFinger = function() {

                FingerprintService.scanFinger(null, { type: null, format: $scope.config.templateFormat }, $scope.config).then(function(data) {
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

                            $scope.scanFinger();
                        },
                        function (error) {
                            $timeout($scope.scanFinger, TIMEOUT);
                        });
                    }
                    else {
                        // no match found, just return
                        $scope.scanFinger();
                    }
                },
                function (error) {
                    $timeout($scope.scanFinger, TIMEOUT);
                });
            };

        }

    ]);

