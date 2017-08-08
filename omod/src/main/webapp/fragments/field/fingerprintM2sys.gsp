<%
    ui.includeJavascript("registrationapp", "field/fingerprintM2sys.js")
%>
<div>
    <a class="button app big" onClick="m2sysEnroll();">
        <i class="icon-hand-up"></i>
        ${ ui.message("registrationapp.biometrics.m2sys.enroll") }
    </a>
    <p>
        <input type="text" name="fingerprintIdInput" class="invisible"/>
    </p>
</div>