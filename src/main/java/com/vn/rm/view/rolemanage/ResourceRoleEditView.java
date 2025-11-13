package com.vn.rm.view.rolemanage;

import com.google.common.base.Strings;
import com.vaadin.flow.router.Route;
import com.vn.rm.view.main.MainView;
import com.vn.rm.view.rolemanage.entityfragment.EntitiesFragment;
import com.vn.rm.view.rolemanage.userinterfacefragment.UserInterfaceFragment;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
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

    @ViewComponent
    private EntitiesFragment entitiesFragment;

    @ViewComponent
    private UserInterfaceFragment userInterfaceFragment;

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

    @Override
    protected String getRouteParamName() {
        return "code";
    }

    @Subscribe
    public void onInit(InitEvent event) {
        scopesField.setItems(Arrays.asList(SecurityScope.UI, SecurityScope.API));
    }

    // ⭐⭐ FIX CHÍNH: thay onReady bằng BeforeShow ⭐⭐
    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        ResourceRoleModel model = roleModelDc.getItemOrNull();
        if (model == null)
            return;

        if (entitiesFragment != null)
            entitiesFragment.reloadFromPolicies();

        if (userInterfaceFragment != null)
            userInterfaceFragment.initUi(model);
    }

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

        // Load DB role
        ResourceRoleEntity roleEntity = dataManager.load(ResourceRoleEntity.class)
                .query("select r from sec_ResourceRoleEntity r left join fetch r.resourcePolicies where r.code = :code")
                .parameter("code", code)
                .optional()
                .orElse(null);

        ResourceRoleModel model;

        if (roleEntity != null) {
            model = mapDbRoleToModel(roleEntity);
        } else {
            // Load annotated
            ResourceRole annotated = roleRepository.findRoleByCode(code);

            if (annotated == null) {
                close(StandardOutcome.CLOSE);
                return;
            }

            model = roleModelConverter.createResourceRoleModel(annotated);

            // annotated role luôn dùng CODE
            model.setSource(RoleSourceType.ANNOTATED_CLASS);

            if (model.getResourcePolicies() == null)
                model.setResourcePolicies(new ArrayList<>());
        }

        // Child roles
        childRolesDc.setItems(loadChildRoleModels(model));

        // Merge vào context
        ResourceRoleModel merged = dataContext.merge(model);
        roleModelDc.setItem(merged);

        // Resource policies
        resourcePoliciesDc.setItems(new ArrayList<>(merged.getResourcePolicies()));
    }

    // SAVE ACTION

    @Subscribe("saveAction")
    public void onSaveAction(ActionPerformedEvent event) {
        ResourceRoleModel model = roleModelDc.getItem();
        if (model == null) return;

        List<ResourcePolicyModel> allPolicies = new ArrayList<>();

        if (entitiesFragment != null)
            allPolicies.addAll(entitiesFragment.buildPoliciesFromMatrix());

        if (userInterfaceFragment != null) {
            allPolicies.addAll(userInterfaceFragment.collectPoliciesFromTree());

            if (userInterfaceFragment.isAllowAllViewsChecked()) {
                ResourcePolicyModel all = new ResourcePolicyModel();
                all.setId(UUID.randomUUID());
                all.setType("VIEW");
                all.setResource("*");
                all.setAction("view");
                all.setEffect("ALLOW");
                allPolicies.add(all);
            }
        }

        model.setResourcePolicies(allPolicies);
        persistRoleToDb(model);
        close(StandardOutcome.SAVE);
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    public void onBeforeSave(DataContext.PreSaveEvent event) {
        ResourceRoleModel model = roleModelDc.getItem();
        if (model == null) return;

        List<ResourcePolicyModel> allPolicies = new ArrayList<>();

        if (entitiesFragment != null)
            allPolicies.addAll(entitiesFragment.buildPoliciesFromMatrix());

        if (userInterfaceFragment != null) {
            allPolicies.addAll(userInterfaceFragment.collectPoliciesFromTree());

            if (userInterfaceFragment.isAllowAllViewsChecked()) {
                ResourcePolicyModel all = new ResourcePolicyModel();
                all.setId(UUID.randomUUID());
                all.setType("VIEW");
                all.setResource("*");
                all.setAction("view");
                all.setEffect("ALLOW");
                allPolicies.add(all);
            }
        }

        model.setResourcePolicies(allPolicies);
    }

    // MAPPING + SAVE...

    private ResourceRoleModel mapDbRoleToModel(ResourceRoleEntity roleEntity) {
        ResourceRoleModel m = metadata.create(ResourceRoleModel.class);
        m.setCode(roleEntity.getCode());
        m.setName(roleEntity.getName());
        m.setDescription(roleEntity.getDescription());
        m.setScopes(roleEntity.getScopes() == null ? Set.of() : new HashSet<>(roleEntity.getScopes()));
        m.setSource(RoleSourceType.DATABASE);

        List<ResourcePolicyModel> ps = new ArrayList<>();

        if (roleEntity.getResourcePolicies() != null) {
            for (ResourcePolicyEntity pe : roleEntity.getResourcePolicies()) {
                ResourcePolicyModel p = metadata.create(ResourcePolicyModel.class);
                p.setType(pe.getType());
                p.setResource(pe.getResource());
                p.setAction(pe.getAction());
                p.setEffect(pe.getEffect());
                p.setPolicyGroup(pe.getPolicyGroup());
                ps.add(p);
            }
        }

        m.setResourcePolicies(ps);
        return m;
    }

    private void persistRoleToDb(ResourceRoleModel model) {
        String code = model.getCode();
        if (Strings.isNullOrEmpty(code))
            throw new IllegalStateException("Code không được rỗng.");

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
        if (role.getResourcePolicies() != null) {
            for (ResourcePolicyEntity pe : role.getResourcePolicies()) {
                existing.put(policyKey(pe.getType(), pe.getResource(), pe.getAction(), pe.getEffect(), pe.getPolicyGroup()), pe);
            }
        } else {
            role.setResourcePolicies(new ArrayList<>());
        }

        Set<String> keepKeys = new HashSet<>();
        List<ResourcePolicyEntity> toPersist = new ArrayList<>();

        for (ResourcePolicyModel p : model.getResourcePolicies()) {
            String key = policyKey(p.getType(), p.getResource(), p.getAction(), p.getEffect(), p.getPolicyGroup());
            keepKeys.add(key);

            ResourcePolicyEntity pe =
                    existing.getOrDefault(key, dataManager.create(ResourcePolicyEntity.class));

            pe.setRole(role);
            pe.setType(p.getType());
            pe.setResource(p.getResource());
            pe.setAction(p.getAction());
            pe.setEffect(p.getEffect());
            pe.setPolicyGroup(p.getPolicyGroup());

            if (!role.getResourcePolicies().contains(pe))
                role.getResourcePolicies().add(pe);

            toPersist.add(pe);
        }

        List<ResourcePolicyEntity> toRemove = role.getResourcePolicies().stream()
                .filter(pe -> !keepKeys.contains(policyKey(pe.getType(), pe.getResource(), pe.getAction(), pe.getEffect(), pe.getPolicyGroup())))
                .toList();

        role.getResourcePolicies().removeIf(toRemove::contains);

        SaveContext ctx = new SaveContext();
        ctx.saving(role);
        toPersist.forEach(ctx::saving);
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
        if (edited.getChildRoles() == null)
            return Collections.emptyList();

        return edited.getChildRoles().stream()
                .map(roleRepository::findRoleByCode)
                .filter(Objects::nonNull)
                .map(roleModelConverter::createResourceRoleModel)
                .collect(Collectors.toList());
    }
}
