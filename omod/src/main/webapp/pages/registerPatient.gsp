<%
    if (sessionContext.authenticated && !sessionContext.currentProvider) {
        throw new IllegalStateException("Logged-in user is not a Provider")
    }
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeJavascript("uicommons", "navigator/validators.js", Integer.MAX_VALUE - 19)
    ui.includeJavascript("uicommons", "navigator/navigator.js", Integer.MAX_VALUE - 20)
    ui.includeJavascript("uicommons", "navigator/navigatorHandlers.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/navigatorModels.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/exitHandlers.js", Integer.MAX_VALUE - 22);
    ui.includeJavascript("registrationapp", "registerPatient.js");

    def genderOptions = [ [label: ui.message("emr.gender.M"), value: 'M'],
                          [label: ui.message("emr.gender.F"), value: 'F'] ]
                          
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
%>
${ ui.includeFragment("uicommons", "validationMessages")}

<script type="text/javascript">
    jQuery(function() {
        KeyboardController();
    });
</script>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("registrationapp.registration.label") }", link: "${ ui.pageLink("registrationapp", "registerPatient") }" }
    ];

    var testFormStructure = "${formStructure}";
    
    var patientDashboardLink = '${ui.pageLink("coreapps", "patientdashboard/patientDashboard")}';
    var getSimilarPatientsLink = '${ ui.actionLink("registrationapp", "matchingPatients", "getSimilarPatients") }';
</script>

<div id="reviewSimilarPatients" class="dialog" style="display: none">
    <div class="dialog-header">
      <h3>Select an existing patient</h3>
    </div>
    <div class="dialog-content">
        <p>
        	<em>Click to pick a patient: </em>
        </p>
        
        <ul id="similarPatientsSelect" class="select"></ul>
       
        <span class="button cancel"> Cancel </span>
    </div>
</div>

<div id="content" class="container">
    <h2>
        ${ ui.message("registrationapp.registration.label") }
    </h2>

	<div id="similarPatients" class="highlighted" style="display: none;">
		   <div class="left" style="padding: 6px"><span id="similarPatientsCount"></span> similar patient(s) found</div><button class="right" id="reviewSimilarPatientsButton">Review patient(s)</button>
		   <div class="clear"></div>
	</div>
	

    <form class="simple-form-ui" id="registration" method="POST">
        <section id="demographics">
            <span class="title">${ui.message("registrationapp.patient.demographics.label")}</span>

            <fieldset>
                <legend>${ui.message("registrationapp.patient.name.label")}</legend>
			    
                <h3>${ui.message("registrationapp.patient.name.question")}</h3>
                <% nameTemplate.lineByLineFormat.each { name -> %>
                    ${ ui.includeFragment("registrationapp", "field/personName", [
                            label: ui.message(nameTemplate.nameMappings[name]),
                            size: nameTemplate.sizeMappings[name],
                            formFieldName: name,
                            dataItems: 4,
                            left: true,
                            classes: [(name == "givenName" || name == "familyName") ? "required" : ""]
                    ])}

                <% } %>
                <input type="hidden" name="preferred" value="true"/>
            </fieldset>

            <fieldset>
                <legend>${ ui.message("emr.gender") }</legend>
                ${ ui.includeFragment("uicommons", "field/radioButtons", [
                        label: ui.message("registrationapp.patient.gender.question"),
                        formFieldName: "gender",
                        maximumSize: 3,
                        options: genderOptions,
                        classes: ["required"]
                ])}
            </fieldset>

            <fieldset>
                <legend>${ui.message("registrationapp.patient.birthdate.label")}</legend>
                <h3>${ui.message("registrationapp.patient.birthdate.question")}</h3>
                ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                        label: "",
                        formFieldName: "birthdate",
                        useTime: false,
                        left: true,
                        classes: ["required"]
                  ])}
            </fieldset>

        </section>
        <!-- read configurable sections from the json config file-->
        <% formStructure.sections.each { structure ->
            def section = structure.value
            def questions=section.questions
        %>
            <section id="${section.id}">
                <span class="title">${ui.message(section.label)}</span>
                    <% questions.each { question ->
                        def fields=question.fields
                    %>
                        <fieldset>
                            <legend>${ ui.message(question.legend)}</legend>
                            <% fields.each { field ->
                                def configOptions = [
                                        label:ui.message(field.label),
                                        formFieldName: field.formFieldName,
                                        left: true]
                                if(field.type == 'address'){
                                    configOptions.addressTemplate = addressTemplate
                                }
                            %>
                                ${ ui.includeFragment(field.fragmentRequest.providerName, field.fragmentRequest.fragmentId, configOptions)}
                            <% } %>
                        </fieldset>
                    <% } %>
            </section>
        <% } %>
        <div id="confirmation">
            <span class="title">${ui.message("registrationapp.patient.confirm.label")}</span>
            <div class="before-dataCanvas"></div>
            <div id="dataCanvas"></div>
            <div class="after-data-canvas"></div>
            <div id="confirmationQuestion">
                Confirm submission? <p style="display: inline"><input type="submit" class="confirm" value="Yes" /></p> or <p style="display: inline"><input id="cancelSubmission" class="cancel" type="button" value="No" /></p>
            </div>
        </div>
    </form>
</div>
