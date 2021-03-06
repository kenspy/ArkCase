package com.armedia.acm.services.protecturl.model;

/*-
 * #%L
 * acm-protect-url
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

import com.armedia.acm.core.AcmObject;
import com.armedia.acm.data.AcmEntity;
import com.armedia.acm.data.converter.LocalDateTimeConverter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Information holder for obfuscated and real url
 * <p>
 * Created by nebojsha on 27.07.2016.
 */
@Entity
@Table(name = "acm_protected_url")
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class ProtectedUrl implements AcmObject, AcmEntity
{
    @Id
    @TableGenerator(name = "acm_protected_url_gen", table = "acm_protected_url_id", pkColumnName = "cm_seq_name", valueColumnName = "cm_seq_num", pkColumnValue = "acm_protected_url", initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "acm_protected_url_gen")
    @Column(name = "cm_id")
    private Long id;

    @Column(name = "cm_original_url")
    private String originalUrl;

    @Column(name = "cm_obfuscated_url")
    private String obfuscatedUrl;

    @Column(name = "cm_valid_from")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime validFrom;

    @Column(name = "cm_valid_to")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime validTo;

    @Column(name = "cm_modifier")
    private String modifier;

    @Column(name = "cm_modified")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    @Column(name = "cm_creator")
    private String creator;

    @Column(name = "cm_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "cm_object_type")
    private String objectType = "PROTECTED_URL";

    public String getOriginalUrl()
    {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl)
    {
        this.originalUrl = originalUrl;
    }

    public String getObfuscatedUrl()
    {
        return obfuscatedUrl;
    }

    public void setObfuscatedUrl(String obfuscatedUrl)
    {
        this.obfuscatedUrl = obfuscatedUrl;
    }

    public LocalDateTime getValidFrom()
    {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom)
    {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidTo()
    {
        return validTo;
    }

    public void setValidTo(LocalDateTime validTo)
    {
        this.validTo = validTo;
    }

    @Override
    public String getObjectType()
    {
        return this.objectType;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @Override
    public String getCreator()
    {
        return creator;
    }

    @Override
    public void setCreator(String creator)
    {
        this.creator = creator;
    }

    @Override
    public String getModifier()
    {
        return modifier;
    }

    @Override
    public void setModifier(String modifier)
    {
        this.modifier = modifier;
    }

    @Override
    public Date getCreated()
    {
        return created;
    }

    @Override
    public void setCreated(Date created)
    {
        this.created = created;
    }

    @Override
    public Date getModified()
    {
        return modified;
    }

    @Override
    public void setModified(Date modified)
    {
        this.modified = modified;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
