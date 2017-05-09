package com.armedia.acm.calendar.service.integration.exchange;

import com.armedia.acm.calendar.service.AcmCalendarEvent;
import com.armedia.acm.calendar.service.AcmCalendarEventInfo;
import com.armedia.acm.calendar.service.AcmCalendarInfo;
import com.armedia.acm.calendar.service.CalendarServiceException;
import com.armedia.acm.plugins.ecm.model.AcmContainerEntity;
import com.armedia.acm.services.users.model.AcmUser;

import org.springframework.security.core.Authentication;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.permission.folder.FolderPermissionLevel;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.search.SortDirection;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.folder.CalendarFolder;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.AppointmentSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.FolderPermission;
import microsoft.exchange.webservices.data.property.complex.FolderPermissionCollection;
import microsoft.exchange.webservices.data.property.definition.PropertyDefinition;
import microsoft.exchange.webservices.data.search.CalendarView;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;

/**
 * @author Lazo Lazarev a.k.a. Lazarius Borg @ zerogravity Apr 26, 2017
 *
 */
public class CalendarCaseFileHandler implements CalendarEntityHandler
{

    private final PropertySet standardProperties = new PropertySet(BasePropertySet.FirstClassProperties, ItemSchema.Subject,
            AppointmentSchema.Location, AppointmentSchema.Start, AppointmentSchema.StartTimeZone, AppointmentSchema.End,
            AppointmentSchema.EndTimeZone, AppointmentSchema.IsAllDayEvent, ItemSchema.DateTimeSent, ItemSchema.DateTimeCreated,
            ItemSchema.DateTimeReceived, ItemSchema.LastModifiedTime, ItemSchema.Body, ItemSchema.Size, AppointmentSchema.IsCancelled,
            AppointmentSchema.IsMeeting, AppointmentSchema.IsRecurring, ItemSchema.ParentFolderId, ItemSchema.ReminderMinutesBeforeStart,
            AppointmentSchema.Sensitivity, AppointmentSchema.Importance, AppointmentSchema.RequiredAttendees,
            AppointmentSchema.OptionalAttendees, AppointmentSchema.Resources, AppointmentSchema.Recurrence, AppointmentSchema.Organizer);

    private Map<String, PropertyDefinition> sortFields;

    @PersistenceContext
    private EntityManager em;

    /**
     *
     */
    public CalendarCaseFileHandler()
    {
        sortFields = new HashMap<>();
        sortFields.put("subject", ItemSchema.Subject);
        sortFields.put("dateTimeCreated", ItemSchema.DateTimeCreated);
        sortFields.put("dateTimeReceived", ItemSchema.DateTimeReceived);
        sortFields.put("dateTimeSent", ItemSchema.DateTimeSent);
        sortFields.put("hasAttachments", ItemSchema.HasAttachments);
        sortFields.put("displayTo", ItemSchema.DisplayTo);
        sortFields.put("size", ItemSchema.Size);
        sortFields.put("dateTimeStart", AppointmentSchema.Start);
    }

