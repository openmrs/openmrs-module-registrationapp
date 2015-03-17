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
        </div>
    </div>
</div>