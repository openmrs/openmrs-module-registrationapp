function isCCDAvailable() {
    return true;
}

function getCCDDate() {
    return "DD/MM/YYYY"
}

function viewCCD() {
    alert("Document is being viewed.");
}

function importCCD() {
    emr.setupConfirmationDialog({
        selector: '#ccd-import-dialog',
        actions: {
            confirm: function () {
                alert("Document is being downloaded.");
                $.modal.close();
            }
        }
    }).show();
}
