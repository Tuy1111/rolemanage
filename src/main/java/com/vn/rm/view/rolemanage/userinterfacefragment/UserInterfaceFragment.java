package com.vn.rm.view.rolemanage.userinterfacefragment;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.menu.MenuItem;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.security.model.ResourcePolicyModel;
import io.jmix.security.model.ResourceRoleModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@FragmentDescriptor("user-interface-fragment.xml")
public class UserInterfaceFragment extends Fragment<VerticalLayout> {

    @Autowired private ViewRegistry viewRegistry;
    @Autowired private MenuConfig menuConfig;
    @Autowired private Messages messages;
    @Autowired private Metadata metadata;

    @ViewComponent private CollectionContainer<PolicyGroupNode> policyTreeDc;
    @ViewComponent private TreeDataGrid<PolicyGroupNode> policyTreeGrid;
    @ViewComponent private Checkbox showAssignedOnly;
    @ViewComponent private Checkbox allowAllViews;

    // ===================================================
    public void initUi(ResourceRoleModel model) {

        buildTree(model);
        setupTreeGrid(model.getSource().name());

        boolean hasAll = model.getResourcePolicies().stream()
                .anyMatch(p -> "*".equals(p.getResource()) && "ALLOW".equalsIgnoreCase(p.getEffect()));

        allowAllViews.setValue(hasAll);
        if (hasAll) applyAllowAllToTree();

        showAssignedOnly.addValueChangeListener(e -> refreshTreeWithFilter());
        allowAllViews.addValueChangeListener(e -> applyAllowAllToTree());
    }

    public boolean isAllowAllViewsChecked() {
        return Boolean.TRUE.equals(allowAllViews.getValue());
    }

    // ===================================================
    private void buildTree(ResourceRoleModel model) {

        PolicyGroupNode viewRoot =new PolicyGroupNode("View Access", true);
        PolicyGroupNode menuRoot = new PolicyGroupNode("Menu Access", true);

        buildViewsTree(viewRoot);
        buildMenuTree(menuRoot);

        // map model policies
        Map<String, PolicyGroupNode> map = new HashMap<>();
        collectLeaves(viewRoot, map);
        collectLeaves(menuRoot, map);

        for (ResourcePolicyModel p : model.getResourcePolicies()) {
            PolicyGroupNode n = map.get(p.getResource());
            if (n != null) {
                n.setEffect(p.getEffect());
                n.setAllow("ALLOW".equalsIgnoreCase(p.getEffect()));
                n.setDeny("DENY".equalsIgnoreCase(p.getEffect()));
            }
        }

        policyTreeDc.setItems(Arrays.asList(viewRoot, menuRoot));
        policyTreeGrid.setItems(Arrays.asList(viewRoot, menuRoot), PolicyGroupNode::getChildren);
    }

    private void collectLeaves(PolicyGroupNode node, Map<String, PolicyGroupNode> map) {
        if (!node.getGroup() && node.getResource() != null)
            map.put(node.getResource(), node);

        for (PolicyGroupNode c : node.getChildren())
            collectLeaves(c, map);
    }

    // ===================================================
    private void buildViewsTree(PolicyGroupNode root) {

        viewRegistry.getViewInfos().forEach(info -> {

            String viewId = info.getId();
            if (viewId == null) return;

            PolicyGroupNode leaf = new PolicyGroupNode(viewId, false);
            leaf.setResource(viewId);
            leaf.setType("VIEW");
            leaf.setAction("view");
            leaf.setParent(root);

            root.getChildren().add(leaf);
        });
    }

    // ===================================================
    private void buildMenuTree(PolicyGroupNode root) {
        for (MenuItem item : menuConfig.getRootItems()) {
            addMenuNode(root, item);
        }
    }

    private void addMenuNode(PolicyGroupNode parentNode, MenuItem item) {

        boolean isView = item.getView() != null;
        boolean hasChildren = !item.getChildren().isEmpty();
        boolean isGroup = (!isView && hasChildren);

        String caption = item.getView() != null ? item.getView() : item.getId();

        PolicyGroupNode group = new PolicyGroupNode(caption, isGroup);
        group.setParent(parentNode);
        group.setType("MENU");

        parentNode.getChildren().add(group);

        // CASE: view-menu item
        if (isView) {

            // Allow in menu
            PolicyGroupNode mn =new PolicyGroupNode("Allow in menu", false);
            mn.setType("MENU");
            mn.setResource(item.getId());
            mn.setAction("menu");
            mn.setParent(group);
            mn.setMeta("(Menu)");
            group.getChildren().add(mn);

            // View:
            PolicyGroupNode vn =new PolicyGroupNode("View: " + caption, false);
            vn.setType("VIEW");
            vn.setResource(item.getView());
            vn.setAction("view");
            vn.setParent(group);
            vn.setMeta("(View)");
            group.getChildren().add(vn);
        }

        // CASE: group menu
        else if (isGroup) {
            for (MenuItem c : item.getChildren())
                addMenuNode(group, c);
        }
    }

