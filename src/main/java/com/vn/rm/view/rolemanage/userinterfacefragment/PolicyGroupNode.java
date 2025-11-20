package com.vn.rm.view.rolemanage.userinterfacefragment;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.InstanceName;

import java.util.ArrayList;
import java.util.List;

@JmixEntity(name = "rm_PolicyGroupNode")
public class PolicyGroupNode {

    private String name;
    private Boolean group;

    private String type;      // VIEW / MENU
    private String resource;  // viewId or menuId
    private String action;    // view / menu
    private String effect;    // ALLOW / DENY
    private String meta;      // (View), (Menu), (View, Menu)

    private Boolean allow = false;
    private Boolean deny = false;

    private PolicyGroupNode parent;
    private List<PolicyGroupNode> children = new ArrayList<>();

    public PolicyGroupNode(String name, boolean group) {
        this.name = name;
        this.group = group;
    }


    // getters - setters

    public String getName() { return name; }
    public void setName(String name){ this.name = name; }

    public Boolean getGroup() { return group; }
    public void setGroup(Boolean group) { this.group = group; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEffect() { return effect; }
    public void setEffect(String effect) { this.effect = effect; }

    public Boolean getAllow() { return allow; }
    public void setAllow(Boolean allow) { this.allow = allow; }

    public Boolean getDeny() { return deny; }
    public void setDeny(Boolean deny) { this.deny = deny; }

    public String getMeta() { return meta; }
    public void setMeta(String meta) { this.meta = meta; }

    public PolicyGroupNode getParent() { return parent; }
    public void setParent(PolicyGroupNode parent) { this.parent = parent; }

    public List<PolicyGroupNode> getChildren() { return children; }
    public void setChildren(List<PolicyGroupNode> children) { this.children = children; }
}
