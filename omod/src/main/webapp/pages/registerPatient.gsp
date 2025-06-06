
<%
    if (sessionContext.authenticated && !sessionContext.currentProvider) {
        throw new IllegalStateException("Logged-in user is not a Provider")
    }
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeJavascript("uicommons", "handlebars/handlebars.min.js", Integer.MAX_VALUE - 1);
    ui.includeJavascript("uicommons", "navigator/validators.js", Integer.MAX_VALUE - 19)
    ui.includeJavascript("uicommons", "navigator/navigator.js", Integer.MAX_VALUE - 20)
    ui.includeJavascript("uicommons", "navigator/navigatorHandlers.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/navigatorModels.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/navigatorTemplates.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/exitHandlers.js", Integer.MAX_VALUE - 22);
    if (combineSubSections == true) {
        ui.includeJavascript("registrationapp", "customMultiInputDateValidator.js");
    }
    ui.includeJavascript("registrationapp", "registerPatient.js");
    ui.includeCss("registrationapp","registerPatient.css")

    def localizedGenderOptions = []
    def genderCodes = []

    genderOptions.each { optn ->
    	localizedGenderOptions << [label: ui.message("coreapps.gender." + optn), value: optn]
    	genderCodes << 'coreapps.gender.' + optn
    }

    Calendar cal = Calendar.getInstance()
    def maxAgeYear = cal.get(Calendar.YEAR)
    def minAgeYear = maxAgeYear - 120
    def minRegistrationAgeYear= maxAgeYear - 15 // do not allow backlog registrations older than 15 years

    def breadcrumbMiddle = breadcrumbOverride ?: '';

    def patientDashboardLink = patientDashboardLink ? ("/${contextPath}/" + patientDashboardLink) : ui.pageLink("coreapps", "clinicianfacing/patient")
    def identifierSectionFound = false

%>

<% if(includeFragments){
    includeFragments.each{ %>
        ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment)}
<%   }
} %>

<!-- used within registerPatient.js -->
<%=	ui.includeFragment("appui", "messages", [ codes: genderCodes.flatten()
]) %>

${ ui.includeFragment("uicommons", "validationMessages")}

<style type="text/css">
.matchingPatientContainer .container {
    overflow: hidden;
}

.matchingPatientContainer .container div {
    margin: 5px 10px;
}

.matchingPatientContainer .container .name {
    font-size: 25px;
    display: inline-block;
}

.matchingPatientContainer .container .info {
    font-size: 15px;
    display: inline-block;
}

.matchingPatientContainer .container .identifiers {
    font-size: 15px;
    display:inline-block;
    min-width: 600px;
}

.matchingPatientContainer .container .identifiers .idName {
    font-size: 15px;
    font-weight: bold;
}

.matchingPatientContainer .container .identifiers .idValue {
    font-size: 15px;
    margin: 0 20px 0 0;
}

fieldset[id\$="-fieldset"] input,
fieldset[id\$="-fieldset"] select {
    border-radius: 5px;
}

fieldset[id\$="-fieldset"] div,
fieldset[id\$="-fieldset"] div > div {
    display: inline-block;
    position: relative;
    width: 100%;
    padding: 5px;
}
</style>
<script type="text/javascript">

    var breadcrumbs = _.compact(_.flatten([
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        ${ breadcrumbMiddle },
        { label: "${ ui.message("registrationapp.registration.label") }", link: "${ ui.pageLink("registrationapp", "registerPatient") }" }
    ]));

    var testFormStructure = "${formStructure}";
    var patientDashboardLink = '${patientDashboardLink}';
    var appId = '${ui.encodeJavaScript(appId)}';

    // hack to create the sections variable used by the unknown patient handler in registerPatient.js
    var sections =  [];
    <% formStructure.sections.each { structure ->
            def section = structure.value;  %>
            sections.push('${section.id}');
    <% } %>

    jq(document).ready(function() {
        if ('${initialFieldValues}') {
            const registrationQuestions = new Set();
            let initialValues = JSON.parse('${initialFieldValues}');
            let fields = Object.keys(initialValues);
            if (fields) {
                fields.forEach((field) => {
                    // fields initial values are passed as a list of property-value
                    // e.g. {
                    //    "demographics.mothersFirstNameLabel.mothersFirstName": "Jen",
                    //    "contactInfo.phoneNumberLabel.phoneNumber" : "432-098-0987"
                    // }
                    // following the existing format of the registration form: SECTION.QUESTION.FIELD
                    const fieldProps = field.split(".");
                    if (fieldProps && fieldProps.length == 3 ) {
                        //section.question.field
                        let fieldName = fieldProps[2];
                        let questionName = fieldProps[1];
                        registrationQuestions.add(questionName);
                        jq('#' + questionName + ' input[name="' + fieldName + '"]').val(initialValues[field]);
                        if (NavigatorController.getQuestionById(questionName) != undefined) {
                            NavigatorController.getQuestionById(questionName).questionLi.addClass("done");
                        }
                        // when the relationships widget is configured to capture the mother info then the 'mother-field' will be present in the form
                        if (fieldName == 'mother-field') {
                            // otherwise the field's change() event that gets trigger automatically would clear the initial values which we just set above
                            jq('#mother-field').autocomplete("option", "disabled", true);
                        } else if (fieldName == 'gender') {
                            // the gender field is a select list of options
                            jq('#' + questionName + ' select[name="' + fieldName + '"] > option').each(function(){
                                if (jq(this).text() == initialValues[field]) {
                                    jq(this).prop('selected', true);
                                }
                            });
                        } else if (fieldName == 'birthdateMonth') {
                            // the birthdateMonth field is a dropdown list of months
                            jq('#' + questionName + ' select[name="' + fieldName + '"]').val(initialValues[field]);
                        }
                    }
                });
                if (registrationQuestions.size > 0) {
                    let formQuestions = NavigatorController.getQuestions();
                    for (let index = 0; index < formQuestions.length; index++) {
                        let questionId = formQuestions[index].id;
                        if (registrationQuestions.has(questionId)) {
                            NavigatorController.getQuestionById(questionId).click();
                        }
                    }
                }
            }
            if ('${goToSectionId}') {
                let sectionId = '${goToSectionId}';
                NavigatorController.getQuestionById(sectionId).click();
            }
        }
    });
