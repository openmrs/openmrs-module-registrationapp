/*API Call*/
function capture(deviceName, templateFormat, engineName, useTemplate,sourceButton) {
    console.log(deviceName);
    console.log(templateFormat);
    console.log(engineName);
    console.log(useTemplate);

    var apiPath = 'http://localhost:15896/api/CloudScanr/FPCapture';
    toggleFingerprintButtonDisplay(sourceButton);
    if (useTemplate === "yes") {
        jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/field/fingerprintM2sys/loadTemplateTemplate.action')
            .success(function (response) {
                processTemplate(response.testTemplate,sourceButton);

            })
            .error(function (xhr, status, err) {
                console.log("Error Processing");
                toggleFingerprintButtonDisplay(sourceButton);
                alert('AJAX error ' + err);
            });
    } else {
        console.log("Using the scanner");
        CallFPBioMetricCapture(SuccessFunc, ErrorFunc, apiPath, deviceName, templateFormat, engineName);
    }
}

function biometricSearch(deviceName, templateFormat, engineName, useTemplate,sourceButton) {
    var apiPath = 'http://localhost:15896/api/CloudScanr/FPCapture';
    toggleFingerprintButtonDisplay(sourceButton);
    if (useTemplate === "yes") {
        jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/field/fingerprintM2sys/loadTemplateTemplate.action')
            .success(function (response) {
                searchPatientByBiometricXml(response.testTemplate,sourceButton);
            })
            .error(function (xhr, status, err) {
                toggleFingerprintButtonDisplay(sourceButton);
                alert('AJAX error ' + err);
            });
    } else {
        CallFPBioMetricCapture(SuccessFunc, ErrorFunc, apiPath, deviceName, templateFormat, engineName);
    }
}

function CallFPBioMetricCapture(SuccessFunc, ErrorFunc, apiPath, deviceName, templateFormat, engineName) {

    var uri = apiPath;

    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function () {
        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
            fpobject = JSON.parse(xmlhttp.responseText);
            SuccessFunc(fpobject, engineName);
        } else if (xmlhttp.status == 404) {
            ErrorFunc(xmlhttp.status)
        }
    }


    var captureType = 'DoubleCapture';
    var quickScan = 'Enable';
    var successMessage = document.getElementById('successMessage').value;
    var errorMessage = document.getElementById('errorMessage').value;
    var engineMessage = document.getElementById('engineMessage').value;

    xmlhttp.open("POST", uri, true);
    xmlhttp.setRequestHeader("Content-Type", "application/json");

    xmlhttp.setRequestHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");

    xmlhttp.send(JSON.stringify({
        "DeviceName": deviceName,
        "TemplateFormat": templateFormat,
        "CaptureType": captureType,
        "QuickScan": quickScan,
        "CaptureTimeOut": 120,
        "CaptureOperationName": "ENROLL"
    }));

    xmlhttp.onerror = function () {
        ErrorFunc(xmlhttp.statusText);
        toggleFingerprintButtonDisplay(jq('#captureButton'));
    }
}

function processTemplate(result,sourceButton) {
    document.getElementById('biometricXml').value = result;
    $('#fingerprintStatus').text("Template Loaded Successfully");

    var patient_id;
    var searchFinger;
    var element;

    element = document.getElementById('patient_id');
    if (element != null) {
        patient_id = element.value;
    } else {
        patient_id = '';
    }


    element = document.getElementById('searchFinger');
    if (element != null) {
        searchFinger = element.value;
    } else {
        searchFinger = '';
    }


    if (patient_id != '') {
        saveFinger(sourceButton);
    }

    if (searchFinger == '1') {
        searchPatient(sourceButton);
    }

}

function SuccessFunc(result, engineName) {
    if (result.CloudScanrStatus.Success) {

        if (result.TemplateData != null && result.TemplateData.length > 0) {
            document.getElementById('biometricXml').value = result.TemplateData;
        }
        //document.getElementById('<%= serverResult.ClientID %>').value = "Capture success. Please click on capture button";
        //$('#serverResult').val('Capture success. Please click on capture button');
        $('#fingerprintStatus').text(result.CloudScanrStatus.Message);

        var patient_id;
        var searchFinger;
        var element;

        element = document.getElementById('patient_id');
        if (element != null) {
            patient_id = element.value;
        } else {
            patient_id = '';
        }


        element = document.getElementById('searchFinger');
        console.log(element);
        if (element != null) {
            searchFinger = element.value;
        } else {
            searchFinger = '';
        }


        if (patient_id != '') {
            saveFinger();
        }

        if (searchFinger == '1') {
            searchPatient();
        }


    } else {
        //$('#serverResult').val(result.CloudScanrStatus.Message);
        $('#fingerprintStatus').text(result.CloudScanrStatus.Message);
        toggleFingerprintButtonDisplay(jq('#captureButton'));
    }
}

