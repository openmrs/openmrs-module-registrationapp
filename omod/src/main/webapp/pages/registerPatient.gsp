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
	ui.includeJavascript("registrationapp", "webcam.js");

    def genderOptions = [ [label: ui.message("emr.gender.M"), value: 'M'],
                          [label: ui.message("emr.gender.F"), value: 'F'] ]
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

            <fieldset id="demographics-gender">
                <legend id="genderLabel">${ ui.message("emr.gender") }</legend>
                ${ ui.includeFragment("uicommons", "field/radioButtons", [
                        label: ui.message("registrationapp.patient.gender.question"),
                        formFieldName: "gender",
                        maximumSize: 3,
                        options: genderOptions,
                        classes: ["required"]
                ])}
            </fieldset>

            <fieldset class="multiple-input-date no-future-date date-required">
                <legend id="birthdateLabel">${ui.message("registrationapp.patient.birthdate.label")}</legend>
                <h3>${ui.message("registrationapp.patient.birthdate.question")}</h3>
                ${ ui.includeFragment("uicommons", "field/multipleInputDate", [
                        label: "",
                        formFieldName: "birthdate",
                        left: true,
                        showEstimated: true
                  ])}
            </fieldset>

		<!-- photo -->
            <fieldset class="photo">
                <legend>${ui.message("registrationapp.photo.label")}</legend>
                <h3>${ui.message("registrationapp.photo.capturemessage.label")}</h3>
                
          <video id="video" width="200" height="150"></video>
          <canvas id="canvas"><img src="omrs.png" id="photo" alt="photo"></canvas>

          <p>
            <a class="button" href="#" id = "startbutton">
                 <input type="hidden" />
                <i class="icon-camera"></i>
            </a>
          </p>
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
                        <fieldset<% if(question.legend == "Person.address"){ %> class="requireOne"<% } %>>
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
        <div id="confirmation">
            <span id="confirmation_label" class="title">${ui.message("registrationapp.patient.confirm.label")}</span>
            <div class="before-dataCanvas"></div>
			<img src="" id="confirmPhoto" alt="">
			<input type="hidden" id="patientPhoto" name="patientPhoto"/>
            <div id="dataCanvas"></div>
            <div class="after-data-canvas"></div>
            <div id="confirmationQuestion">
                Confirm submission? <p style="display: inline"><input type="submit" class="confirm" value="Yes" /></p> or <p style="display: inline"><input id="cancelSubmission" class="cancel" type="button" value="No" /></p>
            </div>
        </div>
    </form>
</div>
