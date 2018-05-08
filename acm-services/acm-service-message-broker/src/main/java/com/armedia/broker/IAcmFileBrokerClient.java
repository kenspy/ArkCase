package com.armedia.broker;

/*-
 * #%L
 * ACM Service: JMS Message Broker
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


import javax.jms.JMSException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * File broker client interface
 * 
 * @author dame.gjorgjievski
 *
 */
public interface IAcmFileBrokerClient
{
    public void sendFile(File file, Map<String, Object> properties) throws JMSException, IOException;

    public void sendFile(File file, String queueName, Map<String, Object> properties) throws JMSException, IOException;

    public File receiveFile() throws JMSException, IOException;

    public File receiveFile(String queueName) throws JMSException, IOException;

}
