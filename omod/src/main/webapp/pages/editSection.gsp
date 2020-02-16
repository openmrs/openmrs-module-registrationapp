<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeJavascript("uicommons", "handlebars/handlebars.min.js", Integer.MAX_VALUE - 1);
    ui.includeJavascript("uicommons", "navigator/validators.js", Integer.MAX_VALUE - 19)
    ui.includeJavascript("uicommons", "navigator/navigator.js", Integer.MAX_VALUE - 20)
    ui.includeJavascript("uicommons", "navigator/navigatorHandlers.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/navigatorModels.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/navigatorTemplates.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/exitHandlers.js", Integer.MAX_VALUE - 22);
    ui.includeJavascript("registrationapp", "editSection.js");

    ui.includeCss("registrationapp", "editSection.css")

    def localizedGenderOptions = []
                              
    genderOptions.each { optn ->
    	localizedGenderOptions << [label: ui.message("emr.gender." + optn), value: optn]
    }

    def monthOptions = [ [label: ui.message("registrationapp.month.1"), value: 1],
                         [label: ui.message("registrationapp.month.2"), value: 2],
                         [label: ui.message("registrationapp.month.3"), value: 3],
                         [label: ui.message("registrationapp.month.4"), value: 4],
                         [label: ui.message("registrationapp.month.5"), value: 5],
                         [label: ui.message("registrationapp.month.6"), value: 6],
                         [label: ui.message("registrationapp.month.7"), value: 7],
                         [label: ui.message("registrationapp.month.8"), value: 8],
                         [label: ui.message("registrationapp.month.9"), value: 9],
                         [label: ui.message("registrationapp.month.10"), value: 10],
                         [label: ui.message("registrationapp.month.11"), value: 11],
                         [label: ui.message("registrationapp.month.12"), value: 12] ]

    Calendar cal = Calendar.getInstance()
    def maxAgeYear = cal.get(Calendar.YEAR)
    def minAgeYear = maxAgeYear - 120

    if (!returnUrl) {
        returnUrl = "/${contextPath}/coreapps/patientdashboard/patientDashboard.page?patientId=${patient.patientId}"
    }
%>
${ ui.includeFragment("uicommons", "validationMessages")}

<script type="text/javascript">
    jQuery(function() {

        jq('#cancelSubmission').unbind(); // unbind the functionality built into the navigator to return to top of the form
        jq('#cancelSubmission').click(function(event){
            window.location='${ ui.encodeJavaScript(returnUrl) }';
        })

        // disable submit button on submit
        jq('#registration-section-form').submit(function() {
            jq('#registration-submit').attr('disabled', 'disabled');
            jq('#registration-submit').addClass("disabled");
        })

        // clicking the save form link should have the same functionality as clicking on the confirmation section title (ie, jumps to confirmation)
        jq('#save-form').click(function() {
            NavigatorController.getSectionById("confirmation").title.click();
        })

    });
</script>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.encodeJavaScript(ui.encodeHtmlContent(ui.format(patient))) }", link: "${ ui.encodeHtml(returnUrl) }" },
        { label: "${ ui.message(section.label) }" }
    ];
