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
           <% if (config.classes) { %>class="${ config.classes.join(' ') }" <% } %> 
           data-provide="typeahead" placeholder="Auto Suggest" dataItems="4" autocomplete="off" />
           
    ${ ui.includeFragment("uicommons", "fieldErrors", [ fieldName: config.formFieldName ]) }
    <% if (config.optional) { %>
        ${ ui.message("emr.optional") }
    <% } %>
</p>

<script type="text/javascript">

	jq('#${ config.id }-field').typeahead({
		source: function (query, process){
			jq.getJSON('${ ui.actionLink("registrationapp", "personName", "getSimilarNames") }',
		        {
		          'searchPhrase': query,
		          'formFieldName': '${ config.formFieldName }'
		        })
				.success(function(data) {
					process(JSON.parse(data));
				})
				.error(function(xhr, status, err) {
					alert('Name search AJAX error' + err);
				});
		}
	});
	
</script>