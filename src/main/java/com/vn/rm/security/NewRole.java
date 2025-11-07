package com.vn.rm.security;

import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "New", code = NewRole.CODE)
public interface NewRole {
    String CODE = "new-role";

}