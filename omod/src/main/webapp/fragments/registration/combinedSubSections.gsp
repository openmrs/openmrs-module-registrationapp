<%
  def section = config.section
  def questions = config.questions
  def identifierSectionFound = config.identifierSectionFound
  def allowManualIdentifier = config.allowManualIdentifier
  def nameTemplate = config.nameTemplate
  def patient = config.patient
  def ui = config.ui
  def minAgeYear = config.minAgeYear
  def maxAgeYear = config.maxAgeYear
  def genderOptions = config.genderOptions
%>

 <% if (section.id == 'demographics') { %>
     <div id="demographics-name" style="display:inline-block;">

         <div>
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
                     initialValue: initialNameFieldValue,
                     classes: [(name == "givenName" || name == "familyName") ? "required" : ""]
             ])}

             <% } %>
         </div>

         <% if (allowUnknownPatients) { %>

         <!-- TODO: fix this horrible method of making this line up properly -->
         <div style="display:inline-block">
             <!-- note that we are deliberately not including this in a p tag because we don't want the handler to pick it up as an actual field -->
             <nobr>
                 <input id="checkbox-unknown-patient" type="checkbox"/>
                 <label for="checkbox-unknown-patient">${ui.message("registrationapp.patient.demographics.unknown")}</label>
             </nobr>
         </div>

         <% } %>


         <input type="hidden" name="preferred" value="true"/>
     </div>

     <div id="demographics-gender" style="display:inline-block;">
         
         <h3>${ui.message("registrationapp.patient.gender.question")}</h3>
         ${ ui.includeFragment("uicommons", "field/dropDown", [
                 id: "gender",
                 formFieldName: "gender",
                 options: genderOptions,
                 classes: ["required"],
                 initialValue: patient.gender,
                 hideEmptyLabel: true,
                 expanded: true
         ])}
         <!-- we "hide" the unknown flag here since gender is the only field not hidden for an unknown patient -->
         <input id="demographics-unknown" type="hidden" name="unknown" value="false"/>
     </div>

     <div id="demographics-birthdate" class="multiple-input-date date-required no-future-date">
        
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
     </div>
 <% } %>

 <!-- allow customization of additional question in the patient identification section, if it is included -->
 <% if (section.id == 'patient-identification-section') {
     identifierSectionFound = true; %>
     <% if (allowManualIdentifier) { %>
         ${ ui.includeFragment("registrationapp", "field/allowManualIdentifier", [
                 identifierTypeName: ui.format(primaryIdentifierType)
         ])}
     <% } %>
 <% } %>

 <% questions.each { question ->
     def fields=question.fields
     def classes = "";
     if (question.legend == "Person.address") {
         classes = "requireOne"
     }
     if (question.cssClasses) {
         classes = classes + (classes.length() > 0 ? ' ' : '') + question.cssClasses.join(" ")
     }
 %>
     <div id="${question.id}" style="display:inline-block;"
             <% if (classes.length() > 0) { %> class="${classes}" <% } %>
             <% if (question.fieldSeparator) { %> field-separator="${question.fieldSeparator}" <% } %>
             <% if (question.displayTemplate) { %> display-template="${ui.escapeAttribute(question.displayTemplate)}" <% } %>
     >
         <% if(question.legend == "Person.address"){ %>
             ${ui.includeFragment("uicommons", "fieldErrors", [fieldName: "personAddress"])}
         <% } %>
         <% if(question.header) { %>
                 <h3>${ui.message(question.header)}</h3>
         <% } %>

         <% fields.each { field ->
             def configOptions = (field.fragmentRequest.configuration != null) ? field.fragmentRequest.configuration : [:] ;
             configOptions.label = ui.message(field.label)
             configOptions.formFieldName = field.formFieldName
             configOptions.left = true
             configOptions.classes = field.cssClasses

             if (field.type == 'personAddress') {
                 configOptions.addressTemplate = addressTemplate
             }

             if (field.type == 'personRelationships') {
                 configOptions.relationshipTypes = relationshipTypes
             }
         %>
             ${ ui.includeFragment(field.fragmentRequest.providerName, field.fragmentRequest.fragmentId, configOptions)}
         <% } %>
     </div>
 <% } %>
                 
           