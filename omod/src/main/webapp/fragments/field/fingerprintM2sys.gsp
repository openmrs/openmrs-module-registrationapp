<%
    ui.includeJavascript("registrationapp", "field/fingerprintM2sys.js")
%>
<div>
    <a class="button app big" onClick="enroll();">
        <i class="icon-hand-up"></i>
        ${ ui.message("registrationapp.biometrics.m2sys.enroll") }
    </a>
</div>