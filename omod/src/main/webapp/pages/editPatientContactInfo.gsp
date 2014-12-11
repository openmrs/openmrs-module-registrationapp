<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeJavascript("uicommons", "navigator/validators.js", Integer.MAX_VALUE - 19)
    ui.includeJavascript("uicommons", "navigator/navigator.js", Integer.MAX_VALUE - 20)
    ui.includeJavascript("uicommons", "navigator/navigatorHandlers.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/navigatorModels.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/exitHandlers.js", Integer.MAX_VALUE - 22);

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

	if (!returnUrl) {
		returnUrl = "/${contextPath}/coreapps/patientdashboard/patientDashboard.page?patientId=${patient.patientId}"
	}
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
        { label: "${ ui.format(patient.familyName) }, ${ ui.format(patient.givenName) }", link: "${returnUrl}" },
        { label: "${ ui.message("registrationapp.editPatientContactInfo.label") }", link: "${ ui.pageLink("registrationapp", "editPatientContactInfo") }" }
    ];
</script>

<div id="content" class="container">
    <h2>
        ${ ui.message("registrationapp.editPatientContactInfo.label") }
    </h2>

    <form class="simple-form-ui" method="POST" action="/${contextPath}/registrationapp/editPatientContactInfo.page?patientId=${patient.patientId}&returnUrl=${returnUrl}&appId=${app.id}">
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
                    if(field.type == 'personAddress'){
                        configOptions.addressTemplate = addressTemplate
                        configOptions.initialValue = patient.personAddress;
                    }else if(field.type == 'personAttribute'){
                        configOptions.initialValue = uiUtils.getAttribute(patient, field.uuid);
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
                ${ui.message("registrationapp.confirm")} <p style="display: inline"><input type="submit" class="confirm" value="${ui.message("general.yes")}" /></p> or <p style="display: inline"><input id="cancelSubmission" class="cancel" type="button" value="${ui.message("general.no")}" /></p>
            </div>
        </div>
    </form>
</div>
