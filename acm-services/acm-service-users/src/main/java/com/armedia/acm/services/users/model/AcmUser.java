package com.armedia.acm.services.users.model;

import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "ACM_USER")
public class AcmUser implements Serializable, AcmLdapEntity
{
    private static final long serialVersionUID = 3399640646540732944L;

    @Id
    @Column(name = "cm_user_id")
    private String userId;

    @Column(name = "cm_full_name")
    private String fullName;

    @Column(name = "cm_first_name")
    private String firstName;

    @Column(name = "cm_last_name")
    private String lastName;

    @Column(name = "cm_user_directory_name", updatable = false)
    private String userDirectoryName;

    @Column(name = "cm_user_created", insertable = true, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "cm_user_modified")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    @Column(name = "cm_user_state")
    private String userState;

    @Column(name="cm_mail")
    private String mail;

    @Transient
    private String distinguishedName;

    @PrePersist
    public void preInsert()
    {
        setCreated(new Date());
        setModified(new Date());
        setUserState("VALID");
    }

    @PreUpdate
    public void preUpdate()
    {
        setModified(new Date());
    }


    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getUserDirectoryName()
    {
        return userDirectoryName;
    }

    public void setUserDirectoryName(String userDirectoryName)
    {
        this.userDirectoryName = userDirectoryName;
    }

    public Date getCreated()
    {
        return created;
    }

    public void setCreated(Date created)
    {
        this.created = created;
    }

    public Date getModified()
    {
        return modified;
    }

    public void setModified(Date modified)
    {
        this.modified = modified;
    }

    public String getUserState()
    {
        return userState;
    }

    public void setUserState(String userState)
    {
        this.userState = userState;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    @Override
    public String getDistinguishedName()
    {
        return distinguishedName;
    }

    @Override
    public void setDistinguishedName(String distinguishedName)
    {
        this.distinguishedName = distinguishedName;
    }

    @Override
    @JsonIgnore
    public boolean isGroup()
    {
        return false;
    }
    
    @Override
    @JsonIgnore
    public int hashCode() {
    	if (getUserId() == null)
    	{
    		return 0;
    	}
    	else
    	{
    		return getUserId().hashCode();
    	}
    }

    @Override
    @JsonIgnore
    public boolean equals(Object obj) {
        if (!(obj instanceof AcmUser)) 
        {
            return false;
        }
        
        AcmUser user = (AcmUser)obj;
        
        if (user.getUserId() == null && getUserId() == null)
    	{
    		return true;
    	}
        
        if (user.getUserId() == null && getUserId() != null)
        {
        	return false;
        }
        
        return user.getUserId().equals(getUserId());
    }
}