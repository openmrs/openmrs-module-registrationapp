<%
    ui.includeJavascript("registrationapp", "field/fingerprintM2sys.js")
%>

<script>
jq = jQuery;

jq(function() {
    jq('#fingerprint_search_button').click(function() {
        jq.getJSON('${ ui.actionLink("getPatients") }', {})
        .success(function(data) {
            patientSearchWidget.reset();
            if (data) {
                patientSearchWidget.updateSearchResults(data);
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

<div id="fingerprint-fragment" style="display:inline;margin-top:10px;margin-left:5px;margin-right:5px;margin-bottom:10px;">
    <button id="fingerprint_search_button">
        <i class="icon-hand-up"></i><span id="fingerprintButtonLabel"></span>
    </button>
    <input type="text" name="fingerprintSubjectId" class="invisible" size="1" style="min-width:1em;"/>
	<div id="fingerprint-status">
		<span id="fingerprintStatus"></span>
		<span id="fingerprintError" class="field-error"></span>
	</div>
</div>