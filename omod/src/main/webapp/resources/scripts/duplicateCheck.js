/*
 * Duplicate patient check for the registration form.
 *
 * The app configures a "Possible duplicates" question wherever it wants it (a field of type possibleDuplicates, see
 * fragments/field/possibleDuplicates.gsp). Once the trigger field (or, without one, the question before it) has been
 * entered, or when the question is reached, the entries so far are checked against existing patients. The question is always
 * there: it lists the matches, or says there are none. It fails validation until a check for the current entries has
 * succeeded and, if there are matches, they have been reviewed: the navigator already refuses to move past (Tab, Next,
 * clicking a later question or section) a question that is invalid, and the form is not submitted while it is.
 *
 * Strong matches are listed first, weaker ones collapsed under a link. A match can be compared side by side with the
 * entries in a side panel, from where the existing record can be opened.
 *
 * If the app uses the "note" style for the similar patients banner, a line above the form also tells how many similar
 * patients there are, until they have been reviewed.
 *
 * Needs NavigatorController, so DuplicateCheck.init() is called from registerPatient.js once that exists.
 */

// the navigator calls validate() on each field of the question; the question has a single field, the button
function DuplicatesGateValidator() {}
DuplicatesGateValidator.prototype = new FieldValidator();
DuplicatesGateValidator.prototype.constructor = DuplicatesGateValidator;
DuplicatesGateValidator.prototype.validate = function() {
    return DuplicateCheck.validationMessage();
};
// added to the existing validators, rather than replacing them
Validators['duplicates-gate'] = new DuplicatesGateValidator();

