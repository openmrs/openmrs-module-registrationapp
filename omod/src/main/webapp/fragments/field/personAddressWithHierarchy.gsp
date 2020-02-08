<%
    ui.includeJavascript("registrationapp", "field/personAddressWithHierarchy.js")

    def parseAsBoolean = {
        if (it instanceof org.codehaus.jackson.node.BooleanNode) {
            return it.booleanValue
        }
        if (it instanceof java.lang.String) {
            return it.toBoolean()
        }
        return it;
    }

    def manualFields = config.manualFields ? config.manualFields : manualFields
%>
<div id="${ config.id }-container">

    <% if (config.shortcutFor) {
        def shortcutDisplay = ui.message(addressTemplate.nameMappings[config.shortcutFor])
    %>
        <p>
            <label><em>${ ui.message('registrationapp.addressHierarchyWidget.shortcut', shortcutDisplay) }</em></label>
            <input type="text" class="address-hierarchy-shortcut" size="60" placeholder="${ ui.message('registrationapp.addressHierarchyWidget.shortcut.instructions',ui.escapeAttribute(shortcutDisplay))}"/>
        </p>
    <% } %>

    <% levels.each { level ->
        def classes = [ "level" ]
        if (parseAsBoolean(config.required) && level.required) {
            classes.add("required")
        }
        def levelInitial = ""
        if (initialValue) {
            // setting this as "value" on the input is not sufficient to set the js state, but we do it anyway
            // so that these values are immediately visible on page load
            levelInitial = initialValue[level.addressField.name] ?: ""
        }
        // else set a default value if it exists
        else {
            levelInitial = addressTemplate.elementDefaults && addressTemplate.elementDefaults[level.addressField.name] ?
                addressTemplate.elementDefaults[level.addressField.name] : "";
        }
    %>
        <p>
            <label>${ ui.message(addressTemplate.nameMappings[level.addressField.name]) }</label>
            <input class="${ classes.join(" ") }" type="text" autocomplete="off" size="40" name="${ config.fieldMappings?.get(level.addressField.name) ?: level.addressField.name }" id="${ config.id }-${ level.addressField.name }" value="${ ui.escapeAttribute(levelInitial) }"/>
            ${ ui.includeFragment("uicommons", "fieldErrors", [fieldName: level.addressField.name]) }
        </p>
    <% } %>
</div>

<script type="text/javascript">

    var personAddressWithHierarchy = {
        id: null,
        container: null,
        initialValue: null,
        shortcutFor: null,
        manualFields: []
    }

    personAddressWithHierarchy.id = '${ config.id }';
    personAddressWithHierarchy.container = jq('#${ config.id }-container');
    <% if (config.shortcutFor) { %>
        personAddressWithHierarchy.shortcutFor = '${ ui.encodeJavaScript(config.shortcutFor) }';
    <% } %>
    <% if (manualFields) { %>
        <% manualFields.each { %>
            personAddressWithHierarchy.manualFields.push('${ it }');
        <% } %>
    <% } %>
    <% if (initialValue) { %>
        personAddressWithHierarchy.initialValue = ${ ui.toJson(initialValue) };
    <% } %>

    PersonAddressWithHierarchy(personAddressWithHierarchy);

</script>
