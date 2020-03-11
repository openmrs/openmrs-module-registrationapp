
// Creating AtLeastDateOrEstimatedAgeValidator() to be used on date/estimated age part of registration app when sub-sections are combined 
// This is done as a hack to make the date of birth or estimated age required when sub-sections are combined
function AtLeastDateOrEstimatedAgeValidator() {

}
AtLeastDateOrEstimatedAgeValidator.prototype = new FieldValidator();
AtLeastDateOrEstimatedAgeValidator.prototype.constructor = AtLeastDateOrEstimatedAgeValidator;
AtLeastDateOrEstimatedAgeValidator.prototype.validate = function(field){
    var multipleInputDateElement = jQuery('div#demographics-birthdate');

    var multipleInputDateQuestionValidator = new MultipleInputDateQuestionValidator();
    var messageContainer = jQuery(multipleInputDateElement).find("span.field-error").first();
    var message;
    if (field.element.is('#birthdateMonths-field')) {
        message = multipleInputDateQuestionValidator.validate({ element : multipleInputDateElement });
    }
    if (message) {
        messageContainer.empty();
        messageContainer.append(message);
        messageContainer.show();      
        return ' ';
    }
    messageContainer.empty();
    return null;
}

// Updating list of validators to include AtLeastDateOrEstimatedAgeValidator()
Validators = {
    required: new RequiredFieldValidator(),
    date: new DateFieldValidator(),
    integer: new IntegerFieldValidator(),
    number: new NumberFieldValidator(),
    "numeric-range": new NumericRangeFieldValidator(),
    regex: new RegexFieldValidator(),
    phone: new PhoneFieldValidator(),
    'atleast-date-or-estimated-age': new AtLeastDateOrEstimatedAgeValidator()
}

jQuery(function() {
    jQuery('#birthdateYear-field').addClass('atleast-date-or-estimated-age');
    jQuery('#birthdateMonths-field').addClass('atleast-date-or-estimated-age');
});

