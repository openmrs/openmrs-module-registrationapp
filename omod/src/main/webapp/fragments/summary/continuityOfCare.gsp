<%
    ui.includeJavascript("registrationapp", "summary/continuityOfCare.js")
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
                <button type="button" class="confirm" onclick="viewCCD()">
                    ${ ui.message("registrationapp.continuityOfCare.document.view") }
                </button>
                <button class="submitButton right" onclick="importCCD(${ config.patientId })")>
                    <i class="icon-download"></i>
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
<% } %>
