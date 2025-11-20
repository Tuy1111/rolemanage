package com.vn.rm.security;

import com.vn.rm.entity.User;
import com.vn.rm.view.rolemanage.entityfragment.EntityMatrixRow;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.datatoolsflowui.view.entityinfo.model.InfoValue;
import io.jmix.flowui.component.genericfilter.model.FilterConfigurationModel;
import io.jmix.flowui.entity.filter.*;
import io.jmix.flowuidata.entity.FilterConfiguration;
import io.jmix.flowuidata.entity.UserSettingsItem;
import io.jmix.security.model.*;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.security.role.assignment.RoleAssignmentModel;
import io.jmix.security.usersubstitution.UserSubstitutionModel;
import io.jmix.securitydata.entity.*;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;
import io.jmix.securityflowui.view.resetpassword.model.UserPasswordValue;
import io.jmix.securityflowui.view.resourcepolicy.AttributeResourceModel;

@ResourceRole(name = "Full Access", code = FullAccessRole.CODE)
public interface FullAccessRole {

    String CODE = "system-full-access";

    @ViewPolicy(viewIds = {"ext_sec_ResourceRoleModel.list", "rm_User.list", "sec_RowLevelRoleModel.list", "datatl_entityInspectorListView", "DataGridEmptyStateByPermissionsFragment", "EntitiesFragment", "FragmentRenderer", "UserInterfaceFragment", "changePasswordView", "datatl_entityInspectorDetailView", "entityInfoView", "flowui_AddConditionView", "flowui_DateIntervalDialog", "flowui_GroupFilterCondition.detail", "flowui_JpqlFilterCondition.detail", "flowui_PropertyFilterCondition.detail", "headerPropertyFilterLayout", "inputDialog", "multiValueSelectDialog", "resetPasswordView", "rm_LoginView", "rm_MainView", "rm_ResourceRoleEditView", "rm_User.detail", "roleAssignmentView", "sec_EntityAttributeResourcePolicyModel.create", "sec_EntityAttributeResourcePolicyModel.detail", "sec_EntityResourcePolicyModel.create", "sec_EntityResourcePolicyModel.detail", "sec_GraphQLResourcePolicyModel.detail", "sec_MenuResourcePolicyModel.create", "sec_MenuResourcePolicyModel.detail", "sec_ResourcePolicyModel.detail", "sec_ResourceRoleModel.detail", "sec_ResourceRoleModel.lookup", "sec_RowLevelPolicyModel.detail", "sec_RowLevelRoleModel.detail", "sec_RowLevelRoleModel.lookup", "sec_SpecificResourcePolicyModel.detail", "sec_SubstituteUserView", "sec_UserSubstitution.detail", "sec_UserSubstitution.view", "sec_ViewResourcePolicyModel.create", "sec_ViewResourcePolicyModel.detail"})
    @MenuPolicy(menuIds = {"ext_sec_ResourceRoleModel.list", "rm_User.list", "sec_RowLevelRoleModel.list", "datatl_entityInspectorListView", "sec_ResourceRoleModel.list"})
    @SpecificPolicy(resources = "*")
    void fullAccess();


    @EntityPolicy(entityClass = AbstractSingleFilterCondition.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = AbstractSingleFilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void abstractSingleFilterCondition();

    @EntityPolicy(entityClass = AttributeResourceModel.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = AttributeResourceModel.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void attributeResourceModel();

    @EntityPolicy(entityClass = BaseRoleModel.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = BaseRoleModel.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void baseRoleModel();

    @EntityPolicy(entityClass = EntityMatrixRow.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = EntityMatrixRow.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void entityMatrixRow();

    @EntityPolicy(entityClass = FilterCondition.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = FilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void filterCondition();

    @EntityPolicy(entityClass = FilterConfiguration.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = FilterConfiguration.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void filterConfiguration();

    @EntityPolicy(entityClass = FilterConfigurationModel.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = FilterConfigurationModel.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void filterConfigurationModel();

    @EntityPolicy(entityClass = FilterValueComponent.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = FilterValueComponent.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void filterValueComponent();

    @EntityPolicy(entityClass = GroupFilterCondition.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = GroupFilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void groupFilterCondition();

    @EntityPolicy(entityClass = HeaderFilterCondition.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = HeaderFilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void headerFilterCondition();


    @EntityPolicy(entityClass = InfoValue.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = InfoValue.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void infoValue();

    @EntityPolicy(entityClass = JpqlFilterCondition.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = JpqlFilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void jpqlFilterCondition();

    @EntityPolicy(entityClass = KeyValueEntity.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = KeyValueEntity.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void keyValueEntity();

    @EntityPolicy(entityClass = LogicalFilterCondition.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = LogicalFilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void logicalFilterCondition();

    @EntityPolicy(entityClass = PropertyFilterCondition.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = PropertyFilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void propertyFilterCondition();

    @EntityPolicy(entityClass = ResourcePolicyEntity.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = ResourcePolicyEntity.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void resourcePolicyEntity();

    @EntityPolicy(entityClass = ResourcePolicyModel.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = ResourcePolicyModel.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void resourcePolicyModel();

    @EntityPolicy(entityClass = ResourceRoleEntity.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = ResourceRoleEntity.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void resourceRoleEntity();

    @EntityPolicy(entityClass = ResourceRoleModel.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = ResourceRoleModel.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void resourceRoleModel();

    @EntityPolicy(entityClass = RoleAssignmentEntity.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = RoleAssignmentEntity.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void roleAssignmentEntity();

    @EntityPolicy(entityClass = RoleAssignmentModel.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = RoleAssignmentModel.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void roleAssignmentModel();

    @EntityPolicy(entityClass = RowLevelPolicyEntity.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = RowLevelPolicyEntity.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void rowLevelPolicyEntity();

    @EntityPolicy(entityClass = RowLevelPolicyModel.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = RowLevelPolicyModel.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void rowLevelPolicyModel();

    @EntityPolicy(entityClass = RowLevelRoleEntity.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = RowLevelRoleEntity.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void rowLevelRoleEntity();

    @EntityPolicy(entityClass = RowLevelRoleModel.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = RowLevelRoleModel.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void rowLevelRoleModel();

    @EntityPolicy(entityClass = User.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = User.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void user();

    @EntityPolicy(entityClass = UserPasswordValue.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = UserPasswordValue.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void userPasswordValue();

    @EntityPolicy(entityClass = UserSettingsItem.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = UserSettingsItem.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void userSettingsItem();

    @EntityPolicy(entityClass = UserSubstitutionEntity.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = UserSubstitutionEntity.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void userSubstitutionEntity();

    @EntityPolicy(entityClass = UserSubstitutionModel.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = UserSubstitutionModel.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void userSubstitutionModel();
}