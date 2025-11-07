package com.vn.rm.view.rolemanage;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.Notifications;

import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.menu.MenuItem;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.jmix.security.model.*;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.securitydata.entity.ResourcePolicyEntity;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import io.jmix.securityflowui.view.resourcepolicy.ResourcePolicyViewUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.vn.rm.entity.PolicyGroupNode;
import com.vn.rm.view.main.MainView;

import java.util.*;

@Route(value = "sec/resource-role-edit-view/:code", layout = MainView.class)
@ViewController("rm_ResourceRoleEditView")
@ViewDescriptor("resource-role-edit-view.xml")
@EditedEntityContainer("roleModelDc")
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

    @ViewComponent
    private InstanceContainer<ResourceRoleModel> roleModelDc;
    @ViewComponent
    private DataContext dataContext;
    @ViewComponent
    private CollectionContainer<PolicyGroupNode> policyTreeDc;
    @ViewComponent
    private TreeDataGrid<PolicyGroupNode> policyTreeGrid;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String code = event.getRouteParameters().get("code").orElse(null);
        if (code == null) {
            notifications.create(" Không có route parameter 'code'").show();
            return;
        }

        ResourceRole annotatedRole = roleRepository.findRoleByCode(code);

        Optional<ResourceRoleEntity> dbEntityOpt = dataManager.load(ResourceRoleEntity.class)
                .all()
                .list()
                .stream()
                .filter(r -> code.equals(r.getCode()))
                .findFirst();

        ResourceRoleModel model = mergeAnnotatedAndDbRole(annotatedRole, dbEntityOpt, code);
        if (model == null) return;

        if (model.getResourcePolicies() != null) {
            for (ResourcePolicyModel p : model.getResourcePolicies()) {
                if (p.getId() == null) p.setId(UUID.randomUUID());
            }
        }

        ResourceRoleModel merged = dataContext.merge(model);
        roleModelDc.setItem(merged);

        buildTree(model);
        setupTreeGrid(model.getSource().name());

        getUI().ifPresent(ui -> ui.access(() -> {
            policyTreeGrid.getDataProvider().refreshAll();
            policyTreeGrid.expandRecursively(policyTreeDc.getItems(), 4);
            policyTreeGrid.getElement().callJsFunction("requestContentUpdate");
        }));
    }

    /** ✅ Merge annotated + DB role */
    private ResourceRoleModel mergeAnnotatedAndDbRole(ResourceRole annotatedRole, Optional<ResourceRoleEntity> dbEntityOpt, String code) {
        if (annotatedRole == null && dbEntityOpt.isEmpty()) {
            notifications.create(" Không tìm thấy role có code: " + code).show();
            return null;
        }

        ResourceRoleModel model;

        if (annotatedRole != null && dbEntityOpt.isPresent()) {
            ResourceRoleModel annotatedModel = roleModelConverter.createResourceRoleModel(annotatedRole);
            ResourceRoleEntity dbEntity = dbEntityOpt.get();

            Map<String, ResourcePolicyModel> merged = new LinkedHashMap<>();
            if (annotatedModel.getResourcePolicies() != null) {
                for (ResourcePolicyModel p : annotatedModel.getResourcePolicies()) {
                    merged.put((p.getType() + ":" + p.getResource()).toLowerCase(), p);
                }
            }
            if (dbEntity.getResourcePolicies() != null) {
                for (ResourcePolicyEntity p : dbEntity.getResourcePolicies()) {
                    String key = (p.getType() + ":" + p.getResource()).toLowerCase();
                    ResourcePolicyModel pm = new ResourcePolicyModel();
                    pm.setId(p.getId());
                    pm.setType(p.getType());
                    pm.setResource(p.getResource());
                    pm.setAction(p.getAction());
                    pm.setEffect(
                            "ALLOW".equalsIgnoreCase(p.getEffect()) ? ResourcePolicyEffect.ALLOW : ResourcePolicyEffect.DENY
                    );
                    merged.put(key, pm);
                }
            }

            annotatedModel.setResourcePolicies(new ArrayList<>(merged.values()));
            model = annotatedModel;
            model.setSource(RoleSourceType.DATABASE);
        } else if (annotatedRole != null) {
            model = roleModelConverter.createResourceRoleModel(annotatedRole);
        } else {
            ResourceRoleEntity dbEntity = dbEntityOpt.get();
            ResourceRoleModel dbModel = new ResourceRoleModel();
            dbModel.setId(dbEntity.getId());
            dbModel.setCode(dbEntity.getCode());
            dbModel.setName(dbEntity.getName());
            dbModel.setDescription(dbEntity.getDescription());
            dbModel.setSource(RoleSourceType.DATABASE);

            List<ResourcePolicyModel> dbPolicies = new ArrayList<>();
            for (ResourcePolicyEntity p : dbEntity.getResourcePolicies()) {
                ResourcePolicyModel pm = new ResourcePolicyModel();
                pm.setId(p.getId());
                pm.setType(p.getType());
                pm.setResource(p.getResource());
                pm.setAction(p.getAction());
                pm.setEffect(
                        "ALLOW".equalsIgnoreCase(p.getEffect()) ? ResourcePolicyEffect.ALLOW : ResourcePolicyEffect.DENY
                );
                dbPolicies.add(pm);
            }
            dbModel.setResourcePolicies(dbPolicies);
            model = dbModel;
        }

        return model;
    }

    /** Build tree View Access + Menu Access */
    private void buildTree(ResourceRoleModel model) {
        System.out.println("=== BUILD TREE (AUTO PACKAGE + MENU) ===");

        PolicyGroupNode viewRoot = new PolicyGroupNode("View Access", true);
        PolicyGroupNode menuRoot = new PolicyGroupNode("Menu Access", true);

        Map<String, String> views = resourcePolicyViewUtils.getViewsOptionsMap(false);

        System.out.println("Views count: " + views.size());

        // 🔹 Lấy id -> class name từ ViewRegistry
        Map<String, String> idToClass = new HashMap<>();
        viewRegistry.getViewInfos().forEach(info -> {
            if (info.getId() != null && info.getControllerClass() != null) {
                idToClass.put(info.getId(), info.getControllerClass().getName());
            }
        });

        // 🔹 Build View Access
        for (String viewId : views.keySet()) {
            if (viewId == null || viewId.isBlank()) continue;
            String className = idToClass.getOrDefault(viewId, viewId);
            String[] parts = className.split("\\.");

            PolicyGroupNode currentParent = viewRoot;
            for (int i = 0; i < parts.length - 1; i++) {
                String part = parts[i];
                PolicyGroupNode existing = currentParent.getChildren().stream()
                        .filter(c -> Boolean.TRUE.equals(c.getGroup()) && c.getName().equals(part))
                        .findFirst()
                        .orElse(null);

                if (existing == null) {
                    PolicyGroupNode folder = new PolicyGroupNode(part, true);
                    folder.setParent(currentParent);
                    currentParent.getChildren().add(folder);
                    currentParent = folder;
                } else {
                    currentParent = existing;
                }
            }

            String simpleName = parts[parts.length - 1];
            PolicyGroupNode leaf = new PolicyGroupNode(simpleName, false);
            leaf.setResource(viewId);
            leaf.setAction("view");
            leaf.setType("VIEW");
            leaf.setParent(currentParent);
            currentParent.getChildren().add(leaf);
        }

        // 🔹 Build Menu Access từ menuConfig
        for (MenuItem root : menuConfig.getRootItems()) {
            PolicyGroupNode rootNode = new PolicyGroupNode(root.getId(), true);
            rootNode.setType("MENU");
            rootNode.setParent(menuRoot);
            menuRoot.getChildren().add(rootNode);
            buildMenuSubTree(rootNode, root);
        }

        // 🔹 Map để đánh dấu quyền Allow/Deny
        Map<String, PolicyGroupNode> allLeafs = new HashMap<>();
        collectLeafNodes(viewRoot, allLeafs);
        collectLeafNodes(menuRoot, allLeafs);

        for (ResourcePolicyModel policy : model.getResourcePolicies()) {
            if (policy.getResource() == null || policy.getType() == null) continue;
            PolicyGroupNode node = allLeafs.get(policy.getResource());
            if (node != null) {
                if (policy.getEffect() == ResourcePolicyEffect.ALLOW) {
                    node.setAllow(true);
                    node.setEffect("ALLOW");
                } else if (policy.getEffect() == ResourcePolicyEffect.DENY) {
                    node.setDeny(true);
                    node.setEffect("DENY");
                }
            }
        }

        // 🔹 Gán vào UI
        List<PolicyGroupNode> roots = Arrays.asList(viewRoot, menuRoot);
        policyTreeDc.setItems(roots);
        policyTreeGrid.setItems(roots, PolicyGroupNode::getChildren);

        System.out.println("=== TREE STRUCTURE ===");
        printTree(viewRoot, "  ");
        printTree(menuRoot, "  ");
    }

    /**  Đệ quy build cây menu thật */
    private void buildMenuSubTree(PolicyGroupNode parentNode, MenuItem menuItem) {
        for (MenuItem child : menuItem.getChildren()) {
            if (child.getChildren().isEmpty()) {
                PolicyGroupNode leaf = new PolicyGroupNode(child.getId(), false);
                leaf.setResource(child.getId());
                leaf.setType("MENU");
                leaf.setAction("view");
                leaf.setParent(parentNode);
                parentNode.getChildren().add(leaf);
            } else {
                PolicyGroupNode folder = new PolicyGroupNode(child.getId(), true);
                folder.setType("MENU");
                folder.setParent(parentNode);
                parentNode.getChildren().add(folder);
                buildMenuSubTree(folder, child);
            }
        }
    }

    private void collectLeafNodes(PolicyGroupNode node, Map<String, PolicyGroupNode> map) {
        if (!Boolean.TRUE.equals(node.getGroup()) && node.getResource() != null) {
            map.put(node.getResource(), node);
        }
        for (PolicyGroupNode child : node.getChildren()) {
            collectLeafNodes(child, map);
        }
    }

    private void printTree(PolicyGroupNode node, String indent) {
        System.out.println(indent + (node.getGroup() ? "[+] " : " - ") + node.getName());
        for (PolicyGroupNode child : node.getChildren()) {
            printTree(child, indent + "  ");
        }
    }

    /**  Setup Tree Grid UI */
    private void setupTreeGrid(String source) {
        policyTreeGrid.removeAllColumns();
        boolean editable = "DATABASE".equalsIgnoreCase(source);

        policyTreeGrid.addHierarchyColumn(PolicyGroupNode::getName)
                .setHeader("Policy Group / Resource").setAutoWidth(true);
        policyTreeGrid.addColumn(PolicyGroupNode::getType).setHeader("Type").setAutoWidth(true);
        policyTreeGrid.addColumn(PolicyGroupNode::getAction).setHeader("Action").setAutoWidth(true);
        policyTreeGrid.addColumn(PolicyGroupNode::getEffect).setHeader("Effect").setAutoWidth(true);

        policyTreeGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (allow, node) -> {
            allow.setVisible(!Boolean.TRUE.equals(node.getGroup()));
            allow.setValue(Boolean.TRUE.equals(node.getAllow()));
            allow.setEnabled(editable);
            allow.addValueChangeListener(e -> {
                node.setAllow(e.getValue());
                if (e.getValue()) {
                    node.setDeny(false);
                    node.setEffect("ALLOW");
                } else if (!Boolean.TRUE.equals(node.getDeny())) node.setEffect(null);
                policyTreeGrid.getDataProvider().refreshItem(node);
            });
        })).setHeader("Allow");

        policyTreeGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (deny, node) -> {
            deny.setVisible(!Boolean.TRUE.equals(node.getGroup()));
            deny.setValue(Boolean.TRUE.equals(node.getDeny()));
            deny.setEnabled(editable);
            deny.addValueChangeListener(e -> {
                node.setDeny(e.getValue());
                if (e.getValue()) {
                    node.setAllow(false);
                    node.setEffect("DENY");
                } else if (!Boolean.TRUE.equals(node.getAllow())) node.setEffect(null);
                policyTreeGrid.getDataProvider().refreshItem(node);
            });
        })).setHeader("Deny");
    }
}
