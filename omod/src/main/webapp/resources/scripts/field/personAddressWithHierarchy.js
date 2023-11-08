
function PersonAddressWithHierarchy(personAddressWithHierarchy) {

    var levels;

    $.getJSON('/' + OPENMRS_CONTEXT_PATH + '/module/addresshierarchy/ajax/getOrderedAddressHierarchyLevels.form', {}, function (result) {
        levels = result;
        _.each(levels, function (item, index) {
            item.index = index;
        });

        if (personAddressWithHierarchy.initialValue) {
            for (var key in personAddressWithHierarchy.initialValue) {
                if (personAddressWithHierarchy.initialValue[key]) {
                    setValue(key, personAddressWithHierarchy.initialValue[key]);
                }
            }
            //After setting all the initial values, look for empty(not filled in) levels
            // and disable the levels which have no hierarchy entries available in the db
            for (let i=0; i <levels.length; i++ ) {
                if (!levels[i].lastSelection) {
                    //this level is empty, check to see if there are available entries
                    preloadLevels(levels[i]);
                }
            }
        } else {
            preloadLevels(levels[0]);
        }
    });

    // our usage of the jquery-ui autocomplete leads to lots of unnecessary queries for the same thing, so we use this
    // to cache the results
    var queryCache = {};

    // uses the cache
    function queryWithCallback(searchString, callback) {
        if (searchString in queryCache) {
            callback(queryCache[searchString]);
            return null;
        }
        else {
            return $.getJSON('/' + OPENMRS_CONTEXT_PATH + '/module/addresshierarchy/ajax/getChildAddressHierarchyEntries.form', {
                searchString: searchString
            }, function(result) {
                queryCache[searchString] = result;
                callback(result);
            });
        }
    }

    // Starting from this level down disable all remaining levels and associated input boxes
    function disableLevelsAfter(level) {
        if (!_.contains(personAddressWithHierarchy.manualFields, level.addressField)) {
            let inputElement = getInputElementFor(level.addressField);
            if (inputElement) {
                inputElement.attr('disabled', 'true');
                inputElement.addClass("disabled");
                inputElement.triggerHandler("disable", this);
            }
        }
        let nextLevel = levelAfter(level.addressField);
        if ( nextLevel != null ) {
            let  isManualField = _.contains(personAddressWithHierarchy.manualFields, nextLevel.addressField);
            if ( !isManualField ) {
                disableLevelsAfter(nextLevel);
            } else {
                // focus on the first manual field
                getInputElementFor(nextLevel.addressField).focus();
            }
        }
    }

    // starting from the top, load whatever we can (pre-filling levels if they only have one option, pre-fetching options
    // for the first level with choices
    function preloadLevels(level) {
        var searchString = searchStringUntil(level.addressField);
        if (searchString != null) {
            queryWithCallback(searchString, function (result) {
                if (result.length == 0) {
                    // there are no more entries from this level down, therefore disable all remaining levels of the hierarchy
                    disableLevelsAfter(level);
                } else if (result.length == 1) {
                    setValue(level.addressField, result[0].name);
                    preloadLevels(levelAfter(level.addressField));
                } else {
                    // focus on the element
                    getInputElementFor(level.addressField).focus();
                }
            });
        }
    }

    function levelFor(addressField) {
        return _.findWhere(levels, {addressField: addressField});
    }

    function getInputElementFor(addressField) {
        // this is to handle integration with HFE, when the id is set on the parent span, not the input element itself
        return $('#' + personAddressWithHierarchy.id + '-' + addressField).is('input') ?
            $('#' + personAddressWithHierarchy.id + '-' + addressField) :
            $('#' + personAddressWithHierarchy.id + '-' + addressField).find('input');
    }

    function getValue(addressField) {
        return getInputElementFor(addressField).val();
    }

    function setValue(addressField, value) {
        getInputElementFor(addressField).val(value);
        // when setting a field via a shortcut, do bookkeeping so that the autocompletes still work right
        getInputElementFor(addressField).data('legalValues', [ value ]);
        levelFor(addressField).lastSelection = value;
    }

    function getAddressField(element) {
        return $(element).attr('id').split('-')[1];
    }

    function levelsBefore(addressField) {
        var foundYet = false;
        var ret = [];
        _.each(levels, function (item) {
            if (!foundYet) {
                if (item.addressField == addressField) {
                    foundYet = true;
                }
                else {
                    ret.push(item);
                }
            }
        });
        return ret;
    }

    function levelsAfter(addressField) {
        var foundYet = false;
        var ret = [];
        _.each(levels, function (item) {
            if (foundYet) {
                ret.push(item);
            }
            else {
                if (item.addressField == addressField) {
                    foundYet = true;
                }
            }
        });
        return ret;
    }

    function levelAfter(addressField) {
        var nextLevels = levelsAfter(addressField);
        return nextLevels.length > 0 ? nextLevels[0] : null;
    }

    function searchStringUntil(addressField) {
        var somethingEmpty = false;
        var result = _.map(levelsBefore(addressField), function (level) {
            var val = getValue(level.addressField);
            if (val == '') {
                somethingEmpty = true;
            }
            return val;
        }).join("|");

        return somethingEmpty ? null : result;
    }

    function formatShortcutResponse(item) {
        var asList = [];
        while (item) {
            asList.push(item.name);
            item = item.parent;
        }
        return asList.join(", ")
        // return asList.join(" → ")
        // asList.reverse();
        // return asList.join(" ← ")
    }

    function shortcutResponseToLevels(item, startingFrom) {
        var startingLevel = 0;
        while (levels[startingLevel].addressField != startingFrom) {
            startingLevel += 1;
        }
        var result = { };
        var level = startingLevel;
        for (var level = startingLevel; level >= 0 && item; level -= 1) {
            result[levels[level].addressField] = item.name;
            item = item.parent;
        }
        return result;
    }

    function firstLevelNotIncluded(data) {
        for (var i = 0; i < levels.length; ++i) {
            var level = levels[i];
            if (!data[level.addressField]) {
                return level;
            }
        }
        return null;
    }

    function enableAddressField(level) {
        let inputElement = getInputElementFor(level.addressField);
        if (inputElement) {
            // reset any elements that might have been disabled when selecting a level with no descendants
            inputElement.removeAttr('disabled');
            inputElement.removeClass("disabled");
            inputElement.triggerHandler("enable", this);
        }
    }
    function clearLevelsAfter(addressField) {
        _.each(levelsAfter(addressField), function (level) {
            enableAddressField(level);
            setValue(level.addressField, '');
        });
    }

    personAddressWithHierarchy.container.find('.level').each(function () {

        var addressField = getAddressField(this);

        // this is to handle integration with HFE, when the id is set on the parent span, not the input element itself
        var element = $(this).is('input') ? $(this) : $(this).find('input');

        if (!_.contains(personAddressWithHierarchy.manualFields, addressField)) {
            element.autocomplete({
                minLength: 0,
                delay: 1,
                autoFocus: true,
                source: function (request, response) {
                    if (element.xhr) {
                        element.xhr.abort();
                    }
                    var level = levelFor(addressField);
                    var searchString = searchStringUntil(level.addressField);
                    if(!searchString && level.index != 0) {
                        return; // only allow empty string searches at the top level, to prevent UHM-1983
                    }
                    element.xhr = queryWithCallback(searchString, function (result) {
                        element.xhr = null;
                        element.data('legalValues', _.pluck(result, 'name'));
                        var regex = new RegExp($.ui.autocomplete.escapeRegex(request.term), "i"); // case insensitive
                        var matches = _.filter(result, function (item) {
                            return regex.test(item.name);
                        })
                        var results = _.map(matches, function (item) {
                            return {
                                label: item.name
                            }
                        });
                        // include an empty option if they haven't typed anything, so that just tabbing through doesn't
                        // auto-select the first option
                        if (request.term == '') {
                            results.unshift({ label: '' });
                        }
                        response(results);
                    });
                },
                select: function(event, ui) {
                    // Since the autocompletechange event doesn't behave as I would expect, instead we look for changes
                    // here in a more roundabout way
                    var level = levelFor(addressField);
                    if (ui.item.value != level.lastSelection) {
                        clearLevelsAfter(addressField);
                        setValue(level.addressField, ui.item.value);
                        preloadLevels(levelAfter(level.addressField));
                    }
                    level.lastSelection = ui.item.value;
                }
            }).blur(function(event) {
                // To make this behave like a autocomplete, where we don't allow invalid values, we need to check for
                // invalid values upon exiting the field.
                // (The autocompletechange event does not fire if you enter invalid values into it twice in a row, and
                // it fires unwantedly when we go to the next screen, so we need to use a plain event on the text field
                // for this.)
                var legalValues = element.data('legalValues');
                if (element.val() && !((legalValues && legalValues.length > 0) && _.contains(legalValues, element.val()))) {
                    element.val('');
                    setTimeout(function () {
                        element.focus();
                    });
                }
            }).focus(function () {
                $(this).select(); // selecting the entire field on focus makes this feel more like an autocomplete
                $(this).data("autocomplete").search($(this).val());
            });
        }
    });

    personAddressWithHierarchy.container.find(".address-hierarchy-shortcut").autocomplete({
        autoFocus: true,
        source: function (request, response) {
            if (request.term.length < 3) {
                return;
            }
            var url = '/' + OPENMRS_CONTEXT_PATH + '/module/addresshierarchy/ajax/getPossibleAddressHierarchyEntriesWithParents.form';
            $.getJSON(url, {
                limit: 50,
                addressField: personAddressWithHierarchy.shortcutFor,
                searchString: request.term
            }, function (result) {
                response(_.map(result, function (item) {
                    return {
                        label: formatShortcutResponse(item),
                        data: shortcutResponseToLevels(item, personAddressWithHierarchy.shortcutFor)
                    }
                }));
            });
        },
        select: function (event, ui) {
            // first, clear everything else
            _.each(levels, function (item) {
                enableAddressField(item);
                setValue(item.addressField, '');
            });
            _.each(ui.item.data, function (value, key) {
                setValue(key, value);
            });

            // go to the first level we didn't just set
            var goToLevel = firstLevelNotIncluded(ui.item.data);
            //this will disable next mandatory levels if no entries are configured in the system
            preloadLevels(goToLevel);

            // if we are using the simple UI navigator, use the NavigatorController so that the simple for UI keeps up
            if (typeof(NavigatorController) != 'undefined') {
                var field = NavigatorController.getFieldById(personAddressWithHierarchy.id + '-' + goToLevel.addressField);
                setTimeout(function () {
                    var oldField = selectedModel(NavigatorController.getFields());
                    if (oldField) {
                        oldField.toggleSelection();
                    }
                    field.select();
                });
            }
            // otherwise just jump manually
            else {
                getInputElementFor(goToLevel.addressField).focus();
            }
        },
        change: function (event, ui) {
            personAddressWithHierarchy.container.find(".address-hierarchy-shortcut").val('');
        }
    });
}
