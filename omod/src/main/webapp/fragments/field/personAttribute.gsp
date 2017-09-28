<%  
    if(codedPersonAttribute){
    	config.conceptId = foreignKey%>
    	${ ui.includeFragment("registrationapp", "field/concept", config)}
    <%} else{%>
    	${ ui.includeFragment("uicommons", "field/text", config)}
    <%}
%>