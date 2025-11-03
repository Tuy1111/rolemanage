package com.vn.rm.view.rolesystem;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vn.rm.view.rolemanage.ResourceRoleEditView;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelListView;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "sec/extresourcerolemodels", layout = DefaultMainViewParent.class)
@ViewController(id = "ext_sec_ResourceRoleModel.list")
@ViewDescriptor(path = "ext-resource-role-model-list-view.xml")
public class ExtResourceRoleModelListView extends ResourceRoleModelListView {
    @Autowired
    private ViewNavigators viewNavigators;
    @ViewComponent
    private DataGrid<ResourceRoleModel> roleModelsTable;

    @Subscribe("roleModelsTable.createCustomRole")
    public void onRoleModelsTableCreateCustomRole(final ActionPerformedEvent event) {
        viewNavigators.view(this,ResourceRoleEditView.class)
                  .navigate();
    }

    @Subscribe("roleModelsTable.editCustomRole")
    public void onRoleModelsTableEditCustomRole(final ActionPerformedEvent event) {
        ResourceRoleModel roleModel = roleModelsTable.getSingleSelectedItem();

        viewNavigators.view(this,ResourceRoleEditView.class)
                .navigate();
    }

}