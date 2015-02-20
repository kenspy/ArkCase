/**
 * 
 */
package com.armedia.acm.services.notification.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.api.client.MuleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.armedia.acm.files.propertymanager.PropertyFileManager;
import com.armedia.acm.services.notification.dao.NotificationDao;
import com.armedia.acm.services.notification.model.ApplicationNotificationEvent;
import com.armedia.acm.services.notification.model.Notification;
import com.armedia.acm.services.notification.model.NotificationConstants;
import com.armedia.acm.services.notification.model.NotificationRule;
import com.armedia.acm.spring.SpringContextHolder;

/**
 * @author riste.tutureski
 *
 */
public class NotificationServiceImpl implements NotificationService {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

    private boolean batchRun;
    private int batchSize;
    private PropertyFileManager propertyFileManager;
    private String notificationPropertyFileLocation;
    private NotificationDao notificationDao;
    private MuleClient muleClient;
    private NotificationEventPublisher notificationEventPublisher;
    private SpringContextHolder springContextHolder;
	
    /**
     * This method is called by scheduled task
     */
	@Override
	public void run() 
	{
		if (!isBatchRun())
		{
			return;
		}
		
		String lastRunDate = getPropertyFileManager().load(getNotificationPropertyFileLocation(), NotificationConstants.SOLR_LAST_RUN_DATE_PROPERTY_KEY, NotificationConstants.DEFAULT_LAST_RUN_DATE);
		
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat(NotificationConstants.DATE_FORMAT);
			
			Date lastRun = getLastRunDate(lastRunDate, dateFormat);
			setLastRunDate(dateFormat);
			
			Map<String, NotificationRule> rules = getSpringContextHolder().getAllBeansOfType(NotificationRule.class);
			
			if (rules != null)
			{
				for (NotificationRule rule : rules.values())
				{
					runRule(lastRun, rule);
				}
			}
						
		}
		catch(Exception e)
		{
			LOG.error("Cannot send notifications to the users: " + e.getMessage(), e);
		}
	}
	
	/**
	 * This method is called for executing the rule query and sending notifications
	 */
	@Override
	public void runRule(Date lastRun, NotificationRule rule)
	{
		int firstResult = 0;
		int maxResult = getBatchSize();
		
		List<Notification> notifications;
		
		do
		{
			notifications = getNotificationDao().executeQuery(lastRun, firstResult, maxResult, rule.getJpaQuery());
			
			if ( !notifications.isEmpty() )
            {
				firstResult += maxResult;
				
				for (Notification notification : notifications)
				{
					// Send notification
					notification = send(notification);	
					
					// Save notification to database
					Notification saved = getNotificationDao().save(notification);
					
					// Raise an event
					ApplicationNotificationEvent event = new ApplicationNotificationEvent(saved, NotificationConstants.OBJECT_TYPE.toLowerCase(), true, null);
					getNotificationEventPublisher().publishNotificationEvent(event);
				}
            }
		}
		while ( !notifications.isEmpty() );
	}
	
	@Override
	public Notification send(Notification notification) 
	{
		// Get all registered senders
		Map<String, NotificationSender> senders = getSpringContextHolder().getAllBeansOfType(NotificationSender.class);
		
		if (senders != null)
		{
			for (NotificationSender sender : senders.values())
			{
				// Send notification
				notification = sender.send(notification);
			}
		}
				
		return notification;
	}
	
	/**
	 * Get the last run date corrected for 1 minute before
	 * 
	 * @param lastRunDate
	 * @return
	 * @throws ParseException
	 */
	private Date getLastRunDate(String lastRunDate, SimpleDateFormat dateFormat) throws ParseException
    {
        Date date = dateFormat.parse(lastRunDate);

        Calendar calendar = Calendar.getInstance();
        
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 1);
        
        date = calendar.getTime();
        
        return date;
    }
	
	private void setLastRunDate(SimpleDateFormat dateFormat)
	{
		String lastRunDate = dateFormat.format(new Date());
		
		Map<String, String> properties = new HashMap<>();
		properties.put(NotificationConstants.SOLR_LAST_RUN_DATE_PROPERTY_KEY, lastRunDate);
		
		getPropertyFileManager().storeMultiple(properties, getNotificationPropertyFileLocation(), false);
	}

	public boolean isBatchRun() {
		return batchRun;
	}

	public void setBatchRun(boolean batchRun) {
		this.batchRun = batchRun;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public PropertyFileManager getPropertyFileManager() {
		return propertyFileManager;
	}

	public void setPropertyFileManager(PropertyFileManager propertyFileManager) {
		this.propertyFileManager = propertyFileManager;
	}

	public String getNotificationPropertyFileLocation() {
		return notificationPropertyFileLocation;
	}

	public void setNotificationPropertyFileLocation(String notificationPropertyFileLocation) {
		this.notificationPropertyFileLocation = notificationPropertyFileLocation;
	}

	public NotificationDao getNotificationDao() {
		return notificationDao;
	}

	public void setNotificationDao(NotificationDao notificationDao) {
		this.notificationDao = notificationDao;
	}

	public MuleClient getMuleClient() {
		return muleClient;
	}

	public void setMuleClient(MuleClient muleClient) {
		this.muleClient = muleClient;
	}

	public NotificationEventPublisher getNotificationEventPublisher() {
		return notificationEventPublisher;
	}

	public void setNotificationEventPublisher(NotificationEventPublisher notificationEventPublisher) {
		this.notificationEventPublisher = notificationEventPublisher;
	}

	public SpringContextHolder getSpringContextHolder() {
		return springContextHolder;
	}

	public void setSpringContextHolder(SpringContextHolder springContextHolder) {
		this.springContextHolder = springContextHolder;
	}
}