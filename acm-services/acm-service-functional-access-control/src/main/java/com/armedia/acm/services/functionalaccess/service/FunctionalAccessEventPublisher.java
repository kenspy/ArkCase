package com.armedia.acm.services.functionalaccess.service;

import com.armedia.acm.services.functionalaccess.model.FunctionalAccessUpdatedEvent;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.core.Authentication;

/**
 * @author riste.tutureski
 *
 */
public class FunctionalAccessEventPublisher implements ApplicationEventPublisherAware
{

    private ApplicationEventPublisher eventPublisher;

    public void publishFunctionalAccessUpdateEvent(Object source, Authentication auth)
    {
        publishFunctionalAccessUpdateEventOnRolesToGroupMap(source, auth.getName());
    }

    public void publishFunctionalAccessUpdateEventOnRolesToGroupMap(Object source, String userId)
    {
        FunctionalAccessUpdatedEvent event = new FunctionalAccessUpdatedEvent(source, userId);
        getEventPublisher().publishEvent(event);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher)
    {
        eventPublisher = applicationEventPublisher;
    }

    public ApplicationEventPublisher getEventPublisher()
    {
        return eventPublisher;
    }

}