    // ===================================================
    private void setupTreeGrid(String source) {

        boolean editable = "DATABASE".equalsIgnoreCase(source);

        policyTreeGrid.removeAllColumns();

        policyTreeGrid.addHierarchyColumn(PolicyGroupNode::getName).setHeader("Resource");
        policyTreeGrid.addColumn(PolicyGroupNode::getType).setHeader("Type");
        policyTreeGrid.addColumn(PolicyGroupNode::getAction).setHeader("Action");
        policyTreeGrid.addColumn(PolicyGroupNode::getEffect).setHeader("Effect");

        policyTreeGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (cb, n) -> {
            cb.setVisible(!n.getGroup());
            cb.setEnabled(editable);
            cb.setValue(n.getAllow());
            cb.addValueChangeListener(e -> {
                boolean v = e.getValue();
                n.setAllow(v);
                n.setDeny(!v);
                n.setEffect(v ? "ALLOW" : "DENY");
            });
        })).setHeader("Allow");

        policyTreeGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (cb, n) -> {
            cb.setVisible(!n.getGroup());
            cb.setEnabled(editable);
            cb.setValue(n.getDeny());
            cb.addValueChangeListener(e -> {
                boolean v = e.getValue();
                n.setDeny(v);
                n.setAllow(!v);
                n.setEffect(v ? "DENY" : "ALLOW");
            });
        })).setHeader("Deny");
    }

    // ===================================================
    private void refreshTreeWithFilter() {
        if (!showAssignedOnly.getValue()) {
            policyTreeGrid.setItems(policyTreeDc.getItems(), PolicyGroupNode::getChildren);
            return;
        }

        List<PolicyGroupNode> filtered = new ArrayList<>();
        for (PolicyGroupNode n : policyTreeDc.getItems()) {
            PolicyGroupNode f = filterNode(n);
            if (f != null) filtered.add(f);
        }

        policyTreeGrid.setItems(filtered, PolicyGroupNode::getChildren);
    }

    private PolicyGroupNode filterNode(PolicyGroupNode node) {

        if (!node.getGroup())
            return node.getEffect() != null ? node : null;

        List<PolicyGroupNode> list = new ArrayList<>();
        for (PolicyGroupNode c : node.getChildren()) {
            PolicyGroupNode x = filterNode(c);
            if (x != null) list.add(x);
        }

        if (list.isEmpty()) return null;

        PolicyGroupNode r =new PolicyGroupNode(node.getName(), true);
        r.setChildren(list);
        return r;
    }

    // ===================================================
    public List<ResourcePolicyModel> collectPoliciesFromTree() {

        if (isAllowAllViewsChecked()) return Collections.emptyList();

        List<ResourcePolicyModel> list = new ArrayList<>();

        for (PolicyGroupNode root : policyTreeDc.getItems())
            collectPolicies(root, list);

        return list;
    }

    private void collectPolicies(PolicyGroupNode node, List<ResourcePolicyModel> list) {

        if (!node.getGroup() && node.getResource() != null) {
            ResourcePolicyModel p = metadata.create(ResourcePolicyModel.class);
            p.setId(UUID.randomUUID());
            p.setType(node.getType());
            p.setResource(node.getResource());
            p.setAction(node.getAction());
            p.setEffect(node.getEffect() != null ? node.getEffect() : "DENY");
            list.add(p);
        }

        for (PolicyGroupNode c : node.getChildren())
            collectPolicies(c, list);
    }

    private void applyAllowAllToTree() {
        boolean enable = allowAllViews.getValue();
        for (PolicyGroupNode r : policyTreeDc.getItems())
            apply(r, enable);
    }

    private void apply(PolicyGroupNode node, boolean enable) {

        if (!node.getGroup()) {
            node.setAllow(enable);
            node.setDeny(!enable);
            node.setEffect(enable ? "ALLOW" : "DENY");
        }

        for (PolicyGroupNode c : node.getChildren())
            apply(c, enable);
    }
}
