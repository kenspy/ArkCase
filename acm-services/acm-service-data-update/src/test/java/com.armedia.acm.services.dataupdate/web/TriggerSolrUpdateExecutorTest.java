package com.armedia.acm.services.dataupdate.web;

import static org.junit.Assert.assertEquals;

import com.armedia.acm.files.propertymanager.PropertyFileManager;
import com.armedia.acm.services.dataupdate.service.TriggerSolrUpdateExecutor;
import com.armedia.acm.services.users.model.AcmUser;
import com.armedia.acm.services.users.model.group.AcmGroup;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TriggerSolrUpdateExecutorTest extends EasyMockSupport
{
    // private String acmUserKey = "solr.last.run.date.com.armedia.acm.services.users.model.AcmUser";
    private String acmUserValue = "";
    // private String acmGroupKey = "solr.last.run.date.com.armedia.acm.services.users.model.group.AcmGroup";
    private String acmGroupValue = "";
    private static final String SOLR_LAST_RUN_DATE_PROPERTY_KEY = "solr.last.run.date";
    private TriggerSolrUpdateExecutor triggerSolrUpdateExecutor;
    private PropertyFileManager propertyFileManager;
    private final String filePath = getClass().getClassLoader().getResource("properties/solrBatchUpdate.properties").getPath();
    private List<String> solrList;
    private Map<String, String> solrMap;

    @Before
    public void setUp() throws Exception
    {
        triggerSolrUpdateExecutor = new TriggerSolrUpdateExecutor();
        propertyFileManager = new PropertyFileManager();
        triggerSolrUpdateExecutor.setLastBatchUpdatePropertyFileLocation(filePath);
        triggerSolrUpdateExecutor.setPropertyFileManager(propertyFileManager);

        solrList = Arrays.asList(SOLR_LAST_RUN_DATE_PROPERTY_KEY + "." + AcmUser.class.getName(),
                SOLR_LAST_RUN_DATE_PROPERTY_KEY + "." + AcmGroup.class.getName());

        solrMap = new HashMap<>();
        solrMap.put(SOLR_LAST_RUN_DATE_PROPERTY_KEY + "." + AcmUser.class.getName(), "testValueUser");
        solrMap.put(SOLR_LAST_RUN_DATE_PROPERTY_KEY + "." + AcmGroup.class.getName(), "testValueGroup");

        fillFile(solrMap);
    }

    public void fillFile(Map<String, String> solrMap) throws IOException
    {
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : solrMap.entrySet())
        {
            properties.put(entry.getKey(), entry.getValue());
        }
        properties.store(new FileOutputStream(filePath), null);
    }

    public String takeValue(String key) throws IOException
    {
        String retval = "";
        Properties properties = new Properties();
        try (InputStream fis = new FileInputStream(filePath))
        {
            properties.load(fis);
            retval = properties.getProperty(key);
        }
        catch (Exception e)
        {
            e.getMessage();
        }
        return retval;
    }

    @Test
    public void validatePath() throws Exception
    {
        assertEquals(triggerSolrUpdateExecutor.getLastBatchUpdatePropertyFileLocation(), filePath);
    }

    @Test
    public void validateRemovedLines() throws Exception
    {
        triggerSolrUpdateExecutor.execute();

        for (String key : solrList)
        {
            String ret = takeValue(key);
            assertEquals(ret, null);
        }
    }

}
