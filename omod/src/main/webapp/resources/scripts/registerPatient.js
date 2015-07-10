jq = jQuery;

// TODO refactor this all into something cleaner

var NavigatorController;

jq(function() {
    NavigatorController = new KeyboardController();

    /* Similar patient functionality */
    reviewSimilarPatients = emr.setupConfirmationDialog({
        selector: '#reviewSimilarPatients',
        actions: {
            cancel: function () {
                reviewSimilarPatients.close();
            }
        }
    });

    jq('#reviewSimilarPatientsButton').click(function () {
        var slideView = $("#similarPatientsSlideView");
        slideView.slideToggle();

        return false;
    });

    getSimilarPatients = function (field) {
        var focusedField = $(':focus');
        jq('.date-component').trigger('blur');

        var formData = jq('#registration').serialize();
        jq.getJSON(emr.fragmentActionLink("registrationapp", "matchingPatients", "getSimilarPatients", { appId: appId }), formData)
            .success(function (data) {
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
                    var isMpi = false;
                    if (data[index].mpiPatient != null && data[index].mpiPatient == true) {
                        isMpi = true;
                    }
                    var link;
                    if (isMpi){
                        link = 'execute_script_which_will_request_service_to_import_patient_from_mpi_to_local_DB_and_redirect_to_patient_info';
                    }else {
                        link = patientDashboardLink;
                        link += '?patientId=' + item.patientId;
                    }
                    var container = $('#matchedPatientTemplates div');
                    var cloned = container.clone();
                    cloned.find('.name').append(item.givenName + ' ' + item.familyName + ' (' + item.gender + ') ' + item.birthdate);
                    cloned.find('.address').append(item.personAddress);
                    cloned.find('.identifier').append(item.patientIdentifier.identifier);

                    var button;
                    var clone_button;
                    if (isMpi){
                        button = $('#matchedPatientTemplates .mpi_button');
                    }else{
                        button = $('#matchedPatientTemplates .local_button');
                    }
                    clone_button = button.clone();
                    clone_button.attr("onclick", "location.href=\'" + link + "\'");
                    cloned.append(clone_button);
                    $('#similarPatientsSelect').append(cloned);


                    //var row = '<li style="width: auto" onclick="location.href=\'' + link + '\'">';
                    //row += item.givenName + ' ' + item.familyName + ' | ' + item.patientIdentifier.identifier + ' | ' + item.gender + ' | ' + item.birthdate + ' | ' + item.personAddress;
                    //row += '</li>';
                    var row = '<li style="width: auto">';
                    row = '<div style="border-color: #00463f white; border-style: solid; margin-bottom: 10px; padding: 5px">';
                    row += '<label>';
                    row += item.givenName + ' ' + item.familyName + ' | ' + item.patientIdentifier.identifier + ' | ' + item.gender + ' | ' + item.birthdate + ' | ' + item.personAddress;
                    row += '</label>';
                    row += '<button style="padding: 2px 8px" onclick="location.href=';
                    if (isMpi == true) {
                        row += '\'/execute_script_which_will_request_service_to_import_patient_from_mpi_to_local_DB_and_redirect_to_patient_info\'">';
                        row += 'Import and Open';
                    } else {
                        row += '\'' + link + '\'">';
                        row += 'Open';
                    }
                    row += '</button>';
                    row += '</div>';
                    row += '</li>';
                    similarPatientsSelect.append(row);
                }
            })
            .error(function (xhr, status, err) {
                alert('AJAX error ' + err);
            });
        focusedField.focus();
    };

    jq('input').change(getSimilarPatients);
    jq('select').change(getSimilarPatients);

    /* Exact match patient functionality */
    $("#confirmation").on('select', function (confSection) {
        var submitButton = $('#confirmationQuestion .submitButton');
        submitButton.prop('disabled', true);
        var formData = jq('#registration').serialize();

        $('#exact-matches').hide();
        $('#mpi-exact-match').hide();
        $('#local-exact-match').hide();
        $.getJSON(emr.fragmentActionLink("registrationapp", "matchingPatients", "getExactPatients", { appId: appId }), formData)
            .success(function (response) {
                if (!jq('#checkbox-unknown-patient').is(':checked') && response.length > 0) {
                    $('#exact-matches').show();
                    var isMpi=false;
                    for (index in response){
                        if (response[index].mpiPatient!=null && response[index].mpiPatient==true){
                            isMpi=true;
                        }
                    }
                    if (isMpi == true) {
                        $('#mpi-exact-match').show();
                    } else {
                        $('#local-exact-match').show();
                    }
                    var exactPatientsSelect = jq('#exactPatientsSelect');
                    exactPatientsSelect.empty();
                    for (index in response) {
                        var item = response[index];
                        var link = patientDashboardLink;
                        link += '?patientId=' + item.patientId;
                        var row = '<li style="width: auto" onclick="location.href=\'' + link + '\'">';
                        if (isMpi == true) {
                            row = '<li style="width: auto" >';
                        } else {
                            row = '<li style="width: auto" onclick="location.href=\'' + link + '\'">';
                        }
                        row += item.givenName + ' ' + item.familyName + ' | ' + item.patientIdentifier.identifier + ' | ' + item.gender + ' | ' + item.birthdate + ' | ' + item.personAddress;

                        row += '</li>';
                        exactPatientsSelect.append(row);
                    }
                    submitButton.prop('disabled', false);
                }
            });
    });

    /* Submit functionality */
    jq('#registration').submit(function (e) {
        e.preventDefault();
        jq('#submit').attr('disabled', 'disabled');
        jq('#cancelSubmission').attr('disabled', 'disabled');
        jq('#validation-errors').hide();
        var formData = jq('#registration').serialize();
        $.getJSON(emr.fragmentActionLink("registrationapp", "registerPatient", "submit", { appId: appId }), formData)
            .success(function (response) {
                emr.navigateTo({"applicationUrl": response.message});
            })
            .error(function (response) {
                jq('#validation-errors-content').html(jq.parseJSON(response.responseText).globalErrors);
                jq('#validation-errors').show();
                jq('#submit').removeAttr('disabled');
                jq('#cancelSubmission').removeAttr('disabled');
        });
    });

    /* Registration date functionality */
    if (NavigatorController.getQuestionById('registration-date') != null) {  // if retro entry configured
        _.each(NavigatorController.getQuestionById('registration-date').fields, function (field) {       // registration fields are is disabled by default
            if (field.id != 'checkbox-enable-registration-date') {
                field.hide();
            }
        });

        jq('#checkbox-enable-registration-date').click(function () {
            if (jq('#checkbox-enable-registration-date').is(':checked')) {
                _.each(NavigatorController.getQuestionById('registration-date').fields, function (field) {
                    if (field.id != 'checkbox-enable-registration-date') {
                        field.hide();
                    }
                });
            }
            else {
                _.each(NavigatorController.getQuestionById('registration-date').fields, function (field) {
                    if (field.id != 'checkbox-enable-registration-date') {
                        field.show();
                    }
                });
            }
        });
    }

    /* Manual patient identifier entry functionality */
    if (NavigatorController.getFieldById('patient-identifier') != null) {   // if manual entry configured
        NavigatorController.getFieldById('patient-identifier').hide();

        jq('#checkbox-autogenerate-identifier').click(function () {
            if (jq('#checkbox-autogenerate-identifier').is(':checked')) {
                NavigatorController.getFieldById('patient-identifier').hide();
            }
            else {
                NavigatorController.getFieldById('patient-identifier').show();
                NavigatorController.getFieldById('patient-identifier').click();
            }
        })
    }

    /* Unknown patient functionality */
    jq('#checkbox-unknown-patient').click(function () {
        if (jq('#checkbox-unknown-patient').is(':checked')) {
            // disable all questions & sections except gender and registration date
            _.each(NavigatorController.getQuestionById('demographics-name').fields, function (field) {
                if (field.id != 'checkbox-unknown-patient') {
                    field.disable();
                }
            });

            _.each(NavigatorController.getSectionById('demographics').questions, function(question) {
                if (question.id != 'demographics-gender' && question.id != 'demographics-name') {
                    question.disable();
                }
            })

            // TODO sections variable is currently hackily defined in registerPatient.gsp
            _.each(sections, function(section) {
                if (section != 'demographics') {
                    NavigatorController.getSectionById(section).disable();
                }
            });

            // set unknown flag
            jq('#demographics-unknown').val('true');

            // jump ahead to gender
            NavigatorController.getQuestionById('demographics-gender').click();
        }
        else {
            // re-enable all functionality
            // hide all questions & sections except gender and registration date
            _.each(NavigatorController.getQuestionById('demographics-name').fields, function (field) {
                if (field.id != 'checkbox-unknown-patient') {
                    field.enable();
                }
            });

            NavigatorController.getQuestionById('demographics-birthdate').enable();

            // TODO sections variable is currently hackily defined in registerPatient.gsp
            _.each(sections, function(section) {
                NavigatorController.getSectionById(section).enable();
            });

            // unset unknown flag
            jq('#demographics-unknown').val('false');
            NavigatorController.getQuestionById('demographics-name').fields[0].click();
        }
    });
});

