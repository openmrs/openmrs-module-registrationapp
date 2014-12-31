// currently we only support having one of these per page. eventually refactor so that the fragment can create an
// instance of this. For now, all these properties must be populated from the GSP/HTML that includes this JS.
var personAddressWithHierarchy = {
    id: null,
    container: null,
    manualFields: []
};

$(function () {

    var levels;
    $.getJSON('/' + OPENMRS_CONTEXT_PATH + '/module/addresshierarchy/ajax/getOrderedAddressHierarchyLevels.form', {}, function (result) {
        levels = result;
        _.each(levels, function (item, index) {
            item.index = index;
        });

        preloadLevels(levels[0]);
    });

    // starting from the top, load whatever we can (pre-filling levels if they only have one option, pre-fetching options
    // for the first level with choices
    function preloadLevels(level) {
        loadOptionsFor(level).then(function () {
            if (level.autocompleteOptions && level.autocompleteOptions.length == 1) {
                setValue(level.addressField, level.autocompleteOptions[0].name);
                preloadLevels(levelAfter(level.addressField));
            }
        });
    }

    function levelFor(addressField) {
        return _.findWhere(levels, {addressField: addressField});
    }

    function getValue(addressField) {
        return $('#' + personAddressWithHierarchy.id + '-' + addressField).val();
    }

    function setValue(addressField, value) {
        $('#' + personAddressWithHierarchy.id + '-' + addressField).val(value);
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

    function clearOptionsFor(level) {
        level.activeQuery = null;
        level.autocompleteOptions = null;
    }

    function loadOptionsFor(level) {
        if (level.activeQuery) {
            return level.activeQuery;
        }
        else if (level.autocompleteOptions) {
            return $.Deferred().resolve();
        }
        else {
            level.autocompleteOptions = null;
            var searchString = searchStringUntil(level.addressField);
            if (searchString == null) {
                // this means that we shouldn't be searching for this level now
                level.activeQuery = null;
                level.autocompleteOptions = null;
                return $.Deferred().resolve();
            }
            level.activeQuery = $.getJSON('/' + OPENMRS_CONTEXT_PATH + '/module/addresshierarchy/ajax/getChildAddressHierarchyEntries.form', {
                searchString: searchString
            }, function (result) {
                level.autocompleteOptions = result;
            });
            level.activeQuery.always(function() {
                level.activeQuery = null;
            });
            return level.activeQuery;
        }
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

    function clearLevelsAfter(addressField) {
        console.log("clearing levels after: " + addressField);
        _.each(levelsAfter(addressField), function (level) {
            setValue(level.addressField, '');
            clearOptionsFor(level);
        });
    }

    personAddressWithHierarchy.container.find('.level').each(function () {
        var element = $(this);
        var addressField = element.attr('name');
        if (personAddressWithHierarchy.manualFields.indexOf(addressField) < 0) {
            $(this).autocomplete({
                minLength: 0,
                delay: 1,
                autoFocus: true,
                source: function (request, response) {
                    var level = levelFor(addressField);
                    loadOptionsFor(level).then(function () {
                        var regex = new RegExp($.ui.autocomplete.escapeRegex(request.term), "i"); // case insensitive
                        var matches = _.filter(level.autocompleteOptions, function (item) {
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
                    if (ui.item.value != getValue(addressField)) {
                        clearLevelsAfter(addressField);
                    }
                },
                change: function (event, ui) {
                    loadOptionsFor(levelAfter(addressField));
                }
            }).change(function () {
                // hitting here means they entered a free-text value (otherwise we'd get a change event in the autocomplete)
                // we don't want to allow this, but the autocomplete widget doesn't have anything built in for this
                setValue(addressField, '');
                clearLevelsAfter(addressField);
                setTimeout(function () {
                    element.focus();
                })
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
                addressField: 'address1',
                searchString: request.term
            }, function (result) {
                response(_.map(result, function (item) {
                    return {
                        label: formatShortcutResponse(item),
                        data: shortcutResponseToLevels(item, 'address1')
                    }
                }));
            });
        },
        select: function (event, ui) {
            // first, clear everything else
            _.each(levels, function (item) {
                setValue(item.addressField, '');
            });
            _.each(ui.item.data, function (value, key) {
                setValue(key, value);
            });

            // go to the first level we didn't just set, using NavigatorController so that the simple for UI keeps up
            var goToLevel = firstLevelNotIncluded(ui.item.data);
            var field = NavigatorController.getFieldById(personAddressWithHierarchy.id + '-' + goToLevel.addressField);
            setTimeout(function () {
                var oldField = selectedModel(NavigatorController.getFields());
                if (oldField) {
                    oldField.toggleSelection();
                }
                field.select();
            });
        },
        change: function (event, ui) {
            personAddressWithHierarchy.container.find(".address-hierarchy-shortcut").val('');
        }
    });
});