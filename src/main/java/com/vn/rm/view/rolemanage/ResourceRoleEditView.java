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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.menu.MenuItem;
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

import io.jmix.securityflowui.view.resourcepolicy.ResourcePolicyViewUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.vn.rm.entity.PolicyGroupNode;
import com.vn.rm.view.main.MainView;

import java.util.*;


@Route(value = "sec/resource-role-edit-view/:code", layout = MainView.class)
@ViewController("rm_ResourceRoleEditView")
@ViewDescriptor("resource-role-edit-view.xml")
@EditedEntityContainer("roleModelDc")

public class ResourceRoleEditView extends StandardDetailView<ResourceRoleModel> {

    // ============================= UI components =============================
    @ViewComponent
    private io.jmix.flowui.component.textfield.TypedTextField<String> codeField;

    @ViewComponent
    private io.jmix.flowui.component.textfield.TypedTextField<String> nameField;

    @ViewComponent
    private io.jmix.flowui.component.textarea.JmixTextArea descriptionField;



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

    @Subscribe
    public void onInitEntity(InitEntityEvent<ResourceRoleModel> event) {
        ResourceRoleModel model = event.getEntity();

        model.setSource(RoleSourceType.DATABASE);
        model.setResourcePolicies(new ArrayList<>());

        nameField.setReadOnly(false);
        codeField.setReadOnly(false);
        descriptionField.setReadOnly(false);
        scopesField.setReadOnly(false);
    }

    @Override
    protected String getRouteParamName() {
        return "code";
    }

    @Subscribe
    public void onInit(InitEvent event) {
        scopesField.setItems(Arrays.asList(SecurityScope.UI, SecurityScope.API));
    }

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

        ResourceRoleModel merged = dataContext.merge(model);
        roleModelDc.setItem(merged);
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

        // Gom hết policies từ 2 fragment
        List<ResourcePolicyModel> allPolicies = collectAllPoliciesFromFragments(model);
        model.setResourcePolicies(allPolicies);

        // Lưu xuống DB - vừa dùng cho tạo mới, vừa dùng cho sửa
        persistRoleToDb(model);

