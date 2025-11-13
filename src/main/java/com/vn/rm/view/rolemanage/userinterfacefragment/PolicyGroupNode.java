package com.vn.rm.view.rolemanage.userinterfacefragment;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.InstanceName;

import java.util.ArrayList;
import java.util.List;

@JmixEntity(name = "rm_PolicyGroupNode")
public class PolicyGroupNode {

    @InstanceName
    private String name;

    private PolicyGroupNode parent;
    private List<PolicyGroupNode> children = new ArrayList<>();

    private String resource;
    private String action;
    private String effect;
    private String type; // VIEW / MENU

    // ✅ Dùng wrapper Boolean (FlowUI không thích primitive boolean)
    private Boolean group = Boolean.FALSE;
    private Boolean allow = Boolean.FALSE;
    private Boolean deny = Boolean.FALSE;

    public PolicyGroupNode() {
    }

    public PolicyGroupNode(String name, Boolean group) {
        this.name = name;
        this.group = group;
    }

    // ==========================
    // ✅ Getter / Setter chuẩn hóa
    // ==========================

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PolicyGroupNode getParent() { return parent; }
    public void setParent(PolicyGroupNode parent) { this.parent = parent; }

    public List<PolicyGroupNode> getChildren() { return children; }
    public void setChildren(List<PolicyGroupNode> children) { this.children = children; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEffect() { return effect; }
    public void setEffect(String effect) { this.effect = effect; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Boolean getGroup() { return group != null && group; }
    public void setGroup(Boolean group) { this.group = group; }

    public Boolean getAllow() { return allow != null && allow; }
    public void setAllow(Boolean allow) { this.allow = allow; }

    public Boolean getDeny() { return deny != null && deny; }
    public void setDeny(Boolean deny) { this.deny = deny; }

    // ==========================
    // ✅ Tiện ích (optional)
    // ==========================

    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("PolicyGroupNode{name='%s', type='%s', resource='%s', allow=%s, deny=%s}",
                name, type, resource, allow, deny);
    }
}