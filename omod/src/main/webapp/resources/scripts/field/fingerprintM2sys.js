function m2sysEnroll() {
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/field/fingerprintM2sys/enroll.action')
    .success(function(data) {
        if (data['success'] == true) {
            $("#fingerprintStatus").text("Success !");
            $("#fingerprintError").text("");
            $("[name='fingerprintSubjectId']").val(data['message']);
        } else {
            $("#fingerprintStatus").text("Failed !");
            $("#fingerprintError").text(data['message']);
            $("[name='fingerprintSubjectId']").val("");
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
    );
    //TODO add success method
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