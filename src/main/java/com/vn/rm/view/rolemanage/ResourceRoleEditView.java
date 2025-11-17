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

    @ViewComponent
    private EntitiesFragment entitiesFragment;

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
        // scopes cho role
        scopesField.setItems(Arrays.asList(SecurityScope.UI, SecurityScope.API));
    }

    /**
     * BeforeShow: lúc này model đã được load (initExistingEntity đã chạy),
     * fragment cũng đã được khởi tạo, nên ta đẩy policies vào fragment.
     */
    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        ResourceRoleModel model = roleModelDc.getItemOrNull();
        if (model == null) {
            return;
        }

        // Đẩy toàn bộ resourcePolicies hiện tại vào fragment Entities (entity + attribute matrix)
        if (entitiesFragment != null) {
            List<ResourcePolicyModel> policies =
                    Optional.ofNullable(model.getResourcePolicies())
                            .map(ArrayList::new)
                            .orElseGet(ArrayList::new);
            entitiesFragment.initPolicies(policies);
        }

        // Khởi tạo tab User Interface (tree view / menu)
        if (userInterfaceFragment != null) {
            userInterfaceFragment.initUi(model);
        }
    }

    // ============================ Load entity ============================

    @Override
    protected void initExistingEntity(String serializedEntityCode) {
        String code = null;
        try {
            code = urlParamSerializer.deserialize(String.class, serializedEntityCode);
        } catch (Exception ignore) {
        }

        if (Strings.isNullOrEmpty(code)) {
            close(StandardOutcome.CLOSE);
            return;
        }

        // 1) Thử load role từ DB
        ResourceRoleEntity roleEntity = dataManager.load(ResourceRoleEntity.class)
                .query("select r from sec_ResourceRoleEntity r left join fetch r.resourcePolicies where r.code = :code")
                .parameter("code", code)
                .optional()
                .orElse(null);

        ResourceRoleModel model;

        if (roleEntity != null) {
            // Role từ database
            model = mapDbRoleToModel(roleEntity);
        } else {
            // 2) Nếu không có trong DB thì thử annotated role
            ResourceRole annotated = roleRepository.findRoleByCode(code);
            if (annotated == null) {
                close(StandardOutcome.CLOSE);
                return;
            }

            model = roleModelConverter.createResourceRoleModel(annotated);
            model.setSource(RoleSourceType.ANNOTATED_CLASS);

            if (model.getResourcePolicies() == null) {
                model.setResourcePolicies(new ArrayList<>());
            }
        }

        // Child roles
        childRolesDc.setItems(loadChildRoleModels(model));

        // Merge vào DataContext, gán vào container
        ResourceRoleModel merged = dataContext.merge(model);
        roleModelDc.setItem(merged);

        // Resource policies cho view (nếu cần hiển thị ở đâu đó)
        resourcePoliciesDc.setItems(
                Optional.ofNullable(merged.getResourcePolicies())
                        .map(ArrayList::new)
                        .orElseGet(ArrayList::new)
        );
    }

    // ============================== SAVE ==============================

    @Subscribe("saveAction")
    public void onSaveAction(ActionPerformedEvent event) {
        ResourceRoleModel model = roleModelDc.getItem();
        if (model == null) {
            return;
        }

        List<ResourcePolicyModel> allPolicies = collectAllPoliciesFromFragments(model);
        model.setResourcePolicies(allPolicies);

        // Đảm bảo có quyền login UI
        ensureLoginToUiPolicy(model);

        persistRoleToDb(model);
        close(StandardOutcome.SAVE);
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    public void onBeforeSave(DataContext.PreSaveEvent event) {
        ResourceRoleModel model = roleModelDc.getItem();
        if (model == null) {
            return;
        }

        List<ResourcePolicyModel> allPolicies = collectAllPoliciesFromFragments(model);
        model.setResourcePolicies(allPolicies);

        // Đảm bảo có quyền login UI
        ensureLoginToUiPolicy(model);
    }

    /**
     * Gom policy từ 2 fragment: EntitiesFragment + UserInterfaceFragment
     */
    private List<ResourcePolicyModel> collectAllPoliciesFromFragments(ResourceRoleModel model) {
        List<ResourcePolicyModel> allPolicies = new ArrayList<>();

        // ENTITY + ATTRIBUTE
        if (entitiesFragment != null) {
            allPolicies.addAll(entitiesFragment.buildPoliciesFromMatrix());
        }

        // VIEW + MENU
        if (userInterfaceFragment != null) {
            allPolicies.addAll(userInterfaceFragment.collectPoliciesFromTree());

            if (userInterfaceFragment.isAllowAllViewsChecked()) {
                ResourcePolicyModel all = metadata.create(ResourcePolicyModel.class);
                all.setType("VIEW");        // hoặc ResourcePolicyType.SPECIFIC nếu bạn dùng enum
                all.setResource("*");
                all.setAction("view");
                all.setEffect("ALLOW");
                allPolicies.add(all);
            }
        }

        return allPolicies;
    }

    /**
     * Đảm bảo role này có specific policy cho 'ui.loginToUi' để user login được vào UI.
     */
    private void ensureLoginToUiPolicy(ResourceRoleModel model) {
        List<ResourcePolicyModel> policies = new ArrayList<>(
                Optional.ofNullable(model.getResourcePolicies())
                        .orElseGet(Collections::emptyList)
        );

        boolean hasLoginToUi = policies.stream().anyMatch(p ->
                p.getType() == ResourcePolicyType.SPECIFIC
                        && "ui.loginToUi".equals(p.getResource())
                        && (p.getEffect() == null || p.getEffect() == ResourcePolicyEffect.ALLOW));

        if (!hasLoginToUi) {
            ResourcePolicyModel p = metadata.create(ResourcePolicyModel.class);
            p.setType(ResourcePolicyType.SPECIFIC);
            p.setResource("ui.loginToUi");
            p.setAction(null); // nếu bạn dùng action khác thì set ở đây
            p.setEffect(ResourcePolicyEffect.ALLOW);
            p.setPolicyGroup("specific");
            policies.add(p);
        }

        model.setResourcePolicies(policies);
    }


    // ======================= Mapping & persistence =======================

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
        if (role.getResourcePolicies() != null) {
            for (ResourcePolicyEntity pe : role.getResourcePolicies()) {
                existing.put(policyKey(pe.getType(), pe.getResource(), pe.getAction(), pe.getEffect(), pe.getPolicyGroup()), pe);
            }
        } else {
            role.setResourcePolicies(new ArrayList<>());
        }

        Set<String> keepKeys = new HashSet<>();
        List<ResourcePolicyEntity> toPersist = new ArrayList<>();

        for (ResourcePolicyModel p : Optional.ofNullable(model.getResourcePolicies()).orElseGet(List::of)) {
            String key = policyKey(p.getType(), p.getResource(), p.getAction(), p.getEffect(), p.getPolicyGroup());
            keepKeys.add(key);

            ResourcePolicyEntity pe = existing.get(key);
            if (pe == null) {
                pe = dataManager.create(ResourcePolicyEntity.class);
                pe.setRole(role);
                role.getResourcePolicies().add(pe);
            }

            pe.setType(p.getType());
            pe.setResource(p.getResource());
            pe.setAction(p.getAction());
            pe.setEffect(p.getEffect());
            pe.setPolicyGroup(p.getPolicyGroup());

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
        if (edited.getChildRoles() == null || edited.getChildRoles().isEmpty()) {
            return Collections.emptyList();
        }

        return edited.getChildRoles().stream()
                .map(roleRepository::findRoleByCode)
                .filter(Objects::nonNull)
                .map(roleModelConverter::createResourceRoleModel)
                .collect(Collectors.toList());
    }
}
