function toggleFingerprintButtonDisplay(button) {
        if (!(button instanceof jQuery)) {
            button = jQuery(button);
        }
        if (button.is(':disabled')) {
            button.prop('disabled', false);
            button.find('i').attr('class', 'icon-hand-up');
        } else {
            button.prop('disabled', true);
            button.find('i').attr('class', 'fa fa-spinner fa-spin');
        }
}


function toggleImportButtonDisplay(button) {
    if (!(button instanceof jQuery)) {
        button = jQuery(button);
    }
    if (button.is(':disabled')) {
        button.prop('disabled', false);
        button.find('i').attr('class', '');
    } else {
        button.prop('disabled', true);
        button.find('i').attr('class', 'fa fa-spinner fa-spin');
    }
}
