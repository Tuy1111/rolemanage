package com.vn.rm.view.rolemanage;

import com.google.common.base.Strings;
import com.vaadin.flow.router.Route;
import com.vn.rm.view.main.MainView;
import com.vn.rm.view.rolemanage.entityfragment.EntitiesFragment;
import com.vn.rm.view.rolemanage.userinterfacefragment.UserInterfaceFragment;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.security.model.*;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.securitydata.entity.ResourcePolicyEntity;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Route(value = "sec/resource-role-edit-view/:code", layout = MainView.class)
@ViewController("rm_ResourceRoleEditView")
@ViewDescriptor("resource-role-edit-view.xml")
@EditedEntityContainer("roleModelDc")
public class ResourceRoleEditView extends StandardDetailView<ResourceRoleModel> {

    // ============================= UI components =============================

    @ViewComponent
    private JmixCheckboxGroup<String> scopesField;
    @ViewComponent
    private DataContext dataContext;
    @ViewComponent
    private InstanceContainer<ResourceRoleModel> roleModelDc;
    @ViewComponent
    private CollectionContainer<ResourceRoleModel> childRolesDc;
    @ViewComponent
    private CollectionContainer<ResourcePolicyModel> resourcePoliciesDc;

    // Fragment matrix ENTITY + ATTR
    @ViewComponent
    private EntitiesFragment entitiesFragment;

    // Fragment UI/Menu tree
    @ViewComponent
    private UserInterfaceFragment userInterfaceFragment;


    // ================================ Services ===============================

    @Autowired
    private UrlParamSerializer urlParamSerializer;
    @Autowired
    private ResourceRoleRepository roleRepository;
    @Autowired
    private RoleModelConverter roleModelConverter;
    @Autowired
    private Metadata metadata;
    @Autowired
    private DataManager dataManager;

    // ============================== Lifecycle ================================

    @Override
    protected String getRouteParamName() {
        return "code";
    }

    @Subscribe
    public void onInit(InitEvent event) {
        scopesField.setItems(Arrays.asList(SecurityScope.UI, SecurityScope.API));
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        if (entitiesFragment != null)
            entitiesFragment.reloadFromPolicies();

        if (userInterfaceFragment != null && roleModelDc.getItem() != null)
            userInterfaceFragment.initUi(roleModelDc.getItem());
    }

    // ============================ Load role ============================

    @Override
    protected void initExistingEntity(String serializedEntityCode) {
        String code = null;
        try {
            code = urlParamSerializer.deserialize(String.class, serializedEntityCode);
        } catch (Exception ignore) {}

        if (Strings.isNullOrEmpty(code)) {
            close(StandardOutcome.CLOSE);
            return;
        }

        ResourceRoleEntity roleEntity = dataManager.load(ResourceRoleEntity.class)
                .query("select r from sec_ResourceRoleEntity r left join fetch r.resourcePolicies where r.code = :code")
                .parameter("code", code)
                .optional()
                .orElse(null);

        if (roleEntity == null) {
            close(StandardOutcome.CLOSE);
            return;
        }

        ResourceRoleModel model = mapDbRoleToModel(roleEntity);

        childRolesDc.setItems(loadChildRoleModels(model));

        roleModelDc.setItem(dataContext.merge(model));

        resourcePoliciesDc.setItems(
                model.getResourcePolicies() != null
                        ? new ArrayList<>(model.getResourcePolicies())
                        : Collections.emptyList()
        );

        // sync lại matrix fragment
        if (entitiesFragment != null)
            entitiesFragment.reloadFromPolicies();

        // sync lại UI/Menu fragment
        if (userInterfaceFragment != null)
            userInterfaceFragment.initUi(model);
    }

    // ============================ SAVE ============================

