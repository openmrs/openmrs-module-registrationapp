<%
    ui.includeJavascript("registrationapp", "field/fingerprintM2sys.js")
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

<div>
    <span id="fingerprintStatus"></span>
    <span id="fingerprintError" class="field-error"></span>

    <p>
        ${ui.message("registrationapp.biometrics.m2sys.register.question")}
    </p>
    <button class="button app big" onClick="m2sysEnroll(this); return false;">
        <i class="icon-hand-up"></i>
        ${ui.message("registrationapp.biometrics.m2sys.register.button.label")}
    </button>

    <p>
        <input type="text" name="fingerprintSubjectId" style="display: none"/>
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

            <div class="buttons">
                <button class="confirm right">${ui.message("registrationapp.biometrics.m2sys.register.alreadyExists.importingQuestion.redirectButton")}</button>
                <button class="cancel">${ui.message("registrationapp.cancel")}</button>
            </div>
        </div>
    </div>
</div>
