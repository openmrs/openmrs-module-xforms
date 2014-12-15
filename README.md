openmrs-module-xforms
=====================

**Overview**
=====================
This module serves as one of the alternatives to Microsoft InfoPath for OpenMRS data entry.
The module converts an OpenMRS form to an XForm.
Data entry can be done using any browser that supports JavaScript. The browser which has been tested most frequently with this module is Mozilla Firefox and hence is the recommended.

This module also consumes and serves OpenMRS forms as XForms from and to applications that use the XForms standard.
An example of such applications are the XForms Mobile Data Collection tools for OpenMRS.
The communication between the mobile device and this module can take places via HTTP, Bluetooth or SMS.
The Bluetooth and SMS communication is implemented by the OpenMRS Bluetooth and SMS scheduled tasks respectively.
Applications that work in offline mode will normally start by downloading a set of patients to collect data for. So these patient sets are supplied by this module.
To ensure that only authorized users will access these applications, the module also serves the users to be downloaded and used for such purposes.

**Installation**
=====================
1. Download the [latest version](https://dev.openmrs.org/modules/view.jsp?module=xforms) from the OpenMRS module            repository and add it to your OpenMRS application using the Administration->Manage Modules page.

**Entering Data**
=====================
1. Search for a patient and select him or her.
2. Click the Form Entry tab.
3. Click Edit to open an existing encounter. If you want to fill a new encounter, select the form you want to fill and    Click the add button to display the form.
4. After filling the form, click the Submit button to save it or Cancel button to discard the changes and go back to     the patient screen.

**Creating an Xform**
=====================
You can create an Xform by:
  1. On the "Administration" screen, select "Manage Forms"
  2. Select the "Basic Form" from the drop down list and click the "Duplicate" button.
  3. Enter the name and description of the form and then click the "Create Form and Duplicate Schema" button.
    
     Versions 3.9.9 and above of the xforms module make it easier to create forms because you just add a new form and      then the default fields are automatically created, hence not requiring duplicating the Basic Form, which may not      be available for installations that do not have demo data.

     Because an xform is built from a form schema, you may want to read about creating form schema at [Administering       Forms](https://wiki.openmrs.org/display/docs/Administering+Forms). If you already have your concepts created, you      do not need to read the administering forms link. Just search for these concepts, from the right hand "Find Field      Elements" textbox, then drag and drop them onto the OBS section of the form on the form schema screen as shown in      the diagram below.

     Ensure that your form has the Encounter Type field set. Forgetting to do so will result into errors while trying      to submit form data.

     One note is that all concepts should be entered under the OBS branch of the schema.
     
  4. Once you are done with building the form schema, click the "Design XForm" link which will open the [XForms Module      Form Designer](https://wiki.openmrs.org/display/docs/XForms+Module+Form+Designer) to allow you customize the          form. Check the Design Surface Tab to ensure that you can see some widgets. If the form looks good (even if you       have made no changes)  you should save the form. When you save the form, it will be ready for data entry.

     Note that after customizing and saving the form in the designer, if you later on remove from or add more fields       to the form schema, in order to have them picked up by the form designer, you will need to right click on the         form name under the form designer's "Form Fields" panel and then select "Refresh" from the menu that pops up.         That way the new fields that you added to the form schema will show up in the form designer. Remember to save         after doing any such changes in the form designer.
     
**Global Properties**
=====================
Use the following global properties for further customizations:

  * xforms.useEncounterXform: Set to true if you want to use XForms to edit encounters instead of the default openmrs     edit encounter screen, else set to false. Default value is false
  
  * xforms.usePatientXform: Set to true if you want to use XForms to create new patients instead of the default           openmrs create patient form, else set to false. Default value is false
  
  * xforms.timeDisplayFormat: The display format of time used by the xforms module. Default value is hh:mm:ss a
  
  * xforms.timeSubmitFormat: The format of the time passed in the xml of the xforms model. Please make sure this          matches with the date format of your data entry applications, else you will get wrong times on the server. Default     value is hh:mm:ss a
  
  * xforms.dateDisplayFormat: The display format of dates used by the xforms module. Default value is dd/MM/yyyy
  
  * xforms.dateSubmitFormat: The format of the dates passed in the xml of the xforms model. Please make sure this         matches with the date format of your data entry applications, else you will get wrong dates on the server. Default     value is yyyy-MM-dd
  
  * xforms.dateTimeDisplayFormat: The display format of datetime used by the xforms module. Default value is              dd/MM/yyyy hh:mm:ss a
  
  * xforms.dateTimeSubmitFormat: The format of the datetime is passed in the xml of the xforms model. Please make sure     this matches with the date format of your data entry applications, else you will get wrong dates on the server.       Default value is yyyy-MM-dd hh:mm:ss a
  
  * xforms.smsSendFailureReports: Set to true if you want sms sender to get failure reports, else set to false.           Default value is true
  
  * xforms.smsSendSuccessReports: Set to true if you want sms sender to get success reports, else set to false.           Default value is true
  
  * xforms.defaultFontFamily: The default font family used by the xforms module. Default value is Verdana, 'Lucida        Grande', 'Trebuchet MS', Arial, Sans-Serif
  
  * xforms.defaultFontSize: The default font size used by the form designer. Default value if 16
  
  * xforms.complexobs_dir: Directory for storing complex obs used by the xforms module. Default value is                  xforms/complexobs
  
  * xforms.error_dir: Directory containing the xforms error items. This will contain xform model xml files that have      not been submitted into the formentry queue because of processing errors. Default value is xforms/error
  
  * xforms.queue_dir: Directory containing the xforms queue items. This will contain xforms xml model files submitted     and awaiting processing to be submitted into the formentry queue. Default value is xforms/queue
  
  * xforms.archive_dir: Directory containing the xforms archive items. This will contain xform model xml files that       have been processed and then submitted successfully into the formentry queue. Default value is                        xforms/archive/%Y/%M
  
  * xforms.showSubmitSuccessMsg: Set to true if you want to display the form submitted successfully message every time     a form is submitted successfully, else set to false. Default value is false
  
  * xforms.localeList: The list of locales or languages supported by the form designer. The format is                     key:name,key:name,key:name ... Default value is en:English,fr:French,gr:German,swa:Swahili. These will be             displayed in the form designer's language drop down list to be used when designing forms in multiple languages.
  
  * xforms.showLanguageTab: Set to true if you want to display the language xml tab of the form designer, else set to     false. Default value is false.
  
  * xforms.showLayoutXmlTab: Set to true if you want to display the layout xml tab of the form designer, else set to      false. Default value is false.
  
  * xforms.showModelXmlTab: Set to true if you want to display the model xml tab of the form designer, else set to        false. Default value is false.
  
  * xforms.showXformsSourceTab: Set to true if you want to display the xforms source tab (xml tab) of the form            designer, else set to false. Default value is false.
  
  * xforms.showJavaScriptTab: Set to true if you want to display the JavaScript tab of the form designer, else set to     false. Default value is false.

  * xforms.setDefaultProvider: Set to true if you want to automatically set the encounter provider to the logged on       user, if he or she has the provider role, when filling a form. Default value is false.

  * xforms.setDefaultLocation: Set to true if you want to automatically set the encounter location to that of the         logged on user, when filling a form. Default value is false.

  * xforms.showOfflineFormDesigner: Set to true if you want to show the form designer in off line mode, else set to       false. Default value is false. The off line mode form designer is accessed from the admin screen after selecting      the "Xforms Designer" link.

  * xforms.allowBindEdit: Set to true if you want to allow editing of question bindings in the form designer, else set     to false. Default value is false.

  * xforms.encounterDateIncludesTime: Set to true if the encounter date should include time, else set to false.           Default value is false.
  
  * xforms.saveFormat: The format in which the xforms will be saved. For OpenRosa / JavaRosa based clients, the value     is: javarosa. The default value is purcforms.
  
  * xforms.new_patient_identifier_type_id: The id of the patient identifier type which will be used when creating new     patients from forms which do not have a patient_identifier.identifier_type_id field. If a form has the                patient_identifier.identifier_type_id field, it takes preference over the one in the global property.

**Copying Xforms between servers**
=====================
You can copy xforms from one server to another as long as the two servers have the same concept dictionary. These are the steps of doing so:
  1. On the source server, open the form in the form designer and select "Save As" from the "File" menu. This will         save a file which you will use to copy the form into the destination server.
  2. On the destination server, create a new form as a duplicate of the OpenMRS basic form. Give it the same name as       the form you want to copy.
  3. Under the "Manage Forms" screen, select the new form and click the "Design Xform" link which will take you to the      form designer.
  4. Delete the form from the designer by clicking the "Delete Selected" toolbar icon or right click on the form in        the Forms tree and select "delete" from the menu that pops up.
  5. Select "Open" from the "File" menu or click the open toolbar icon and browse to locate the file you saved from        the source server.
  6. After opening the file and seeing the form loaded in the designer, select "Save" from the "File" menu or click        the save toolbar icon. That's all.

NOTE: The above steps do not copy the form schema. Therefore you may need to use the [FormImportExport Module](https://wiki.openmrs.org/display/docs/FormImportExport+Module) to copy the form schema.

**General Recommendations from Building XForms**
  1. If you want to have a listbox i.e. pulldown menu, the concept for the question must be of type coded. Then you        will have to create concepts for each of the answers, place them as responses to the question concept within the      concept dictionary in OpenMRS.  Then when you go to create the XForm it'll appear as a listbox.  The concepts for      the answer will probably be of type N/A.
  2. In order to be able to submit an XForm (at least with version 3.9.7 a user must have the Provider privileges, as      well as these View Users, Add HL7 Inbound Archive, Update HL7 Inbound Archive, Edit Encounters, Add Encounters.       Otherwise you will get a Transaction rolled back because it has been marked as rollback-only error.

View Users
Add HL7 Inbound Archive
Update HL7 Inbound Archive

Purge HL7 inbound queue

View HL7

Add HL7 inbound exception

Delete HL7 inbound queue

Edit Encounters
Add Encounters

If your system does not have any of these privileges, just create them using the [Add Privilege](http://demo.openmrs.org/openmrs/admin/users/privilege.form) link under [Manage Privileges](http://demo.openmrs.org/openmrs/admin/users/privilege.list) on the [Administration](http://demo.openmrs.org/openmrs/admin) page.

**Known Issues**
  1. When using Bluetooth to connect to openmrs, any changes to form definitions are received only after restarting        OpenMRS or the Bluetooth service.
