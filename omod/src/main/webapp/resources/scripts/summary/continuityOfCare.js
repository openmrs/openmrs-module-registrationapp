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

function importCCD(patientId, linkHref) {
    emr.setupConfirmationDialog({
        selector: '#ccd-import-dialog',
        actions: {
            confirm: function () {
                jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/summary/continuityOfCare/importCCD.action', {patientId: patientId},
                    function (ccdContent) {
                        window.location.replace(linkHref);
                    });
                $.modal.close();
            }
        }
    }).show();
}

function redirectToPatientCcd(newWindow,ccdContent) {
    newWindow.document.write('<title>Ccd document</title>' + ccdContent);
    newWindow.document.close();
}

function reloadPage(){
    location.reload();
    return false;
}


function refreshCcd(patientId, linkHref) {
    var element = document.getElementById('ccdRefresh');

    //add spinning animation to indicate loading
    element.classList.add("loader");
    jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/registrationapp/summary/continuityOfCare/importCCD.action', {patientId: patientId},
        function () {
            //    remove the animation from the reload icon and reload the web page to show the view button
            element.classList.remove("loader");
            window.location.replace(linkHref);
        });
}
