package com.vn.rm.view.rolemanage;


import com.google.common.collect.Sets;
import com.vaadin.flow.router.Route;
import com.vn.rm.view.main.MainView;
import io.jmix.flowui.view.*;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.model.RoleSourceType;
import io.jmix.security.model.SecurityScope;

@Route(value = "sec/resource-role-edit-view", layout = MainView.class)
@ViewController(id = "rm_ResourceRoleEditView")
@ViewDescriptor(path = "resource-role-edit-view.xml")
//@EditedEntityContainer("roleModelDc")
public class ResourceRoleEditView  extends StandardView {
    public static final String ROUTE_PARAM_NAME = "code";
//
//    @Subscribe
//    public void onInitNewEntity(InitEntityEvent<ResourceRoleModel> event) {
//        ResourceRoleModel entity = event.getEntity();
//        entity.setSource(RoleSourceType.DATABASE);
//        entity.setScopes(Sets.newHashSet(SecurityScope.UI));
//    }
//    @Override
//    protected String getRouteParamName() {
//        return ROUTE_PARAM_NAME;
//    }
//
//    @Override
//    protected void setupEntityToEdit(ResourceRoleModel entityToEdit) {
//        // do nothing
//    }

}