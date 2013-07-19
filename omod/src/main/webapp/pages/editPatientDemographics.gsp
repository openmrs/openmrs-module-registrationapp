<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeJavascript("uicommons", "navigator/validators.js", Integer.MAX_VALUE - 19)
    ui.includeJavascript("uicommons", "navigator/navigator.js", Integer.MAX_VALUE - 20)
    ui.includeJavascript("uicommons", "navigator/navigatorHandlers.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/navigatorModels.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/exitHandlers.js", Integer.MAX_VALUE - 22);

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
        { label: "${ ui.message("registrationapp.patientDashboard")}", link: "/${contextPath}/coreapps/patientdashboard/patientDashboard.page?patientId=${patient.patientId}" },
        { label: "${ ui.message("registrationapp.editPatientDemographics.label") }", link: "${ ui.pageLink("registrationapp", "editPatientDemographics") }" }
    ];
</script>

<div id="content" class="container">
    <h2>
        ${ ui.message("registrationapp.editPatientDemographics.label") }
    </h2>

    <form class="simple-form-ui" method="POST">
        <section id="demographics">
            <span class="title">${ui.message("registrationapp.patient.demographics.label")}</span>

            <fieldset>
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

            <fieldset>
                <legend>${ ui.message("emr.gender") }</legend>
                ${ ui.includeFragment("uicommons", "field/radioButtons", [
                        label: ui.message("registrationapp.patient.gender.question"),
                        formFieldName: "gender",
                        maximumSize: 3,
                        options: genderOptions,
                        classes: ["required"],
                        initialValue: patient.gender
                ])}
            </fieldset>

            <fieldset class="multiple-input-date no-future-date">
                <legend>${ui.message("registrationapp.patient.birthdate.label")}</legend>
                <h3>${ui.message("registrationapp.patient.birthdate.question")}</h3>
                ${ ui.includeFragment("uicommons", "field/multipleInputDate", [
                        label: "",
                        formFieldName: "birthdate",
                        left: true,
                        classes: ["required"],
                        initialValue: patient.birthdate
                ])}
            </fieldset>

        </section>

        <div id="confirmation">
            <span class="title">${ui.message("registrationapp.patient.confirm.label")}</span>
            <div class="before-dataCanvas"></div>
            <div id="dataCanvas"></div>
            <div class="after-data-canvas"></div>
            <div id="confirmationQuestion">
                Confirm submission? <p style="display: inline"><input type="submit" class="confirm" value="${ui.message("general.yes")}" /></p> or <p style="display: inline"><input id="cancelSubmission" class="cancel" type="button" value="${ui.message("general.no")}" /></p>
            </div>
        </div>
    </form>
</div>
