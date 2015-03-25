package org.openmrs.module.registrationapp.contextmodel;


import org.openmrs.Patient;

public class PatientContextModel {

    private String uuid;
    private Integer patientId;

    public PatientContextModel(Patient patient) {
        this.uuid = patient.getUuid();
        this.patientId = patient.getPatientId();
    }

    public String getUuid() {
        return uuid;
    }

    public Integer getPatientId() {return patientId; }
}
