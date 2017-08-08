function enroll() {

        jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/field/fingerprintM2sys/enroll.action')
        .success(function(data) {
            $("[name='fingerprintIdInput']").val(data['id']);
        });
    }