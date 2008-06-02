package org.openmrs.module.xforms;


/**
 * 
 * @author daniel
 *
 */
public class XformCohort {

    private int cohortId;
    private String name;
    
    public XformCohort() {
        super();
    }

    public XformCohort(int cohortId, String name) {
        super();
        this.cohortId = cohortId;
        this.name = name;
    }

    public int getCohortId() {
        return cohortId;
    }

    public void setCohortId(int cohortId) {
        this.cohortId = cohortId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
 }
