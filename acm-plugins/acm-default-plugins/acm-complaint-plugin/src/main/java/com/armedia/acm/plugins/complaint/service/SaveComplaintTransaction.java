package com.armedia.acm.plugins.complaint.service;

import com.armedia.acm.data.AuditPropertyEntityAdapter;
import com.armedia.acm.muletools.mulecontextmanager.MuleContextManager;
import com.armedia.acm.plugins.complaint.dao.ComplaintDao;
import com.armedia.acm.plugins.complaint.model.Complaint;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Implement transactional responsibilities for the SaveComplaintController.
 *
 * JPA does all database writes at commit time.  Therefore, if the transaction demarcation was in the controller,
 * exceptions would not be raised until after the controller method returns; i.e. the exception message goes write
 * to the browser.  Also, separating transaction management (in this class) and exception handling (in the
 * controller) is a good idea in general.
 */
public class SaveComplaintTransaction
{
    private MuleContextManager muleContextManager;

    @Transactional
    public Complaint saveComplaint(
            Complaint complaint,
            Authentication authentication)
            throws MuleException
    {
        complaint.setModified(new Date());
        complaint.setModifier(authentication.getName());

        Map<String, Object> messageProps = new HashMap<>();
        messageProps.put("acmUser", authentication);

        MuleMessage received = getMuleContextManager().send("vm://saveComplaint.in", complaint, messageProps);

        Complaint saved = received.getPayload(Complaint.class);
        MuleException e = received.getInboundProperty("saveException");

        if ( e != null )
        {
            throw e;
        }

        return saved;

    }


    public MuleContextManager getMuleContextManager()
    {
        return muleContextManager;
    }

    public void setMuleContextManager(MuleContextManager muleContextManager)
    {
        this.muleContextManager = muleContextManager;
    }

}
