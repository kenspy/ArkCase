package com.armedia.acm.services.transcribe.model;

import com.armedia.acm.core.AcmObject;
import com.armedia.acm.data.AcmEntity;
import com.armedia.acm.data.converter.BooleanToStringConverter;
import com.armedia.acm.services.transcribe.converter.DurationConverter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Duration;
import java.util.Date;

/**
 * Created by Riste Tutureski <riste.tutureski@armedia.com> on 02/27/2018
 */
@Entity
@Table(name = "acm_transcribe_item")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "className", defaultImpl = TranscribeItem.class)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "cm_transcribe_item_class_name", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("com.armedia.acm.services.transcribe.model.TranscribeItem")
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class TranscribeItem implements AcmObject, AcmEntity, Serializable
{
    @Id
    @TableGenerator(name = "transcribe_item_gen", table = "acm_transcribe_item_id", pkColumnName = "cm_seq_name", valueColumnName = "cm_seq_num", pkColumnValue = "acm_transcribe_item", initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "transcribe_item_gen")
    @Column(name = "cm_transcribe_item_id")
    private Long id;

    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.DETACH, CascadeType.PERSIST }, optional = false)
    @JoinColumn(name = "cm_transcribe_id", nullable = false)
    private Transcribe transcribe;

    @Column(name = "cm_transcribe_item_start_time")
    @Convert(converter = DurationConverter.class)
    private Duration startTime;

    @Column(name = "cm_transcribe_item_end_time")
    @Convert(converter = DurationConverter.class)
    private Duration endTime;

    @Column(name = "cm_transcribe_item_confidence")
    private int confidence;

    @Column(name = "cm_transcribe_item_corrected")
    @Convert(converter = BooleanToStringConverter.class)
    private Boolean corrected = Boolean.FALSE;

    @Lob
    @Column(name = "cm_transcribe_item_text")
    private String text;

    @Column(name = "cm_transcribe_item_creator")
    private String creator;

    @Column(name = "cm_transcribe_item_created")
    private Date created;

    @Column(name = "cm_transcribe_item_modifier")
    private String modifier;

    @Column(name = "cm_transcribe_item_modified")
    private Date modified;

    @Column(name = "cm_transcribe_item_class_name")
    private String className = this.getClass().getName();

    @Override
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Transcribe getTranscribe()
    {
        return transcribe;
    }

    public void setTranscribe(Transcribe transcribe)
    {
        this.transcribe = transcribe;
    }

    public Duration getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Duration startTime)
    {
        this.startTime = startTime;
    }

    public Duration getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Duration endTime)
    {
        this.endTime = endTime;
    }

    public int getConfidence()
    {
        return confidence;
    }

    public void setConfidence(int confidence)
    {
        this.confidence = confidence;
    }

    public Boolean getCorrected()
    {
        return corrected;
    }

    public void setCorrected(Boolean corrected)
    {
        this.corrected = corrected;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
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
    public Date getModified()
    {
        return modified;
    }

    @Override
    public void setModified(Date modified)
    {
        this.modified = modified;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    @Override
    public String getObjectType()
    {
        return TranscribeConstants.OBJECT_TYPE_ITEM;
    }
}
