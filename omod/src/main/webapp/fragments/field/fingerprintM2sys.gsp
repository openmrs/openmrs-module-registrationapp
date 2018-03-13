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
        <input type="text" name="fingerprintSubjectId" style="display: none" />
    </p>
	<div style="display:none" id="imported-patient-dialog" class="dialog">
		<div class="dialog-header">
			${ ui.message("registrationapp.biometrics.m2sys.register.alreadyExists.importingQuestion.label") }
		</div>
		<div class="dialog-content">
			<p>
				${ ui.message("registrationapp.biometrics.m2sys.register.alreadyExists.importingQuestion.description")}
			</p>
			<br/>
			<div class="buttons">
				<button class="confirm right">Redirect</button>
				<button class="cancel">Abort</button>
			</div>
		</div>
	</div>
</div>