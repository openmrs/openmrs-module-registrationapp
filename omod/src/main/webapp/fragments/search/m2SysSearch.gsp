<%
    ui.includeJavascript("registrationapp", "field/fingerprintM2sys.js")
    ui.includeJavascript("registrationapp", "fingerprintUtils.js")
     ui.includeJavascript("registrationapp", "field/m2sysnew.js")
%>

<script>
    var jq = jQuery;

    function redirectToPatient(patientUuid) {
      emr.navigateTo({
        provider: 'coreapps',
        page: 'clinicianfacing/patient',
        query: { patientId: patientUuid }
      });
    }

    var handlePatientRowSelection = new function () {
      this.handle = function (patient) {
        if (patient.onlyInMpi === true) {
          showImportingDialog(patient)
        } else {
          redirectToPatient(patient.uuid)
        }
      }
    };

    jq(function () {
        var searchButton = jq('#fingerprint_search_button');
        searchButton.click(function () {
            biometricSearch('${deviceName}', '${ templateFormat }', '${ engineName }','${ useTemplate }',searchButton);
        });
    });

    function importMpiPatientWithCcd(patient) {
        emr.getFragmentActionWithCallback(
                "registrationapp", "search/m2SysSearch", "importMpiPatientWithCcd",
                { nationalFingerprintId: patient.nationalFingerprintPatientIdentifier.identifier },
                function (successResponse) {
                    redirectToPatient(successResponse.message);
                },
                function (failResponse) {
                    emr.handleError(failResponse);
                });
    }

    function showImportingDialog(patient) {
      var emrDialog = emr.setupConfirmationDialog({
        selector: '#patient-importing-dialog',
        actions: {
          confirm: function () {
            emrDialog.close();
            importMpiPatientWithCcd(patient);
          },
          cancel: function () {
          }
        }
      });
      emrDialog.show();
    }
</script>

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
<div id="fingerprint-fragment" style="display:inline;margin-top:10px;margin-left:5px;margin-right:5px;margin-bottom:10px;">
    <button id="fingerprint_search_button">
        <i class="icon-hand-up"></i><span id="fingerprintButtonLabel"></span>
    </button>
    <input type="text" name="fingerprintSubjectId" class="invisible" size="1" style="min-width:1em;"/>
    <input type="hidden" id="biometricXml" name="biometricXml"/>
    <input type="hidden" id="searchFinger" name="searchFinger" value="1"/>
	<input type="hidden" name="successMessage" id="successMessage" value="${ ui.message("registrationapp.biometrics.m2sys.register.success") }"/>
	<input type="hidden" name="errorMessage" id="errorMessage" value="${ ui.message("registrationapp.biometrics.m2sys.register.failure") }"/>
	<input type="hidden" name="engineMessage" id="engineMessage" value="${ ui.message("registrationapp.biometrics.m2sys.errorEngine") }"/>
</div>

<div style="display:none" id="patient-importing-dialog" class="dialog">
    <div class="dialog-header">
        ${ui.message("registrationapp.biometrics.m2sys.register.alreadyExists.importingQuestion.label")}
    </div>

    <div class="dialog-content">
        <p>
            ${ui.message("registrationapp.biometrics.m2sys.register.alreadyExists.importingQuestion.description")}
        </p>
        <br/>

        <div class="buttons">
            <button class="confirm right">${ui.message("registrationapp.biometrics.m2sys.register.alreadyExists.importingQuestion.redirectButton")}</button>
            <button class="cancel">${ui.message("registrationapp.cancel")}</button>
        </div>
    </div>
</div>
<div style="display:none" id="patient-biometric-search-dialog" class="dialog">
    <div class="dialog-header">
        ${ui.message("registrationapp.biometrics.m2sys.register.alreadyExists.searchingQuestion.label")}
    </div>

    <div class="dialog-content">
        <p>
            ${ui.message("registrationapp.biometrics.m2sys.register.alreadyExists.searchingQuestion.description")}
        </p>
        <br/>

        <div class="buttons">
            <button class="confirm right">${ui.message("registrationapp.biometrics.m2sys.register.alreadyExists.importingQuestion.redirectButton")}</button>
            <button class="cancel">${ui.message("registrationapp.cancel")}</button>
        </div>
    </div>
</div>
