package com.vn.rm.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "New", code = NewRole.CODE)
public interface NewRole {
    String CODE = "new-role";

    @MenuPolicy(menuIds = {"rm_User.list", "ext_sec_ResourceRoleModel.list"})
    @ViewPolicy(viewIds = {"rm_User.list", "ext_sec_ResourceRoleModel.list"})
    void screens();
}