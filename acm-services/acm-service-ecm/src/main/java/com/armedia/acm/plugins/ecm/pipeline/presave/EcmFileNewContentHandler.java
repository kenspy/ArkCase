package com.armedia.acm.plugins.ecm.pipeline.presave;

/*-
 * #%L
 * ACM Service: Enterprise Content Management
 * %%
 * Copyright (C) 2014 - 2018 ArkCase LLC
 * %%
 * This file is part of the ArkCase software. 
 * 
 * If the software was purchased under a paid ArkCase license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * ArkCase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * ArkCase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArkCase. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.armedia.acm.plugins.ecm.model.EcmFile;
import com.armedia.acm.plugins.ecm.pipeline.EcmFileTransactionPipelineContext;
import com.armedia.acm.plugins.ecm.utils.EcmFileMuleUtils;
import com.armedia.acm.services.pipeline.exception.PipelineProcessException;
import com.armedia.acm.services.pipeline.handler.PipelineHandler;

import org.apache.chemistry.opencmis.client.api.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by joseph.mcgrady on 9/28/2015.
 */
public class EcmFileNewContentHandler implements PipelineHandler<EcmFile, EcmFileTransactionPipelineContext>
{
    private transient final Logger log = LoggerFactory.getLogger(getClass());

    private EcmFileMuleUtils ecmFileMuleUtils;

    @Override
    public void execute(EcmFile entity, EcmFileTransactionPipelineContext pipelineContext) throws PipelineProcessException
    {
        if (entity == null)
        {
            throw new PipelineProcessException("ecmFile is null");
        }

        pipelineContext.setFileAlreadyInEcmSystem(pipelineContext.getCmisDocument() != null);

        if (!pipelineContext.getIsAppend() && !pipelineContext.isFileAlreadyInEcmSystem())
        {

            try (InputStream fileInputStream = new FileInputStream(pipelineContext.getFileContents()))
            {
                // Adds the file to the ECM content repository as a new document... using the context filename
                // as the filename for the repository.
                String arkcaseFilename = entity.getFileName();
                entity.setFileName(pipelineContext.getOriginalFileName());
                Document newDocument = ecmFileMuleUtils.addFile(entity, pipelineContext.getCmisFolderId(),
                        fileInputStream);
                // now, restore the ArkCase file name
                entity.setFileName(arkcaseFilename);
                pipelineContext.setCmisDocument(newDocument);
            }
            catch (Exception e)
            {
                log.error("mule pre save handler failed: {}", e.getMessage(), e);
                throw new PipelineProcessException(e);
            }

        }

    }

    @Override
    public void rollback(EcmFile entity, EcmFileTransactionPipelineContext pipelineContext) throws PipelineProcessException
    {
        log.debug("mule pre save handler rollback called");

        // JPA cannot rollback content in the Alfresco repository so it must be manually deleted
        if (!pipelineContext.getIsAppend() && !pipelineContext.isFileAlreadyInEcmSystem())
        {
            try
            {
                // We need the cmis id of the file in order to delete it
                Document cmisDocument = pipelineContext.getCmisDocument();
                if (cmisDocument == null)
                {
                    throw new Exception("cmisDocument is null");
                }

                // Removes the document from the Alfresco content repository
                ecmFileMuleUtils.deleteFile(entity, cmisDocument.getId());

            }
            catch (Exception e)
            { // since the rollback failed an orphan document will exist in Alfresco
                log.error("rollback of file upload failed: {}", e.getMessage(), e);
                throw new PipelineProcessException(e);
            }
            log.debug("mule pre save handler rollback ended");
        }
    }

    public EcmFileMuleUtils getEcmFileMuleUtils()
    {
        return ecmFileMuleUtils;
    }

    public void setEcmFileMuleUtils(EcmFileMuleUtils ecmFileMuleUtils)
    {
        this.ecmFileMuleUtils = ecmFileMuleUtils;
    }
}
