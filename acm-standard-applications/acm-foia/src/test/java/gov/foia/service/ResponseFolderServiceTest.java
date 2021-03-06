package gov.foia.service;

/*-
 * #%L
 * ACM Standard Application: Freedom of Information Act
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

import static com.armedia.acm.plugins.ecm.model.EcmFileConstants.OBJECT_FOLDER_TYPE;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

import com.armedia.acm.plugins.casefile.model.CaseFile;
import com.armedia.acm.plugins.ecm.exception.AcmFolderException;
import com.armedia.acm.plugins.ecm.model.AcmContainer;
import com.armedia.acm.plugins.ecm.model.AcmFolder;
import com.armedia.acm.plugins.ecm.service.AcmFolderService;

import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Arrays;

/**
 * @author sasko.tanaskoski
 *
 */

@RunWith(EasyMockRunner.class)
public class ResponseFolderServiceTest extends EasyMockSupport
{

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private CaseFile mockedCaseFile;
    @Mock
    private AcmFolderService mockedFolderService;
    @Mock
    private AcmContainer mockedContainer;
    @Mock
    private AcmFolder mockedRootFolder;
    @Mock
    private AcmFolder mockedRequestFolder;
    @Mock
    private AcmFolder mockedWorkingFolder;
    @Mock
    private AcmFolder mockedResponseFolder;
    private ResponseFolderService responseFolderService;

    @Before
    public void setUp() throws Exception
    {
        responseFolderService = new ResponseFolderService();
        responseFolderService.setFolderService(mockedFolderService);
        responseFolderService.setResponseFolderName("Response");
    }

    /**
     * Test method for
     * {@link ResponseFolderService#getResponseFolder(CaseFile)}.
     *
     * @throws Exception
     */
    @Test
    public void testGetResponseFolder() throws Exception
    {

        AcmFolder rootFolder = new AcmFolder();
        rootFolder.setId(102l);

        AcmFolder childFolder1 = new AcmFolder();
        childFolder1.setName("Request");
        childFolder1.setParentFolder(rootFolder);

        AcmFolder childFolder2 = new AcmFolder();
        childFolder2.setName("Working");
        childFolder2.setParentFolder(rootFolder);

        AcmFolder childFolder3 = new AcmFolder();
        childFolder3.setName("Response");
        childFolder3.setParentFolder(rootFolder);

        AcmContainer container = new AcmContainer();
        container.setFolder(rootFolder);

        CaseFile caseFile = new CaseFile();
        caseFile.setContainer(container);

        expect(mockedCaseFile.getContainer()).andReturn(mockedContainer);
        expect(mockedContainer.getFolder()).andReturn(mockedRootFolder);
        expect(mockedRootFolder.getId()).andReturn(102l);

        expect(mockedFolderService.getFolderChildren(102l))
                .andReturn(Arrays.asList(mockedRequestFolder, mockedWorkingFolder, mockedResponseFolder));

        expect(mockedRequestFolder.getObjectType()).andReturn(OBJECT_FOLDER_TYPE).times(2);
        expect(mockedRequestFolder.getName()).andReturn("Request");
        expect(mockedWorkingFolder.getObjectType()).andReturn(OBJECT_FOLDER_TYPE).times(2);
        expect(mockedWorkingFolder.getName()).andReturn("Working");
        expect(mockedResponseFolder.getObjectType()).andReturn(OBJECT_FOLDER_TYPE).times(2);
        expect(mockedResponseFolder.getName()).andReturn("Response");

        replayAll();

        AcmFolder responseFolder = responseFolderService.getResponseFolder(caseFile);

        assertNotNull(responseFolder);

    }

    /**
     * Test method for
     * {@link ResponseFolderService#getResponseFolder(CaseFile)}.
     *
     * @throws Exception
     */
    @Test
    public void testMissingResponseFolder() throws Exception
    {
        AcmFolder rootFolder = new AcmFolder();
        rootFolder.setId(102l);

        AcmFolder childFolder1 = new AcmFolder();
        childFolder1.setName("Request");
        childFolder1.setParentFolder(rootFolder);

        AcmFolder childFolder2 = new AcmFolder();
        childFolder2.setName("Working");
        childFolder2.setParentFolder(rootFolder);

        AcmFolder childFolder3 = new AcmFolder();
        childFolder3.setName("NoResponse");
        childFolder3.setParentFolder(rootFolder);

        AcmContainer container = new AcmContainer();
        container.setFolder(rootFolder);

        CaseFile caseFile = new CaseFile();
        caseFile.setContainer(container);

        expect(mockedCaseFile.getContainer()).andReturn(mockedContainer);
        expect(mockedContainer.getFolder()).andReturn(mockedRootFolder);
        expect(mockedRootFolder.getId()).andReturn(102l);

        expect(mockedFolderService.getFolderChildren(102l))
                .andReturn(Arrays.asList(mockedRequestFolder, mockedWorkingFolder, mockedResponseFolder));

        expect(mockedRequestFolder.getObjectType()).andReturn(OBJECT_FOLDER_TYPE).times(2);
        expect(mockedRequestFolder.getName()).andReturn("Request").anyTimes();
        expect(mockedWorkingFolder.getObjectType()).andReturn(OBJECT_FOLDER_TYPE).times(2);
        expect(mockedWorkingFolder.getName()).andReturn("Working");
        expect(mockedResponseFolder.getObjectType()).andReturn(OBJECT_FOLDER_TYPE).times(2);
        expect(mockedResponseFolder.getName()).andReturn("NoResponse");

        replayAll();

        expectedException.expect(AcmFolderException.class);
        expectedException.expectMessage(is(String.format("No response folder in folder with id %d was found!", 102l)));

        responseFolderService.getResponseFolder(caseFile);

    }

}
