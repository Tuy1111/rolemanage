package com.vn.rm.view.rolemanage.userinterfacefragment;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.Subscribe;
import io.jmix.security.model.ResourcePolicyModel;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.securityflowui.view.resourcepolicy.ResourcePolicyViewUtils;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.menu.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@FragmentDescriptor("user-interface-fragment.xml")
public class UserInterfaceFragment extends Fragment<VerticalLayout> {

    @Autowired private ResourcePolicyViewUtils resourcePolicyViewUtils;
    @Autowired private ViewRegistry viewRegistry;
    @Autowired private MenuConfig menuConfig;
    @Autowired private Notifications notifications;

    @ViewComponent
    private CollectionContainer<PolicyGroupNode> policyTreeDc;
    @ViewComponent
    private TreeDataGrid<PolicyGroupNode> policyTreeGrid;
    @ViewComponent
    private Checkbox showAssignedOnly;
    @ViewComponent
    private Checkbox allowAllViews;

    // ================================================================
    //               API gọi từ ResourceRoleEditView
    // ================================================================
    public void initUi(ResourceRoleModel model) {
        buildTree(model);
        setupTreeGrid(model.getSource().name());

        boolean hasAllowAll = model.getResourcePolicies().stream()
                .anyMatch(p -> "*".equals(p.getResource())
                        && "ALLOW".equalsIgnoreCase(p.getEffect()));
        allowAllViews.setValue(hasAllowAll);

        showAssignedOnly.addValueChangeListener(e -> refreshTreeWithFilter());

        allowAllViews.addValueChangeListener(e -> {
            if (Boolean.TRUE.equals(e.getValue())) {
                toggleAll(true);
                notifications.create("Allow All Views").show();
            } else {
                toggleAll(false);
                notifications.create("Reset All → DENY").show();
            }
        });
    }

    public void reloadFromPolicies() {
        policyTreeGrid.getDataProvider().refreshAll();
    }

    public List<ResourcePolicyModel> collectPoliciesFromTree() {
        List<ResourcePolicyModel> list = new ArrayList<>();
        for (PolicyGroupNode root : policyTreeDc.getItems()) {
            collectPoliciesRecursive(root, list);
        }
        return list;
    }

    public boolean isAllowAllViewsChecked() {
        return Boolean.TRUE.equals(allowAllViews.getValue());
    }

    // ================================================================
    //                         BUILD TREE
    // ================================================================
    private void buildTree(ResourceRoleModel model) {

        PolicyGroupNode viewRoot = new PolicyGroupNode("View Access", true);
        PolicyGroupNode menuRoot = new PolicyGroupNode("Menu Access", true);

        buildViewsTree(viewRoot);
        viewRoot = compressSingleChildFolders(viewRoot);

        for (MenuItem root : menuConfig.getRootItems()) {
            PolicyGroupNode rootNode = new PolicyGroupNode(root.getId(), true);
            rootNode.setType("MENU");
            rootNode.setParent(menuRoot);
            menuRoot.getChildren().add(rootNode);
            buildMenuSubTree(rootNode, root);
        }

        Map<String, PolicyGroupNode> allLeafs = new HashMap<>();
        collectLeafNodes(viewRoot, allLeafs);
        collectLeafNodes(menuRoot, allLeafs);

        // Áp quyền từ DB
        for (ResourcePolicyModel p : model.getResourcePolicies()) {
            PolicyGroupNode node = allLeafs.get(p.getResource());
            if (node != null) {
                String eff = p.getEffect();
                node.setEffect(eff);
                node.setAllow("ALLOW".equalsIgnoreCase(eff));
                node.setDeny("DENY".equalsIgnoreCase(eff));
            }
        }

        List<PolicyGroupNode> roots = Arrays.asList(viewRoot, menuRoot);
        policyTreeDc.setItems(roots);
        policyTreeGrid.setItems(roots, PolicyGroupNode::getChildren);
    }

    // ================================================================
    //                BUILD VIEWS TREE THEO PACKAGE
    // ================================================================
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

