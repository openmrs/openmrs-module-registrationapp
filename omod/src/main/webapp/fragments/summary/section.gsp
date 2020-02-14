<%
    def patient = config.patient
    def appId  = config.appId

    def returnUrl = "/${contextPath}/registrationapp/registrationSummary.page?patientId=${patient.patient.id}&appId=${appId}";


%>
<% if (section) { %>
<div class="info-section">
    <div class="info-header">
        <i class="icon-user"></i>
        <h3>${ (section.id == 'demographics' ? ui.message("registrationapp.patient.demographics.label") : ui.message(section.label)).toUpperCase() }</h3>
        <i id="${ section.id }-edit-link" class="icon-pencil edit-action right" title="${ ui.message("coreapps.edit") }"
           onclick="location.href='${ui.pageLink("registrationapp", "editSection", [patientId: patient.patient.id, returnUrl: returnUrl, sectionId: section.id, appId: appId ])}#visits';"></i>

    </div>
    <div class="info-body summary-section">
        <!-- display basic baked-in demographics information if demographics section -->
        <% if (section.id == 'demographics') { %>
            <% nameTemplate.lines.each { line ->
                // go through each line in the template and find the first name token; assumption is there is only one name token per line
                def name = line.find({it['isToken'] == 'IS_NAME_TOKEN'})['codeName'];
                def initialNameFieldValue = ""
                if(patient.personName && patient.personName[name]){
                    initialNameFieldValue = patient.personName[name]
                }
            %>
            <div>
                <h3>${ ui.message(nameTemplate.nameMappings[name]) } </h3>
                <p class="left">
                    ${ ui.message(ui.encodeHtml(initialNameFieldValue)) }
                </p>
            </div>
            <% } %>
            
            <% section.questions.each { question ->
                // Render custom/additional name fields if present in personName question from 
                // defined person attributes types
                if (question.id && question.id == 'personName') {
                	question.fields.each { field -> 
                %>
                	<div>
                		<h3>${ ui.message(field.label) }</h3>
                			<p class="left">
                				${ ui.encodeHtml(uiUtils.getPersonAttributeDisplayValue(patient.patient, field.uuid)?.replace("\n", "<br />")) ?: ''}&nbsp;
                			</p>
                		</div>
                	<% } %>
                <% } %>
            <% } %>

            <div>
                <h3>${ ui.message("emr.gender") }:</h3>
                <p class="left">
                    ${ui.message("coreapps.gender." + ui.encodeHtml(patient.gender))}&nbsp;
                </p>
            </div>

            <div>
                <h3>${ui.message("registrationapp.patient.birthdate.label")}</h3>
                <p class="left">
                    <% if (patient.birthdate) { %>
                    <% if (patient.birthdateEstimated) { %>~<% } %>${ ui.formatDatePretty(patient.birthdate) }
                    <% } else { %>
                        ${ui.message("coreapps.unknownAge")}
                    <% } %>
                </p>
            </div>
        <% } %>

        <!-- display other fields -->
        <% section.questions.each { question ->
            // Skip re-rendering configured person name fields if present in question because these have
            // already be rendered
	        if (question.id == null || question.id != 'personName') {
	            // TODO do we want to display any labels for questions?
	            def fields = question.fields %>
	            <div id="${ question.id }">
	                <h3>${ ui.message(question.legend) }</h3>
	                <p class="left">
	                    <% fields.each { field ->
	                        def displayValue = "";
	                        if (field.type == 'personAttribute') {
	                            displayValue = uiUtils.getPersonAttributeDisplayValue(patient.patient, field.uuid)?.replace("\n", "<br />");
	                        }
	                        else if (field.type == 'personAddress') {
	                            displayValue = ui.format(config.patient.personAddress).replace("\n", "<br />");
	                        }
	                        else if (field.type == "patientIdentifier") {
	                            displayValue = uiUtils.getIdentifier(patient.patient, field.uuid)
	                        }
                            else if (field.type == "personRelationships") {
                                displayValue = uiUtils.getPatientRelationships(patient.patient)
                            }
	                        // TODO support other types besides personAttribute and personAddress
	                    %>
	                        ${ displayValue ? ui.encodeHtmlContent(displayValue): ''}&nbsp;
	                    <% } %>
	                </p>
	            </div>
            <% } %>
        <% } %>
    </div>
</div>
<% } %>