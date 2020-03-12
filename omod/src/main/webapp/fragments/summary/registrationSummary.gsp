<%
    def returnUrl = "/${contextPath}/registrationapp/registrationSummary.page?patientId=${patient.patient.id}&appId=${appId}"

%>


<div class="clear"></div>
<div class="dashboard clear">
        <div class="info-container column">
            ${ ui.includeFragment("registrationapp", "summary/section", [patient: patient, appId: appId, sectionId: "demographics"]) }

            <% if (firstColumnFragments) {
                firstColumnFragments.each {
                    // create a base map from the fragmentConfig if it exists, otherwise just create an empty map
                    def configs = [:];
                    if(it.extensionParams.fragmentConfig != null){
                        configs = it.extensionParams.fragmentConfig;
                    }
                    configs << [patient: patient, patientId: patient.patient.id, app: it.appId, appId: appId, returnUrl: returnUrl ]
            %>
                    ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, configs)}
            <% }
            } %>
        </div>

        <div class="info-container column">
            ${ ui.includeFragment("registrationapp", "summary/section", [patient: patient, appId: appId, sectionId: "contactInfo"]) }

            <% if (secondColumnFragments) {
                secondColumnFragments.each {
                    // create a base map from the fragmentConfig if it exists, otherwise just create an empty map
                    def configs = [:];
                    if(it.extensionParams.fragmentConfig != null){
                        configs = it.extensionParams.fragmentConfig;
                    }
                    configs << [patient: patient, patientId: patient.patient.id, app: it.appId, appId: appId, returnUrl: returnUrl ]
            %>
                    ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, configs)}
            <% }
            } %>

        </div>
    </div>
