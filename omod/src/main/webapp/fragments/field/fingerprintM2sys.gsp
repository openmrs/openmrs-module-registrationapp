<%
    ui.includeJavascript("registrationapp", "field/fingerprintM2sys.js")
%>

<div>
    <span id="fingerprintStatus"></span>
    <span id="fingerprintError" class="field-error"></span>
	   <p>
	        ${ ui.message("registrationapp.biometrics.m2sys.register.question") }
	    </p>
	    <a class="button app big" onClick="m2sysEnroll();">
	        <i class="icon-hand-up"></i>
	        ${ ui.message("registrationapp.biometrics.m2sys.register.button.label") }
	    </a>
    <p>
        <input type="text" name="fingerprintSubjectId" class="invisible"/>
    </p>
</div>