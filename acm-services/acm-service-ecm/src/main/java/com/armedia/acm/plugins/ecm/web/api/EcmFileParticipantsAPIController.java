package com.armedia.acm.plugins.ecm.web.api;

import com.armedia.acm.core.exceptions.AcmAccessControlException;
import com.armedia.acm.core.exceptions.AcmCreateObjectFailedException;
import com.armedia.acm.core.exceptions.AcmParticipantsException;
import com.armedia.acm.core.exceptions.AcmUserActionFailedException;
import com.armedia.acm.plugins.ecm.model.EcmFileConstants;
import com.armedia.acm.plugins.ecm.service.AcmFolderService;
import com.armedia.acm.plugins.ecm.service.impl.EcmFileParticipantService;
import com.armedia.acm.services.participants.model.AcmParticipant;
import com.armedia.acm.services.participants.service.AcmParticipantService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.FlushModeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by bojan.milenkoski on 06.10.2017
 */
@Controller
@RequestMapping({ "/api/v1/service/ecm/participants", "/api/latest/service/ecm/participants" })
public class EcmFileParticipantsAPIController
{
    private AcmFolderService folderService;
    private AcmParticipantService participantService;
    private EcmFileParticipantService fileParticipantService;

    private transient final Logger log = LoggerFactory.getLogger(getClass());

    @PreAuthorize("hasPermission(#objectId, #objectType, 'write|group-write')")
    @RequestMapping(value = "/{objectType}/{objectId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<AcmParticipant> saveParticipants(@PathVariable(value = "objectType") String objectType,
            @PathVariable(value = "objectId") Long objectId, @RequestBody List<AcmParticipant> participants, Authentication authentication)
            throws AcmCreateObjectFailedException, AcmUserActionFailedException, AcmParticipantsException, AcmAccessControlException
    {
        log.info("Participants will be set on object [{}]:[{}]", objectType, objectId);

        if (!objectType.equals(EcmFileConstants.OBJECT_FOLDER_TYPE) && !objectType.equals(EcmFileConstants.OBJECT_FILE_TYPE))
        {
            throw new AcmAccessControlException(Arrays.asList(""),
                    "The called method cannot be executed on objectType {" + objectType + "}!");
        }
        getFileParticipantService().validateFileParticipants(participants);

        List<AcmParticipant> participantsToReturn = new ArrayList<>();

        List<AcmParticipant> existingParticipants = getParticipantService().listAllParticipantsPerObjectTypeAndId(objectType, objectId,
                FlushModeType.COMMIT);

        // change existing participants role
        for (AcmParticipant participant : participants)
        {
            Optional<AcmParticipant> returnedParticipant = existingParticipants.stream()
                    .filter(existingParticipant -> existingParticipant.getParticipantLdapId().equals(participant.getParticipantLdapId()))
                    .findFirst();

            if (!returnedParticipant.isPresent())
            {
                continue;
            }

            if (!returnedParticipant.get().getParticipantType().equals(participant.getParticipantType()))
            {
                AcmParticipant changedParticipant = getParticipantService().changeParticipantRole(participant,
                        participant.getParticipantType());

                if (objectType.equals(EcmFileConstants.OBJECT_FOLDER_TYPE) && (participant.isReplaceChildrenParticipant()))
                {
                    getFileParticipantService().setParticipantToFolderChildren(getFolderService().findById(objectId), participant);
                }

                participantsToReturn.add(changedParticipant);
            }
            else
            {
                participantsToReturn.add(participant);
            }
        }

        // remove deleted participants
        for (AcmParticipant existingParticipant : existingParticipants)
        {
            if (participants.stream()
                    .filter(participant -> participant.getParticipantLdapId().equals(existingParticipant.getParticipantLdapId()))
                    .count() == 0)
            {
                getParticipantService().removeParticipant(existingParticipant.getParticipantLdapId(),
                        existingParticipant.getParticipantType(), existingParticipant.getObjectType(), existingParticipant.getObjectId());

                if (objectType.equals(EcmFileConstants.OBJECT_FOLDER_TYPE))
                {
                    getFileParticipantService().removeParticipantFromFolderAndChildren(getFolderService().findById(objectId),
                            existingParticipant.getParticipantLdapId(), existingParticipant.getObjectType());
                }
            }
        }

        // add new participants
        for (AcmParticipant participant : participants)
        {
            if (existingParticipants.stream()
                    .filter(existingParticipant -> existingParticipant.getParticipantLdapId().equals(participant.getParticipantLdapId()))
                    .count() == 0)
            {
                AcmParticipant addedParticipant = getParticipantService().saveParticipant(participant.getParticipantLdapId(),
                        participant.getParticipantType(), objectId, objectType);

                if (objectType.equals(EcmFileConstants.OBJECT_FOLDER_TYPE) && (participant.isReplaceChildrenParticipant()))
                {
                    getFileParticipantService().setParticipantToFolderChildren(getFolderService().findById(objectId), participant);
                }

                participantsToReturn.add(addedParticipant);
            }
        }

        return participantsToReturn;
    }

    public AcmFolderService getFolderService()
    {
        return folderService;
    }

    public void setFolderService(AcmFolderService folderService)
    {
        this.folderService = folderService;
    }

    public AcmParticipantService getParticipantService()
    {
        return participantService;
    }

    public void setParticipantService(AcmParticipantService acmParticipantService)
    {
        this.participantService = acmParticipantService;
    }

    public EcmFileParticipantService getFileParticipantService()
    {
        return fileParticipantService;
    }

    public void setFileParticipantService(EcmFileParticipantService fileParticipantService)
    {
        this.fileParticipantService = fileParticipantService;
    }
}
