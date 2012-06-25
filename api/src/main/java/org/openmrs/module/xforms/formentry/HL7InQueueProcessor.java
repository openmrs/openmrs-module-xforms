package org.openmrs.module.xforms.formentry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7InArchive;
import org.openmrs.hl7.HL7InError;
import org.openmrs.hl7.HL7InQueue;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.hl7v2.HL7Exception;


/**
 * Processes message in the HL7 inbound queue. Messages are moved into either the archive or error
 * table depending on success or failure of the processing. You may, however, set a global property
 * that causes the processor to ignore messages regarding unknown patients from a non-local HL7
 * source. (i.e. those messages neither go to the archive or the error table.)
 * 
 * Code in this class has been copied from the openmrs api and modified
 * such that hl7 processing exceptions get back to the caller.
 * Very useful feature when one submits a form in the browser and never
 * get any error message yet when deep down during hl7 processing, the 
 * form has errors.
 * 
 * @version 1.0
 */
@Transactional
public class HL7InQueueProcessor /* implements Runnable */{

	private final Log log = LogFactory.getLog(this.getClass());

	private HL7Receiver receiver = new HL7Receiver();


	// processor per JVM

	/**
	 * Empty constructor (requires context to be set using <code>setContext(Context)</code> method
	 * before any other calls are made)
	 */
	public HL7InQueueProcessor() {
	}

	/**
	 * Process a single queue entry from the inbound HL7 queue
	 * 
	 * @param hl7InQueue queue entry to be processed
	 */
	public void processHL7InQueue(HL7InQueue hl7InQueue, boolean propagateErrors) throws Exception {

		if (log.isDebugEnabled())
			log.debug("Processing HL7 inbound queue (id=" + hl7InQueue.getHL7InQueueId() + ",key="
					+ hl7InQueue.getHL7SourceKey() + ")");

		// Parse the HL7 into an HL7Message or abort with failure
		String hl7Message = hl7InQueue.getHL7Data();
		try {
			// Send the inbound HL7 message to our receiver routine for
			// processing
			if (log.isDebugEnabled())
				log.debug("Sending HL7 message to HL7 receiver");
			receiver.processMessage(hl7Message);

			// Move HL7 inbound queue entry into the archive before exiting
			if (log.isDebugEnabled())
				log.debug("Archiving HL7 inbound queue entry");
			HL7InArchive hl7InArchive = new HL7InArchive(hl7InQueue);
			Context.getHL7Service().saveHL7InArchive(hl7InArchive);
			if (log.isDebugEnabled())
				log.debug("Removing HL7 message from inbound queue");
			Context.getHL7Service().purgeHL7InQueue(hl7InQueue);
		}
		catch (HL7Exception e) {
			boolean skipError = false;
			log.error("Unable to process hl7inqueue: " + hl7InQueue.getHL7InQueueId(), e);
			log.error("Hl7inqueue source: " + hl7InQueue.getHL7Source());
			log.error("hl7_processor.ignore_missing_patient_non_local? "
					+ Context.getAdministrationService().getGlobalProperty("hl7_processor.ignore_missing_patient_non_local",
					"false"));
			if (e.getCause() != null
					&& e.getCause().getMessage().equals("Could not resolve patient")
					&& !hl7InQueue.getHL7Source().getName().equals("local")
					&& Context.getAdministrationService().getGlobalProperty(
							"hl7_processor.ignore_missing_patient_non_local", "false").equals("true")) {
				skipError = true;
			}
			if (!skipError)
				setFatalError(hl7InQueue, "Trouble parsing HL7 message (" + hl7InQueue.getHL7SourceKey() + ")", e,propagateErrors);
			
			if(propagateErrors)
				throw e;
			
			return;
		}
		catch (Exception e) {
			setFatalError(hl7InQueue, "Exception while attempting to process HL7 In Queue (" + hl7InQueue.getHL7SourceKey()
					+ ")", e,propagateErrors);
			
			if(propagateErrors)
				throw e;
		}

		// clean up memory after processing each queue entry (otherwise, the
		// memory-intensive process may crash or eat up all our memory)
		try {
			Context.getHL7Service().garbageCollect();
		}
		catch (Exception e) {
			log.error("Exception while performing garbagecollect in hl7 inbound processor", e);
		}

	}


	/**
	 * Convenience method to respond to fatal errors by moving the queue entry into an error bin
	 * prior to aborting
	 */
	private void setFatalError(HL7InQueue hl7InQueue, String error, Throwable cause, boolean propagateErrors) {
		HL7InError hl7InError = new HL7InError(hl7InQueue);
		hl7InError.setError(error);
		hl7InError.setErrorDetails(cause == null ? "" : cause.getMessage());
		
		if(!propagateErrors)
			Context.getHL7Service().saveHL7InError(hl7InError);
		
		Context.getHL7Service().purgeHL7InQueue(hl7InQueue);
		log.error(error, cause);
	}
}