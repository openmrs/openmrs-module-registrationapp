<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeJavascript("uicommons", "handlebars/handlebars.min.js", Integer.MAX_VALUE - 1);
    ui.includeJavascript("uicommons", "navigator/validators.js", Integer.MAX_VALUE - 19)
    ui.includeJavascript("uicommons", "navigator/navigator.js", Integer.MAX_VALUE - 20)
    ui.includeJavascript("uicommons", "navigator/navigatorHandlers.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/navigatorModels.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/navigatorTemplates.js", Integer.MAX_VALUE - 21)
    ui.includeJavascript("uicommons", "navigator/exitHandlers.js", Integer.MAX_VALUE - 22);

    ui.includeCss("registrationapp", "editSection.css")

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

    Calendar cal = Calendar.getInstance()
    def maxAgeYear = cal.get(Calendar.YEAR)
    def minAgeYear = maxAgeYear - 120

    if (!returnUrl) {
        returnUrl = "/${contextPath}/coreapps/patientdashboard/patientDashboard.page?patientId=${patient.patientId}"
    }

    def cleanup = {
        return (it instanceof org.codehaus.jackson.node.TextNode) ? it.textValue : it;
    }
%>
${ ui.includeFragment("uicommons", "validationMessages")}

<script type="text/javascript">
    var NavigatorController;
    jQuery(function() {
        NavigatorController = KeyboardController();
        jq('#cancelSubmission').unbind(); // unbind the functionality built into the navigator to return to top of the form
        jq('#cancelSubmission').click(function(event){
            window.location='${returnUrl}';
        })
    });
</script>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.escapeJs(ui.format(patient)) }", link: "${returnUrl}" },
        { label: "${ ui.message(section.label) }" }
    ];
</script>

<div id="content" class="container">
    <h2>
        ${ ui.message(section.label)  }
    </h2>

    <% if (returnUrl) { %>
    <div id="exit-form-container">
        <a href="${ ui.escapeAttribute(returnUrl) }">
            <i class="icon-signout small"></i>
            ${ ui.message("htmlformentryui.exitForm") }
        </a>
    </div>
    <% } %>

    <form class="simple-form-ui" method="POST" action="/${contextPath}/registrationapp/editSection.page?patientId=${patient.patientId}&returnUrl=${returnUrl}&appId=${app.id}&sectionId=${section.id}">
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
                            initialValue: patient.gender,
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
                    def fields=question.fields
                %>
                <fieldset
                    <% if (question.fieldSeparator) { %> field-separator="${question.fieldSeparator}" <% } %>
                    <% if (question.displayTemplate) { %> display-template="${ui.escapeAttribute(question.displayTemplate)}" <% } %>
                >
                    <legend>${ ui.message(question.legend)}</legend>
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

        <div id="confirmation">
            <span class="title">${ui.message("registrationapp.patient.confirm.label")}</span>
            <div class="before-dataCanvas"></div>
            <div id="dataCanvas"></div>
            <div class="after-data-canvas"></div>
            <div id="confirmationQuestion">
                ${ui.message("registrationapp.confirm")}
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
