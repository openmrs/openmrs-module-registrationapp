<%
    ui.includeJavascript("registrationapp", "field/fingerprintM2sys.js")
    ui.includeJavascript("registrationapp", "fingerprintUtils.js")
    ui.includeJavascript("registrationapp", "field/m2sysnew.js")
%>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
<div id="fingerprint-fragment" style="margin-top:10px;margin-left:5px;margin-right:5px;margin-bottom:10px;">
    <p>
        <span id="fingerprintStatus"></span>
        <span id="fingerprintError" class="field-error"></span>
    </p>

    <p align="right">
        <b>${ui.message("registrationapp.biometrics.m2sys.label")}:</b>
        <button id="fingerprintButton">
            <i class="icon-hand-up"></i><span id="fingerprintButtonLabel"></span>
        </button><br/>
        <input type="text" name="fingerprintSubjectId" class="invisible"/>
    </p>
    <input type="hidden" id="biometricXml" name="biometricXml"/>
    <input type="hidden" name="identifierValue" id="identifierValue" value="${biometricID}"/>
    <input type="hidden" name="patient_id" id="patient_id" value="${patient.id}"/>
    <input type="hidden" name="successMessage" id="successMessage"
           value="${ui.message("registrationapp.biometrics.m2sys.register.success")}"/>
    <input type="hidden" name="errorMessage" id="errorMessage"
           value="${ui.message("registrationapp.biometrics.m2sys.register.failure")}"/>
    <input type="hidden" name="engineMessage" id="engineMessage"
           value="${ui.message("registrationapp.biometrics.m2sys.errorEngine")}"/>

</div>
<script type="text/javascript">
    emr.loadMessages([
        'registrationapp.biometrics.m2sys.register.success',
        'registrationapp.biometrics.m2sys.register.failure',
        'registrationapp.biometrics.m2sys.register.alreadyExists.failure'
    ]);
    var fingerprintSubjectIdField = jq("[name='fingerprintSubjectId']");

    var buttonLabelEnroll = "${ ui.message("registrationapp.biometrics.m2sys.register.button.label") }";
    var buttonLabelUpdate = "${ ui.message("registrationapp.biometrics.m2sys.update.button.label") }";
    var enrollOrUpdate = "${ enrollOrUpdate }";
    var biometricID = "${ biometricID }";
    jq(function () {

        jq(document).ready(function () {
            if (enrollOrUpdate == "update") {
                jq('#fingerprintButtonLabel').text(buttonLabelUpdate);
            } else {
                jq('#fingerprintButtonLabel').text(buttonLabelEnroll);
            }
        });

        jq(fingerprintButton).click(function () {
            capture('${ deviceName }','${ templateFormat }','${ engineName }','${ useTemplate }',this);

        });

        jq(fingerprintSubjectIdField).change(function () {
            biometricID = jq(fingerprintSubjectIdField).val();
            alert("this is a test 1");
            if (biometricID) {
                emr.getFragmentActionWithCallback('coreapps', 'editPatientIdentifier', 'editPatientIdentifier',
                    {
                        patientId: ${ patient.id },
                        identifierTypeId: ${ biometricPatientIdentifierType.id },
                        identifierValue: biometricID
                    },
                    function (data) {
                        emr.successMessage(data.message);
                        enrollOrUpdate = "update";
                        jq('#fingerprintButtonLabel').text(buttonLabelUpdate);
                    },
                    function (err) {
                        emr.handleError(err);
                    });
            }
        });
    });
</script>
