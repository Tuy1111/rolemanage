package com.vn.rm.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "New", code = NewRole.CODE)
public interface NewRole {
    String CODE = "new-role";

    @MenuPolicy(menuIds = {"sec_ResourceRoleModel.list", "datatl_entityInspectorListView", "ext_sec_ResourceRoleModel.list"})
    @ViewPolicy(viewIds = {"sec_ResourceRoleModel.list", "datatl_entityInspectorListView", "rm_LoginView", "ext_sec_ResourceRoleModel.list"})
    void screens();
}