            PolicyGroupNode leaf = new PolicyGroupNode(parts[parts.length - 1], false);
            leaf.setResource(viewId);
            leaf.setAction("view");
            leaf.setType("VIEW");
            leaf.setParent(cur);
            cur.getChildren().add(leaf);
        }
    }

    // ================================================================
    //                     COMPRESS FOLDERS
    // ================================================================
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

    // ================================================================
    //                       MENU TREE
    // ================================================================
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

    // ================================================================
    //                         TREE GRID UI
    // ================================================================
    private void setupTreeGrid(String source) {
        policyTreeGrid.removeAllColumns();
        boolean editable = "DATABASE".equalsIgnoreCase(source);

        policyTreeGrid.addHierarchyColumn(PolicyGroupNode::getName)
                .setHeader("Resource");

        policyTreeGrid.addColumn(PolicyGroupNode::getType).setHeader("Type");
        policyTreeGrid.addColumn(PolicyGroupNode::getAction).setHeader("Action");
        policyTreeGrid.addColumn(PolicyGroupNode::getEffect).setHeader("Effect");

        policyTreeGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (cb, node) -> {
            cb.setVisible(!Boolean.TRUE.equals(node.getGroup()));
            cb.setValue(Boolean.TRUE.equals(node.getAllow()));
            cb.setEnabled(editable);
            cb.addValueChangeListener(e -> {
                boolean v = e.getValue();
                node.setAllow(v);
                node.setDeny(!v);
                node.setEffect(v ? "ALLOW" : "DENY");
                policyTreeGrid.getDataProvider().refreshItem(node);
            });
        })).setHeader("Allow");

        policyTreeGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (cb, node) -> {
            cb.setVisible(!Boolean.TRUE.equals(node.getGroup()));
            cb.setValue(Boolean.TRUE.equals(node.getDeny()));
            cb.setEnabled(editable);
            cb.addValueChangeListener(e -> {
                boolean v = e.getValue();
                node.setDeny(v);
                node.setAllow(!v);
                node.setEffect(v ? "DENY" : "ALLOW");
                policyTreeGrid.getDataProvider().refreshItem(node);
            });
        })).setHeader("Deny");
    }

    // ================================================================
    //                        FILTER "Assigned Only"
    // ================================================================
    private void refreshTreeWithFilter() {
        boolean only = Boolean.TRUE.equals(showAssignedOnly.getValue());

        if (!only) {
            policyTreeGrid.setItems(policyTreeDc.getItems(), PolicyGroupNode::getChildren);
            policyTreeGrid.getDataProvider().refreshAll();
            return;
        }

        List<PolicyGroupNode> filtered = new ArrayList<>();
        for (PolicyGroupNode r : policyTreeDc.getItems()) {
            PolicyGroupNode f = filterAssignedRecursive(r);
            if (f != null) filtered.add(f);
        }
        policyTreeGrid.setItems(filtered, PolicyGroupNode::getChildren);
        policyTreeGrid.getDataProvider().refreshAll();
    }

    private PolicyGroupNode filterAssignedRecursive(PolicyGroupNode node) {
        if (!Boolean.TRUE.equals(node.getGroup()))
            return node.getEffect() != null ? node : null;

        List<PolicyGroupNode> children = new ArrayList<>();
        for (PolicyGroupNode c : node.getChildren()) {
            PolicyGroupNode f = filterAssignedRecursive(c);
            if (f != null) children.add(f);
        }
        if (children.isEmpty()) return null;

        PolicyGroupNode copy = new PolicyGroupNode(node.getName(), true);
        copy.setChildren(children);
        return copy;
    }

    // ================================================================
    //                    TOGGLE ALL (ALLOW / DENY)
    // ================================================================
    private void toggleAll(Boolean allow) {
        for (PolicyGroupNode root : policyTreeDc.getItems()) {
            applyToChildrenRecursive(root, allow);
        }
        policyTreeGrid.getDataProvider().refreshAll();
    }

    private void applyToChildrenRecursive(PolicyGroupNode node, Boolean allow) {
        if (!Boolean.TRUE.equals(node.getGroup())) {
            node.setAllow(allow);
            node.setDeny(!allow);
            node.setEffect(allow ? "ALLOW" : "DENY");
        }
        for (PolicyGroupNode c : node.getChildren()) {
            applyToChildrenRecursive(c, allow);
        }
    }

    // ================================================================
    //                COLLECT POLICIES FROM TREE
    // ================================================================
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
        for (PolicyGroupNode c : node.getChildren()) {
            collectPoliciesRecursive(c, list);
        }
    }
}
