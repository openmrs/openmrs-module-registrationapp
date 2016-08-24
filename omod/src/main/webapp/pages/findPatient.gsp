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
        { label: "${ ui.message("registrationapp.findPatient") }", link: "${ ui.pageLink("registrationapp", "findPatient") }" }
    ];

    jq(function() {
        jq('#patient-search').focus();
    });

    var selectPatientHandler = {
        handle: function (row, widgetData) {
            var query = widgetData.lastQuery;
            history.replaceState({ query: query }, "", "${baseUrl}&search=" + query);
            location.href = emr.pageLink("registrationapp", "registrationSummary", { patientId: row.uuid, appId: '${appId}', search: query, breadcrumbOverride: '${breadcrumbOverride}' });
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
        <a href="${ ui.pageLink("registrationapp", "registerPatient", [ appId: appId, breadcrumbOverride: breadcrumbOverride ]) }">
            <button id="register-patient-button">${ ui.message("registrationapp.new.registration") }</button>
        </a>
    </div>
</div>


<% if (includeFragments) {
    includeFragments.each { %>
        ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, it.extensionParams.fragmentConfig) }
    <%  }
} %>

<br>
<br>

<% if (mostRecentRegistrationEncounters != null) { %>
    <div class="container">
        <div id="encounters-list">
            <h3>${ ui.message("registrationapp.previouslyRegisteredPatients") }</h3>
            <table id="encounters-table">
                <thead>
                <tr>
                    <th>${ ui.message("coreapps.patient.identifier") }</th>
                    <% if (paperRecordIdentifierDefinitionAvailable) { %>
                        <th>${ ui.message("paperrecord.archivesRoom.recordNumber.label") }</th>
                    <% } %>
                    <th>${ ui.message("coreapps.person.name") }</th>
                    <th>${ ui.message("coreapps.gender") }</th>
                    <th>${ ui.message("coreapps.birthdate") }</th>
                    <th>${ ui.message("registrationapp.encounterDate") }</th>
                    <th>${ ui.message("registrationapp.dateCreated") }</th>
                </tr>
                </thead>
                <tbody>
                <% if (mostRecentRegistrationEncounters.size() == 0) { %>
                <tr>
                    <td colspan="6">${ ui.message("uicommons.dataTable.emptyTable") }</td>
                </tr>
                <% } %>
                <% mostRecentRegistrationEncounters.sort{ it.dateCreated }.reverse().each { encounter ->
                    // def minutesAgo = (long) ((System.currentTimeMillis() - enc.encounterDatetime.time) / 1000 / 60)
                %>
                <tr>
                    <td>${ encounter.patient.patientIdentifier }</td>
                    <% if (paperRecordIdentifierDefinitionAvailable) { %>
                        <td>${  paperRecordIdentifierMap.get(encounter.patient.patientId) ? ui.format( paperRecordIdentifierMap.get(encounter.patient.patientId) ) : ''}</td>
                    <% } %>
                    <td>
                        <a href="${ ui.pageLink("registrationapp", "registrationSummary", [ patientId: encounter.patient.patientId, appId: appId,  breadcrumbOverride: breadcrumbOverride ]) }">
                        ${ ui.format((encounter.patient)) }
                    </td>
                    <td>${ ui.format( encounter.patient.gender) }</td>
                    <td>${ ui.format( encounter.patient.birthdate) }</td>
                    <td>${ ui.format(encounter.encounterDatetime) }</td>
                    <td>${ ui.format(encounter.dateCreated) }</td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </div>
    <% if (mostRecentRegistrationEncounters.size() > 0) { %>
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
<% } %>