    private Object getCaseFile(String objectId, boolean restrictedOnly)
    {
        Query query;
        if (restrictedOnly)
        {
            query = em.createQuery("SELECT cf FROM CaseFile cf WHERE cf.id = :objectId AND cf.restricted = :restricted");
            query.setParameter("restricted", true);
        } else
        {
            query = em.createQuery("SELECT cf FROM CaseFile cf WHERE cf.id = :objectId");
        }
        query.setParameter("objectId", Long.valueOf(objectId));
        List<?> resultList = query.getResultList();
        if (!resultList.isEmpty())
        {
            return resultList.get(0);
        } else
        {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.armedia.acm.calendar.service.integration.exchange.EntityHandler#checkPermission(com.armedia.acm.services.
     * users.model.AcmUser, org.springframework.security.core.Authentication, java.lang.String)
     */
    @Override
    public boolean checkPermission(ExchangeService service, AcmUser user, Authentication auth, String objectId,
            PermissionType permissionType) throws CalendarServiceException
    {
        Object caseFile = getCaseFile(objectId, true);
        if (caseFile == null)
        {
            return true;
        }
        AcmContainerEntity entity = (AcmContainerEntity) caseFile;
        String calendarId = entity.getContainer().getCalendarFolderId();
        try
        {
            CalendarFolder folder = CalendarFolder.bind(service, new FolderId(calendarId));
            FolderPermissionCollection permissions = folder.getPermissions();
            for (FolderPermission permission : permissions.getItems())
            {
                if (permission.getUserId().getPrimarySmtpAddress().equals(user.getMail()))
                {
                    if (hasPermission(permission.getPermissionLevel(), permissionType))
                    {
                        return true;
                    }
                }
            }
        } catch (Exception e)
        {
            throw new CalendarServiceException(e);
        }
        return false;
    }

    /**
     * @param permissionLevel
     * @param permissionType
     * @return
     */
    private boolean hasPermission(FolderPermissionLevel permissionLevel, PermissionType permissionType)
    {
        switch (permissionType)
        {
        case READ:
            return permissionLevel.equals(FolderPermissionLevel.Owner) || permissionLevel.equals(FolderPermissionLevel.PublishingEditor)
                    || permissionLevel.equals(FolderPermissionLevel.Editor)
                    || permissionLevel.equals(FolderPermissionLevel.PublishingAuthor)
                    || permissionLevel.equals(FolderPermissionLevel.Author)
                    || permissionLevel.equals(FolderPermissionLevel.NoneditingAuthor)
                    || permissionLevel.equals(FolderPermissionLevel.Reviewer) || permissionLevel.equals(FolderPermissionLevel.Contributor);
        case WRITE:
            return permissionLevel.equals(FolderPermissionLevel.Owner) || permissionLevel.equals(FolderPermissionLevel.PublishingEditor)
                    || permissionLevel.equals(FolderPermissionLevel.Editor)
                    || permissionLevel.equals(FolderPermissionLevel.PublishingAuthor)
                    || permissionLevel.equals(FolderPermissionLevel.Author) || permissionLevel.equals(FolderPermissionLevel.Contributor);

        case DELETE:
            return permissionLevel.equals(FolderPermissionLevel.Owner) || permissionLevel.equals(FolderPermissionLevel.PublishingAuthor)
                    || permissionLevel.equals(FolderPermissionLevel.Author);
        default:
            return false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.armedia.acm.calendar.service.integration.exchange.EntityHandler#listCalendars(com.armedia.acm.services.users.
     * model.AcmUser, org.springframework.security.core.Authentication, java.lang.String, java.lang.String,
     * java.lang.String, int, int)
     */
    @Override
    public List<AcmCalendarInfo> listCalendars(ExchangeService service, AcmUser user, Authentication auth, String sort,
            String sortDirection, int start, int maxItems)
    {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.armedia.acm.calendar.service.integration.exchange.EntityHandler#getCalendarId(java.lang.String)
     */
    @Override
    public String getCalendarId(String objectId) throws CalendarServiceException
    {
        Object caseFile = getCaseFile(objectId, false);
        if (caseFile == null)
        {
            throw new CalendarServiceException(String.format("No calendar associated with CASE_FILE with id %s.", objectId));
        }
        AcmContainerEntity entity = (AcmContainerEntity) caseFile;
        return entity.getContainer().getCalendarFolderId();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.armedia.acm.calendar.service.integration.exchange.EntityHandler#listItems(java.time.ZonedDateTime,
     * java.time.ZonedDateTime, java.lang.String, java.lang.String, int, int)
     */
    @Override
    public List<AcmCalendarEventInfo> listItemsInfo(ExchangeService service, String objectId, ZonedDateTime after, ZonedDateTime before,
            String sort, String sortDirection, int start, int maxItems) throws CalendarServiceException
    {
        String calendarId = getCalendarId(objectId);
        try
        {
            FindItemsResults<Appointment> findResults = retreiveAppointments(service, after, before, sort, sortDirection, start, maxItems,
                    calendarId);

            List<AcmCalendarEventInfo> events = new ArrayList<>();
            for (Appointment appointment : findResults.getItems())
            {
                AcmCalendarEventInfo event = new AcmCalendarEventInfo();
                event.setCalendarId(calendarId);
                event.setCreatorId(appointment.getOrganizer().getAddress());
                event.setEventId(appointment.getId().getUniqueId());
                event.setObjectId(objectId);
                event.setObjectType("CASE_FILE");
                event.setSubject(appointment.getSubject());

                events.add(event);
            }
            return events;
        } catch (Exception e)
        {
            throw new CalendarServiceException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.armedia.acm.calendar.service.integration.exchange.EntityHandler#listItems(java.time.ZonedDateTime,
     * java.time.ZonedDateTime, java.lang.String, java.lang.String)
     */
    @Override
    public List<AcmCalendarEvent> listItems(ExchangeService service, String objectId, ZonedDateTime after, ZonedDateTime before,
            String sort, String sortDirection, int start, int maxItems) throws CalendarServiceException
    {
        String calendarId = getCalendarId(objectId);
        try
        {
            FindItemsResults<Appointment> findResults = retreiveAppointments(service, after, before, sort, sortDirection, start, maxItems,
                    calendarId);

            List<AcmCalendarEvent> events = new ArrayList<>();
            for (Appointment appointment : findResults.getItems())
            {
                AcmCalendarEvent event = new AcmCalendarEvent();
                ExchangeTypesConverter.setEventProperties(event, appointment);
                event.setObjectId(objectId);
                // event.setCalendarId(calendarId);
                events.add(event);
            }
            return events;
        } catch (Exception e)
        {
            throw new CalendarServiceException(e);
        }
    }

    /**
     * @param service
     * @param after
     * @param before
     * @param sort
     * @param sortDirection
     * @param start
     * @param maxItems
     * @param calendarId
     * @return
     * @throws ServiceLocalException
     * @throws Exception
     */
    private FindItemsResults<Appointment> retreiveAppointments(ExchangeService service, ZonedDateTime after, ZonedDateTime before,
            String sort, String sortDirection, int start, int maxItems, String calendarId) throws ServiceLocalException, Exception
    {
        ItemView view = new ItemView(maxItems, start);
        Date startDate = Date.from(after.toInstant());
        Date endDate = Date.from(before.toInstant());
        CalendarView calendarView = new CalendarView(startDate, endDate, maxItems);

        PropertyDefinition orderBy = sort == null || sort.trim().isEmpty() || !sortFields.containsKey(sort) ? ItemSchema.DateTimeReceived
                : sortFields.get(sort);

        view.getOrderBy().add(orderBy, "ASC".equals(sortDirection) ? SortDirection.Ascending : SortDirection.Descending);

        PropertySet allProperties = new PropertySet();
        allProperties.addRange(standardProperties);
        // calendarView.setPropertySet(allProperties);

        FindItemsResults<Appointment> findResults = service.findAppointments(new FolderId(calendarId), calendarView);
        if (!findResults.getItems().isEmpty())
        {
            List<Item> appointmentItems = findResults.getItems().stream().map(item -> {
                return (Item) item;
            }).collect(Collectors.toList());
            service.loadPropertiesForItems(appointmentItems, allProperties);
        }
        return findResults;
    }

}