</script>


    <h2>
        ${ ui.message(section.label)  }
    </h2>

    <div id="exit-form-container">
        <a href="#" id="save-form">
            <i class="icon-save small"></i>
            ${ ui.message("htmlformentryui.saveForm") }
        </a>
        <% if (returnUrl) { %>
            <a href="${ ui.escapeAttribute(returnUrl) }">
                <i class="icon-signout small"></i>
                ${ ui.message("htmlformentryui.exitForm") }
            </a>
        <% } %>
    </div>

    <form id="registration-section-form" class="simple-form-ui ${section.skipConfirmation ? 'skip-confirmation-section' : ''}" method="POST" action="/${contextPath}/registrationapp/editSection.page?patientId=${patient.patientId}&returnUrl=${ ui.urlEncode(returnUrl) }&appId=${app.id}&sectionId=${ ui.encodeHtml(section.id) }">
        <!-- read configurable sections from the json config file-->
        <section id="${section.id}" class="non-collapsible">
            <span class="title">${section.id == 'demographics' ? ui.message("registrationapp.patient.demographics.label") : ui.message(section.label)}</span>

            <% if (section.id == 'demographics') { %>

                <fieldset id="demographics-name">
                    <legend>${ui.message("registrationapp.patient.name.label")}</legend>
                    <h3>${ui.message("registrationapp.patient.name.question")}</h3>
                    <% nameTemplate.lines.each { line ->
                        // go through each line in the template and find the first name token; assumption is there is only one name token per line
                        def name = line.find({it['isToken'] == 'IS_NAME_TOKEN'})['codeName'];
                        def initialNameFieldValue = ""
                        if(patient.personName && patient.personName[name]){
                            initialNameFieldValue = ui.encodeHtml(patient.personName[name])
                        }
                    %>
                    ${ ui.includeFragment("registrationapp", "field/personName", [
                            label: ui.message(nameTemplate.nameMappings[name]),
                            size: nameTemplate.sizeMappings[name],
                            formFieldName: name,
                            dataItems: 4,
                            left: true,
                            ignoreCheckForSimilarNames: true,
                            initialValue: initialNameFieldValue,
                            classes: [(name == "givenName" || name == "familyName") ? "required" : ""]
                    ])}

                    <% } %>
                    
                    <div style="display:inline-block;width:100%">
                            <% section.questions.each { question ->
                            // Render custom/additional name fields if present in app defition/formStructure
			                    if (question.id && question.id == 'personName') {
			                    	def fields=question.fields
			                        def classes = "";
			                        fields.each { field ->
			                            def configOptions = (field.fragmentRequest.configuration != null) ? field.fragmentRequest.configuration : [:] ;
			                            configOptions.label = ui.message(field.label)
			                            configOptions.formFieldName = field.formFieldName
			                            configOptions.left = true
			                            configOptions.classes = field.cssClasses
			                            configOptions.initialValue = ui.escapeAttribute(uiUtils.getAttribute(patient, field.uuid))
		                            %>
		                              
		                              ${ ui.includeFragment(field.fragmentRequest.providerName, field.fragmentRequest.fragmentId, configOptions)}
		                    
		                        	<% } %>
			                	<% } %>
		                    <% } %>
		            </div>
                    <input type="hidden" name="preferred" value="true"/>
                </fieldset>

                <fieldset id="demographics-gender">
                    <legend id="genderLabel">${ ui.message("emr.gender") }</legend>
                    ${ ui.includeFragment("uicommons", "field/dropDown", [
                            label: ui.message("registrationapp.patient.gender.question"),
                            emptyOptionLabel: "uicommons.select",
                            formFieldName: "gender",
                            options: localizedGenderOptions,
                            classes: ["required"],
                            initialValue: ui.encodeHtml(patient.gender),
                            hideEmptyLabel: true,
                            expanded: true
                    ])}
                </fieldset>

                <fieldset id="demographics-birthdate" class="multiple-input-date no-future-date date-required">
                    <legend id="birthdateLabel">${ui.message("registrationapp.patient.birthdate.label")}</legend>
                    <h3>${ui.message("registrationapp.patient.birthdate.question")}</h3>
                    ${ ui.includeFragment("uicommons", "field/multipleInputDate", [
                            label: "",
                            formFieldName: "birthdate",
                            left: true,
                            showEstimated: true,
                            estimated: patient.birthdateEstimated,
                            initialValue: patient.birthdate,
                            minYear: minAgeYear,
                            maxYear: maxAgeYear
                    ])}
                </fieldset>
            <% } %>

            <% section.questions.each { question ->
                    // Skip re-rendering custom name field if present in app definition/formStructure
	                if (question.id == null || question.id != 'personName') {
                    def fields=question.fields
                %>
                <fieldset id="${question.id}"
                    <% if (question.cssClasses) { %> class="${question.cssClasses?.join(' ')}" <% } %>
                    <% if (question.fieldSeparator) { %> field-separator="${question.fieldSeparator}" <% } %>
                    <% if (question.displayTemplate) { %> display-template="${ui.escapeAttribute(question.displayTemplate)}" <% } %>
                >
                    <legend>${ ui.message(question.legend)}</legend>
                     <% if(question.header) { %>
                        <h3>${ui.message(question.header)}</h3>
                     <% } %>
                    <% fields.each { field ->
                        def configOptions = (field.fragmentRequest.configuration != null) ? field.fragmentRequest.configuration : [:] ;
                        configOptions.label = ui.message(field.label)
                        configOptions.formFieldName = field.formFieldName
                        configOptions.left = true
                        configOptions.classes = field.cssClasses

                        if(field.type == 'personAddress'){
                            configOptions.addressTemplate = addressTemplate
                            configOptions.initialValue = patient.personAddress
                        }else if(field.type == 'personAttribute'){
                            configOptions.initialValue = ui.escapeAttribute(uiUtils.getAttribute(patient, field.uuid));
                        }
                        else if (field.type == "patientIdentifier") {
                            configOptions.initialValue = uiUtils.getIdentifier(patient, field.uuid)
                        }
                        else if (field.type == 'personRelationships') {
                                    configOptions.relationshipTypes = relationshipTypes
                        }
                    %>
                    	${ ui.includeFragment(field.fragmentRequest.providerName, field.fragmentRequest.fragmentId, configOptions)}
                    <% } %>
                </fieldset>
                <% } %>
            <% } %>
            <input id="patientUuid" type="hidden" name="patientUuid" value="${patientUuid}"/>
        </section>

        <div id="confirmation">
            <span class="title">${ui.message("registrationapp.patient.confirm.label")}</span>
            <div class="before-dataCanvas"></div>
            <div id="dataCanvas"></div>
            <div class="after-data-canvas"></div>
            <div id="confirmationQuestion">
                ${ui.message("registrationapp.confirm")}
                <p style="display: inline">
                    <button id="registration-submit" type="submit" class="submitButton confirm right">
                        ${ui.message("registrationapp.patient.confirm.label")}
                    </button>
                </p>
                <p style="display: inline">
                    <button id="cancelSubmission" class="cancel" type="button">
                        ${ui.message("registrationapp.cancel")}
                    </button>
                </p>
            </div>
        </div>
    </form>
