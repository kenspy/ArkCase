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

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Google Analytics configuration service.
 * Created by Petar Ilin <petar.ilin@armedia.com> on 31.03.2017.
 */
public class GoogleAnalyticsConfigService
{

    /**
     * Logger instance.
     */
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Configuration file.
     */
    private File configFile;

    /**
     * config.js Freemarker template
     */
    private Template template;

    /**
     * Default constructor, initialize Freemarker configuration and load the template.
     */
    public GoogleAnalyticsConfigService()
    {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);
        configuration.setClassForTemplateLoading(getClass(), "/templates");
        try
        {
            template = configuration.getTemplate("config.js.ftl");
        }
        catch (IOException e)
        {
            logger.error("Cannot read template [classpath:/templates/config.js.ftl]", e);
        }
    }

    /**
     * Retrieve the Google Analytics configuration as a JavaScript file.
     *
     * @return javascript global variables
     */
    public String getGoogleAnalyticsSettingsJs()
    {
        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream(configFile);
                Writer stringWriter = new StringWriter())
        {
            properties.load(fis);
            template.process(properties, stringWriter);
            return stringWriter.toString();
        }
        catch (IOException e)
        {
            logger.error("Cannot read configuration file [{}]", configFile.getAbsolutePath(), e);
        }
        catch (TemplateException e)
        {
            logger.error("Cannot process [config.js.ftl] template", e);
        }
        catch (NullPointerException e)
        {
            logger.error("Template [config.js.ftl] not loaded", e);
        }

        return "";
    }

    /**
     * Retrieve Google Analytics configuration as JSON object (used in Admin UI).
     *
     * @return configuration represented as JSON
     */
    public String getGoogleAnalyticsSettings()
    {
        // TODO: this method probably needs improvement/adaptation once Admin UI is developed
        logger.debug("Retrieving Google Analytics configuration");
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile))
        {
            properties.load(fis);

        }
        catch (IOException e)
        {
            logger.error("Cannot read configuration file [{}]", configFile.getAbsolutePath(), e);
        }
        JSONObject jsonObject = new JSONObject();
        for (Object key : properties.keySet())
        {
            jsonObject.put((String) key, properties.get(key));
        }
        return jsonObject.toString();
    }

    /**
     * Store Google Analytics configuration as key-value properties (used in Admin UI).
     *
     * @param configuration
     *            JSON representation of GA settings
     * @return properties
     */
    public String setGoogleAnalyticsSettings(String configuration)
    {
        // TODO: this method probably needs improvement/adaptation once Admin UI is developed
        logger.debug("Storing Google Analytics properties");
        JSONObject jsonConfiguration = new JSONObject(configuration);
        Properties properties = new Properties();
        for (Object key : jsonConfiguration.keySet())
        {
            // FIXME: validate keys!
            properties.put(key, jsonConfiguration.get((String) key));
        }
        try (FileOutputStream fos = new FileOutputStream(configFile))
        {
            properties.store(fos, "Google Analytics configuration");
            logger.debug("Google Analytics configuration stored");
        }
        catch (IOException e)
        {
            logger.error("Cannot write configuration file [{}]", configFile.getAbsolutePath(), e);
        }
        return properties.toString();
    }

    public File getConfigFile()
    {
        return configFile;
    }

    public void setConfigFile(File configFile)
    {
        this.configFile = configFile;
    }
}
