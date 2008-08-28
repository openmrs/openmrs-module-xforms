package org.openmrs.module.xforms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;

/**
 * Provides default serialization of cohorts (name and cohort_id) to clients. This class is used
 * when sending a list of cohorts to clients like mobile devices that may want
 * to collect data for selected patient cohorts.
 * 
 * For those who want a different serialization format for cohorts, just
 * implement the SerializableData interface and specify the class using the
 * openmrs global property {xforms.cohortSerializer}. The jar containing this
 * class can then be put under the webapps/openmrs/web-inf/lib folder. One of
 * the reasons one could want a different serialization format is for
 * performance by doing a more optimized and compact format. Another reason i
 * can foresee is for non java clients who may say want the cohorts in xml or
 * any other format, because the default implementation assumes java clients.
 * 
 * @author Daniel
 * 
 */
public class DefaultCohortSerializer {

    private Log log = LogFactory.getLog(this.getClass());

    public DefaultCohortSerializer() {

    }

    /**
     * @see org.openmrs.module.xforms.SerializableData#serialize(java.io.DataOutputStream,java.lang.Object)
     */
    public void serialize(DataOutputStream dos, Object data) {
        try {
            List<Cohort> cohorts = (List<Cohort>) data;

            dos.writeByte(cohorts.size());
            for (Cohort cohort : cohorts)
                serialize(cohort, dos);

        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
    }

    /**
     * Serializes a cohort to the stream.
     * 
     * @param cohort - the cohort to serialize.
     * @param dos - the stream to write to.
     */
    protected void serialize(Cohort cohort, DataOutputStream dos) {
        try {
            dos.writeInt(cohort.getCohortId());
            dos.writeUTF(cohort.getName());
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

    /**
     * @see org.openmrs.module.xforms.SerializableData#deSerialize(java.io.DataInputStream,
     *      java.lang.Object)
     */
    public Object deSerialize(DataInputStream dis, Object data) {
        return null;
    }
}
