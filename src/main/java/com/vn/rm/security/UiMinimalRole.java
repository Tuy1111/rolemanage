package com.vn.rm.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.UiMinimalPolicies;

@ResourceRole(name = "UI: minimal access", code = UiMinimalRole.CODE)
public interface UiMinimalRole extends UiMinimalPolicies {

    String CODE = "ui-minimal";

    @SpecificPolicy(resources = "ui.loginToUi")
    void login();
}