</script>

<div id="modal-overlay">
    <div id="modal-content">
        <div class="spinner"></div>
        <p>${ui.message("registrationapp.pleaseDoNotRefreshPage")}</p>
    </div>
</div>

<div id="validation-errors" class="note-container" style="display: none" >
    <div class="note error">
        <div id="validation-errors-content" class="text">

        </div>
    </div>
</div>

    <h2>
        ${ ui.message("registrationapp.registration.label") }
    </h2>

	<div id="similarPatients" class="highlighted" style="display: none;">
		   <div class="left" style="padding: 6px"><span id="similarPatientsCount"></span> ${ ui.message("registrationapp.similarPatientsFound") }</div><button class="right" id="reviewSimilarPatientsButton">${ ui.message("registrationapp.reviewSimilarPatients.button") }</button>
		   <div class="clear"></div>
	</div>

    <div id="similarPatientsSlideView" style="display: none;">
        <ul id="similarPatientsSelect" class="matchingPatientContainer select" style="width: auto;">

        </ul>
    </div>

    <div id="biometricPatients" class="highlighted" style="display: none;">
        <div class="left" style="padding: 6px"><span id="biometricPatientsCount"></span> ${ ui.message("registrationapp.biometrics.matchingPatientsFound") }</div><button class="right" id="reviewBiometricPatientsButton">${ ui.message("registrationapp.reviewSimilarPatients.button") }</button>
        <div class="clear"></div>
    </div>

    <div id="biometricPatientsSlideView" style="display: none;">
        <ul id="biometricPatientsSelect" class="matchingPatientContainer select" style="width: auto;">

        </ul>
    </div>

    <div id="matchedPatientTemplates" style="display:none;">
        <div class="container"
             style="border-color: #00463f; border-style: solid; border-width:2px; margin-bottom: 10px;">
            <div class="name"></div>
            <div class="info"></div>
            <div class="identifiers">
                <span class="idName idNameTemplate"></span><span class="idValue idValueTemplate"></span>
            </div>
        </div>
        <button class="local_button" style="float:right; margin:10px; padding: 2px 8px" onclick="location.href='/openmrs-standalone/coreapps/clinicianfacing/patient.page?patientId=7'">
            ${ui.message("registrationapp.open")}
        </button>
        <button class="mpi_button" style="float:right; margin:10px; padding: 2px 8px" onclick="location.href='/execute_script_which_will_request_service_to_import_patient_from_mpi_to_local_DB_and_redirect_to_patient_info'">
            ${ui.message("registrationapp.importAndOpen")}
        </button>
    </div>

    <form class="simple-form-ui" id="registration" method="POST">
        <p>
            <input type="hidden" id="returnUrl" name="returnUrl" value="${ returnUrl }"/>
        </p>
        <% if (includeRegistrationDateSection) { %>
        <section id="registration-info" class="non-collapsible">
            <span class="title">${ui.message("registrationapp.registrationDate.label")}</span>

            <fieldset id="registration-date" class="multiple-input-date no-future-date">
                <legend id="registrationDateLabel">${ui.message("registrationapp.registrationDate.label")}</legend>
                <h3>${ui.message("registrationapp.registrationDate.question")}</h3>

                <p>
                    <input id="checkbox-enable-registration-date" type="checkbox" checked/>
                    <label for="checkbox-enable-registration-date">${ui.message("registrationapp.registrationDate.today")}</label>
                </p>

                ${ ui.includeFragment("uicommons", "field/multipleInputDate", [
                        label: "",
                        formFieldName: "registrationDate",
                        left: true,
                        classes: ['required'],
                        showEstimated: false,
                        initialValue: new Date(),
                        minYear: minRegistrationAgeYear,
                        maxYear: maxAgeYear,
                ])}
            </fieldset>
        </section>
        <% } %>

        <!-- read configurable sections from the json config file-->
        <% formStructure.sections.each { structure ->
            def section = structure.value
            def questions = section.questions
        %>

            <section id="${section.id}" class="non-collapsible">
                <span id="${section.id}_label" class="title">${section.id == 'demographics' ? ui.message("registrationapp.patient.demographics.label") : ui.message(section.label)}</span>

                  <${combineSubSections == true ? "fieldset id=\"" + section.id + "-fieldset\"" : "div"}>
                  ${combineSubSections == true ? "<legend>" + ui.message(section.label) + "</legend>" : ""}

                    <!-- hardcoded name, gender, and birthdate are added for the demographics section -->
                    <% if (section.id == 'demographics') { %>

                        <${combineSubSections == true ? "div" : "fieldset"} id="demographics-name">

                            ${combineSubSections == true ? "" : "<legend>" + ui.message("registrationapp.patient.name.label") + "</legend>"}
                            <div>
                                <h3>${ui.message("registrationapp.patient.name.question")}</h3>

                                <% nameTemplate.lines.each { line ->
                                    // go through each line in the template and find the first name token; assumption is there is only one name token per line
                                    def name = line.find({it['isToken'] == 'IS_NAME_TOKEN'})['codeName'];
                                    def initialNameFieldValue = ""
                                    if(patient.personName && patient.personName[name]){
                                        initialNameFieldValue = patient.personName[name]
                                    }
                                %>
                                ${ ui.includeFragment("registrationapp", "field/personName", [
                                        label: ui.message(nameTemplate.nameMappings[name]),
                                        size: nameTemplate.sizeMappings[name],
                                        formFieldName: name,
                                        dataItems: 4,
                                        left: true,
                                        initialValue: initialNameFieldValue,
                                        classes: [(name == "givenName" || name == "familyName") ? "required" : ""]
                                ])}

                                <% } %>
                            </div>

                            <div style="display:inline-block;width:100%">
                            <% questions.each { question ->
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
		                            %>

		                              ${ ui.includeFragment(field.fragmentRequest.providerName, field.fragmentRequest.fragmentId, configOptions)}

		                        	<% } %>
			                	<% } %>
		                    <% } %>
		                    </div>

                            <% if (allowUnknownPatients) { %>

                            <!-- TODO: fix this horrible method of making this line up properly -->
                            <div style="display:inline-block">
                                <!-- note that we are deliberately not including this in a p tag because we don't want the handler to pick it up as an actual field -->
                                <nobr>
                                    <input id="checkbox-unknown-patient" type="checkbox"/>
                                    <label for="checkbox-unknown-patient">${ui.message("registrationapp.patient.demographics.unknown")}</label>
                                </nobr>
                            </div>

                            <% } %>


                            <input type="hidden" name="preferred" value="true"/>
                        <${combineSubSections == true ? "/div" : "/fieldset"}>

                        <${combineSubSections == true ? "div" : "fieldset"} id="demographics-gender">

                            ${combineSubSections == true ? "" : "<legend id=\"genderLabel\">" + ui.message("Patient.gender") + "</legend>"}

                            ${ ui.includeFragment("uicommons", "field/dropDown", [
                                    id: "gender",
                                    label: ui.message("registrationapp.patient.gender.question"),
                                    formFieldName: "gender",
                                    options: localizedGenderOptions,
                                    classes: ["required"],
                                    initialValue: patient.gender,
                                    hideEmptyLabel: true,
                                    expanded: true
                            ])}
                            <!-- we "hide" the unknown flag here since gender is the only field not hidden for an unknown patient -->
                            <input id="demographics-unknown" type="hidden" name="unknown" value="false"/>
                        <${combineSubSections == true ? "/div" : "/fieldset"}>

                        <${combineSubSections == true ? "div" : "fieldset"} id="demographics-birthdate" class="multiple-input-date date-required no-future-date">

                            ${combineSubSections == true ? "" : "<legend id=\"birthdateLabel\">" + ui.message("registrationapp.patient.birthdate.label") + "</legend>"}

                            ${ ui.includeFragment("uicommons", "field/multipleInputDate", [
                                    label: ui.message("registrationapp.patient.birthdate.question"),
                                    formFieldName: "birthdate",
                                    classes: ["requiredTitle"],
                                    left: true,
                                    showEstimated: true,
                                    estimated: patient.birthdateEstimated,
                                    initialValue: patient.birthdate,
                                    minYear: minAgeYear,
                                    maxYear: maxAgeYear
                            ])}
                        <${combineSubSections == true ? "/div" : "/fieldset"}>
                    <% } %>

                    <!-- allow customization of additional question in the patient identification section, if it is included -->
                    <% if (section.id == 'patient-identification-section') {
                        identifierSectionFound = true; %>
                        <% if (allowManualIdentifier) { %>
                            ${ ui.includeFragment("registrationapp", "field/allowManualIdentifier", [
                                    identifierTypeName: ui.format(primaryIdentifierType)
                            ])}
                        <% } %>
                    <% } %>

                    <% questions.each { question ->
	                    // Skip re-rendering custom name field if present in app definition/formStructure
	                    if (question.id == null || question.id != 'personName') {
	                        def fields=question.fields
	                        def classes = "";
	                        if (question.legend == "Person.address") {
	                            classes = "requireOne"
	                        }
	                        if (question.cssClasses) {
	                            classes = classes + (classes.length() > 0 ? ' ' : '') + question.cssClasses.join(" ")
	                        }
	                    %>
	                        <${combineSubSections == true ? "div" : "fieldset"} id="${question.id}"
	                                <% if (classes.length() > 0) { %> class="${classes}" <% } %>
	                                <% if (question.fieldSeparator) { %> field-separator="${question.fieldSeparator}" <% } %>
	                                <% if (question.displayTemplate) { %> display-template="${ui.escapeAttribute(question.displayTemplate)}" <% } %>
	                        >
	                            ${combineSubSections == true ? "" : "<legend>" + ui.message(question.legend) + "</legend>"}

	                            <% if(question.legend == "Person.address"){ %>
	                                ${ui.includeFragment("uicommons", "fieldErrors", [fieldName: "personAddress"])}
	                            <% } %>
	                            <% if(question.header) { %>
	                                    <h3>${ui.message(question.header)}</h3>
	                            <% } %>

	                            <% fields.each { field ->
	                                def configOptions = (field.fragmentRequest.configuration != null) ? field.fragmentRequest.configuration : [:] ;
	                                configOptions.label = ui.message(field.label)
	                                configOptions.formFieldName = field.formFieldName
	                                configOptions.left = true
	                                configOptions.classes = field.cssClasses

	                                if (field.type == 'personAddress') {
	                                    configOptions.addressTemplate = addressTemplate
                                        // if a mother has been specified for this patient, then use the mother's address
                                        if ( question.id == 'personAddressQuestion' && mother) {
                                            configOptions.initialValue = mother.personAddress
                                        }
	                                }

	                                if (field.type == 'personRelationships') {
	                                    configOptions.relationshipTypes = relationshipTypes
	                                }
		                            %>
		                            ${ ui.includeFragment(field.fragmentRequest.providerName, field.fragmentRequest.fragmentId, configOptions)}
		                        <% } %>
	                        <${combineSubSections == true ? "/div" : "/fieldset"}>
	                    <% } %>
                    <% } %>
                  <${combineSubSections == true ? "/fieldset" : "/div"}>
            </section>
        <% } %>

        <% if (allowManualIdentifier && !identifierSectionFound) { %>
            <section id="patient-identification-section" class="non-collapsible">
                <span class="title">${ui.message("registrationapp.patient.identifiers.label")}</span>

                ${ ui.includeFragment("registrationapp", "field/allowManualIdentifier", [
                        identifierTypeName: ui.format(primaryIdentifierType)
                ])}
            </section>
        <% } %>

        <div id="confirmation" class="container">
            <span id="confirmation_label" class="title">${ui.message("registrationapp.patient.confirm.label")}</span>
            <div class="before-dataCanvas"></div>
            <div id="dataCanvas"></div>
            <div class="after-data-canvas"></div>
            <div id="exact-matches" style="display: none; margin-bottom: 20px">
                <span class="field-error">${ui.message("registrationapp.exactPatientFound")}</span>
                <ul id="exactPatientsSelect" class="select"></ul>
            </div>
            <div id="confirmationQuestion">
                ${ ui.message("registrationapp.confirm") }
                <p style="display: inline">
                    <input id="submit" type="submit" class="submitButton confirm right" value="${ui.message("registrationapp.patient.confirm.label")}" />
                </p>
                <p style="display: inline">
                    <input id="cancelSubmission" class="cancel" type="button" value="${ui.message("registrationapp.cancel")}" />
                </p>
            </div>
        </div>
    </form>
