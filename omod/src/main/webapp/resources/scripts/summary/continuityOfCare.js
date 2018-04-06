function viewCCD() {
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/summary/continuityOfCare/viewCCD.action');
}

function importCCD(patientId) {
    emr.setupConfirmationDialog({
        selector: '#ccd-import-dialog',
        actions: {
            confirm: function () {
                jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/summary/continuityOfCare/importCCD.action', {patientId: patientId});
                $.modal.close();
            }
        }
    }).show();
}
