<%
    ui.includeJavascript("registrationapp", "summary/continuityOfCare.js")
    ui.includeCss("registrationapp","continuityOfCare.css")
%>
<% if (isCCDAvailable) { %>
<div class="info-section">
    <div class="info-header">
        <i class="icon-user"></i>
        <h3>${ ui.message("registrationapp.continuityOfCare.label") }</h3>
    </div>
    <div class="info-body summary-section">
        <div id="ccd-fragment">
            <div>
                <h3>${ ui.message("registrationapp.continuityOfCare.document.date") }</h3>
                <p class="left">${ ui.message(CCDDate) }</p>
                <i id="ccdRefresh" class="icon-refresh edit-action right" title="Update CCD" onclick="refreshCcd(${ config.patientId })"></i>
            </div>
            <div>
                <h3></h3>
                <p class="left">${ ui.message("registrationapp.continuityOfCare.info") }</p>
            </div>
        </div>
        <div id="buttons">
            <button type="button" class="confirm" onclick="viewCCD(${ config.patientId })">
                ${ ui.message("registrationapp.continuityOfCare.document.view") }
            </button>
        </div>
    </div>
</div>
<% } else{%>
<div class="info-section">
    <div class="info-header">
        <i class="icon-user"></i>
        <h3>${ ui.message("registrationapp.continuityOfCare.label") }</h3>
    </div>
    <div class="info-body summary-section">
        <div id="ccd-import-fragment">
            <div>
                <h3></h3>
                <p class="left">${ ui.message("registrationapp.continuityOfCare.info.missing") }</p>
            </div>
        </div>
        <div id="import-buttons">
            <button type="button" class="confirm" onclick="importCCD(${ config.patientId })">
                ${ ui.message("registrationapp.continuityOfCare.document.import") }
            </button>
        </div>
    </div>

    <div style="display:none" id="ccd-import-dialog" class="dialog">
        <div class="dialog-header">
            ${ui.message("registrationapp.continuityOfCare.document.import.dialog.label")}
        </div>
        <div class="dialog-content">
            <p>
                ${ui.message("registrationapp.continuityOfCare.document.import.dialog.message")}
            </p>
            <br/>
            <div class="buttons">
                <button class="confirm right">${ui.message("emr.yes")}</button>
                <button class="cancel">${ui.message("emr.no")}</button>
            </div>
        </div>
    </div>
</div>

<%}%>
