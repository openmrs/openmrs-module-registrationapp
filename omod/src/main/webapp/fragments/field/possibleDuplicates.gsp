<%
    // The body of the "Possible duplicates" question, see duplicateCheck.js. Rendered by registerPatient.gsp, which also
    // adds the "duplicates-gate" and "no-confirmation" classes to the question for a field of this type.
    // config.triggerField is the name of the form field that, once entered, starts the check; without it, the check starts
    // once the question before this one has been completed.
%>
<div id="duplicates-widget" <% if (config.triggerField) { %>data-trigger-field="${ ui.escapeAttribute(config.triggerField) }"<% } %>>
    <h3 id="duplicates-heading" tabindex="-1">${ ui.message("registrationapp.duplicates.title") }</h3>
    <div id="duplicates-screen">
        <div class="note-container"><div class="note warning"><div class="text" id="duplicates-warning"></div></div></div>
        <ul id="duplicates-strong" class="duplicate-list"></ul>
        <a href="#" id="duplicates-toggle-weak" role="button" aria-expanded="false" aria-controls="duplicates-weak" style="display: none;"></a>
        <ul id="duplicates-weak" class="duplicate-list" style="display: none;"></ul>
        <p class="duplicates-hint">${ ui.message("registrationapp.duplicates.hint") }</p>
    </div>
    <p>
        <span class="field-error"></span>
        <button type="button" id="duplicates-none" class="confirm"></button>
    </p>
</div>
