package com.armedia.acm.plugins.ecm.model;

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

import org.springframework.context.ApplicationEvent;

/**
 * Notify interested parties <strong>after</strong> a file
 * is successfully added to ArkCase.
 * <p/>
 * The original <code>EcmFileAddedEvent</code> is raised
 * <strong>during</strong> the upload transaction.
 * <p/>
 * It is not an <code>AcmEvent</code> since we don't want
 * another audit log entry... the exising <code>EcmFileAddedEvent</code>
 * is already audited.
 */
public class EcmFilePostUploadEvent extends ApplicationEvent
{
    private EcmFile file;

    private String username;

    public EcmFilePostUploadEvent(EcmFile uploaded, String username)
    {
        super(uploaded);

        file = uploaded;
        this.username = username;
    }

    public EcmFile getFile()
    {
        return file;
    }

    /**
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

}
