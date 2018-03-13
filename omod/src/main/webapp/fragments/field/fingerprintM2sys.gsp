<%
    ui.includeJavascript("registrationapp", "field/fingerprintM2sys.js")
    ui.includeJavascript("registrationapp", "fingerprintUtils.js")
%>

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
<div>
    <span id="fingerprintStatus"></span>
    <span id="fingerprintError" class="field-error"></span>
	   <p>
	        ${ ui.message("registrationapp.biometrics.m2sys.register.question") }
	    </p>
	    <button class="button app big" onClick="m2sysEnroll(this);">
	        <i class="icon-hand-up"></i>
	        ${ ui.message("registrationapp.biometrics.m2sys.register.button.label") }
	    </button>
    <p>
        <input type="text" name="fingerprintSubjectId" style="display: none" />
    </p>
</div>