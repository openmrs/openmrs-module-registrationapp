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
            </div>
            <div>
                <h3></h3>
                <p class="left">${ ui.message("registrationapp.continuityOfCare.info") }</p>
            </div>
        </div>
        <div id="buttons">
            <button type="button" class="confirm"onclick="location.href='${ui.pageLink("registrationapp", "viewCcd", [patientId: config.patientId, appId: appId ])}';">
                ${ ui.message("registrationapp.continuityOfCare.document.view") }
            </button>
            <a class="button confirm right" href="#" onclick="refreshCcd(${ config.patientId },'${ui.pageLink("registrationapp", "viewCcd", [patientId: config.patientId, appId: appId ])}')">
                ${ ui.message("registrationapp.continuityOfCare.document.refresh") }
                <i id="ccdRefresh" class="icon-refresh" title="Update CCD"> </i>
            </a>
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
            <button type="button" class="confirm" onclick="importCCD(${ config.patientId }, '${ui.pageLink("registrationapp", "viewCcd", [patientId: config.patientId, appId: appId ])}')">
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
