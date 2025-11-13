package com.vn.rm.security;

import com.vn.rm.entity.PolicyGroupNode;
import com.vn.rm.entity.User;
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

    @SpecificPolicy(resources = "*")
    void fullAccess();

    @EntityAttributePolicy(entityClass = AttributeResourceModel.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = AttributeResourceModel.class, actions = EntityPolicyAction.ALL)
    void attributeResourceModel();

    @EntityAttributePolicy(entityClass = BaseRoleModel.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = BaseRoleModel.class, actions = EntityPolicyAction.ALL)
    void baseRoleModel();

    @EntityAttributePolicy(entityClass = FilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = FilterCondition.class, actions = EntityPolicyAction.ALL)
    void filterCondition();

    @EntityAttributePolicy(entityClass = FilterConfiguration.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = FilterConfiguration.class, actions = EntityPolicyAction.ALL)
    void filterConfiguration();

    @EntityAttributePolicy(entityClass = FilterConfigurationModel.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = FilterConfigurationModel.class, actions = EntityPolicyAction.ALL)
    void filterConfigurationModel();

    @EntityAttributePolicy(entityClass = FilterValueComponent.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = FilterValueComponent.class, actions = EntityPolicyAction.ALL)
    void filterValueComponent();

    @EntityAttributePolicy(entityClass = GroupFilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = GroupFilterCondition.class, actions = EntityPolicyAction.ALL)
    void groupFilterCondition();

    @EntityAttributePolicy(entityClass = HeaderFilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = HeaderFilterCondition.class, actions = EntityPolicyAction.ALL)
    void headerFilterCondition();

    @EntityAttributePolicy(entityClass = InfoValue.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = InfoValue.class, actions = EntityPolicyAction.ALL)
    void infoValue();

    @EntityAttributePolicy(entityClass = JpqlFilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = JpqlFilterCondition.class, actions = EntityPolicyAction.ALL)
    void jpqlFilterCondition();

    @EntityAttributePolicy(entityClass = KeyValueEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = KeyValueEntity.class, actions = EntityPolicyAction.ALL)
    void keyValueEntity();

    @EntityAttributePolicy(entityClass = LogicalFilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = LogicalFilterCondition.class, actions = EntityPolicyAction.ALL)
    void logicalFilterCondition();

    @EntityAttributePolicy(entityClass = PolicyGroupNode.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = PolicyGroupNode.class, actions = EntityPolicyAction.ALL)
    void policyGroupNode();

    @EntityAttributePolicy(entityClass = PropertyFilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = PropertyFilterCondition.class, actions = EntityPolicyAction.ALL)
    void propertyFilterCondition();

    @EntityAttributePolicy(entityClass = ResourcePolicyEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ResourcePolicyEntity.class, actions = EntityPolicyAction.ALL)
    void resourcePolicyEntity();

    @EntityAttributePolicy(entityClass = ResourcePolicyModel.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ResourcePolicyModel.class, actions = EntityPolicyAction.ALL)
    void resourcePolicyModel();

    @EntityAttributePolicy(entityClass = ResourceRoleEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ResourceRoleEntity.class, actions = EntityPolicyAction.ALL)
    void resourceRoleEntity();

    @EntityAttributePolicy(entityClass = ResourceRoleModel.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ResourceRoleModel.class, actions = EntityPolicyAction.ALL)
    void resourceRoleModel();

    @EntityAttributePolicy(entityClass = RoleAssignmentEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = RoleAssignmentEntity.class, actions = EntityPolicyAction.ALL)
    void roleAssignmentEntity();

    @EntityAttributePolicy(entityClass = RoleAssignmentModel.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = RoleAssignmentModel.class, actions = EntityPolicyAction.ALL)
    void roleAssignmentModel();

    @EntityAttributePolicy(entityClass = RowLevelPolicyEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = RowLevelPolicyEntity.class, actions = EntityPolicyAction.ALL)
    void rowLevelPolicyEntity();

    @EntityAttributePolicy(entityClass = RowLevelPolicyModel.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = RowLevelPolicyModel.class, actions = EntityPolicyAction.ALL)
    void rowLevelPolicyModel();

    @EntityAttributePolicy(entityClass = RowLevelRoleEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = RowLevelRoleEntity.class, actions = EntityPolicyAction.ALL)
    void rowLevelRoleEntity();

    @EntityAttributePolicy(entityClass = RowLevelRoleModel.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = RowLevelRoleModel.class, actions = EntityPolicyAction.ALL)
    void rowLevelRoleModel();

    @EntityAttributePolicy(entityClass = User.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = User.class, actions = EntityPolicyAction.ALL)
    void user();

    @EntityAttributePolicy(entityClass = UserPasswordValue.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = UserPasswordValue.class, actions = EntityPolicyAction.ALL)
    void userPasswordValue();

    @EntityAttributePolicy(entityClass = UserSettingsItem.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = UserSettingsItem.class, actions = EntityPolicyAction.ALL)
    void userSettingsItem();

    @EntityAttributePolicy(entityClass = UserSubstitutionEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = UserSubstitutionEntity.class, actions = EntityPolicyAction.ALL)
    void userSubstitutionEntity();

    @EntityAttributePolicy(entityClass = UserSubstitutionModel.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = UserSubstitutionModel.class, actions = EntityPolicyAction.ALL)
    void userSubstitutionModel();

    @EntityAttributePolicy(entityClass = AbstractSingleFilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void abstractSingleFilterCondition();

    @MenuPolicy(menuIds = {"rm_User.list", "sec_ResourceRoleModel.list", "sec_RowLevelRoleModel.list", "datatl_entityInspectorListView", "ext_sec_ResourceRoleModel.list"})
    @ViewPolicy(viewIds = {"ext_sec_ResourceRoleModel.list", "rm_User.list", "sec_ResourceRoleModel.list", "sec_RowLevelRoleModel.list", "datatl_entityInspectorListView", "rm_LoginView", "rm_MainView", "EntitiesFragment", "UserInterfaceFragment", "rm_ResourceRoleEditView", "DataGridEmptyStateByPermissionsFragment", "headerPropertyFilterLayout", "inputDialog", "multiValueSelectDialog", "flowui_AddConditionView", "flowui_GroupFilterCondition.detail"})
    void screens();
}