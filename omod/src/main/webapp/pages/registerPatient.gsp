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
    ui.includeJavascript("registrationapp", "registerPatient.js");

    def genderOptions = [ [label: ui.message("emr.gender.M"), value: 'M'],
                          [label: ui.message("emr.gender.F"), value: 'F'] ]

    def cleanup = {
        return (it instanceof org.codehaus.jackson.node.TextNode) ? it.textValue : it;
    }
%>
${ ui.includeFragment("uicommons", "validationMessages")}

<script type="text/javascript">
    var NavigatorController;
    jQuery(function() {
        NavigatorController = KeyboardController();

        // TODO: move any of this into registration app js?

        // handle registration date functionality
        <% if (includeRegistrationDateSection) { %>
            // registration fields are is disabled by default
            _.each(NavigatorController.getQuestionById('registration-date').fields, function(field) {
                if (field.id != 'checkbox-enable-registration-date') {
                    field.hide();
                }
            });

            jq('#checkbox-enable-registration-date').click(function () {
                if(jq('#checkbox-enable-registration-date').is(':checked')) {
                    _.each(NavigatorController.getQuestionById('registration-date').fields, function(field) {
                        if (field.id != 'checkbox-enable-registration-date') {
                            field.hide();
                        }
                    });
                }
                else {
                    _.each(NavigatorController.getQuestionById('registration-date').fields, function(field) {
                        if (field.id != 'checkbox-enable-registration-date') {
                            field.show();
                        }
                    });
                }
            });
        <% } %>

        // handle patient identifier function
        NavigatorController.getFieldById('patient-identifier').hide();

        jq('#checkbox-autogenerate-identifier').click(function () {
            if(jq('#checkbox-autogenerate-identifier').is(':checked')) {
                NavigatorController.getFieldById('patient-identifier').hide();
            }
            else {
                NavigatorController.getFieldById('patient-identifier').show();
                NavigatorController.getFieldById('patient-identifier').click();
            }
        })

        // handle unknown patient functionality
        jq('#checkbox-unknown-patient').click(function () {

            if(jq('#checkbox-unknown-patient').is(':checked')) {

                // hide all questions & sections except gender and registration date
                _.each(NavigatorController.getQuestionById('demographics-name').fields, function(field) {
                    if (field.id != 'checkbox-unknown-patient') {
                        field.disable();
                    }
                });

                NavigatorController.getQuestionById('demographics-birthdate').disable();

                <% formStructure.sections.each { structure ->
                    def section = structure.value;  %>
                NavigatorController.getSectionById('${section.id}').disable();
                <% } %>

                // set unknown flag
                jq('#demographics-unknown').val('true');

                // jump ahead to gender
                NavigatorController.getQuestionById('demographics-gender').click();
            }
            else {
                // re-enable all functionality
                // hide all questions & sections except gender and registration date
                _.each(NavigatorController.getQuestionById('demographics-name').fields, function(field) {
                    if (field.id != 'checkbox-unknown-patient') {
                        field.enable();
                    }
                });

                NavigatorController.getQuestionById('demographics-birthdate').enable();
                <% formStructure.sections.each { structure ->
                    def section = structure.value;  %>
                NavigatorController.getSectionById('${section.id}').enable();
                <% } %>

                // unset unknown flag
                jq('#demographics-unknown').val('false');
                NavigatorController.getQuestionById('demographics-name').fields[0].click();
            }
        })
    });
</script>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("registrationapp.registration.label") }", link: "${ ui.pageLink("registrationapp", "registerPatient") }" }
    ];

    var testFormStructure = "${formStructure}";
    
    var patientDashboardLink = '${ui.pageLink("coreapps", "clinicianfacing/patient")}';
    var getSimilarPatientsLink = '${ ui.actionLink("registrationapp", "matchingPatients", "getSimilarPatients") }&appId=${appId}';
    
</script>

<div id="reviewSimilarPatients" class="dialog" style="display: none">
    <div class="dialog-header">
      <h3>${ ui.message("registrationapp.reviewSimilarPatients")}</h3>
    </div>
    <div class="dialog-content">
        <p>
        	<em>${ ui.message("registrationapp.selectSimilarPatient") }</em>
        </p>
        
        <ul id="similarPatientsSelect" class="select"></ul>
       
        <span class="button cancel"> ${ ui.message("registrationapp.cancel") } </span>
    </div>
</div>

