<%
    if (sessionContext.authenticated && !sessionContext.currentProvider) {
        throw new IllegalStateException("Logged-in user is not a Provider")
    }

    ui.decorateWith("appui", "standardEmrPage")

%>

<script type="text/javascript">

    var breadcrumbs = _.compact(_.flatten([
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.encodeJavaScript(ui.format(patient)) }" ,
            link: '${ui.pageLink("registrationapp", "registrationSummary", [patientId: patient.id, appId: registrationAppId])}'},
        { label: '${ ui.message("registrationapp.biometrics.edit") }' }
    ]))

    var patient = { id: ${ patient.id }, uuid: '${ patient.uuid }' };

    var deleteBiometricsDialog;

    function initDeleteBiometricsDialog(deleteButton) {

        // expected import format = "identifier:identifier-uuid"
        // "identifier" is used to delete the biometrics on in the matching server
        // "identifier-uuid" is to void the identifier on the OpenMRS side
        var identifier = deleteButton.attr('id').split('|');

        deleteBiometricsDialog = emr.setupConfirmationDialog({
            selector: '#delete-biometrics-dialog',
            actions: {
                confirm: function() {

                    deleteBiometricsDialog.close();

                    // delete the identifier on the matching server
                    emr.getFragmentActionWithCallback('registrationapp', 'biometrics/biometrics', 'delete'
                        , { uuid: identifier[0] }
                        , function(data) {
                            // if this is successful, void the identifier in the database (using it's uuid)
                            if (data && data.success) {

                                jq.ajax({
                                    url: '/' + OPENMRS_CONTEXT_PATH + '/ws/rest/v1/patient/' + patient.uuid + '/identifier/' + identifier[1] + '?!purge',
                                    type: 'DELETE',
                                    success: function() {
                                        // remove the row from the table
                                        jq(deleteButton).closest('tr').hide();
                                    }
                                })

                            }
                        });
                },
                cancel: function() {
                    deleteBiometricsDialog.close();
                }
            }
        });
    }

    jq(function() {
        jq('.delete').click(function(){
            initDeleteBiometricsDialog(jq(this));
            deleteBiometricsDialog.show();
        })
    })

</script>


${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient, activeVisit: null, appContextModel: null ]) }

<div class="info-body summary-section">
    ${ ui.message(status) }

    <% if (identifierToSubjectMap) { %>
        <table>
            <tr>
                <th>${ ui.message('registrationapp.biometrics.label') }</th>
                <th>${ ui.message('registrationapp.biometrics.dateCollected') }</th>
                <th>&nbsp;</th>
            </tr>

            <% identifierToSubjectMap.each { identifier, subject ->
                    def fingers = []
                    subject.fingerprints.each {
                        fingers.push(ui.message('registrationapp.biometrics.' + it.type) != 'registrationapp.biometrics.' + it.type ? ui.message('registrationapp.biometrics.' + it.type) : it.type)
                    }
            %>
            <tr>
                <td>${ fingers.join(", ") }</td>
                <td>${ ui.format(identifier.dateCreated) }</td>
                <td><button class="delete cancel" id="${ identifier.identifier }|${ identifier.uuid }">${ ui.message('coreapps.delete') }</button></td>
            </tr>
            <% } %>
        </table>
    <% } %>

    <br/>

    <!-- TODO can we avoid hardcoding "patient-biometrics-section"? -->
    <button class="confirm"  onclick="location.href='${ui.pageLink("registrationapp", "editSection", [patientId: patient.id, sectionId: "patient-biometrics-section", appId: registrationAppId, returnUrl: ui.pageLink("registrationapp", "biometrics/editBiometrics", [patientId: patient.id, registrationAppId: registrationAppId]) ])}'">
        ${ ui.message('registrationapp.biometrics.add')}
    </button>
    <button class="cancel" onclick="location.href='${ui.pageLink("registrationapp", "registrationSummary", [patientId: patient.id, appId: registrationAppId])}'">
        ${ ui.message('uicommons.return')}
    </button>

</div>


<div id="delete-biometrics-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ ui.message("registrationapp.biometrics.delete") }</h3>
    </div>
    <div class="dialog-content">
        <p class="dialog-instructions">${ ui.message("registrationapp.biometrics.confirmDelete") }</p>
        <button class="confirm right">${ ui.message("uicommons.yes") }</button>
        <button class="cancel">${ ui.message("uicommons.no") }</button>
    </div>
</div>