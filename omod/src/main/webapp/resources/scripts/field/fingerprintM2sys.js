

$(document).ready(function() {
  var dialog = emr.setupConfirmationDialog({
    selector: '#imported-patient-dialog',
    actions: {
      confirm: function() {
        var patientUrl = '/' + OPENMRS_CONTEXT_PATH + '/coreapps/clinicianfacing/patient.page?patientId=d491b4f6-1e89-4580-933f-9e2fbd26b630';
        $(location).attr('href', patientUrl);
      },
      cancel: function() {
        dialog.close();
      }
    }
  });

  dialog.show();
});

function m2sysEnroll() {
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/field/fingerprintM2sys/enroll.action')
    .success(function(data) {
        if (data['success'] === true) {
            $("#fingerprintStatus").text("Success!");
            $("#fingerprintError").text("");
            $("[name='fingerprintSubjectId']").val(data['message']).trigger('change');
        } else {
            $("#fingerprintStatus").text("Failed!");
            $("#fingerprintError").text(data['message']);
            $("[name='fingerprintSubjectId']").val("").trigger('change');
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