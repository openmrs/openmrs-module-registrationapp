function viewCCD(patientId) {
  var newWindow = window.open();
  jq.getJSON(
        '/' + OPENMRS_CONTEXT_PATH + '/registrationapp/summary/continuityOfCare/viewCCD.action',
        {patientId: patientId},
        function(ccdContent) {
            newWindow.document.write('<title>Ccd document</title>' + ccdContent);
            newWindow.document.close();
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