<div id="content" class="container">
    <h2>
        ${ ui.message("registrationapp.registration.label") }
    </h2>

	<div id="similarPatients" class="highlighted" style="display: none;">
		   <div class="left" style="padding: 6px"><span id="similarPatientsCount"></span> ${ ui.message("registrationapp.similarPatientsFound") }</div><button class="right" id="reviewSimilarPatientsButton">${ ui.message("registrationapp.reviewSimilarPatients.button") }</button>
		   <div class="clear"></div>
	</div>

    <form class="simple-form-ui" id="registration" method="POST">

        <% if (includeRegistrationDateSection) { %>
        <section id="registration-info">
            <span class="title">${ui.message("registrationapp.registrationDate.label")}</span>

            <fieldset id="registration-date" class="multiple-input-date no-future-date date-required">
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
                        showEstimated: false,
                        initialValue: new Date()
                ])}
            </fieldset>
        </section>
        <% } %>

        <section id="demographics">
            <span class="title">${ui.message("registrationapp.patient.demographics.label")}</span>

            <fieldset id="demographics-name">

                <legend>${ui.message("registrationapp.patient.name.label")}</legend>
                    <h3>${ui.message("registrationapp.patient.name.question")}</h3>
                    <% nameTemplate.lineByLineFormat.each { name ->
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

                    <!-- note that we are deliberately not including this in a p tag because we don't want the handler to pick it up as an actual field -->
                    <input id="checkbox-unknown-patient" name="test-me" type="checkbox"/>
                    <label for="checkbox-unknown-patient">${ui.message("registrationapp.patient.demographics.unknown")}</label>

                    <input type="hidden" name="preferred" value="true"/>
            </fieldset>

            <fieldset id="demographics-gender">
                <legend id="genderLabel">${ ui.message("emr.gender") }</legend>
                <h3>${ui.message("registrationapp.patient.gender.question")}</h3>
                ${ ui.includeFragment("uicommons", "field/dropDown", [
                        id: "gender",
                        emptyOptionLabel: "uicommons.select",
                        formFieldName: "gender",
                        options: genderOptions,
                        classes: ["required"],
                        initialValue: patient.gender
                ])}
                <!-- we "hide" the unknown flag here since gender is the only field not hidden for an unknown patient -->
                <input id="demographics-unknown" type="hidden" name="unknown" value="false"/>
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
                        initialValue: patient.birthdate
                  ])}
            </fieldset>

        </section>

        <!-- read configurable sections from the json config file-->
        <% formStructure.sections.each { structure ->
            def section = structure.value
            def questions=section.questions
        %>
            <section id="${section.id}">
                <span id="${section.id}_label" class="title">${ui.message(section.label)}</span>
                    <% questions.each { question ->
                        def fields=question.fields
                    %>
                        <fieldset
                            <% if(question.legend == "Person.address"){ %> class="requireOne"<% } %>
                            <% if (question.fieldSeparator) { %> field-separator="${question.fieldSeparator}" <% } %>
                            <% if (question.displayTemplate) { %> display-template="${ui.escapeAttribute(question.displayTemplate)}" <% } %>
                        >
                            <legend id="${question.id}">${ ui.message(question.legend)}</legend>
                            <% if(question.legend == "Person.address"){ %>
                                ${ui.includeFragment("uicommons", "fieldErrors", [fieldName: "personAddress"])}
                            <% } %>
                            <% fields.each { field ->
                                def configOptions = [
                                        label:ui.message(field.label),
                                        formFieldName: field.formFieldName,
                                        left: true,
                                        "classes": field.cssClasses
                                ]
                                if (field.widget.config) {
                                    field.widget.config.fields.each {
                                        configOptions[it.key] = cleanup(it.value);
                                    }
                                }
                                if(field.type == 'personAddress'){
                                    configOptions.addressTemplate = addressTemplate
                                }
                            %>
                                ${ ui.includeFragment(field.fragmentRequest.providerName, field.fragmentRequest.fragmentId, configOptions)}
                            <% } %>
                        </fieldset>
                    <% } %>
            </section>
        <% } %>

        <section id="patient-identification-section">
            <span class="title">${ui.message("registrationapp.patient.identifiers.label")}</span>

            <fieldset id="patient-identifier-question">
                <legend id="patientIdentifierLabel">${ui.format(primaryIdentifierType)}</legend>
                <h3>${ui.message("registrationapp.patient.identifier.question", ui.format(primaryIdentifierType))}</h3>

                <p>
                    <input id="checkbox-autogenerate-identifier" type="checkbox" checked/>
                    <label for="checkbox-autogenerate-identifier">${ui.message("registrationapp.patient.identifier.autogenerate.label")}</label>
                </p>

                <p>
                    <label for="patient-identifier">${ui.message("registrationapp.patient.identifier.label")}</label>
                    <input id="patient-identifier" name="patientIdentifier"/>
                </p>

            </fieldset>
        </section>

        <div id="confirmation">
            <span id="confirmation_label" class="title">${ui.message("registrationapp.patient.confirm.label")}</span>
            <div class="before-dataCanvas"></div>
            <div id="dataCanvas"></div>
            <div class="after-data-canvas"></div>
            <div id="confirmationQuestion">
                ${ ui.message("registrationapp.confirm") }
                <p style="display: inline">
                    <input type="submit" class="submitButton confirm right" value="${ui.message("registrationapp.patient.confirm.label")}" />
                </p>
                <p style="display: inline">
                    <input id="cancelSubmission" class="cancel" type="button" value="${ui.message("registrationapp.cancel")}" />
                </p>
            </div>
        </div>
    </form>
</div>
