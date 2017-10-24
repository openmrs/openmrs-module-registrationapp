<%
    ui.includeJavascript("registrationapp", "field/fingerprintM2sys.js")
%>
<%= ui.resourceLinks() %>

<script>
jq = jQuery;

jq(function() {
    jq('#fingerprint_search_button').click(function() {
        jq.getJSON('${ ui.actionLink("getPatients") }', {})
        .success(function(data) {
            if (data) {
                var i;
                for (i = 0; i < data.length; i++) {
                    result = data[i];
                    addPatientToResults(result.patientIdentifier);
                }
            } else {
                alert("No matches");
            }
        })
        .error(function(xhr, status, err) {
            alert('AJAX error ' + err);
        })
    });
});
</script>

<div id="fingerprint-fragment" style="margin-top:10px;margin-left:5px;margin-right:5px;margin-bottom:10px;">
    <p>
    	<span id="fingerprintStatus"></span>
    	<span id="fingerprintError" class="field-error"></span>
    </p>
    <p align="right">
        <b>${ ui.message("registrationapp.biometrics.m2sys.label") }: </b>
	    <button id="ffingerprint_search_button">
	        <i class="icon-hand-up"></i><span id="fingerprintButtonLabel"></span>
	    </button><br />
	    <input type="text" name="fingerprintSubjectId" class="invisible"/>
	</p>

</div>