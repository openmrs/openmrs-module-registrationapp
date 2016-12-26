<%
    ui.includeJavascript("uicommons", "angular.js")
    ui.includeJavascript("uicommons", "angular-common.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-ui/ui-bootstrap-tpls-0.6.0.min.js")
    ui.includeJavascript("uicommons", "services/patientService.js")
    ui.includeJavascript("registrationapp", "field/personRelationship.js")
%>

<div ng-app="personRelationships" ng-controller="PersonRelationshipController">

    <div ng-repeat="relationship in relationships">
        <p class="left">
            <select name="relationship_type" class="rel_type">
                <option value="">Select Relationship Type</option>
                <% relationshipTypes.each { type -> %>
                <option value="${type.uuid}-A">${type.aIsToB}</option>
                <% } %>
                <% relationshipTypes.each { type -> %>
                <% if (type.aIsToB != type.bIsToA) { %>
                <option value="${type.uuid}-B">${type.bIsToA}</option>
                <% } %>
                <% } %>
            </select>
        </p>

        <p class="left">
            <input type="text" class="person-typeahead" placeholder="Patient Name"
                   ng-model="relationship.name"
                   typeahead="patient.name for patient in getPatients(\$viewValue) | filter: \$viewValue | limitTo:5"
                   typeahead-min-length="3"
                   typeahead-on-select="selectPatient(\$item, \$index)"/>
            <input type="text" name="other_person_uuid" ng-model="relationship.uuid" ng-show="false"/>
        </p>

        <p style="padding: 10px">
            <a ng-click="addNewRelationship()">
                <i class="icon-plus-sign edit-action"></i>
            </a>
            <a ng-click="removeRelationship(relationship)">
                <i class="icon-minus-sign edit-action"></i>
            </a>
        </p>
    </div>
</div>