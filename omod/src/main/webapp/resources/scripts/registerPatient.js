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
        reviewSimilarPatients.show();
        return false;
    });

    renderPatientAsDelimitedString = function (patient) {
        var str = "";
        // TODO handle this recursively, support more than one level of nesting levels?
        _.map(patient, function(value, key){
            if (key != 'patientId') {
                if (value !== null && typeof value === 'object') {
                    _.map(value, function(nestedValue) {
                        if (nestedValue) {
                            str += nestedValue + " | ";
                        }
                    });
                }
                else if (value) {
                    str += value + " | ";
                }
            }
        })
        return str.substring(0, str.length - 2);  // chop off trailing " | "
    }

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
                    var link = patientDashboardLink;
                    link += '?patientId=' + item.patientId;
                    var row = '<li style="width: auto" onclick="location.href=\'' + link + '\'">';
                    row += renderPatientAsDelimitedString(item);
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
        $.getJSON(emr.fragmentActionLink("registrationapp", "matchingPatients", "getExactPatients", { appId: appId }), formData)
            .success(function (response) {
                if (!jq('#checkbox-unknown-patient').is(':checked') && response.length > 0) {
                    $('#exact-matches').show();
                    var exactPatientsSelect = jq('#exactPatientsSelect');
                    exactPatientsSelect.empty();
                    for (index in response) {
                        var item = response[index];
                        var link = patientDashboardLink;
                        link += '?patientId=' + item.patientId;
                        var row = '<li style="width: auto" onclick="location.href=\'' + link + '\'">';
                        row += renderPatientAsDelimitedString(item);
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

