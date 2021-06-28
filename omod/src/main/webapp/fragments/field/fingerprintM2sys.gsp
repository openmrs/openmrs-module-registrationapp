<%
    ui.includeJavascript("registrationapp", "field/fingerprintM2sys.js")
    ui.includeJavascript("registrationapp", "field/m2sysnew.js")
    ui.includeJavascript("registrationapp", "fingerprintUtils.js")
%>

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">

<script type="text/javascript">
    emr.loadMessages([
        'registrationapp.biometrics.m2sys.register.success',
        'registrationapp.biometrics.m2sys.register.failure',
        'registrationapp.biometrics.m2sys.register.alreadyExists.failure'
    ]);
</script>
<style>
.box {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr 1fr;
    grid-gap: 1px;
}
.address {
    display: grid;
    grid-template-columns: 95px auto;
    grid-gap: 1px;
}

.box :first-child {
    align-self: center;
}
.address :first-child {
    align-self: center;
}
</style>

<div>
    <span id="fingerprintStatus"></span>
    <span id="fingerprintError" class="field-error"></span>

    <p>
        ${ui.message("registrationapp.biometrics.m2sys.register.question")}
    </p>
    <button id="captureButton" class="button app big" onClick="capture('${ deviceName }','${ templateFormat }','${ engineName }','${ useTemplate }',this);
    return false;">
        <i class="icon-hand-up"></i>
        ${ui.message("registrationapp.biometrics.m2sys.register.button.label")}
    </button>

    <p>
        <input type="hidden" id="localBiometricSubjectId" name="localBiometricSubjectId" />
        <input type="hidden" id="nationalBiometricSubjectId" name="nationalBiometricSubjectId" />
        <input type="hidden" id="biometricXml" name="biometricXml"/>
        <input type="hidden" name="successMessage" id="successMessage"
               value="${ui.message("registrationapp.biometrics.m2sys.register.success")}"/>
        <input type="hidden" name="errorMessage" id="errorMessage"
               value="${ui.message("registrationapp.biometrics.m2sys.register.failure")}"/>
        <input type="hidden" name="engineMessage" id="engineMessage"
               value="${ui.message("registrationapp.biometrics.m2sys.errorEngine")}"/>
        <input type="hidden" id="searchFinger" name="searchFinger" value="1"/>
    </p>



    <div style="display:none" id="imported-patient-dialog" class="dialog">
        <div class="dialog-header">
            ${ui.message("registrationapp.biometrics.m2sys.register.alreadyExists.importingQuestion.label")}
        </div>

        <div class="dialog-content">
            <p>
                ${ui.message("registrationapp.biometrics.m2sys.register.alreadyExists.importingQuestion.description")}
            </p>
            <br/>
            <div class="box">
                <div>Name:</div>
                <div><span id="patientName"></span></div>
                <div>Phone:</div>
                <div><span id="phoneNumber"></span></div>
                <div>D.o.b:</div>
                <div><span id="patientDob"></span></div>
                <div>Source:</div>
                <div><span id="sourceLocation"></span></div>
                <div>Gender:</div>
                <div><span id="patientGender"></span></div>
                <div>Mother:</div>
                <div><span id="mothersName"></span></div>
            </div>
            <div class="address">
                <div>Address:</div>
                <div><span id="personAddress"></span></div>
                <div>Identifiers:</div>
                <div><span id="patientIdentifiers"></span></div>
            </div>

            <div class="buttons">
                <button class="confirm right">${ui.message("registrationapp.biometrics.m2sys.register.alreadyExists.importingQuestion.redirectButton")}</button>
                <button class="cancel">${ui.message("registrationapp.cancel")}</button>
            </div>
        </div>
    </div>

    <div style="display:none" id="patient-fp-search-dialog" class="dialog">
        <div class="dialog-header">
            ${ui.message("registrationapp.biometrics.m2sys.register.alreadyExists.searchingQuestion.label")}
        </div>

        <div class="dialog-content">
            <p>
                ${ui.message("registrationapp.biometrics.m2sys.register.alreadyExists.searchingQuestion.description")}
            </p>


            <div class="box">
                <div>Name:</div>
                <div><span id="mpatientName"></span></div>
                <div>Phone:</div>
                <div><span id="mphoneNumber"></span></div>
                <div>D.o.b:</div>
                <div><span id="mpatientDob"></span></div>
                <div>Source:</div>
                <div><span id="msourceLocation"></span></div>
                <div>Gender:</div>
                <div><span id="mpatientGender"></span></div>
                <div>Mother:</div>
                <div><span id="mmothersName"></span></div>
            </div>
            <div class="address">
                <div>Address:</div>
                <div><span id="mpersonAddress"></span></div>
                <div>Identifiers:</div>
                <div><span id="mpatientIdentifiers"></span></div>
            </div>
            <div class="buttons">
                <button id = "fpImport" class="confirm right">${ui.message("registrationapp.importAndOpen")}</button>
                <button class="cancel">${ui.message("registrationapp.cancel")}</button>
            </div>
        </div>
    </div>

    <div style="display:none" id="patient-fp-search-dialog-missing" class="dialog">
        <div class="dialog-header">
            ${ui.message("registrationapp.biometrics.m2sys.register.nofpmatch.header")}
        </div>

        <div class="dialog-content">
            <p>
                ${ui.message("registrationapp.biometrics.m2sys.register.nofpmatch.detail")}
            </p>
            <br/>

            <div class="buttons">
                <button id="ocrImportButton" class="confirm right">${ui.message("registrationapp.dismiss")}</button>
                <button class="cancel" style="visibility: hidden !important;">${ui.message("registrationapp.cancel")}</button>
            </div>
        </div>
    </div>
</div>
