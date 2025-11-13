package com.vn.rm.view.rolemanage.userinterfacefragment;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.security.model.ResourcePolicyModel;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.securityflowui.view.resourcepolicy.ResourcePolicyViewUtils;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.menu.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@FragmentDescriptor("user-interface-fragment.xml")
public class UserInterfaceFragment extends Fragment<VerticalLayout> {

    @Autowired
    private ResourcePolicyViewUtils resourcePolicyViewUtils;
    @Autowired
    private ViewRegistry viewRegistry;
    @Autowired
    private MenuConfig menuConfig;
    @Autowired
    private Notifications notifications;

    @ViewComponent
    private CollectionContainer<PolicyGroupNode> policyTreeDc;
    @ViewComponent
    private TreeDataGrid<PolicyGroupNode> policyTreeGrid;

    @ViewComponent
    private Checkbox showAssignedOnly;
    @ViewComponent
    private Checkbox allowAllViews;

    // ====================== API cho View cha gọi ======================

    /**
     * Gọi sau khi roleModel đã được set để build tree + bind event.
     */
    public void initUi(ResourceRoleModel model) {
        buildTree(model);
        setupTreeGrid(model.getSource().name());

        boolean hasAllowAll = model.getResourcePolicies().stream()
                .anyMatch(p -> "*".equals(p.getResource())
                        && "ALLOW".equalsIgnoreCase(String.valueOf(p.getEffect())));
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
    }

    /**
     * Thu thập policies từ cây để view cha save xuống DB.
     */
    public List<ResourcePolicyModel> collectPoliciesFromTree() {
        List<ResourcePolicyModel> list = new ArrayList<>();
        for (PolicyGroupNode root : policyTreeDc.getItems()) {
            collectPoliciesRecursive(root, list);
        }
        return list;
    }

    /**
     * Cho view cha biết checkbox "Allow all views" đang bật không.
     */
    public boolean isAllowAllViewsChecked() {
        return Boolean.TRUE.equals(allowAllViews.getValue());
    }

    // ============================ BUILD TREE ============================

    private void buildTree(ResourceRoleModel model) {
        PolicyGroupNode viewRoot = new PolicyGroupNode("View Access", true);
        PolicyGroupNode menuRoot = new PolicyGroupNode("Menu Access", true);

        // VIEW TREE theo package
        buildViewsTree(viewRoot);
        viewRoot = compressSingleChildFolders(viewRoot);

        // MENU TREE
        for (MenuItem root : menuConfig.getRootItems()) {
            PolicyGroupNode rootNode = new PolicyGroupNode(root.getId(), true);
            rootNode.setType("MENU");
            rootNode.setParent(menuRoot);
            menuRoot.getChildren().add(rootNode);
            buildMenuSubTree(rootNode, root);
        }

        // Map resource -> leaf node
        Map<String, PolicyGroupNode> allLeafs = new HashMap<>();
        collectLeafNodes(viewRoot, allLeafs);
        collectLeafNodes(menuRoot, allLeafs);

        // Apply effect từ policies của role
        for (ResourcePolicyModel policy : model.getResourcePolicies()) {
            PolicyGroupNode node = allLeafs.get(policy.getResource());
            if (node != null) {
                String eff = String.valueOf(policy.getEffect());
                node.setEffect(eff);
                node.setAllow("ALLOW".equalsIgnoreCase(eff));
                node.setDeny("DENY".equalsIgnoreCase(eff));
            }
        }

        List<PolicyGroupNode> roots = Arrays.asList(viewRoot, menuRoot);
        policyTreeDc.setItems(roots);
        policyTreeGrid.setItems(roots, PolicyGroupNode::getChildren);
    }

    private void buildViewsTree(PolicyGroupNode root) {
        Map<String, String> views = resourcePolicyViewUtils.getViewsOptionsMap(false);

        Map<String, String> idToClass = new HashMap<>();
        viewRegistry.getViewInfos().forEach(info -> {
            if (info.getId() != null && info.getControllerClass() != null) {
                idToClass.put(info.getId(), info.getControllerClass().getName());
            }
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

    private PolicyGroupNode compressSingleChildFolders(PolicyGroupNode node) {
        if (!Boolean.TRUE.equals(node.getGroup())) return node;

        List<PolicyGroupNode> newChildren = new ArrayList<>();
        for (PolicyGroupNode c : node.getChildren()) {
            newChildren.add(compressSingleChildFolders(c));
        }
        node.setChildren(newChildren);

        if (node.getChildren().size() == 1 && node.getChildren().get(0).getGroup()) {
            PolicyGroupNode only = node.getChildren().get(0);

            if (!"View Access".equals(node.getName())) {
                only.setName(node.getName() + "." + only.getName());
            }

            return only;
        }

        return node;
    }

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
        if (!Boolean.TRUE.equals(node.getGroup()) && node.getResource() != null) {
            map.put(node.getResource(), node);
        }
        for (PolicyGroupNode child : node.getChildren()) {
            collectLeafNodes(child, map);
        }
    }

    // ============================ TREE GRID UI ============================

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

    // ============================== FILTER ==============================

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
        if (!Boolean.TRUE.equals(node.getGroup())) {
            return (node.getEffect() != null) ? node : null;
        }

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

    // ========================= ALLOW ALL / DENY ALL =========================

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

    // ========================== Policies helper ==========================

    private void collectPoliciesRecursive(PolicyGroupNode node, List<ResourcePolicyModel> list) {
        if (!Boolean.TRUE.equals(node.getGroup())) {
            ResourcePolicyModel p = new ResourcePolicyModel();
            p.setId(UUID.randomUUID());
            p.setType(node.getType());
            p.setResource(node.getResource());
            p.setAction(node.getAction());
            Object eff = node.getEffect();
            p.setEffect(eff != null ? eff.toString() : "DENY");
            list.add(p);
        }
        for (PolicyGroupNode child : node.getChildren()) {
            collectPoliciesRecursive(child, list);
        }
    }
}
