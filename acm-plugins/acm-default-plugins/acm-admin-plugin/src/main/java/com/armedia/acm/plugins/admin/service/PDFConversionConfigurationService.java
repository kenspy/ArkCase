package com.armedia.acm.plugins.admin.service;

/*-
 * #%L
 * ACM Default Plugin: admin
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

import com.armedia.acm.files.ConfigurationFileChangedEvent;
import com.armedia.acm.files.propertymanager.PropertyFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PDFConversionConfigurationService implements ApplicationListener<ConfigurationFileChangedEvent>
{
    private Logger log = LoggerFactory.getLogger(getClass());

    private String propertiesFile;
    private PropertyFileManager propertyFileManager;

    private Map<String, String> properties = new HashMap<>();

    public void initBean()
    {
        try
        {
            properties = getPropertyFileManager().readFromFileAsMap(new File(getPropertiesFile()));
        }
        catch (IOException e)
        {
            log.error("Could not read properties file [{}]", propertiesFile);
        }
    }

    @Override
    public void onApplicationEvent(ConfigurationFileChangedEvent configurationFileChangedEvent)
    {
        if(configurationFileChangedEvent.getConfigFile().getName().equals(propertiesFile.substring(propertiesFile.lastIndexOf("/") + 1)))
        {
            initBean();
        }
    }

    public void  saveProperties(Map<String, String> properties)
    {
        getPropertyFileManager().storeMultiple(properties, getPropertiesFile(), true);
    }

    public Map<String, String> loadProperties()
    {
        return properties;
    }

    public Boolean isResponseFolderConversionEnabled()
    {
        Map<String, String> pdfProperties = loadProperties();

        return !pdfProperties.isEmpty() && pdfProperties.containsKey("responseFolderConversion") && "true".equals(pdfProperties.get("responseFolderConversion"));
    }

    public String getPropertiesFile()
    {
        return propertiesFile;
    }

    public void setPropertiesFile(String propertiesFile)
    {
        this.propertiesFile = propertiesFile;
    }

    public PropertyFileManager getPropertyFileManager()
    {
        return propertyFileManager;
    }

    public void setPropertyFileManager(PropertyFileManager propertyFileManager)
    {
        this.propertyFileManager = propertyFileManager;
    }
}