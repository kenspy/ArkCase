package com.armedia.acm.webdav;

import com.armedia.acm.muletools.mulecontextmanager.MuleContextManager;
import com.armedia.acm.plugins.ecm.dao.AcmFolderDao;
import com.armedia.acm.plugins.ecm.dao.EcmFileDao;
import com.armedia.acm.plugins.ecm.model.AcmFolder;
import com.armedia.acm.plugins.ecm.model.EcmFile;
import com.armedia.acm.plugins.ecm.service.EcmFileTransaction;
import com.armedia.acm.plugins.ecm.utils.FolderAndFilesUtils;
import io.milton.http.LockManager;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;

/**
 * @author Lazo Lazarev a.k.a. Lazarius Borg @ zerogravity
 */
public class AcmFileSystemResourceFactory implements ResourceFactory
{

    private EcmFileDao fileDao;

    private AcmFolderDao folderDao;

    private EcmFileTransaction ecmFileTransaction;

    private FolderAndFilesUtils folderAndFilesUtils;

    private MuleContextManager muleContextManager;

    private LockManager lockManager;

    private AcmWebDAVSecurityManager securityManager;

    private Long maxAgeSeconds;

    private String filterMapping;

    @Override
    public Resource getResource(String host, String path) throws NotAuthorizedException, BadRequestException
    {
        //.docx and .doc should be removed
        if (path.endsWith(".docx"))
            path = path.substring(0, path.length() - 5);
        else if (path.endsWith(".doc"))
            path = path.substring(0, path.length() - 4);

        String strippedPath = path.substring(path.indexOf(filterMapping) + filterMapping.length());
        if (strippedPath.endsWith("/"))
        {
            strippedPath = strippedPath.substring(0, strippedPath.length() - 1);
        }

        ResourceHandler hanlder = getResourceHandler(strippedPath);
        return hanlder.getResource(host, strippedPath);

    }

    private ResourceHandler getResourceHandler(String path) throws BadRequestException
    {

        if (path.contains("/"))
        {
            return new AcmFileResourceHandler();
        } else
        {
            return new AcmFolderResourceHandler();
        }

    }

    public String getCmisFileId(EcmFile ecmFile)
    {
        return folderAndFilesUtils.getActiveVersionCmisId(ecmFile);
    }

    public EcmFileDao getFileDao()
    {
        return fileDao;
    }

    public void setFileDao(EcmFileDao fileDao)
    {
        this.fileDao = fileDao;
    }

    public AcmFolderDao getFolderDao()
    {
        return folderDao;
    }

    public void setFolderDao(AcmFolderDao folderDao)
    {
        this.folderDao = folderDao;
    }

    public void setFolderAndFilesUtils(FolderAndFilesUtils folderAndFilesUtils)
    {
        this.folderAndFilesUtils = folderAndFilesUtils;
    }

    public void setFilterMapping(String filterMapping)
    {
        if (filterMapping.endsWith("/"))
        {
            this.filterMapping = filterMapping;
        } else
        {
            this.filterMapping = filterMapping + "/";
        }
    }

    public AcmWebDAVSecurityManager getSecurityManager()
    {
        return securityManager;
    }

    public void setSecurityManager(AcmWebDAVSecurityManager securityManager)
    {
        this.securityManager = securityManager;
    }

    public Long getMaxAgeSeconds()
    {
        return maxAgeSeconds;
    }

    public void setMaxAgeSeconds(Long maxAgeSeconds)
    {
        this.maxAgeSeconds = maxAgeSeconds;
    }

    public LockManager getLockManager()
    {
        return lockManager;
    }

    public void setLockManager(LockManager lockManager)
    {
        this.lockManager = lockManager;
    }

    public EcmFileTransaction getEcmFileTransaction()
    {
        return ecmFileTransaction;
    }

    public void setEcmFileTransaction(EcmFileTransaction ecmFileTransaction)
    {
        this.ecmFileTransaction = ecmFileTransaction;
    }

    public MuleContextManager getMuleContextManager()
    {
        return muleContextManager;
    }

    public void setMuleContextManager(MuleContextManager muleContextManager)
    {
        this.muleContextManager = muleContextManager;
    }

    interface ResourceHandler
    {

        AcmFileSystemResource getResource(String host, String path) throws BadRequestException;

    }

    private class AcmFileResourceHandler implements ResourceHandler
    {

        @Override
        public AcmFileSystemResource getResource(String host, String path) throws BadRequestException
        {

            Long fileId = Long.valueOf(path.substring(path.indexOf('/') + 1));

            EcmFile ecmFile = fileDao.find(fileId);
            return new AcmFileResource(host, ecmFile, AcmFileSystemResourceFactory.this);

        }

    }

    private class AcmFolderResourceHandler implements ResourceHandler
    {

        @Override
        public AcmFileSystemResource getResource(String host, String path)
        {
            Long folderId = Long.valueOf(path);

            AcmFolder acmFolder = folderDao.find(folderId);
            return new AcmFolderResource(host, acmFolder, AcmFileSystemResourceFactory.this);
        }

    }
}