    @Subscribe("saveAction")
    public void onSaveAction(ActionPerformedEvent event) {
        ResourceRoleModel model = roleModelDc.getItem();
        if (model == null)
            return;

        List<ResourcePolicyModel> nonEntityPolicies =
                Optional.ofNullable(model.getResourcePolicies())
                        .orElseGet(ArrayList::new)
                        .stream()
                        .filter(p -> p.getType() != ResourcePolicyType.ENTITY
                                && p.getType() != ResourcePolicyType.ENTITY_ATTRIBUTE)
                        .collect(Collectors.toList());

        List<ResourcePolicyModel> matrixPolicies =
                entitiesFragment != null
                        ? entitiesFragment.buildPoliciesFromMatrix()
                        : List.of();

        // ADD UI/MENU policies từ UserInterfaceFragment
        if (userInterfaceFragment != null) {
            nonEntityPolicies.addAll(
                    userInterfaceFragment.collectPoliciesFromTree()
            );

            // add '*' allow all nếu checkbox bật
            if (userInterfaceFragment.isAllowAllViewsChecked()) {
                ResourcePolicyModel all = new ResourcePolicyModel();
                all.setType("VIEW");
                all.setResource("*");
                all.setAction("view");
                all.setEffect("ALLOW");
                all.setId(UUID.randomUUID());
                nonEntityPolicies.add(all);
            }
        }

        nonEntityPolicies.addAll(matrixPolicies);
        model.setResourcePolicies(nonEntityPolicies);

        persistRoleToDb(model);
        close(StandardOutcome.SAVE);
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    public void onBeforeSave(DataContext.PreSaveEvent event) {
        ResourceRoleModel model = roleModelDc.getItem();
        if (model == null) return;

        List<ResourcePolicyModel> nonEntityPolicies =
                Optional.ofNullable(model.getResourcePolicies())
                        .orElseGet(ArrayList::new)
                        .stream()
                        .filter(p -> p.getType() != ResourcePolicyType.ENTITY
                                && p.getType() != ResourcePolicyType.ENTITY_ATTRIBUTE)
                        .collect(Collectors.toList());

        List<ResourcePolicyModel> matrixPolicies =
                entitiesFragment != null
                        ? entitiesFragment.buildPoliciesFromMatrix()
                        : List.of();

        // UI/Menu policies
        if (userInterfaceFragment != null) {
            nonEntityPolicies.addAll(
                    userInterfaceFragment.collectPoliciesFromTree()
            );

            if (userInterfaceFragment.isAllowAllViewsChecked()) {
                ResourcePolicyModel all = new ResourcePolicyModel();
                all.setType("VIEW");
                all.setResource("*");
                all.setAction("view");
                all.setEffect("ALLOW");
                all.setId(UUID.randomUUID());
                nonEntityPolicies.add(all);
            }
        }

        nonEntityPolicies.addAll(matrixPolicies);
        model.setResourcePolicies(nonEntityPolicies);
    }

    // ============================ MAP DB → MODEL ============================

    private ResourceRoleModel mapDbRoleToModel(ResourceRoleEntity roleEntity) {
        ResourceRoleModel m = metadata.create(ResourceRoleModel.class);
        m.setCode(roleEntity.getCode());
        m.setName(roleEntity.getName());
        m.setDescription(roleEntity.getDescription());
        m.setScopes(roleEntity.getScopes() == null ? Set.of() : new HashSet<>(roleEntity.getScopes()));

        // ⭐ FIX NullPointerException — luôn set source
        m.setSource(RoleSourceType.DATABASE);

        if (roleEntity.getResourcePolicies() != null) {
            List<ResourcePolicyModel> ps = roleEntity.getResourcePolicies().stream().map(pe -> {
                ResourcePolicyModel p = metadata.create(ResourcePolicyModel.class);
                p.setType(pe.getType());
                p.setResource(pe.getResource());
                p.setAction(pe.getAction());
                p.setEffect(pe.getEffect());
                p.setPolicyGroup(pe.getPolicyGroup());
                return p;
            }).collect(Collectors.toList());
            m.setResourcePolicies(ps);
        }
        return m;
    }

    // ============================ DB SAVE ============================

    private void persistRoleToDb(ResourceRoleModel model) {
        String code = model.getCode();
        if (Strings.isNullOrEmpty(code)) {
            throw new IllegalStateException("Code không được rỗng.");
        }

        ResourceRoleEntity role = dataManager.load(ResourceRoleEntity.class)
                .query("select r from sec_ResourceRoleEntity r left join fetch r.resourcePolicies where r.code = :code")
                .parameter("code", code)
                .optional()
                .orElseGet(() -> dataManager.create(ResourceRoleEntity.class));

        role.setCode(code);
        role.setName(model.getName());
        role.setDescription(model.getDescription());
        role.setScopes(model.getScopes() == null ? Set.of() : new HashSet<>(model.getScopes()));

        Map<String, ResourcePolicyEntity> existing = new HashMap<>();
        if (role.getResourcePolicies() == null) {
            role.setResourcePolicies(new ArrayList<>());
        } else {
            for (ResourcePolicyEntity pe : role.getResourcePolicies()) {
                existing.put(policyKey(pe.getType(), pe.getResource(), pe.getAction(), pe.getEffect(), pe.getPolicyGroup()), pe);
            }
        }

        Set<String> keepKeys = new HashSet<>();
        List<ResourcePolicyEntity> toPersistOrUpdate = new ArrayList<>();
        for (ResourcePolicyModel p : Optional.ofNullable(model.getResourcePolicies()).orElseGet(List::of)) {
            String key = policyKey(p.getType(), p.getResource(), p.getAction(), p.getEffect(), p.getPolicyGroup());
            keepKeys.add(key);

            ResourcePolicyEntity pe = existing.get(key);
            if (pe == null) {
                pe = dataManager.create(ResourcePolicyEntity.class);
                pe.setRole(role);
                pe.setType(p.getType());
                pe.setResource(p.getResource());
                pe.setAction(p.getAction());
                pe.setEffect(p.getEffect());
                pe.setPolicyGroup(p.getPolicyGroup());
                role.getResourcePolicies().add(pe);
            } else {
                pe.setType(p.getType());
                pe.setResource(p.getResource());
                pe.setAction(p.getAction());
                pe.setEffect(p.getEffect());
                pe.setPolicyGroup(p.getPolicyGroup());
            }
            toPersistOrUpdate.add(pe);
        }

        List<ResourcePolicyEntity> toRemove = role.getResourcePolicies().stream()
                .filter(pe -> !keepKeys.contains(policyKey(pe.getType(), pe.getResource(), pe.getAction(), pe.getEffect(), pe.getPolicyGroup())))
                .toList();

        role.getResourcePolicies().removeIf(toRemove::contains);

        io.jmix.core.SaveContext ctx = new io.jmix.core.SaveContext();
        ctx.saving(role);
        toPersistOrUpdate.forEach(ctx::saving);
        toRemove.forEach(ctx::removing);
        dataManager.save(ctx);
    }

    private static String policyKey(Object type, Object resource, Object action, Object effect, Object group) {
        return (Objects.toString(type, "") + "|" +
                Objects.toString(resource, "") + "|" +
                Objects.toString(action, "") + "|" +
                Objects.toString(effect, "") + "|" +
                Objects.toString(group, ""));
    }

    private List<ResourceRoleModel> loadChildRoleModels(ResourceRoleModel edited) {
        if (edited.getChildRoles() == null || edited.getChildRoles().isEmpty())
            return Collections.emptyList();

        return edited.getChildRoles().stream()
                .map(roleRepository::findRoleByCode)
                .filter(Objects::nonNull)
                .map(roleModelConverter::createResourceRoleModel)
                .collect(Collectors.toList());
    }
}