function ErrorFunc(status) {
//$('#serverResult').val('CloudScanr client may not started. Please check.');
    alert(engineMessage);
    toggleFingerprintButtonDisplay(jq('#captureButton'));
    //$('#fingerprintStatus').text(engineMessage);
}

function saveFinger(sourceButton) {
    var biometricXml = document.getElementById('biometricXml').value;
    var identifierValue = document.getElementById('identifierValue').value;
    var patient_id = document.getElementById('patient_id').value;
    var successMessage = document.getElementById('successMessage').value;
    var errorMessage = document.getElementById('errorMessage').value;
    if (identifierValue == '') identifierValue = 0;
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/field/fingerprintM2sys/update.action', {
        patientId: patient_id,
        biometricXml: biometricXml,
        identifierValue: identifierValue
    })
        .success(function (data) {
            var messageStatus = data.success === false ? "Failure! " : "Success! ";
            var message = data.message;

            $('#fingerprintStatus').text(messageStatus + message);
            toggleFingerprintButtonDisplay(sourceButton);
        })
        .error(function (data) {
            console.log("Error occurred!!!...................")
            console.log(data);
            toggleFingerprintButtonDisplay(sourceButton);
            $('#fingerprintError').text(errorMessage);
        });
}


function searchPatient(sourceButton) {
    var biometricXml = document.getElementById('biometricXml').value;
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/search/M2SysSearch/search.action', {biometricXml: biometricXml})
        .success(function (data) {
            if (data['success'] === true) {
                if (data['status'] === 'ALREADY_REGISTERED' && data['patientUuid']) {
                    m2SysShowAlreadyExistingFingerprintsDialog(data);
                } else {
                    m2SysSuccess();
                    m2SysSetSubjectIdInput(data['localBiometricSubjectId'], data['nationalBiometricSubjectId'])
                    toggleFingerprintButtonDisplay(jq('#captureButton'));
                    // toggleFingerprintButtonDisplay(sourceButton);
                }
            } else {
                m2SysErrorMessage(data['message']);
                m2SysClearSubjectIdInput();
            }
        })
        .error(function (data) {
            alert(errorMessage);
            $('#fingerprintError').text(errorMessage);
        });
}

function mpiImportingDialog(data,sourceButton) {
    jq('#patientName').textContent=data['patientUuid'];
    jq('#patientDob').textContent=data['patientUuid'];
    jq('#patientGender').textContent=data['patientUuid'];
    document.getElementById("patientName").textContent=data['patientName'];
    document.getElementById("patientDob").textContent=data['patientDob'];
    document.getElementById("patientGender").textContent=data['patientGender'];
    var emrDialog = emr.setupConfirmationDialog({
        selector: '#patient-biometric-search-dialog',
        actions: {
            confirm: function () {
                emrDialog.close();
                redirectToPatient(data['patientUuid']);
            },
            cancel: function () {
                toggleFingerprintButtonDisplay(sourceButton);
            }
        }
    });
    emrDialog.show();
}

function mpiSearchImport(data) {
    var importButton = jq('#mpiImportButton');
    var emrDialog = emr.setupConfirmationDialog({
        selector: '#patient-biometric-search-import-dialog',
        actions: {
            confirm: function () {
                toggleFingerprintButtonDisplay(importButton);
                var biometricId = data['nationalBiometricSubjectId'];
                $.getJSON(emr.fragmentActionLink("registrationapp", "registerPatient", "importMpiPatient", {mpiPersonId: biometricId}))
                    .success(function (response) {
                        console.log(response);
                        if(response.message){
                            redirectToPatient(response.message);
                        }else{
                            alert("No Patient with the given Biometric ID does not exist in the MPI");
                            toggleFingerprintButtonDisplay(importButton);
                        }
                    })
                    .error(function (xhr, status, err) {
                        alert('AJAX error ' + err);
                        toggleFingerprintButtonDisplay(importButton);
                    });
            },
            cancel: function () {
                emrDialog.close();
            }
        }
    });
    emrDialog.show();
}

function searchPatientByBiometricXml(biometricXml,sourceButton) {
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/search/M2SysSearch/search.action', {biometricXml: biometricXml})
        .success(function (data) {
            if (data['success'] === true) {
                if (data['status'] === 'ALREADY_REGISTERED' && data['patientUuid']) {
                    mpiImportingDialog(data,sourceButton);
                } else {
                    //Fetch patient from the Client Registry
                    toggleFingerprintButtonDisplay(sourceButton);
                    if(data['nationalBiometricSubjectId'] !== ""){
                        mpiSearchImport(data);
                    }
                }
            } else {
                console.log(data['message']);
            }
        })
        .error(function (data) {
            alert("Error while processing fingerprint details "+data['message']);
        });
}

function redirectToPatient(patientUuid) {
    emr.navigateTo({
        provider: 'coreapps',
        page: 'clinicianfacing/patient',
        query: {patientId: patientUuid}
    });
}

	