<%
    def returnUrl = "/${contextPath}/registrationapp/registrationSummary.page?patientId=${patient.patient.id}"

%>


<div class="clear"></div>
<div class="container">
    <div class="dashboard clear">
        <div class="info-container column">
            ${ ui.includeFragment("registrationapp", "summary/section", [patient: patient, appId: "registrationapp.registerPatient", sectionId: "demographics"]) }

            <% if (registrationFragments) {
                registrationFragments.each { %>
            ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, [patientId: patient.patient.id, app: it.appId, returnUrl: returnUrl ])}
            <% }
            } %>
        </div>

        <div class="info-container column">
            ${ ui.includeFragment("registrationapp", "summary/section", [patient: patient, appId: "registrationapp.registerPatient", sectionId: "contactInfo"]) }

            <% if (secondColumnFragments) {
                secondColumnFragments.each { %>
            ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, [patientId: patient.patient.id, app: it.appId, returnUrl: returnUrl ])}
            <% }
            } %>

        </div>

        <div class="action-container column">
            <div class="action-section">
                <ul>
                    <h3>${ ui.message("coreapps.clinicianfacing.overallActions") }</h3>
                    <%
                        overallActions.each { ext -> %>
                            <a href="${ ui.escapeJs(ext.url("/" + ui.contextPath(), appContextModel, ui.thisUrl())) }" id="${ ext.id }">
                                <li>
                                    <i class="${ ext.icon }"></i>
                                    ${ ui.message(ext.label) }
                                </li>
                            </a>
                    <% } %>
                </ul>
            </div>
        </div>
    </div>
</div>