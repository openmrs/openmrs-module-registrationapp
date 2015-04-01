<%
    if (sessionContext.authenticated && !sessionContext.currentProvider) {
        throw new IllegalStateException("Logged-in user is not a Provider")
    }
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("registrationapp", "findPatient.css")

    def baseUrl = ui.pageLink("registrationapp", "findPatient", [appId: appId])
    def afterSelectedUrl = '/registrationapp/registrationSummary.page?patientId={{patientId}}&appId=' + appId

%>
${ ui.includeFragment("uicommons", "validationMessages")}

<script type="text/javascript">

    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("Patient.find") }", link: "${ ui.pageLink("registrationapp", "findPatient") }" }
    ];

    jq(function() {
        jq('#patient-search').focus();
    });

    var selectPatientHandler = {
        handle: function (row, widgetData) {
            var query = widgetData.lastQuery;
            history.replaceState({ query: query }, "", "${baseUrl}&search=" + query);
            location.href = emr.pageLink("registrationapp", "registrationSummary", { patientId: row.uuid, appId: '${appId}', search: query});
        }
    }

</script>


<h1>
    ${ ui.message("registrationapp.app.registerPatient.label") }
</h1>

${ ui.message("coreapps.searchPatientHeading") }
<div class="container">
    <div id="search-patient-div" class="search-div">
${ ui.includeFragment("coreapps", "patientsearch/patientSearchWidget",
        [ afterSelectedUrl: afterSelectedUrl,
          rowSelectionHandler: "selectPatientHandler",
          initialSearchFromParameter: "search",
          showLastViewedPatients: 'false' ])}
    </div>
    <div id="register-patient-div" class="search-div">
        <a href="${ ui.pageLink("registrationapp", "registerPatient", [ appId: appId ]) }">
            <button id="register-patient-button">${ ui.message("registrationapp.new.registration") }</button>
        </a>
    </div>
</div>

<br>
<br>

<div class="container">

    <div id="encounters-list">
        <h3>${ ui.message("Previous patients registered at this location") }</h3>
        <table id="encounters-table">
            <thead>
            <tr>
                <th>${ ui.message("coreapps.patient.identifier") }</th>
                <th>${ ui.message("coreapps.person.name") }</th>
                <th>${ ui.message("coreapps.gender") }</th>
                <th>${ ui.message("coreapps.birthdate") }</th>
                <th>${ ui.message("coreapps.patientDashBoard.date") }</th>
            </tr>
            </thead>
            <tbody>
            <% if ( (appEncounters == null)
                    || (appEncounters!= null && appEncounters.size() == 0)) { %>
            <tr>
                <td colspan="5">${ ui.message("uicommons.dataTable.emptyTable") }</td>
            </tr>
            <% } %>
            <% appEncounters.sort{ it.encounterDatetime }.reverse().each { encounter ->
                // def minutesAgo = (long) ((System.currentTimeMillis() - enc.encounterDatetime.time) / 1000 / 60)
            %>
            <tr>
                <td>${ encounter.patient.patientIdentifier }</td>
                <td>
                    <a href="${ ui.pageLink("registrationapp", "registrationSummary", [ patientId: encounter.patient.patientId ]) }">
                    ${ ui.format((encounter.patient)) }
                </td>
                <td>${ ui.format( encounter.patient.gender) }</td>
                <td>${ ui.format( encounter.patient.birthdate) }</td>
                <td>${ ui.format(encounter.encounterDatetime) }</td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
</div>

<% if (appEncounters !=null && appEncounters.size() > 0) { %>
${ ui.includeFragment("uicommons", "widget/dataTable", [ object: "#encounters-table",
                                                         options: [
                                                                 bFilter: false,
                                                                 bJQueryUI: true,
                                                                 bLengthChange: false,
                                                                 iDisplayLength: 5,
                                                                 sPaginationType: '\"full_numbers\"',
                                                                 bSort: false,
                                                                 sDom: '\'ft<\"fg-toolbar ui-toolbar ui-corner-bl ui-corner-br ui-helper-clearfix datatables-info-and-pg \"ip>\''
                                                         ]
]) }
<% } %>