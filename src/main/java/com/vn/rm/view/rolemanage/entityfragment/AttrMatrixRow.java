package com.vn.rm.view.rolemanage.entityfragment;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.entity.annotation.JmixId;

import java.util.UUID;

@JmixEntity(name = "rm_AttrMatrixRow")
public class AttrMatrixRow {

    @JmixId
    @JmixGeneratedValue
    @JmixProperty
    private UUID id;

    @JmixProperty
    private String entityName;

    @JmixProperty
    private String attribute;

    @JmixProperty
    private Boolean canView = false;

    @JmixProperty
    private Boolean canModify = false;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public Boolean getCanView() {
        return canView;
    }

    public void setCanView(Boolean canView) {
        this.canView = canView;
    }

    public Boolean getCanModify() {
        return canModify;
    }

    public void setCanModify(Boolean canModify) {
        this.canModify = canModify;
    }
}
