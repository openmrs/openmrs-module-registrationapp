<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeJavascript("uicommons", "handlebars/handlebars.min.js", Integer.MAX_VALUE - 1);
    ui.includeJavascript("uicommons", "navigator/validators.js", Integer.MAX_VALUE - 19)
    ui.includeJavascript("uicommons", "navigator/navigator.js", Integer.MAX_VALUE - 20)
    ui.includeJavascript("uicommons", "navigator/navigatorHandlers.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/navigatorModels.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/navigatorTemplates.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/exitHandlers.js", Integer.MAX_VALUE - 22);

    def genderOptions = [ [label: ui.message("emr.gender.M"), value: 'M'],
            [label: ui.message("emr.gender.F"), value: 'F'] ]

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
       KeyboardController();
    });
</script>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.format(patient.familyName) }, ${ ui.format(patient.givenName) }", link: "${returnUrl}"},
        { label: "${ ui.message("registrationapp.editPatientDemographics.label") }", link: "${ ui.pageLink("registrationapp", "editPatientDemographics") }" }
    ];
</script>

<div id="content" class="container">
    <h2>
        ${ ui.message("registrationapp.editPatientDemographics.label") }
    </h2>

    <form class="simple-form-ui" method="POST" action="/${contextPath}/registrationapp/editPatientDemographics.page?patientId=${patient.patientId}&returnUrl=${returnUrl}">
        <section id="demographics" class="non-collapsible">
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
                        ignoreCheckForSimilarNames: true,
                        initialValue: initialNameFieldValue,
                        classes: [(name == "givenName" || name == "familyName") ? "required" : ""]
                ])}

                <% } %>
                <input type="hidden" name="preferred" value="true"/>
            </fieldset>

            <fieldset id="demographics-gender">
                <legend id="genderLabel">${ ui.message("emr.gender") }</legend>
                ${ ui.includeFragment("uicommons", "field/dropDown", [
                        label: ui.message("registrationapp.patient.gender.question"),
                        emptyOptionLabel: "uicommons.select",
                        formFieldName: "gender",
                        options: genderOptions,
                        classes: ["required"],
                        initialValue: patient.gender
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
        </section>

        <div id="confirmation">
            <span class="title">${ui.message("registrationapp.patient.confirm.label")}</span>
            <div class="before-dataCanvas"></div>
            <div id="dataCanvas"></div>
            <div class="after-data-canvas"></div>
            <div id="confirmationQuestion">
                ${ui.message("registrationapp.confirm")} <p style="display: inline"><input type="submit" class="confirm right" value="${ui.message("registrationapp.patient.confirm.label")}" /></p><p style="display: inline"><input id="cancelSubmission" class="cancel" type="button" value="${ui.message("registrationapp.cancel")}" /></p>
            </div>
        </div>
    </form>
</div>
