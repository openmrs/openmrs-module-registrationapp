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

		var formData = jq('#registration').serialize();
		jq.getJSON(emr.fragmentActionLink("registrationapp", "matchingPatients", "getSimilarPatients", { appId: appId }), formData)
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

    $("#confirmation").on('select', function(confSection) {
        var submitButton = $('#confirmationQuestion .submitButton');
        submitButton.prop('disabled', true);
        var formData = jq('#registration').serialize();

        $.getJSON(emr.fragmentActionLink("registrationapp", "matchingPatients", "getExactPatients", { appId: appId }), formData)
            .success(function(response) {
                console.log(response);
                if (response.length == 0) {
                    $('#exact-matches').hide();
                } else {
                    $('#exact-matches').show();
                }
                submitButton.prop('disabled', false);
            });
    });
});