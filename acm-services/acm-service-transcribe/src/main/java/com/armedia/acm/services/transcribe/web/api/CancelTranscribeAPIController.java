package com.armedia.acm.services.transcribe.web.api;

import com.armedia.acm.services.transcribe.exception.SaveTranscribeException;
import com.armedia.acm.services.transcribe.model.Transcribe;
import com.armedia.acm.services.transcribe.service.ArkCaseTranscribeService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Riste Tutureski <riste.tutureski@armedia.com> on 03/22/2018
 */
@Controller
@RequestMapping({ "/api/v1/service/transcribe", "/api/latest/service/transcribe" })
public class CancelTranscribeAPIController
{
    private ArkCaseTranscribeService arkCaseTranscribeService;

    @RequestMapping(value = "/{id}/cancel", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Transcribe cancelTranscribe(@PathVariable(value = "id") Long id) throws SaveTranscribeException
    {
        return getArkCaseTranscribeService().cancel(id);
    }

    public ArkCaseTranscribeService getArkCaseTranscribeService()
    {
        return arkCaseTranscribeService;
    }

    public void setArkCaseTranscribeService(ArkCaseTranscribeService arkCaseTranscribeService)
    {
        this.arkCaseTranscribeService = arkCaseTranscribeService;
    }
}