        close(StandardOutcome.SAVE);
    }



    private List<ResourcePolicyModel> collectAllPoliciesFromFragments(ResourceRoleModel model) {
        List<ResourcePolicyModel> allPolicies = new ArrayList<>();

        if (entitiesFragment != null) {
            allPolicies.addAll(entitiesFragment.buildPoliciesFromMatrix());
        }

        if (userInterfaceFragment != null) {
            allPolicies.addAll(userInterfaceFragment.collectPoliciesFromTree());

            if (userInterfaceFragment.isAllowAllViewsChecked()) {
                ResourcePolicyModel all = metadata.create(ResourcePolicyModel.class);
                all.setType("VIEW");
                all.setResource("*");
                all.setAction("view");
                all.setEffect("ALLOW");
                allPolicies.add(all);
            }

        }

        return allPolicies;
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

public class ResourceRoleEditView extends StandardDetailView<ResourceRoleModel> implements BeforeEnterObserver {

    @Autowired
    private ResourceRoleRepository roleRepository;
    @Autowired
    private RoleModelConverter roleModelConverter;
    @Autowired
    private Notifications notifications;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private ResourcePolicyViewUtils resourcePolicyViewUtils;
    @Autowired
    private ViewRegistry viewRegistry;
    @Autowired
    private MenuConfig menuConfig;
    @PersistenceContext
    private EntityManager entityManager;

    @ViewComponent
    private InstanceContainer<ResourceRoleModel> roleModelDc;
    @ViewComponent
    private DataContext dataContext;
    @ViewComponent
    private CollectionContainer<PolicyGroupNode> policyTreeDc;
    @ViewComponent
    private TreeDataGrid<PolicyGroupNode> policyTreeGrid;

    @ViewComponent
    private Checkbox showAssignedOnly;
    @ViewComponent
    private Checkbox allowAllViews;
    @ViewComponent
    private Button saveBtn;

    // ====================================================================================
    // BEFORE ENTER
    // ====================================================================================
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String code = event.getRouteParameters().get("code").orElse(null);
        if (code == null) {
            notifications.create("Không có route parameter 'code'").show();
            return;
        }

        Optional<ResourceRoleEntity> dbEntityOpt = dataManager.load(ResourceRoleEntity.class)
                .all()
                .list()
                .stream()
                .filter(r -> code.equals(r.getCode()))
                .findFirst();

        ResourceRoleModel model = dbEntityOpt.map(this::convertDbEntityToModel)
                .orElseGet(() -> {
                    ResourceRole annotated = roleRepository.findRoleByCode(code);
                    if (annotated != null) {
                        ResourceRoleModel m = roleModelConverter.createResourceRoleModel(annotated);
                        m.setSource(RoleSourceType.DATABASE);
                        return m;
                    }
                    notifications.create("Không tìm thấy role có code: " + code).show();
                    return null;
                });

        if (model == null) return;

        ResourceRoleModel merged = dataContext.merge(model);
        roleModelDc.setItem(merged);

        buildTree(model);
        setupTreeGrid(model.getSource().name());

        boolean hasAllowAll = model.getResourcePolicies().stream()
                .anyMatch(p -> "VIEW".equalsIgnoreCase(p.getType())
                        && "*".equals(p.getResource())
                        && "ALLOW".equalsIgnoreCase(p.getEffect()));
        allowAllViews.setValue(hasAllowAll);

        showAssignedOnly.addValueChangeListener(e -> refreshTreeWithFilter());

        allowAllViews.addValueChangeListener(e -> {
            if (Boolean.TRUE.equals(e.getValue())) {
                toggleAll(true);
                notifications.create("All views allowed").show();
            } else {
                toggleAll(false);
                notifications.create("Reset all permissions to DENY").show();
            }
        });

        saveBtn.addClickListener(this::onSaveClick);
    }

    // Convert DB entity → Model
    private ResourceRoleModel convertDbEntityToModel(ResourceRoleEntity dbEntity) {
        ResourceRoleModel model = new ResourceRoleModel();
        model.setId(dbEntity.getId());
        model.setCode(dbEntity.getCode());
        model.setName(dbEntity.getName());
        model.setDescription(dbEntity.getDescription());
        model.setSource(RoleSourceType.DATABASE);

        List<ResourcePolicyModel> dbPolicies = new ArrayList<>();
        for (ResourcePolicyEntity p : dbEntity.getResourcePolicies()) {
            ResourcePolicyModel pm = new ResourcePolicyModel();
            pm.setId(UUID.randomUUID());
            pm.setType(p.getType());
            pm.setResource(p.getResource());
            pm.setAction(p.getAction());
            pm.setEffect(p.getEffect());
            dbPolicies.add(pm);
        }
        model.setResourcePolicies(dbPolicies);
        return model;
    }

    // ====================================================================================
    // SAVE
    // ====================================================================================
    private void onSaveClick(ClickEvent<Button> event) {
        try {
            saveToDatabase();
            notifications.create("Đã lưu quyền thành công!").show();
        } catch (Exception e) {
            e.printStackTrace();
            notifications.create("Lỗi khi lưu: " + e.getMessage()).show();
        }
    }

    private void saveToDatabase() {
        ResourceRoleModel role = roleModelDc.getItem();
        if (role == null) return;

        List<ResourcePolicyModel> newPolicies = collectPoliciesFromTree();

        ResourceRoleEntity entity = dataManager.load(ResourceRoleEntity.class)
                .query("select r from sec_ResourceRoleEntity r where r.code = :code")
                .parameter("code", role.getCode())
                .optional()
                .orElse(null);

        if (entity == null) {
            notifications.create("Không tìm thấy role trong database để cập nhật").show();
            return;
        }

        entity.setName(role.getName());
        entity.setDescription(role.getDescription());

        Map<String, ResourcePolicyEntity> existingMap = new HashMap<>();
        for (ResourcePolicyEntity old : entity.getResourcePolicies()) {
            String key = (old.getType() + ":" + old.getResource()).toLowerCase();
            existingMap.put(key, old);
        }

        List<ResourcePolicyEntity> updatedList = new ArrayList<>();

        for (ResourcePolicyModel p : newPolicies) {
            String key = (p.getType() + ":" + p.getResource()).toLowerCase();
            ResourcePolicyEntity existing = existingMap.remove(key);

            if (existing != null) {
                existing.setEffect(p.getEffect());
                existing.setAction(p.getAction());
                updatedList.add(existing);
            } else {
                ResourcePolicyEntity created = dataManager.create(ResourcePolicyEntity.class);
                created.setType(p.getType());
                created.setResource(p.getResource());
                created.setAction(p.getAction());
                created.setEffect(p.getEffect());
                created.setRole(entity);
                updatedList.add(created);
            }
        }

        for (ResourcePolicyEntity removed : existingMap.values()) {
            dataManager.remove(removed);
        }
        entity.setResourcePolicies(updatedList);

        if (Boolean.TRUE.equals(allowAllViews.getValue())) {
            boolean hasAll = updatedList.stream()
                    .anyMatch(p -> "*".equals(p.getResource()) && "ALLOW".equalsIgnoreCase(p.getEffect()));
            if (!hasAll) {
                ResourcePolicyEntity all = dataManager.create(ResourcePolicyEntity.class);
                all.setType("VIEW");
                all.setResource("*");
                all.setAction("view");
                all.setEffect("ALLOW");
                all.setRole(entity);
                updatedList.add(all);
            }
        } else {
            updatedList.removeIf(p -> "*".equals(p.getResource()));
        }

        dataManager.save(entity);
    }

    // Collect policies from tree
    private List<ResourcePolicyModel> collectPoliciesFromTree() {
        List<ResourcePolicyModel> list = new ArrayList<>();
        for (PolicyGroupNode root : policyTreeDc.getItems())
            collectPoliciesRecursive(root, list);
        return list;
    }

    private void collectPoliciesRecursive(PolicyGroupNode node, List<ResourcePolicyModel> list) {
        if (!Boolean.TRUE.equals(node.getGroup())) {
            ResourcePolicyModel p = new ResourcePolicyModel();
            p.setId(UUID.randomUUID());
            p.setType(node.getType());
            p.setResource(node.getResource());
            p.setAction(node.getAction());
            p.setEffect(node.getEffect() != null ? node.getEffect() : "DENY");
            list.add(p);
        }
        for (PolicyGroupNode child : node.getChildren())
            collectPoliciesRecursive(child, list);
    }

    // ====================================================================================
    // ⭐⭐⭐ BUILD TREE (Views + Menu + Compress folder)
    // ====================================================================================
    private void buildTree(ResourceRoleModel model) {

        PolicyGroupNode viewRoot = new PolicyGroupNode("View Access", true);
        PolicyGroupNode menuRoot = new PolicyGroupNode("Menu Access", true);

        // ⭐ Build VIEW TREE (packages)
        buildViewsTree(viewRoot);

        // ⭐ NÉN các folder 1-child → com.vn.rm.view
        viewRoot = compressSingleChildFolders(viewRoot);

        // ⭐ Build menu tree
        for (MenuItem root : menuConfig.getRootItems()) {
            PolicyGroupNode rootNode = new PolicyGroupNode(root.getId(), true);
            rootNode.setType("MENU");
            rootNode.setParent(menuRoot);
            menuRoot.getChildren().add(rootNode);
            buildMenuSubTree(rootNode, root);
        }

        // Map view/menu id to nodes
        Map<String, PolicyGroupNode> allLeafs = new HashMap<>();
        collectLeafNodes(viewRoot, allLeafs);
        collectLeafNodes(menuRoot, allLeafs);

        // Apply effect from DB
        for (ResourcePolicyModel policy : model.getResourcePolicies()) {
            PolicyGroupNode node = allLeafs.get(policy.getResource());
            if (node != null) {
                node.setEffect(policy.getEffect());
                node.setAllow("ALLOW".equalsIgnoreCase(policy.getEffect()));
                node.setDeny("DENY".equalsIgnoreCase(policy.getEffect()));
            }
        }

        List<PolicyGroupNode> roots = Arrays.asList(viewRoot, menuRoot);
        policyTreeDc.setItems(roots);
        policyTreeGrid.setItems(roots, PolicyGroupNode::getChildren);
    }

    // ====================================================================================
    // ⭐ BUILD VIEWS TREE (theo package)
    // ====================================================================================
    private void buildViewsTree(PolicyGroupNode root) {
        Map<String, String> views = resourcePolicyViewUtils.getViewsOptionsMap(false);

        Map<String, String> idToClass = new HashMap<>();
        viewRegistry.getViewInfos().forEach(info -> {
            if (info.getId() != null && info.getControllerClass() != null)
                idToClass.put(info.getId(), info.getControllerClass().getName());
        });

        for (String viewId : views.keySet()) {
            String className = idToClass.getOrDefault(viewId, viewId);
            String[] parts = className.split("\\.");

            PolicyGroupNode cur = root;

            for (int i = 0; i < parts.length - 1; i++) {
                String part = parts[i];

                PolicyGroupNode folder = cur.getChildren().stream()
                        .filter(c -> Boolean.TRUE.equals(c.getGroup()) && c.getName().equals(part))
                        .findFirst()
                        .orElse(null);

                if (folder == null) {
                    folder = new PolicyGroupNode(part, true);
                    folder.setParent(cur);
                    cur.getChildren().add(folder);
                }

                cur = folder;
            }

            String name = parts[parts.length - 1];
            PolicyGroupNode leaf = new PolicyGroupNode(name, false);
            leaf.setResource(viewId);
            leaf.setAction("view");
            leaf.setType("VIEW");
            leaf.setParent(cur);
            cur.getChildren().add(leaf);
        }
    }

    // ====================================================================================
    // ⭐ COMPRESS FOLDERS (gom com → com.vn → com.vn.rm → com.vn.rm.view)
    // ====================================================================================
    private PolicyGroupNode compressSingleChildFolders(PolicyGroupNode node) {
        if (!Boolean.TRUE.equals(node.getGroup())) return node;

        List<PolicyGroupNode> newChildren = new ArrayList<>();
        for (PolicyGroupNode c : node.getChildren()) {
            newChildren.add(compressSingleChildFolders(c));
        }
        node.setChildren(newChildren);

        if (node.getChildren().size() == 1 && node.getChildren().get(0).getGroup()) {
            PolicyGroupNode only = node.getChildren().get(0);

            if (!node.getName().equals("View Access")) {
                only.setName(node.getName() + "." + only.getName());
            }

            return only;
        }

        return node;
    }

    // ====================================================================================
    // MENU TREE BUILDER
    // ====================================================================================
    private void buildMenuSubTree(PolicyGroupNode parentNode, MenuItem menuItem) {
        for (MenuItem child : menuItem.getChildren()) {
            boolean hasChildren = !child.getChildren().isEmpty();
            boolean isGroup = (child.getView() == null && hasChildren);

            PolicyGroupNode node = new PolicyGroupNode(child.getId(), isGroup);
            node.setType("MENU");
            node.setResource(child.getId());
            node.setAction("view");
            node.setParent(parentNode);

            if (child.getView() != null) {
                node.setGroup(false);
            }

            parentNode.getChildren().add(node);

            if (hasChildren) {
                buildMenuSubTree(node, child);
            }
        }
    }

    private void collectLeafNodes(PolicyGroupNode node, Map<String, PolicyGroupNode> map) {
        if (!Boolean.TRUE.equals(node.getGroup()) && node.getResource() != null)
            map.put(node.getResource(), node);

        for (PolicyGroupNode child : node.getChildren())
            collectLeafNodes(child, map);
    }

    // ====================================================================================
    // TREE GRID UI
    // ====================================================================================
    private void setupTreeGrid(String source) {
        policyTreeGrid.removeAllColumns();
        boolean editable = "DATABASE".equalsIgnoreCase(source);

        policyTreeGrid.addHierarchyColumn(PolicyGroupNode::getName)
                .setHeader("Policy Group / Resource");

        policyTreeGrid.addColumn(PolicyGroupNode::getType).setHeader("Type");
        policyTreeGrid.addColumn(PolicyGroupNode::getAction).setHeader("Action");
        policyTreeGrid.addColumn(PolicyGroupNode::getEffect).setHeader("Effect");

        policyTreeGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (allow, node) -> {
            allow.setVisible(!Boolean.TRUE.equals(node.getGroup()));
            allow.setValue(Boolean.TRUE.equals(node.getAllow()));
            allow.setEnabled(editable);
            allow.addValueChangeListener(e -> {
                boolean value = e.getValue();
                node.setAllow(value);
                node.setDeny(!value);
                node.setEffect(value ? "ALLOW" : "DENY");
                policyTreeGrid.getDataProvider().refreshItem(node);
            });
        })).setHeader("Allow");

        policyTreeGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (deny, node) -> {
            deny.setVisible(!Boolean.TRUE.equals(node.getGroup()));
            deny.setValue(Boolean.TRUE.equals(node.getDeny()));
            deny.setEnabled(editable);
            deny.addValueChangeListener(e -> {
                boolean value = e.getValue();
                node.setDeny(value);
                node.setAllow(!value);
                node.setEffect(value ? "DENY" : "ALLOW");
                policyTreeGrid.getDataProvider().refreshItem(node);
            });
        })).setHeader("Deny");
    }

    // ====================================================================================
    // FILTER
    // ====================================================================================
    private void refreshTreeWithFilter() {
        boolean onlyAssigned = Boolean.TRUE.equals(showAssignedOnly.getValue());
        if (onlyAssigned) {
            List<PolicyGroupNode> filteredRoots = new ArrayList<>();
            for (PolicyGroupNode root : policyTreeDc.getItems()) {
                PolicyGroupNode clone = filterAssignedRecursive(root);
                if (clone != null) filteredRoots.add(clone);
            }
            policyTreeGrid.setItems(filteredRoots, PolicyGroupNode::getChildren);
        } else {
            policyTreeGrid.setItems(policyTreeDc.getItems(), PolicyGroupNode::getChildren);
        }
        policyTreeGrid.getDataProvider().refreshAll();
    }

    private PolicyGroupNode filterAssignedRecursive(PolicyGroupNode node) {
        if (!Boolean.TRUE.equals(node.getGroup()))
            return (node.getEffect() != null) ? node : null;

        List<PolicyGroupNode> filteredChildren = new ArrayList<>();
        for (PolicyGroupNode child : node.getChildren()) {
            PolicyGroupNode f = filterAssignedRecursive(child);
            if (f != null) filteredChildren.add(f);
        }
        if (!filteredChildren.isEmpty()) {
            PolicyGroupNode copy = new PolicyGroupNode(node.getName(), true);
            copy.getChildren().addAll(filteredChildren);
            return copy;
        }
        return null;
    }

    // ====================================================================================
    // ALLOW ALL / DENY ALL
    // ====================================================================================
    private void toggleAll(Boolean allow) {
        for (PolicyGroupNode root : policyTreeDc.getItems()) {
            applyToChildrenRecursive(root, allow);
        }
        policyTreeGrid.getDataProvider().refreshAll();
    }

    private void applyToChildrenRecursive(PolicyGroupNode node, Boolean allow) {
        if (!Boolean.TRUE.equals(node.getGroup())) {
            node.setAllow(Boolean.TRUE.equals(allow));
            node.setDeny(!Boolean.TRUE.equals(allow));
            node.setEffect(Boolean.TRUE.equals(allow) ? "ALLOW" : "DENY");
        }
        for (PolicyGroupNode child : node.getChildren()) {
            applyToChildrenRecursive(child, allow);
        }
    }

