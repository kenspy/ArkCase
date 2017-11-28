package com.armedia.acm.services.participants.service;

import com.armedia.acm.core.exceptions.AcmAccessControlException;
import com.armedia.acm.services.participants.dao.AcmParticipantDao;
import com.armedia.acm.services.participants.model.AcmAssignedObject;
import com.armedia.acm.services.participants.model.AcmParticipant;
import com.armedia.acm.services.participants.model.CheckParticipantListModel;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.FlushModeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by marjan.stefanoski on 01.04.2015.
 */
public class AcmParticipantService
{

    private AcmParticipantDao participantDao;
    private ParticipantsBusinessRule participantsBusinessRule;
    private AcmParticipantEventPublisher acmParticipantEventPublisher;

    private transient final Logger log = LoggerFactory.getLogger(getClass());

    public AcmParticipant saveParticipant(String userId, String participantType, Long objectId, String objectType)
            throws AcmAccessControlException
    {
        AcmParticipant returnedParticipant = getParticipantByLdapIdParticipantTypeObjectTypeObjectId(userId, participantType, objectType,
                objectId, FlushModeType.AUTO);

        if (returnedParticipant != null)
        {
            log.debug("Participant {} already exists and is added on object [{}]:[{}] as a {}", userId, objectType, objectId,
                    participantType);

            return returnedParticipant;
        }

        AcmParticipant participant = new AcmParticipant();
        participant.setParticipantLdapId(userId);
        participant.setParticipantType(participantType);
        participant.setObjectId(objectId);
        participant.setObjectType(objectType);

        CheckParticipantListModel model = new CheckParticipantListModel();
        List<String> errorListAfterRules = applyParticipantRules(participant, model);
        if (errorListAfterRules != null && !errorListAfterRules.isEmpty())
        {
            throw new AcmAccessControlException(errorListAfterRules,
                    "Conflict permissions combination has occurred for the chosen participants");
        }

        AcmParticipant savedParticipant = getParticipantDao().save(participant);

        getAcmParticipantEventPublisher().publishParticipantCreatedEvent(savedParticipant, true);

        log.debug("Added participant [{}] to object type [{}] with object id [{}]", userId, objectType, objectId);

        return savedParticipant;
    }

    private List<String> applyParticipantRules(AcmParticipant participant, CheckParticipantListModel model)
    {
        List<AcmParticipant> allParticipantsFromParentObject = participantDao.findParticipantsForObject(participant.getObjectType(),
                participant.getObjectId(), FlushModeType.AUTO);
        if (allParticipantsFromParentObject != null)
        {
            allParticipantsFromParentObject
                    .removeIf(parentObjectParticipant -> parentObjectParticipant.getId().equals(participant.getId()));
            allParticipantsFromParentObject.add(participant);

            model.setParticipantList(allParticipantsFromParentObject);
            model.setObjectType(participant.getObjectType());
            model = participantsBusinessRule.applyRules(model);

            List<String> listOfErrors = new ArrayList<>();
            if (!model.getErrorsList().isEmpty())
            {
                listOfErrors = model.getErrorsList();
            }
            return listOfErrors;
        }
        return null;
    }

    public AcmParticipant getParticipantByLdapIdParticipantTypeObjectTypeObjectId(String participantLdapId, String participantType,
            String objectType,
            Long objectId, FlushModeType flushModeType)
    {
        return getParticipantDao().getParticipantByLdapIdParticipantTypeObjectTypeObjectId(participantLdapId, participantType, objectType,
                objectId,
                flushModeType);
    }

    public List<AcmParticipant> getParticipantsByLdapIdObjectTypeObjectId(String participantLdapId, String objectType, Long objectId,
            FlushModeType flushModeType)
    {
        return getParticipantDao().getParticipantsByLdapIdObjectTypeObjectId(participantLdapId, objectType, objectId,
                flushModeType);
    }

    public AcmParticipant changeParticipantRole(AcmParticipant participant, String newRole) throws AcmAccessControlException
    {
        participant.setParticipantType(newRole);
        CheckParticipantListModel model = new CheckParticipantListModel();

        applyParticipantRules(participant, model);

        AcmParticipant updatedParticipant = getParticipantDao().save(participant);

        getAcmParticipantEventPublisher().publishParticipantUpdatedEvent(updatedParticipant, true);

        return updatedParticipant;
    }

    public List<AcmParticipant> listAllParticipantsPerObjectTypeAndId(String objectType, Long objectId, FlushModeType flushModeType)
    {
        return getParticipantDao().findParticipantsForObject(objectType, objectId, flushModeType);
    }

    public void removeParticipant(Long participantId)
    {
        AcmParticipant participant = getParticipantDao().find(participantId);
        if (participant == null)
        {
            return;
        }
        getParticipantDao().deleteParticipant(participantId);
        getAcmParticipantEventPublisher().publishParticipantDeletedEvent(participant, true);
    }

    public void removeParticipant(AcmParticipant participant)
    {
        if (participant == null)
        {
            return;
        }
        removeParticipant(participant.getId());
    }

    public void removeParticipant(String userId, String participantType, String objectType, Long objectId)
    {
        AcmParticipant participant = getParticipantByLdapIdParticipantTypeObjectTypeObjectId(userId, participantType, objectType, objectId,
                FlushModeType.AUTO);
        removeParticipant(participant);
    }

    public AcmParticipant findParticipant(Long id)
    {
        return getParticipantDao().find(id);
    }

    public List<AcmParticipant> getParticipantsFromParentObject(String objectType, Long objectId)
    {
        Reflections reflections = new Reflections("com.armedia");
        Set<Class<? extends AcmAssignedObject>> classes = reflections.getSubTypesOf(AcmAssignedObject.class);

        for (Class<? extends AcmAssignedObject> class1 : classes)
        {
            AcmAssignedObject assignedObject = null;
            try
            {
                assignedObject = class1.newInstance();
            }
            catch (Exception e)
            {
                // should never happen
                log.error("Cannot create new instance of class: " + class1.getName(), e);
                return new ArrayList<>();
            }
            if (assignedObject.getObjectType().equals(objectType))
            {
                return assignedObject.getParticipants();
            }
        }

        log.warn("No participants found for objectType: " + objectType + " objectId: " + objectId);

        return new ArrayList<>();
    }

    public AcmParticipantDao getParticipantDao()
    {
        return participantDao;
    }

    public void setParticipantDao(AcmParticipantDao participantDao)
    {
        this.participantDao = participantDao;
    }

    public ParticipantsBusinessRule getParticipantsBusinessRule()
    {
        return participantsBusinessRule;
    }

    public void setParticipantsBusinessRule(ParticipantsBusinessRule participantsBusinessRule)
    {
        this.participantsBusinessRule = participantsBusinessRule;
    }

    public AcmParticipantEventPublisher getAcmParticipantEventPublisher()
    {
        return acmParticipantEventPublisher;
    }

    public void setAcmParticipantEventPublisher(AcmParticipantEventPublisher acmParticipantEventPublisher)
    {
        this.acmParticipantEventPublisher = acmParticipantEventPublisher;
    }
}
