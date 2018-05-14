function viewCCD(patientId) {
    jq.getJSON(
        '/' + OPENMRS_CONTEXT_PATH + '/registrationapp/summary/continuityOfCare/viewCCD.action',
        {patientId: patientId},
        function(ccdContent) {
            var x = window.open();
            x.document.write('<title>Ccd document</title>' + ccdContent);
            x.document.close();
        }
    );
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
