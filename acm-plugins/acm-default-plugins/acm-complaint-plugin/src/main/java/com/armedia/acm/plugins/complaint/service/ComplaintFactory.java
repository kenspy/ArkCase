package com.armedia.acm.plugins.complaint.service;

import com.armedia.acm.form.config.xml.ParticipantItem;
import com.armedia.acm.frevvo.config.FrevvoFormName;
import com.armedia.acm.plugins.addressable.model.ContactMethod;
import com.armedia.acm.plugins.addressable.model.PostalAddress;
import com.armedia.acm.plugins.addressable.model.xml.GeneralPostalAddress;
import com.armedia.acm.plugins.addressable.model.xml.InitiatorContactMethod;
import com.armedia.acm.plugins.addressable.model.xml.InitiatorPostalAddress;
import com.armedia.acm.plugins.addressable.model.xml.PeopleContactMethod;
import com.armedia.acm.plugins.addressable.model.xml.PeoplePostalAddress;
import com.armedia.acm.plugins.complaint.model.Complaint;
import com.armedia.acm.plugins.complaint.model.complaint.ComplaintForm;
import com.armedia.acm.plugins.complaint.model.complaint.Contact;
import com.armedia.acm.plugins.complaint.model.complaint.MainInformation;
import com.armedia.acm.plugins.complaint.model.complaint.xml.InitiatorContact;
import com.armedia.acm.plugins.complaint.model.complaint.xml.InitiatorMainInformation;
import com.armedia.acm.plugins.complaint.model.complaint.xml.PeopleContact;
import com.armedia.acm.plugins.complaint.model.complaint.xml.PeopleMainInformation;
import com.armedia.acm.plugins.person.dao.PersonDao;
import com.armedia.acm.plugins.person.model.Organization;
import com.armedia.acm.plugins.person.model.Person;
import com.armedia.acm.plugins.person.model.PersonAlias;
import com.armedia.acm.plugins.person.model.PersonAssociation;
import com.armedia.acm.plugins.person.model.xml.InitiatorOrganization;
import com.armedia.acm.plugins.person.model.xml.InitiatorPersonAlias;
import com.armedia.acm.plugins.person.model.xml.PeopleOrganization;
import com.armedia.acm.plugins.person.model.xml.PeoplePersonAlias;
import com.armedia.acm.services.participants.model.AcmParticipant;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ComplaintFactory
{
	private static final String ANONYMOUS = "Anonymous";
	private static final String DEFAULT_USER = "*";
	
	private PersonDao personDao;
	
    public Complaint asAcmComplaint(ComplaintForm formComplaint)
    {
        Complaint retval = new Complaint();
        
        retval.setDetails(formComplaint.getComplaintDescription());
        retval.setIncidentDate(formComplaint.getDate());
        retval.setPriority(formComplaint.getPriority());
        retval.setComplaintTitle(formComplaint.getComplaintTitle());
        retval.setParticipants(convertToAcmParticipants(formComplaint.getParticipants()));
        
        Calendar  cal = Calendar.getInstance();
        cal.setTime(formComplaint.getDate());
        cal.add(Calendar.DATE, 3);
        
        Date dueDate = cal.getTime();        
        retval.setDueDate(dueDate);
        retval.setComplaintType(formComplaint.getCategory());
        retval.setTag(formComplaint.getComplaintTag());
        retval.setFrequency(formComplaint.getFrequency());

        if (formComplaint.getLocation() != null)
        {
        	retval.setLocation(formComplaint.getLocation().returnBase());
        }
        
        if ( formComplaint.getInitiator() != null )
        {
            PersonAssociation pa = new PersonAssociation();
            Person p = new Person();

            if (null != formComplaint.getInitiator().getContactType() && "existingInitiator".equals(formComplaint.getInitiator().getContactType()))
            {
            	if (null != formComplaint.getInitiator().getSearchResult() && null != formComplaint.getInitiator().getSearchResult().getId())
            	{
            		p = getPersonDao().find(formComplaint.getInitiator().getSearchResult().getId());
            	}
            }

            pa.setPerson(p);
            retval.setOriginator(pa);

            populatePerson(formComplaint.getInitiator().returnBase(), pa, p);
        }

        if ( formComplaint.getPeople() != null )
        {
            for ( Contact person : formComplaint.getPeople() )
            {
            	if (person.getMainInformation() != null && person.getMainInformation().getFirstName() != null && person.getMainInformation().getLastName() != null){
	                PersonAssociation pa = new PersonAssociation();
	                Person p = new Person();
	                pa.setPerson(p);
	                retval.getPersonAssociations().add(pa);
	
	                populatePerson(person.returnBase(), pa, p);
	            }
            }
        }

        return retval;
    }
    
    public ComplaintForm asFrevvoComplaint(Complaint complaint)
    {
    	ComplaintForm complaintForm = new ComplaintForm();
    	
    	complaintForm.setComplaintId(complaint.getComplaintId());
    	complaintForm.setComplaintNumber(complaint.getComplaintNumber());
    	complaintForm.setComplaintTitle(complaint.getComplaintTitle());
    	complaintForm.setCategory(complaint.getComplaintType());
    	complaintForm.setComplaintDescription(complaint.getDetails());
    	complaintForm.setPriority(complaint.getPriority());
    	complaintForm.setDate(complaint.getCreated());
    	complaintForm.setComplaintTag(complaint.getTag());
    	complaintForm.setFrequency(complaint.getFrequency());
    	
    	if (complaint.getLocation()!= null)
    	{
    		complaintForm.setLocation(new GeneralPostalAddress(complaint.getLocation()));
    	}
    	
    	if (complaint.getOriginator() != null && complaint.getOriginator().getPerson() != null)
    	{
    		Contact contact = new InitiatorContact();
    		contact = populateFrevvoContact(contact, complaint.getOriginator(), complaint.getOriginator().getPerson());
    		
    		complaintForm.setInitiator(contact);
    	}
    	
    	if (complaint.getPersonAssociations() != null && complaint.getPersonAssociations().size() > 0)
    	{
    		Contact initiator = complaintForm.getInitiator();
    		List<Contact> contacts = new ArrayList<Contact>();
    		for (PersonAssociation personAssociation : complaint.getPersonAssociations())
    		{
    			if (personAssociation.getPerson() != null)
    			{
    				Contact contact = new PeopleContact();
    				contact = populateFrevvoContact(contact, personAssociation, personAssociation.getPerson());
    				
    				boolean addContact = true;
    				if (initiator != null && initiator.getId() != null && contact != null && contact.getId() != null && initiator.getId().equals(contact.getId()))
    				{
    					addContact = false;
    				}
    				
    				if (addContact)
    				{
    					contacts.add(contact);
    				}
    			}
    		}
    		
    		complaintForm.setPeople(contacts);
    	}
    	
    	complaintForm.setCmisFolderId(complaint.getEcmFolderId());
    	
    	// Populate participants
    	if (complaint.getParticipants() != null && complaint.getParticipants().size() > 0)
    	{
    		List<ParticipantItem> participants = new ArrayList<ParticipantItem>();
    		for (AcmParticipant participant : complaint.getParticipants())
    		{
    			if (!DEFAULT_USER.equals(participant.getParticipantType()))
    			{
	    			ParticipantItem pi = new ParticipantItem();
	    			pi.setType(participant.getParticipantType());
	    			pi.setValue(participant.getParticipantLdapId());
	    			
	    			participants.add(pi);
    			}
    		}
    		
    		complaintForm.setParticipants(participants);
    	}
    	
    	return complaintForm;
    }
    
    private Contact populateFrevvoContact(Contact contact, PersonAssociation personAssociation, Person person)
    {
    	if (contact != null && person != null && personAssociation != null)
    	{
    		contact.setId(person.getId());
    		contact.setContactType(personAssociation.getPersonType());
    		contact.setNotes(personAssociation.getNotes());
    		
    		// Populate main information
    		populateMainInformation(contact, person, personAssociation);
    		
    		// Populate communication devices
    		populateCommunicationDevices(contact, person);
    		
    		// Populate organizations
    		populateOrganizations(contact, person);
    		
    		// Populate locations
    		populateLocations(contact, person);
    		
    		// Populate alias
    		populateAlias(contact, person);
    	}
    	
    	return contact;
    }
    
    private void populateMainInformation(Contact contact, Person person, PersonAssociation personAssociation)
    {
    	MainInformation mainInformation = null;
		if (contact instanceof InitiatorContact)
		{
			mainInformation = new InitiatorMainInformation();
		} 
		else if (contact instanceof PeopleContact)
		{
			mainInformation = new PeopleMainInformation();
		}
		
    	if (mainInformation != null)
		{
    		mainInformation.setTitle(person.getTitle());
    		mainInformation.setFirstName(person.getGivenName());
    		mainInformation.setLastName(person.getFamilyName());
    		mainInformation.setType(personAssociation.getPersonType());
    		mainInformation.setDescription(personAssociation.getPersonDescription());
    		if (personAssociation.getTags() != null)
    		{
    			for (String tag : personAssociation.getTags())
    			{
    				if (ANONYMOUS.equals(tag))
    				{
    					mainInformation.setAnonymous("true");
    					break;
    				}
    			}
    		}
		}
		
		contact.setMainInformation(mainInformation);
    }
    
    private void populateCommunicationDevices(Contact contact, Person person)
    {
    	if (person.getContactMethods() != null && person.getContactMethods().size() > 0)
		{
			List<ContactMethod> communicationDevices = new ArrayList<ContactMethod>();
			for (ContactMethod contactMethod : person.getContactMethods())
			{
				if (contact instanceof InitiatorContact)
	    		{
					ContactMethod c = new InitiatorContactMethod(contactMethod);
					communicationDevices.add(c);
	    		} 
	    		else if (contact instanceof PeopleContact)
	    		{
	    			ContactMethod c = new PeopleContactMethod(contactMethod);
					communicationDevices.add(c);
	    		}
			}
			
			contact.setCommunicationDevice(communicationDevices);
		}
    }
    
    private void populateOrganizations(Contact contact, Person person)
    {
    	if (person.getOrganizations() != null && person.getOrganizations().size() > 0)
		{
			List<Organization> organizations = new ArrayList<Organization>();
			for (Organization organization : person.getOrganizations())
			{
				if (contact instanceof InitiatorContact)
	    		{
					Organization o = new InitiatorOrganization(organization);
					organizations.add(o);
	    		} 
	    		else if (contact instanceof PeopleContact)
	    		{
	    			Organization o = new PeopleOrganization(organization);
	    			organizations.add(o);
	    		}
			}
			
			contact.setOrganization(organizations);
		}
    }
    
    private void populateLocations(Contact contact, Person person)
    {
    	if (person.getAddresses() != null && person.getAddresses().size() > 0)
		{
			List<PostalAddress> locations = new ArrayList<PostalAddress>();
			for (PostalAddress postalAddress : person.getAddresses())
			{
				if (contact instanceof InitiatorContact)
	    		{
					PostalAddress a = new InitiatorPostalAddress(postalAddress);
					locations.add(a);
	    		} 
	    		else if (contact instanceof PeopleContact)
	    		{
	    			PostalAddress a = new PeoplePostalAddress(postalAddress);
	    			locations.add(a);
	    		}
			}
			
			contact.setLocation(locations);
		}
    }
    
    private void populateAlias(Contact contact, Person person)
    {
    	if (person.getPersonAliases() != null && person.getPersonAliases().size() > 0)
		{
			PersonAlias personAlias = person.getPersonAliases().get(0);
			if (contact instanceof InitiatorContact)
    		{
				contact.setAlias(new InitiatorPersonAlias(personAlias));
    		} 
    		else if (contact instanceof PeopleContact)
    		{
    			contact.setAlias(new PeoplePersonAlias(personAlias));
    		}
		}
    }

    private void populatePerson(Contact contact, PersonAssociation pa, Person p)
    {
        pa.setPersonDescription(contact.getMainInformation().getDescription());
        pa.setPersonType(contact.getMainInformation().getType());
        pa.setNotes(contact.getNotes());
        
        if (null == p.getId())
        {
	        p.setTitle(contact.getMainInformation().getTitle());
	        p.setGivenName(contact.getMainInformation().getFirstName());
	        p.setFamilyName(contact.getMainInformation().getLastName());
	
	        if ( contact.getAlias() != null )
	        {
	            p.getPersonAliases().add(contact.getAlias().returnBase());
	        }
	
	        if ( contact.getLocation() != null && ! contact.getLocation().isEmpty() )
	        {
	        	List<PostalAddress> addresses = new ArrayList<PostalAddress>();
	        	for (PostalAddress postalAddress : contact.getLocation())
        		{
        			PostalAddress base = postalAddress.returnBase();
        			addresses.add(base);
        		}
	            p.getAddresses().addAll(addresses);
	        }
	
	        if ( contact.getOrganization() != null && ! contact.getOrganization().isEmpty() )
	        {
	        	List<Organization> organizations = new ArrayList<Organization>();
	        	for (Organization organization : contact.getOrganization())
        		{
        			Organization base = organization.returnBase();
        			organizations.add(base);
        		}
	            p.getOrganizations().addAll(organizations);
	        }
	
	        if ( contact.getCommunicationDevice() != null && ! contact.getCommunicationDevice().isEmpty() )
	        {
	        	List<ContactMethod> contactMethods = new ArrayList<ContactMethod>();
	        	for (ContactMethod contactMethod : contact.getCommunicationDevice())
        		{
	        		ContactMethod base = contactMethod.returnBase();
	        		contactMethods.add(base);
        		}
	            p.getContactMethods().addAll(contactMethods);
	        }
        }


        if ( "true".equalsIgnoreCase(contact.getMainInformation().getAnonymous()) )
        {
            pa.getTags().add(ANONYMOUS);
        }
    }
    
    private List<AcmParticipant> convertToAcmParticipants(List<ParticipantItem> items){
    	List<AcmParticipant> participants = new ArrayList<AcmParticipant>();
    	
    	if (items != null && items.size() > 0){
    		for (ParticipantItem item : items){
    			AcmParticipant participant = new AcmParticipant();
    			
    			participant.setObjectType(FrevvoFormName.COMPLAINT.toUpperCase());
    			participant.setParticipantLdapId(item.getValue());
				participant.setParticipantType(item.getType());
				
				participants.add(participant);
    		}
    	}
    	
    	return participants;
    }
    

	/**
	 * @return the personDao
	 */
	public PersonDao getPersonDao() {
		return personDao;
	}

	/**
	 * @param personDao the personDao to set
	 */
	public void setPersonDao(PersonDao personDao) {
		this.personDao = personDao;
	}
}