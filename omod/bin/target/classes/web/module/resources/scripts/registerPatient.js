jq = jQuery;
 
jq(function() {
	reviewSimilarPatients = emr.setupConfirmationDialog({
        selector: '#reviewSimilarPatients',
        actions: {
            cancel: function() {
                reviewSimilarPatients.close();
            }
        }
    });

	jq('#reviewSimilarPatientsButton').click(function() {
		reviewSimilarPatients.show();
		return false;
	});
	
	getSimilarPatients = function(field) {
        jq('.date-component').trigger('blur');
		var formData = jq('#registration').find('input, textarea, select').not('.ignore').serialize();
		jq.getJSON(getSimilarPatientsLink, formData)
	    .success(function(data) {
	    	if (data.length == 0) {
	    		jq("#similarPatients").hide();
	    	} else {
	    		jq("#similarPatients").show();
	    	}
	    	
	        jq('#similarPatientsCount').text(data.length);
	        var similarPatientsSelect = jq('#similarPatientsSelect');
	        similarPatientsSelect.empty();
	        for (index in data) {
	            var item = data[index];
	            var link = patientDashboardLink;
	            link += '?patientId=' + item.patientId;
	            var row = '<li style="width: auto" onclick="location.href=\'' + link + '\'">';
	            row += item.givenName + ' ' + item.familyName + ' | ' + item.patientIdentifier.identifier + ' | ' + item.gender + ' | ' + item.birthdate + ' | ' + item.personAddress;
	            row += '</li>';
	            similarPatientsSelect.append(row);
	        }
	    })
	    .error(function(xhr, status, err) {
	        alert('AJAX error ' + err);
	    });
	};
	
	jq('input').change(getSimilarPatients);
    jq('select').change(getSimilarPatients);

    jq('body').keydown(function(event) {
        if (jq('#demographics-gender').hasClass('focused')) {
            if (event.keyCode == '70') {
                jq('input[name=gender][value=F]').prop("checked",true);
            } else if (event.keyCode == '77') {
                jq('input[name=gender][value=M]').prop("checked",true);
            }
        }
    });
    
});