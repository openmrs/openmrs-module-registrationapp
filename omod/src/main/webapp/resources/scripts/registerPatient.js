jq = jQuery;

// TODO refactor this all into something cleaner

// we expose this in the global scope so that other javascript widgets can access it--probably should have a better pattern for this
var NavigatorController;

function importMpiPatient(id) {
    $.getJSON(emr.fragmentActionLink("registrationapp", "registerPatient", "importMpiPatient", {mpiPersonId: id}))
        .success(function (response) {
            var link = patientDashboardLink;
            link += (link.indexOf('?') == -1 ? '?' : '&') + 'patientId=' + response.message;
            location.href = link;
        })
        .error(function (xhr, status, err) {
            alert('AJAX error ' + err);
        });
}

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

    function showSimilarPatients(data) {
        if (data.length == 0 || jq('#checkbox-unknown-patient').is(':checked')) {
            jq("#similarPatients").hide();
            jq("#similarPatientsSlideView").hide();
            return;
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

            var container = $('#matchedPatientTemplates .container');
            var cloned = container.clone();

            cloned.find('.name').append(item.givenName + ' ' + item.familyName);

            var gender = emr.message('emr.gender.' + item.gender);

            var attributes = "";
            if (item.attributeMap) {
                _.each(item.attributeMap, function(value, key) {
                    if (value) {
                        attributes = attributes + ", " + value;
                    }
                });
            }

            cloned.find('.info').append(gender + ', ' + item.birthdate + ', ' + item.personAddress + attributes);

            if (item.identifiers) {
                var identifiers = cloned.find('.identifiers');
                item.identifiers.forEach(function (entry) {
                    var clonedIdName = identifiers.find('.idNameTemplate').clone();
                    clonedIdName.text(entry.name + ': ');
                    clonedIdName.removeClass("idNameTemplate");
                    identifiers.append(clonedIdName);

                    var clonedIdValue = identifiers.find(".idValueTemplate").clone();
                    clonedIdValue.text(entry.value);
                    clonedIdValue.removeClass("idValueTemplate");
                    identifiers.append(clonedIdValue);
                });
            }

            var button;
            if (isMpi) {
                button = $('#matchedPatientTemplates .mpi_button').clone();
                button.attr("onclick", "importMpiPatient(" + item.uuid + ")");
            } else {
                button = $('#matchedPatientTemplates .local_button').clone();
                var link = patientDashboardLink;
                link += (link.indexOf('?') == -1 ? '?' : '&') + 'patientId=' + item.uuid;
                button.attr("onclick", "location.href=\'" + link + "\'");
            }
            cloned.append(button);

            $('#similarPatientsSelect').append(cloned);
        }
    }

    getSimilarPatients = function (field) {
        var focusedField = $(':focus');
        jq('.date-component').trigger('blur');

        var formData = jq('#registration').serialize();
        var url = '/' + OPENMRS_CONTEXT_PATH + '/registrationapp/matchingPatients/getSimilarPatients.action?appId='+appId;
        jq.post(url, formData, function(data) {
            jq("#reviewSimilarPatientsButton").show();
            showSimilarPatients(data);
        }, "json");

        focusedField.focus();
    };

    jq('input').change(getSimilarPatients);
    jq('select').change(getSimilarPatients);

    // Biometric matching
    // TODO: This is mostly copied from similar patients above.  Refactor into shared, common functionality as appropriate

    /* Biometric patient functionality */
    reviewBiometricPatients = emr.setupConfirmationDialog({
        selector: '#reviewBiometricPatients',
        actions: {
            cancel: function () {
                reviewBiometricPatients.close();
            }
        }
    });

    jq('#reviewBiometricPatientsButton').click(function () {
        var slideView = $("#biometricPatientsSlideView");
        slideView.slideToggle();

        return false;
    });

    function showBiometricPatients(data) {

        if (data.length == 0) {
            jq("#biometricPatients").hide();
            jq("#biometricPatientsSlideView").hide();
            return;
        } else {
            jq("#biometricPatients").show();
        }

        jq('#biometricPatientsCount').text(data.length);
        var biometricPatientsSelect = jq('#biometricPatientsSelect');
        biometricPatientsSelect.empty();
        for (index in data) {
            var item = data[index];
            var container = $('#matchedPatientTemplates .container');
            var cloned = container.clone();

            cloned.find('.name').append(item.givenName + ' ' + item.familyName);

            var gender = emr.message('emr.gender.' + item.gender);

            var attributes = "";
            if (item.attributeMap) {
                _.each(item.attributeMap, function(value, key) {
                    if (value) {
                        attributes = attributes + ", " + value;
                    }
                });
            }

            cloned.find('.info').append(gender + ', ' + item.birthdate + ', ' + item.personAddress + attributes);

            if (item.identifiers) {
                var identifiers = cloned.find('.identifiers');
                item.identifiers.forEach(function (entry) {
                    var clonedIdName = identifiers.find('.idNameTemplate').clone();
                    clonedIdName.text(entry.name + ': ');
                    clonedIdName.removeClass("idNameTemplate");
                    identifiers.append(clonedIdName);

                    var clonedIdValue = identifiers.find(".idValueTemplate").clone();
                    clonedIdValue.text(entry.value);
                    clonedIdValue.removeClass("idValueTemplate");
                    identifiers.append(clonedIdValue);
                });
            }

            var button = $('#matchedPatientTemplates .local_button').clone();
            var link = patientDashboardLink;
            link += (link.indexOf('?') == -1 ? '?' : '&') + 'patientId=' + item.uuid;
            button.attr("onclick", "location.href=\'" + link + "\'");
            cloned.append(button);

            $('#biometricPatientsSelect').append(cloned);
        }
    }

    getBiometricMatches = function(formData) {
        var biometricUrl = '/' + OPENMRS_CONTEXT_PATH + '/registrationapp/matchingPatients/getBiometricMatches.action?appId='+appId;
        jq.post(biometricUrl, formData, function(data) {
            jq("#reviewBiometricPatientsButton").show();
            showBiometricPatients(data);
            // TODO: Uncomment the below if we wish to display the matching patient list open by default
            //jq("#biometricPatientsSlideView").show();
        }, "json");
    };

    /* Exact match patient functionality */
    jq("#confirmation").on('select', function (confSection) {

        var formData = jq('#registration').serialize();

        jq('#exact-matches').hide();
        jq('#mpi-exact-match').hide();
        jq('#local-exact-match').hide();

        var url = '/' + OPENMRS_CONTEXT_PATH + '/registrationapp/matchingPatients/getExactPatients.action?appId='+appId;
        jq.post(url, formData, function(data) {
            jq("#reviewSimilarPatientsButton").hide();
            showSimilarPatients(data);
            jq("#similarPatientsSlideView").show();
        }, "json");
    });

    /* Submit functionality */
    jq('#registration').submit(function (e) {
        e.preventDefault();
        jq('#submit').attr('disabled', 'disabled');
        jq('#cancelSubmission').attr('disabled', 'disabled');
        jq('#validation-errors').hide();
        var formData = jq('#registration').serialize();

        var url = '/' + OPENMRS_CONTEXT_PATH + '/registrationapp/registerPatient/submit.action?appId=' + appId;
        jq.ajax({
            url: url,
            type: "POST",
            data: formData,
            dataType: "json",
            success: function(response) {
                emr.navigateTo({"applicationUrl": response.message});
            },
            error: function(response) {
                jq('#validation-errors-content').html(response.responseJSON.globalErrors);
                jq('#validation-errors').show();
                jq('#submit').removeAttr('disabled');
                jq('#cancelSubmission').removeAttr('disabled');
            }
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
            if (NavigatorController.getQuestionById('demographics-name') != undefined) {    
                _.each(NavigatorController.getQuestionById('demographics-name').fields, function (field) {
                    if (field.id != 'checkbox-unknown-patient') {
                        field.disable();
                    }
                });
            }

            if (NavigatorController.getQuestionById('demographics-fieldset') != undefined) {
                _.each(NavigatorController.getQuestionById('demographics-fieldset').fields, function (field) {
                    if (field.id != 'checkbox-unknown-patient' && field.id != 'gender-field') {
                        field.disable();
                    }
                });
            }

            _.each(NavigatorController.getSectionById('demographics').questions, function(question) {
                if (question.id != 'demographics-gender' && question.id != 'demographics-name' && question.id != 'demographics-fieldset') {
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
            if (NavigatorController.getQuestionById('demographics-gender') != undefined) {
                NavigatorController.getQuestionById('demographics-gender').click();
            } else {
                NavigatorController.getFieldById('gender-field').click();
            }
        }
        else {
            // re-enable all functionality
            // hide all questions & sections except gender and registration date
            if (NavigatorController.getQuestionById('demographics-name') != undefined) {
                _.each(NavigatorController.getQuestionById('demographics-name').fields, function (field) {
                    if (field.id != 'checkbox-unknown-patient') {
                        field.enable();
                    }
                });    
            }
            
            if (NavigatorController.getQuestionById('demographics-birthdate') != undefined) {
                NavigatorController.getQuestionById('demographics-birthdate').enable();
            }

            // TODO sections variable is currently hackily defined in registerPatient.gsp
            _.each(sections, function(section) {
                NavigatorController.getSectionById(section).enable();
            });

            // unset unknown flag
            jq('#demographics-unknown').val('false');
            if (NavigatorController.getQuestionById('demographics-name') != undefined) {
                NavigatorController.getQuestionById('demographics-name').fields[0].click();
            } else {
                NavigatorController.getQuestionById('demographics-fieldset').fields[0].click();
            }
        }
    });
});

