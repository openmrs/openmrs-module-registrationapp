function m2SysSetSubjectIdInput(localSubjectId, nationalSubjectId) {
    $("[name='localBiometricSubjectId']").val(localSubjectId).trigger('change');
    $("[name='nationalBiometricSubjectId']").val(nationalSubjectId).trigger('change');
}

function m2SysClearSubjectIdInput() {
    m2SysSetSubjectIdInput('', '');
}

function m2SysErrorMessage(errorDetails) {
    $("#fingerprintStatus").text(emr.message('registrationapp.biometrics.m2sys.register.failure'));
    $("#fingerprintError").text(errorDetails);
}

function m2SysSuccess() {
    $("#fingerprintStatus").text(emr.message('registrationapp.biometrics.m2sys.register.success'));
    $("#fingerprintError").text("");
}

function m2SysShowAlreadyExistingFingerprintsDialog(data) {
    emr.setupConfirmationDialog({
        selector: '#imported-patient-dialog',
        actions: {
            confirm: function () {
                redirectToPatient(data['patientUuid']);
            },
            cancel: function () {
                m2SysErrorMessage(emr.message(
                        'registrationapp.biometrics.m2sys.register.alreadyExists.failure'));
                toggleFingerprintButtonDisplay(jq('#captureButton'));
            }
        }
    }).show();
}

function m2sysEnroll(button) {
    m2SysClearSubjectIdInput();
    toggleFingerprintButtonDisplay(button);
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/field/fingerprintM2sys/enroll.action')
        .always(function () {
            toggleFingerprintButtonDisplay(button);
        })
        .success(function (data) {
            if (data['success'] === true) {
                if (data['status'] === 'ALREADY_REGISTERED' && data['patientUuid']) {
                    m2SysShowAlreadyExistingFingerprintsDialog(data);
                } else {
                    m2SysSuccess();
                    m2SysSetSubjectIdInput(data['localBiometricSubjectId'], data['nationalBiometricSubjectId'])
                }
            } else {
                m2SysErrorMessage(data['message']);
                m2SysClearSubjectIdInput();
            }
        });
}

function m2sysEnrollAndSave(patientId, button, callback) {
    m2SysClearSubjectIdInput();
    toggleFingerprintButtonDisplay(button);
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/field/fingerprintM2sys/enrollAndSave.action',
        { patientId: patientId })
        .always(function () {
            toggleFingerprintButtonDisplay(button);
        })
        .success(function (data) {
            if (data['success'] === true) {
                if (data['status'] === 'ALREADY_REGISTERED') {
                    m2SysShowAlreadyExistingFingerprintsDialog(data);
                } else {
                    m2SysSuccess();
                    m2SysSetSubjectIdInput(data['localBiometricSubjectId'], data['nationalBiometricSubjectId'])
                }
                callback(data['localBiometricSubjectId']);
            } else {
                m2SysErrorMessage(data['message']);
                m2SysClearSubjectIdInput();
            }
        });
}

function m2sysGetStatus() {
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/field/fingerprintM2sys/getStatus.action');
    //TODO add success method
}

function m2sysUpdate(idValue, button) {
    toggleFingerprintButtonDisplay(button);
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/field/fingerprintM2sys/update.action', {id: idValue})
        .always(function () {
            toggleFingerprintButtonDisplay(button);
        })
        .success(function (data) {
            if (data['success'] === true) {
                m2SysSuccess();
            } else {
                m2SysErrorMessage(data['message']);
            }
        });
}

function m2sysUpdateSubjectId(oldIdValue, newIdValue) {
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/field/fingerprintM2sys/updateSubjectId.action',
        {
            oldId: oldIdValue,
            newId: newIdValue
        }
    );
    //TODO add success method
}

function m2sysSearch(idValue) {
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/field/fingerprintM2sys/search.action',
        {
            id: idValue
        }
    );
    //TODO add success method
}

function m2sysLookup(idValue) {
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/field/fingerprintM2sys/lookup.action',
        {
            id: idValue
        }
    );
    //TODO add success method
}

function m2sysDelete(idValue) {
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/field/fingerprintM2sys/delete.action',
        {
            id: idValue
        }
    );
    //TODO add success method
}
