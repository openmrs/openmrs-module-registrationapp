function viewCCD(patientId) {
    var newWindow = window.open();
    jq.getJSON(
        '/' + OPENMRS_CONTEXT_PATH + '/registrationapp/summary/continuityOfCare/viewCCD.action',
        {patientId: patientId},
        function (ccdContent) {
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
                jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/summary/continuityOfCare/importCCD.action', {patientId: patientId},
                    function (patientUuid) {
                        //    reload the page to show the view button
                        redirectToPatient(patientUuid);
                    });
                $.modal.close();
            }
        }
    }).show();
}

function redirectToPatient(patientUuid) {
    emr.navigateTo({
        provider: 'coreapps',
        page: 'clinicianfacing/patient',
        query: {patientId: patientUuid}
    });
}


function refreshCcd(patientId) {
    var element = document.getElementById('ccdRefresh');
    //add spinning animation to indicate loading
    element.classList.add("loader");
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/summary/continuityOfCare/importCCD.action', {patientId: patientId},
        function (patientUuid) {
            //    remove the animation from the reload icon and reload the web page to show the view button
            element.classList.remove("loader");
            redirectToPatient(patientUuid);
        });
}
