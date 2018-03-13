
function m2SysSetSubjectIdInput(value) {
    $("[name='fingerprintSubjectId']").val(value).trigger('change');
}

function m2SysSubjectIdInput() {
    m2SysSetSubjectIdInput('');
}

function m2SysError(errorDetails) {
    $("#fingerprintStatus").text('Failure!');
    $("#fingerprintError").text(errorDetails);
    m2SysSubjectIdInput();
}

function m2SysSuccess(subjectId) {
    $("#fingerprintStatus").text('Success!');
    $("#fingerprintError").text("");
    m2SysSetSubjectIdInput(subjectId)
}

function m2SysShowAlreadyExistingFingerprintsDialog(data) {
     emr.setupConfirmationDialog({
        selector: '#imported-patient-dialog',
        actions: {
            confirm: function () {
                var patientUrl = '/' + OPENMRS_CONTEXT_PATH + '/coreapps/clinicianfacing/patient.page?patientId='
                        + data['patientUuid'];
                $(location).attr('href', patientUrl);
            },
            cancel: function () {
                m2SysError('Fingerprints already registered, you cannot register the same fingerprints again!');
            }
        }
    }).show();
}

function m2sysEnroll() {
    m2SysSubjectIdInput();
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/field/fingerprintM2sys/enroll.action')
        .success(function(data) {
            if (data['success'] === true) {
                if (data['status'] === 'ALREADY_REGISTERED') {
                    m2SysShowAlreadyExistingFingerprintsDialog(data);
                } else {
                    m2SysSuccess(data['message']);
                }
            } else {
                m2SysError(data['message']);
            }
        });
}

function m2sysGetStatus() {
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/field/fingerprintM2sys/getStatus.action');
    //TODO add success method
}

function m2sysUpdate(idValue) {
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/field/fingerprintM2sys/update.action',
        {
            id: idValue
        }
    )
    .success(function(data) {
        if (data['success'] == true) {
            $("#fingerprintStatus").text("Success!");
            $("#fingerprintError").text("");
        } else {
            $("#fingerprintStatus").text("Failed!");
            $("#fingerprintError").text(data['message']);
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