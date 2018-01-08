<%
	import groovy.json.*
    config.require("formFieldName")
    config.require("conceptId")
    
    def maxResults = config.maxResults ? config.maxResults : 20;
    def conceptAnswers = new JsonBuilder(conceptAnswers);
%>

<script type="text/javascript">
    jq(function() {
    var consAnswers = ${conceptAnswers};
        jq('#${ config.id }-field').autocomplete({
            source: function(request, response){
            	var respData = jq.grep(consAnswers, function(elem){
					return elem.value.match(new RegExp(request.term, 'i')) != null;
				});
				respData = respData.slice(0,${maxResults});
				response(respData);
            },
            autoFocus: false,
            minLength: 1,
            delay: 300
        }); 
    });
</script>

<p <% if (config.left) { %> class="left" <% } %> >

    <label for="${ config.id }-field">
        ${ config.label } <% if (config.classes && config.classes.contains("required")) { %><span>(${ ui.message("emr.formValidation.messages.requiredField.label") })</span><% } %>
    </label>

    <input type="text" id="${ config.id }-field" name="${ config.formFieldName }" value="${ config.initialValue ?: '' }" size="40"
           <% if (config.classes) { %>class="${ config.classes.join(' ') }" <% } %> />

	${ ui.includeFragment("uicommons", "fieldErrors", [ fieldName: config.formFieldName ]) }
    <% if (config.optional) { %>
    ${ ui.message("emr.optional") }
    <% } %>
</p>