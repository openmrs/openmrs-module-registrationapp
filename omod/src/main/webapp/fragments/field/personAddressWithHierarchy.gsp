<%
    ui.includeJavascript("registrationapp", "field/personAddressWithHierarchy.js")
%>
<div id="${ config.id }-container">

    <% if (config.shortcutFor) {
        def shortcutDisplay = ui.message(addressTemplate.nameMappings[config.shortcutFor])
    %>
        <p>
            <label><em>(Optional) Quick search by ${ shortcutDisplay }</em></label>
            <input type="text" class="address-hierarchy-shortcut" size="60" placeholder="${ui.escapeAttribute(shortcutDisplay)}, or skip to search manually"/>
        </p>
    <% } %>

    <% levels.each { level ->
        def classes = [ "level" ]
        if (level.required) {
            classes.add("required")
        }
    %>
        <p>
            <label>${ ui.message(addressTemplate.nameMappings[level.addressField.name]) }</label>
            <input class="${ classes.join(" ") }" type="text" autocomplete="off" size="40" name="${ level.addressField.name }" id="${ config.id }-${ level.addressField.name }"/>
            ${ ui.includeFragment("uicommons", "fieldErrors", [fieldName: level.addressField.name]) }
        </p>
    <% } %>
</div>

<script type="text/javascript">
    personAddressWithHierarchy.id = '${ config.id }';
    personAddressWithHierarchy.container = jq('#${ config.id }-container');
    <% if (config.shortcutFor) { %>
        personAddressWithHierarchy.shortcutFor = '${ ui.escapeJs(config.shortcutFor) }';
    <% } %>
    <% if (config.manualFields) { %>
        <% config.manualFields.each { %>
            personAddressWithHierarchy.manualFields.push(${ it }); // since this comes from json config, it's a jackson text node, so we don't put quotes
        <% } %>
    <% } %>
</script>