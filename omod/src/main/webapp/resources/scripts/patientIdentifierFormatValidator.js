
// Validates a manually entered patient identifier against the format of its identifier type, so an invalid
// identifier is reported before leaving the question instead of only when the registration is submitted

function PatientIdentifierFormatValidator() {
}

PatientIdentifierFormatValidator.prototype = new FieldValidator();
PatientIdentifierFormatValidator.prototype.constructor = PatientIdentifierFormatValidator;
PatientIdentifierFormatValidator.prototype.validate = function(field) {
    var value = field.value();
    var format = field.element.attr('data-identifier-format');
    // a blank value is not a format violation
    if (!format || !value || $.trim(value).length == 0) {
        return null;
    }
    // the server-side PatientIdentifierValidator uses String.matches(), which must match the whole identifier
    if (!new RegExp('^(?:' + format + ')$').test(value)) {
        return field.element.attr('data-identifier-format-message');
    }
    return null;
}

// Adding PatientIdentifierFormatValidator() to the validators defined by uicommons (rather than replacing the map)
Validators['patient-identifier-format'] = new PatientIdentifierFormatValidator();
