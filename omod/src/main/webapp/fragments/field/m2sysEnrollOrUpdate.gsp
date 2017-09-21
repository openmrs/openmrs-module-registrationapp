<%
    ui.includeJavascript("registrationapp", "field/fingerprintM2sys.js")
%>
<div id="fingerprint-fragment">
    <span id="fingerprintStatus"></span>
    <span id="fingerprintError" class="field-error"></span>
    <p>
        <b>${ ui.message("registrationapp.biometrics.m2sys.label") }: </b>
	    <button id="fingerprintButton">
	        <i class="icon-hand-up"></i><span id="fingerprintButtonLabel"></span>
	    </button>
	</p>
    <p>
        <input type="text" name="fingerprintSubjectId" class="invisible"/>
    </p>
</div>
<script type="text/javascript">
    var fingerprintSubjectIdField = jq("[name='fingerprintSubjectId']");
	var buttonLabelEnroll = "${ ui.message("registrationapp.biometrics.m2sys.register.button.label") }";
	var buttonLabelUpdate = "${ ui.message("registrationapp.biometrics.m2sys.update.button.label") }";
	var enrollOrUpdate = "${ enrollOrUpdate }";
	var biometricID = "${ biometricID }";
	jq(function() {
		
		jq(document).ready(function() {
			if (enrollOrUpdate == "update") {
          		jq('#fingerprintButtonLabel').text(buttonLabelUpdate);
        	} else {
				jq('#fingerprintButtonLabel').text(buttonLabelEnroll);
        	}

		});
		
		jq(fingerprintButton).click(function() {
			if (enrollOrUpdate == "update") {
          		m2sysUpdate(biometricID);
        	} else {
				m2sysEnroll();
        	}
		});
		
	    jq(fingerprintSubjectIdField).change(function() {
	    	var idVal = jq(fingerprintSubjectIdField).val();
	        var returnValue = "";
	        biometricID = idVal;
	        if (idVal) {
	        	returnValue = savePatientIdentifier(${ patient.id }, ${ biometricPatientIdentifierType.id }, idVal);
		        if (returnValue == "success") {
		        	enrollOrUpdate = "update";
		        	jq('#fingerprintButtonLabel').text(buttonLabelUpdate);
		        }
		    }	        
    	});
    });
</script>