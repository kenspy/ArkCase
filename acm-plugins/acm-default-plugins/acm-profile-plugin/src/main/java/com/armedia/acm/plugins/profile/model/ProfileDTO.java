package com.armedia.acm.plugins.profile.model;

/*-
 * #%L
 * ACM Default Plugin: Profile
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

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class", defaultImpl = ProfileDTO.class)
public class ProfileDTO
{

    private String userId;
    private String companyName;
    private String firstAddress;
    private String secondAddress;
    private String mainOfficePhone;
    private String fax;
    private String city;
    private String state;
    private String zip;
    private String website;
    private String location;
    private String imAccount;
    private String imSystem;
    private String officePhoneNumber;
    private String mobilePhoneNumber;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private Long ecmFileId;
    private Long ecmSignatureFileId;
    private String title;
    private List<String> groups;
    private Long userOrgId;
    private String langCode;

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }

    public String getFirstAddress()
    {
        return firstAddress;
    }

    public void setFirstAddress(String firstAddress)
    {
        this.firstAddress = firstAddress;
    }

    public String getSecondAddress()
    {
        return secondAddress;
    }

    public void setSecondAddress(String secondAddress)
    {
        this.secondAddress = secondAddress;
    }

    public String getMainOfficePhone()
    {
        return mainOfficePhone;
    }

    public void setMainOfficePhone(String mainOfficePhone)
    {
        this.mainOfficePhone = mainOfficePhone;
    }

    public String getFax()
    {
        return fax;
    }

    public void setFax(String fax)
    {
        this.fax = fax;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public String getState()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public String getZip()
    {
        return zip;
    }

    public void setZip(String zip)
    {
        this.zip = zip;
    }

    public String getWebsite()
    {
        return website;
    }

    public void setWebsite(String website)
    {
        this.website = website;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getImAccount()
    {
        return imAccount;
    }

    public void setImAccount(String imAccount)
    {
        this.imAccount = imAccount;
    }

    public String getImSystem()
    {
        return imSystem;
    }

    public void setImSystem(String imSystem)
    {
        this.imSystem = imSystem;
    }

    public String getOfficePhoneNumber()
    {
        return officePhoneNumber;
    }

    public void setOfficePhoneNumber(String officePhoneNumber)
    {
        this.officePhoneNumber = officePhoneNumber;
    }

    public String getMobilePhoneNumber()
    {
        return mobilePhoneNumber;
    }

    public void setMobilePhoneNumber(String mobilePhoneNumber)
    {
        this.mobilePhoneNumber = mobilePhoneNumber;
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

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Long getEcmFileId()
    {
        return ecmFileId;
    }

    public void setEcmFileId(Long ecmFileId)
    {
        this.ecmFileId = ecmFileId;
    }

    public Long getEcmSignatureFileId()
    {
        return ecmSignatureFileId;
    }

    public void setEcmSignatureFileId(Long ecmSignatureFileId)
    {
        this.ecmSignatureFileId = ecmSignatureFileId;
    }

    public List<String> getGroups()
    {
        return groups;
    }

    public void setGroups(List<String> groups)
    {
        this.groups = groups;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Long getUserOrgId()
    {
        return userOrgId;
    }

    public void setUserOrgId(Long userOrgId)
    {
        this.userOrgId = userOrgId;
    }

    public String getLangCode()
    {
        return langCode;
    }

    public void setLangCode(String langCode)
    {
        this.langCode = langCode;
    }

}