var DuplicateCheck = (function(jq) {

    // the phone number is one of the entries compared with the existing patients
    var PHONE_FIELD_NAME = 'phoneNumber';

    // the question that holds the widget, and the name of the field that starts the check, if the app has set one
    var questionId = null;
    var triggerFieldName = null;

    // the keys the search algorithm uses for the per-field results, mapped to the registration form field they compare
    var COMPARE_FIELDS = ['name', 'gender', 'birthdate', 'mothersFirstName', 'address', 'phone'];
    var ADDRESS_FIELD_NAMES = ['country', 'stateProvince', 'countyDistrict', 'cityVillage', 'address1', 'address2', 'address3',
        'address4', 'address5', 'address6', 'address7', 'address8', 'address9', 'address10', 'address11', 'address12',
        'address13', 'address14', 'address15', 'postalCode'];

    var noteEnabled = false;

    var state = {
        status: 'idle',       // idle (not checked yet), pending, failed or done; only a check that is done can pass
        matches: [],          // the possible duplicates, strongest first
        key: '',              // identifies the set of matches, so that a changed set has to be reviewed again
        acknowledgedKey: null,
        started: false,       // set once the trigger has been reached; from then on, any change re-runs the check
        selected: false,      // whether the question is the one being answered
        noteCount: null,      // the number of similar patients in the note above the form, if known
        lastPayload: null,
        lastEarlyPayload: null,
        request: 0,
        earlyRequest: 0
    };

    var drawer = { current: -1, opener: null, shown: 0 };

    // the formatted address of an existing patient can end in the separator of an empty trailing part
    function cleanAddress(address) {
        return address ? address.replace(/[,\s]+$/, '') : '';
    }

    function msg(code) {
        var text = emr.message(code);
        for (var i = 1; i < arguments.length; i++) {
            text = text.replace('{' + (i - 1) + '}', arguments[i]);
        }
        return text;
    }

    function actionUrl(action) {
        return '/' + OPENMRS_CONTEXT_PATH + '/registrationapp/matchingPatients/' + action + '.action?appId=' + appId;
    }

    function question() {
        return questionId ? NavigatorController.getQuestionById(questionId) : null;
    }

    function isUnknownPatient() {
        return jq('#checkbox-unknown-patient').is(':checked');
    }

    // the check passes when it has succeeded for the current entries and, if it found matches, they have been reviewed
    function isSatisfied() {
        if (isUnknownPatient()) {
            return true;
        }
        return state.status === 'done' && (state.matches.length === 0 || state.acknowledgedKey === state.key);
    }

    function validationMessage() {
        if (isSatisfied()) {
            return null;
        }
        if (state.status === 'idle') {
            runCheck();  // reached before the trigger, e.g. by clicking on the question
        }
        return state.status === 'failed' ? msg('registrationapp.duplicates.failed') :
            state.status === 'done' ? msg('registrationapp.duplicates.mustReview') :
            msg('registrationapp.duplicates.checking');
    }

    function formData() {
        // the date components update the hidden date value on blur, which takes the focus off the field that is being changed
        var focusedField = jq(':focus');
        jq('.date-component').trigger('blur');
        focusedField.focus();
        return jq('#registration').serialize();
    }

    function matchKey(matches) {
        return jq.map(matches, function(match) { return match.uuid; }).sort().join(',');
    }

    function patientLink(uuid) {
        var link = patientDashboardLink;
        return link + (link.indexOf('?') == -1 ? '?' : '&') + 'patientId=' + uuid;
    }

    function openPatient(match) {
        if (match.mpiPatient === true) {
            importMpiPatient(match.uuid);
        } else {
            location.href = patientLink(match.uuid);
        }
    }

    /* The note above the form */

    // shown while there are similar patients that haven't been reviewed, and while no check has been made it says nothing
    function updateNote() {
        if (!noteEnabled) {
            return;
        }
        var note = jq('#duplicates-note');
        var count = state.noteCount;
        var reviewed = state.status === 'done' && count > 0 && state.acknowledgedKey === state.key;
        if (count === null || state.selected || reviewed) {
            note.hide();
            return;
        }
        note.find('.text').text(count > 0 ? msg('registrationapp.duplicates.noteFound', count) : msg('registrationapp.duplicates.noteNone'));
        note.find('.note').toggleClass('warning', count > 0);
        note.show();
    }

    function updateAcknowledgedMarker() {
        // makes the navigator tick the question once it is satisfied
        var button = jq('#duplicates-none');
        if (state.status === 'done' && isSatisfied()) {
            button.attr('data-display-value', msg('registrationapp.duplicates.reviewed'));
        } else {
            button.removeAttr('data-display-value');
        }
    }

    // when a check finishes without having been passed by the clerk, who has already moved past the question, take them back to it
    function returnToQuestion() {
        var q = question();
        if (q && !isSatisfied()) {
            q.click();
        }
    }

    function returnToQuestionIfPassed() {
        var q = question();
        if (!q || isSatisfied()) {
            return;
        }
        var questions = NavigatorController.getQuestions();
        var selected = jq.grep(questions, function(candidate) { return candidate.isSelected; })[0];
        if (selected && questions.indexOf(selected) > questions.indexOf(q)) {
            q.click();
        }
    }

    /* Running the check */

    function applyMatches(matches) {
        var previousKey = state.key;
        state.status = 'done';
        state.matches = matches;
        state.key = matchKey(matches);
        state.noteCount = matches.length;
        if (state.key !== previousKey) {
            closeDrawer(false);
        }
        renderScreen();
        updateNote();
        returnToQuestionIfPassed();
    }

    function applyFailure() {
        state.status = 'failed';
        state.lastPayload = null;  // so that trying again sends the request again
        renderScreen();
        returnToQuestionIfPassed();
    }

    // the check that gates the form, same search as the banner above the form (getSimilarPatients)
    function runCheck(force) {
        if (isUnknownPatient()) {
            return;
        }
        var payload = formData();
        if (!force && payload === state.lastPayload) {
            return;
        }
        state.lastPayload = payload;
        state.status = 'pending';
        var request = ++state.request;
        renderScreen();
        jq.post(actionUrl('getSimilarPatients'), payload, function(data) {
            if (request === state.request) {
                applyMatches(data);
            }
        }, 'json').fail(function() {
            if (request === state.request) {
                applyFailure();
            }
        });
    }

    // before the trigger is reached, only the note above the form is kept up to date
    function runEarlyCheck() {
        if (state.started || isUnknownPatient()) {
            return;
        }
        var payload = formData();
        if (payload === state.lastEarlyPayload) {
            return;
        }
        state.lastEarlyPayload = payload;
        var request = ++state.earlyRequest;
        jq.post(actionUrl('getSimilarPatients'), payload, function(data) {
            if (request === state.earlyRequest && !state.started) {
                state.noteCount = data.length;
                updateNote();
            }
        }, 'json');
    }

    function onTriggerReached() {
        state.started = true;
        runCheck();
    }

    function onAnyChange(event) {
        if (triggerFieldName && jq(event.target).attr('name') === triggerFieldName) {
            return;
        }
        if (state.started) {
            runCheck();
        } else if (noteEnabled) {
            runEarlyCheck();
        }
    }

    /* The "Possible duplicates" screen */

    function fieldLabel(field) {
        return msg('registrationapp.duplicates.field.' + field);
    }

    function statusLabel(status) {
        return status === 'match' ? msg('registrationapp.duplicates.same') :
            status === 'similar' ? msg('registrationapp.duplicates.similar') :
            status === 'different' ? msg('registrationapp.duplicates.different') :
            msg('registrationapp.duplicates.notRecorded');
    }

    function statusOf(match, field) {
        var result = jq.grep(match.fieldResults || [], function(r) { return r.field === field; })[0];
        return result ? result.status : null;
    }

    function buildAgreement(match) {
        var wrapper = jq('<div class="duplicate-agreement"></div>');
        jq.each(['match', 'similar', 'different'], function(i, status) {
            var fields = jq.map(COMPARE_FIELDS, function(field) {
                return statusOf(match, field) === status ? fieldLabel(field) : null;
            });
            if (fields.length > 0) {
                wrapper.append(jq('<div class="duplicate-agreement-' + status + '"></div>')
                    .append(jq('<strong></strong>').text(statusLabel(status) + ': '))
                    .append(document.createTextNode(fields.join(', '))));
            }
        });
        return wrapper;
    }

    function buildCard(match, index) {
        var card = jq('<li class="duplicate-card"></li>');

        var head = jq('<div class="duplicate-card-head"></div>');
        head.append(jq('<span class="duplicate-name"></span>').text(match.givenName + ' ' + match.familyName));
        if (match.fieldResults && match.fieldResults.length > 0 || match.strong) {
            head.append(jq('<span class="duplicate-tag"></span>')
                .addClass(match.strong ? 'strong' : 'possible')
                .text(match.strong ? msg('registrationapp.duplicates.tag.strong') : msg('registrationapp.duplicates.tag.possible')));
        }
        card.append(head);

        var info = [emr.message('coreapps.gender.' + match.gender), match.birthdate, cleanAddress(match.personAddress)];
        card.append(jq('<div class="duplicate-info"></div>').text(jq.grep(info, function(value) { return value; }).join(', ')));

        if (match.identifiers && match.identifiers.length > 0) {
            var ids = jq('<div class="duplicate-ids"></div>');
            jq.each(match.identifiers, function(i, identifier) {
                ids.append(jq('<span class="duplicate-id-name"></span>').text(identifier.name + ': '));
                ids.append(jq('<span class="duplicate-id-value"></span>').text(identifier.value));
            });
            card.append(ids);
        }

        card.append(buildAgreement(match));

        card.append(jq('<button type="button" class="duplicate-compare"></button>')
            .text(msg('registrationapp.duplicates.compare'))
            .attr('data-match-index', index));
        return card;
    }

    function renderScreen() {
        var strongList = jq('#duplicates-strong').empty();
        var weakList = jq('#duplicates-weak').empty();
        var toggle = jq('#duplicates-toggle-weak');
        var hasMatches = state.status === 'done' && state.matches.length > 0;

        var weakCount = 0;
        if (hasMatches) {
            jq.each(state.matches, function(index, match) {
                if (match.strong) {
                    strongList.append(buildCard(match, index));
                } else {
                    weakList.append(buildCard(match, index));
                    weakCount++;
                }
            });
        }

        // weaker matches are collapsed, unless there is nothing else to show
        var hasStrong = strongList.children().length > 0;
        if (hasStrong && weakCount > 0) {
            weakList.hide();
            toggle.text(msg('registrationapp.duplicates.showOther', weakCount)).attr('aria-expanded', 'false').show();
        } else {
            toggle.hide();
            weakList.toggle(weakCount > 0);
        }

        // what the screen says, and what its button does, depends on the state of the check
        var text, button, warning = false;
        if (state.status === 'failed') {
            text = msg('registrationapp.duplicates.failed');
            button = msg('registrationapp.duplicates.retry');
            warning = true;
        } else if (state.status !== 'done') {
            text = msg('registrationapp.duplicates.checking');
            button = msg('registrationapp.duplicates.checking');
        } else if (!hasMatches) {
            text = msg('registrationapp.duplicates.noMatches');
            button = msg('registrationapp.duplicates.continue');
        } else {
            text = msg('registrationapp.duplicates.warning', state.matches.length);
            button = state.matches.length == 1 ? msg('registrationapp.duplicates.none.one') :
                msg('registrationapp.duplicates.none.many', state.matches.length);
            warning = true;
        }
        jq('#duplicates-warning').text(text).closest('.note').toggleClass('warning', warning);
        jq('#duplicates-none').text(button);
        jq('#duplicates-screen .duplicates-hint').toggle(hasMatches);
        updateAcknowledgedMarker();
    }

    function toggleWeak(event) {
        event.preventDefault();
        var weakList = jq('#duplicates-weak');
        var expanded = !weakList.is(':visible');
        weakList.toggle(expanded);
        var weakCount = weakList.children().length;
        jq(this).attr('aria-expanded', expanded ? 'true' : 'false')
            .text(expanded ? msg('registrationapp.duplicates.hideOther') : msg('registrationapp.duplicates.showOther', weakCount));
    }

    // the one button of the question
    function onButton() {
        if (state.status === 'failed') {
            runCheck(true);
        } else if (state.status === 'done') {
            state.acknowledgedKey = state.key;
            updateAcknowledgedMarker();
            updateNote();
            NavigatorController.stepForward();
        }
    }

    /* The side panel comparing the entries with a match */

    function valueOfRegistration(name) {
        // :input, as a label can have a name too (the address fields of uicommons' personAddress)
        return jq.trim(jq('#registration').find(':input[name="' + name + '"]').not(':radio').first().val() || '');
    }

    var formattedBirthdates = {};

    // The server formats dates according to its settings, so the birthdate that was entered is formatted there too, to
    // read like the birthdate of an existing patient. Falls back to the entered parts if that fails.
    function withRegistrationBirthdate(mine, callback) {
        var parts = mine.birthdateParts;
        if (!parts) {
            callback(mine.birthdate);
            return;
        }
        var key = parts.day + '-' + parts.month + '-' + parts.year;
        if (formattedBirthdates.hasOwnProperty(key)) {
            callback(formattedBirthdates[key]);
            return;
        }
        jq.getJSON(actionUrl('formatBirthdate'), { day: parts.day, month: parts.month, year: parts.year }, function(data) {
            formattedBirthdates[key] = data.birthdate || parts.fallback;
            callback(formattedBirthdates[key]);
        }).fail(function() {
            callback(parts.fallback);
        });
    }

    function registrationValues() {
        var names = jq.grep([valueOfRegistration('givenName'), valueOfRegistration('middleName'), valueOfRegistration('familyName')],
            function(part) { return part; });

        var genderValue = jq('#registration').find('[name="gender"]:radio:checked').val() || valueOfRegistration('gender');

        var birthdate = '';
        var birthdateParts = null;
        var day = valueOfRegistration('birthdateDay');
        var monthSelect = jq('#registration').find('select[name="birthdateMonth"]');
        var year = valueOfRegistration('birthdateYear');
        if (day && monthSelect.val() && year) {
            // formatted by the server like the birthdate of an existing patient, see withRegistrationBirthdate()
            birthdateParts = { day: day, month: monthSelect.val(), year: year, fallback: day + ' ' + monthSelect.find('option:selected').text() + ' ' + year };
            birthdate = birthdateParts.fallback;
        } else if (valueOfRegistration('birthdateYears')) {
            birthdate = '~' + valueOfRegistration('birthdateYears');
        }

        var address = jq.grep(jq.map(ADDRESS_FIELD_NAMES, valueOfRegistration), function(part) { return part; });

        return {
            name: names.join(' '),
            gender: genderValue ? emr.message('coreapps.gender.' + genderValue) : '',
            birthdate: birthdate,
            birthdateParts: birthdateParts,
            mothersFirstName: valueOfRegistration('mothersFirstName'),
            address: address.join(', '),
            phone: valueOfRegistration(PHONE_FIELD_NAME)
        };
    }

    function existingValues(match) {
        var attributes = match.attributes || {};
        return {
            name: match.givenName + ' ' + match.familyName,
            gender: match.gender ? emr.message('coreapps.gender.' + match.gender) : '',
            birthdate: match.birthdate || '',
            mothersFirstName: attributes.mothersFirstName || '',
            address: cleanAddress(match.personAddress),
            phone: attributes[PHONE_FIELD_NAME] || ''
        };
    }

    function ensureDrawer() {
        if (jq('#duplicates-drawer').length > 0) {
            return;
        }
        var overlay = jq('<div id="duplicates-overlay" class="duplicates-overlay" style="display: none;"></div>');
        var panel = jq('<aside id="duplicates-drawer" class="duplicates-drawer" role="dialog" aria-modal="true" ' +
            'aria-labelledby="duplicates-drawer-title" tabindex="-1" style="display: none;"></aside>');

        var header = jq('<div class="duplicates-drawer-header"></div>');
        header.append(jq('<h2 id="duplicates-drawer-title"></h2>').text(msg('registrationapp.duplicates.compareTitle')));
        var pager = jq('<div class="duplicates-drawer-pager"></div>');
        pager.append(jq('<button type="button" id="duplicates-prev"></button>').text('\u2039').attr('aria-label', msg('registrationapp.duplicates.previous')));
        pager.append(jq('<span id="duplicates-position" aria-live="polite"></span>'));
        pager.append(jq('<button type="button" id="duplicates-next"></button>').text('\u203a').attr('aria-label', msg('registrationapp.duplicates.next')));
        pager.append(jq('<button type="button" id="duplicates-close"></button>').text('\u00d7').attr('aria-label', msg('registrationapp.duplicates.close')));
        header.append(pager);

        var table = jq('<table class="duplicates-compare-table"></table>');
        table.append(jq('<thead></thead>').append(jq('<tr></tr>')
            .append('<th></th>')
            .append(jq('<th></th>').text(msg('registrationapp.duplicates.thisRegistration')))
            .append(jq('<th></th>').text(msg('registrationapp.duplicates.existingRecord')))));
        table.append('<tbody id="duplicates-compare-body"></tbody>');

        var footer = jq('<div class="duplicates-drawer-footer"></div>');
        footer.append(jq('<button type="button" id="duplicates-back"></button>').text(msg('registrationapp.duplicates.backToList')));
        footer.append(jq('<button type="button" id="duplicates-this-is-patient" class="confirm"></button>').text(msg('registrationapp.duplicates.thisIsPatient')));

        panel.append(header).append(table).append(footer);
        jq('body').append(overlay).append(panel);

        overlay.click(function() { closeDrawer(true); });
        jq('#duplicates-close, #duplicates-back').click(function() { closeDrawer(true); });
        jq('#duplicates-prev').click(function() { showMatchInDrawer(drawer.current - 1); });
        jq('#duplicates-next').click(function() { showMatchInDrawer(drawer.current + 1); });
        jq('#duplicates-this-is-patient').click(function() { openPatient(state.matches[drawer.current]); });

        // the navigator listens to key presses on the whole page (Tab, arrows, Esc and Enter move around the form), so the
        // panel keeps them to itself
        panel.on('keydown', function(event) {
            event.stopPropagation();
            if (event.which === 27) {
                event.preventDefault();
                closeDrawer(true);
            } else if (event.which === 37 && !jq(event.target).is('input, select, textarea')) {
                event.preventDefault();
                showMatchInDrawer(drawer.current - 1);
            } else if (event.which === 39 && !jq(event.target).is('input, select, textarea')) {
                event.preventDefault();
                showMatchInDrawer(drawer.current + 1);
            } else if (event.which === 9) {
                trapFocus(event, panel);
            }
        });
    }

    function trapFocus(event, panel) {
        var focusable = panel.find('button:not(:disabled), a[href], input, select, textarea').filter(':visible');
        if (focusable.length === 0) {
            return;
        }
        var first = focusable.first()[0];
        var last = focusable.last()[0];
        if (event.shiftKey && (document.activeElement === first || document.activeElement === panel[0])) {
            event.preventDefault();
            last.focus();
        } else if (!event.shiftKey && document.activeElement === last) {
            event.preventDefault();
            first.focus();
        }
    }

    function showMatchInDrawer(index) {
        if (index < 0 || index >= state.matches.length) {
            return;
        }
        drawer.current = index;
        var match = state.matches[index];
        var mine = registrationValues();
        var theirs = existingValues(match);
        var shown = ++drawer.shown;

        withRegistrationBirthdate(mine, function(birthdate) {
            if (shown !== drawer.shown) {
                return;  // the drawer has moved on to another match, or was closed
            }
            mine.birthdate = birthdate;
            var body = jq('#duplicates-compare-body').empty();
            jq.each(COMPARE_FIELDS, function(i, field) {
                var status = statusOf(match, field);
                var row = jq('<tr></tr>');
                row.append(jq('<th scope="row"></th>').text(fieldLabel(field)));
                row.append(jq('<td></td>').text(mine[field] || '\u2014'));
                var existing = jq('<td></td>').text(theirs[field] || '\u2014');
                // "not recorded" only when a side is empty; two addresses that couldn't be compared field by field aren't "not recorded"
                var comparedNothing = status === 'missing' && mine[field] && theirs[field];
                if (status && !comparedNothing) {
                    existing.append(jq('<span class="duplicate-status"></span>').addClass(status).text(statusLabel(status)));
                }
                row.append(existing);
                body.append(row);
            });
        });

        jq('#duplicates-position').text(msg('registrationapp.duplicates.position', index + 1, state.matches.length));
        jq('#duplicates-prev').prop('disabled', index === 0);
        jq('#duplicates-next').prop('disabled', index === state.matches.length - 1);
    }

    function openDrawer(index, opener) {
        ensureDrawer();
        drawer.opener = opener;
        showMatchInDrawer(index);
        jq('#duplicates-overlay').show();
        jq('#duplicates-drawer').show().focus();
    }

    function closeDrawer(returnFocus) {
        if (jq('#duplicates-drawer').length === 0 || !jq('#duplicates-drawer').is(':visible')) {
            return;
        }
        drawer.shown++;
        jq('#duplicates-overlay').hide();
        jq('#duplicates-drawer').hide();
        if (returnFocus && drawer.opener && jq.contains(document.documentElement, drawer.opener[0])) {
            drawer.opener.focus();
        }
        drawer.opener = null;
    }

    /* Setting up */

    function init(withNote) {
        var widget = jq('#duplicates-widget');
        if (widget.length === 0) {
            return;  // the form has no possible duplicates question
        }
        noteEnabled = withNote === true;
        questionId = widget.closest('fieldset').attr('id');
        triggerFieldName = widget.attr('data-trigger-field') || null;

        var q = question();
        if (!q) {
            return;
        }

        if (triggerFieldName) {
            // leaving the field counts too, since the field may be left blank
            jq('#registration').find(':input[name="' + triggerFieldName + '"]').on('change unselect', onTriggerReached);
        } else {
            var questions = NavigatorController.getQuestions();
            var previous = questions[questions.indexOf(q) - 1];
            if (previous) {
                previous.element.on('unselect', onTriggerReached);
            } else {
                state.started = true;
            }
        }

        // other fields: keep the note above the form up to date, and re-check once the trigger has been reached
        jq('#registration').on('change', 'input, select', onAnyChange);

        jq('#duplicates-none').click(onButton);
        jq('#duplicates-toggle-weak').click(toggleWeak);
        jq('#duplicates-screen').on('click', '.duplicate-compare', function() {
            openDrawer(parseInt(jq(this).attr('data-match-index'), 10), jq(this));
        });

        // The navigator listens to key presses on the whole page, and always moves to another field on Tab, and presses the
        // selected field (the button) on Enter. On the screen, Tab is left to the browser until the first or
        // last control is reached, and Enter, Esc and the up and down keys only apply to the control that has the focus.
        jq('#' + questionId).on('keydown', function(event) {
            var onActionButton = jq(event.target).is('#duplicates-none');
            if (event.which === 9) {
                var focusable = jq('#duplicates-heading, #duplicates-screen button, #duplicates-screen a[href], #duplicates-none').filter(':visible');
                var current = focusable.index(document.activeElement);
                var atEdge = event.shiftKey ? current <= 0 : current === focusable.length - 1;
                if (!atEdge) {
                    event.stopPropagation();
                }
            } else if (!onActionButton && (event.which === 13 || event.which === 27 || event.which === 38 || event.which === 40)) {
                event.stopPropagation();
            }
        });

        jq('#' + questionId).on('select', function(event) {
            if (event.target !== this) {
                return;
            }
            state.selected = true;
            updateNote();
            if (state.status === 'idle') {
                runCheck();
            }
            // the screen is read from the top, and Tab then walks through the matches
            setTimeout(function() { jq('#duplicates-heading').focus(); }, 0);
        });
        jq('#' + questionId).on('unselect', function(event) {
            if (event.target === this) {
                state.selected = false;
                updateNote();
            }
        });

        // the check is skipped for an unknown patient, so make it again when that is unchecked
        jq('#checkbox-unknown-patient').click(function() {
            setTimeout(function() {
                if (!isUnknownPatient() && state.started) {
                    runCheck();
                }
                updateNote();
            }, 0);
        });

        renderScreen();
    }

    return {
        init: init,
        isSatisfied: isSatisfied,
        validationMessage: validationMessage,
        returnToQuestion: returnToQuestion
    };

})(jQuery);
