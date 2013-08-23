<%
    ui.includeJavascript("uicommons", "typeahead.js");

    config.require("label")
    config.require("formFieldName")

    def dataItems = 3;
    if (config.dataItems) {
        dataItems = config.dataItems;
    }
%>

<p <% if (config.left) { %> class="left" <% } %> >

    <label for="${ config.id }-field">
        ${ config.label } <% if (config.classes && config.classes.contains("required")) { %><span>(${ ui.message("emr.formValidation.messages.requiredField.label") })</span><% } %>
    </label>

    <input type="text" id="${ config.id }-field" name="${ config.formFieldName }" value="${ config.initialValue ?: '' }"
           <% if (config.classes) { %>class="${ config.classes.join(' ') }" <% } %> />

    ${ ui.includeFragment("uicommons", "fieldErrors", [ fieldName: config.formFieldName ]) }
    <% if (config.optional) { %>
    ${ ui.message("emr.optional") }
    <% } %>
</p>

<% if (!config.ignoreCheckForSimilarNames) { %>
<script type="text/javascript">

    jq(function() {
        jq("#${ config.id }-field" ).autocomplete({
            source: function( request, response ) {
                jq.ajax({
                    url: "${ ui.actionLink("registrationapp", "personName", "getSimilarNames") }",
                    dataType: "json",
                    data: {
                        'searchPhrase': request.term,
                        'formFieldName': '${ config.formFieldName }'
                    },
                    success: function( data ) {
                        response( jq.map( data.names, function( item ) {
                            return {
                                label: item,
                                value: item
                            }
                        }));
                    }
                });
            },
            minLength: 1
        });
    });

</script>
<% } %>