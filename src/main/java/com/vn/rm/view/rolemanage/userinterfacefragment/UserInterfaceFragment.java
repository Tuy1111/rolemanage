package com.vn.rm.view.rolemanage.userinterfacefragment;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vn.rm.view.rolemanage.service.RoleManagerService;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.security.model.ResourcePolicyModel;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.flowui.menu.MenuItem;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@FragmentDescriptor("user-interface-fragment.xml")
public class UserInterfaceFragment extends Fragment<VerticalLayout> {

    @ViewComponent private CollectionContainer<PolicyGroupNode> policyTreeDc;
    @ViewComponent private TreeDataGrid<PolicyGroupNode> policyTreeGrid;
    @ViewComponent private Checkbox allowAllViews;

    private boolean suppressAllowAllEvent = false;

    @Autowired
    private RoleManagerService roleManagerService;

    // =====================================================================
    // INIT
    // =====================================================================
    public void initUi(ResourceRoleModel model) {

        buildTree(model);
        setupTreeGrid(model.getSource().name());

        boolean hasAllowAll = model.getResourcePolicies().stream()
                .anyMatch(p -> "*".equals(p.getResource())
                        && "ALLOW".equalsIgnoreCase(p.getEffect()));

        suppressAllowAllEvent = true;
        allowAllViews.setValue(hasAllowAll);
        suppressAllowAllEvent = false;

        allowAllViews.addValueChangeListener(e -> {
            if (suppressAllowAllEvent) return;
            if (!e.isFromClient()) return;
            applyAllowAll(Boolean.TRUE.equals(e.getValue()));
        });
    }

    public boolean isAllowAllViewsChecked() {
        return Boolean.TRUE.equals(allowAllViews.getValue());
    }

    // =====================================================================
    // BUILD TREE
    // =====================================================================
    private void buildTree(ResourceRoleModel model) {

        Map<String, List<MenuItem>> viewMenuMap = roleManagerService.buildViewMenuMap();

        PolicyGroupNode viewRoot = new PolicyGroupNode("View Access", true);
        PolicyGroupNode menuRoot = new PolicyGroupNode("Menu Access", true);

        roleManagerService.buildViewsTree(viewRoot, viewMenuMap);
        viewRoot = roleManagerService.compress(viewRoot);

        roleManagerService.buildMenuTree(menuRoot);

        roleManagerService.clearIndex();
        roleManagerService.indexLeaves(menuRoot);
        roleManagerService.indexLeaves(viewRoot);

        // Reset state => mặc định DENY
        for (PolicyGroupNode node : roleManagerService.getAllIndexedLeaves()) {
            node.resetState();
        }

        // ==========================================
        // APPLY DB POLICIES (THEO RESOURCE THƯỜNG)
        // ==========================================
        for (ResourcePolicyModel p : model.getResourcePolicies()) {

            if (!"ALLOW".equalsIgnoreCase(p.getEffect()))
                continue;

            if ("*".equals(p.getResource()))
                continue; // skip wildcard ở đây, xử lý riêng phía dưới

            List<PolicyGroupNode> nodes = roleManagerService.getNodesByKey(
                    roleManagerService.buildLeafKey(p.getResource(), p.getAction()));

            if (nodes == null)
                continue;

            for (PolicyGroupNode n : nodes) {
                roleManagerService.applyState(n, true);
                n.setDenyDefault(false);
            }
        }

        // ==========================================
        // 🔥 WILDCARD (*) — APPLY CHO TẤT CẢ LEAF
        // ==========================================
        boolean allowAllViewsDB = model.getResourcePolicies().stream()
                .anyMatch(p -> "*".equals(p.getResource())
                        && "view".equals(p.getAction())
                        && "ALLOW".equals(p.getEffect()));

        boolean allowAllMenusDB = model.getResourcePolicies().stream()
                .anyMatch(p -> "*".equals(p.getResource())
                        && "menu".equals(p.getAction())
                        && "ALLOW".equals(p.getEffect()));

        if (allowAllViewsDB || allowAllMenusDB) {
            for (PolicyGroupNode node : roleManagerService.getAllIndexedLeaves()) {

                if (allowAllViewsDB && "view".equals(node.getAction())) {
                    roleManagerService.applyState(node, true);
                }

                if (allowAllMenusDB && "menu".equals(node.getAction())) {
                    roleManagerService.applyState(node, true);
                }

                node.setDenyDefault(false);
            }
        }

        policyTreeDc.setItems(Arrays.asList(viewRoot, menuRoot));
        policyTreeGrid.setItems(Arrays.asList(viewRoot, menuRoot), PolicyGroupNode::getChildren);
    }

    // =====================================================================
    // RENDER COLUMNS
    // =====================================================================
    private void setupTreeGrid(String source) {
        boolean editable = "DATABASE".equalsIgnoreCase(source);

        policyTreeGrid.removeAllColumns();

        policyTreeGrid.addHierarchyColumn(node ->
                        node.getMeta() != null
                                ? node.getName() + "   " + node.getMeta()
                                : node.getName())
                .setHeader("Resource")
                .setFlexGrow(6)
                .setAutoWidth(true)
                .setResizable(true)
                .setTextAlign(ColumnTextAlign.START);

        policyTreeGrid.addColumn(PolicyGroupNode::getType)
                .setHeader("Type")
                .setTextAlign(ColumnTextAlign.CENTER);

        policyTreeGrid.addColumn(PolicyGroupNode::getAction)
                .setHeader("Action")
                .setTextAlign(ColumnTextAlign.CENTER);

        // ALLOW
        policyTreeGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (cb, node) -> {
            cb.setVisible(node.isLeaf());
            cb.setEnabled(editable);
            cb.setValue("ALLOW".equals(node.getEffect()));

            cb.addValueChangeListener(e -> {
                if (!e.isFromClient()) return;
                boolean checked = Boolean.TRUE.equals(e.getValue());
                roleManagerService.syncLinkedLeaves(node, checked);
                policyTreeGrid.getDataProvider().refreshAll();
            });

        })).setHeader("Allow");

        // DENY
        policyTreeGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (cb, node) -> {
            cb.setVisible(node.isLeaf());
            cb.setEnabled(editable);
            cb.setValue(!"ALLOW".equals(node.getEffect()));

            cb.addValueChangeListener(e -> {
                if (!e.isFromClient()) return;
                boolean checked = Boolean.TRUE.equals(e.getValue());
                if (checked) {
                    roleManagerService.syncLinkedLeaves(node, false);
                    policyTreeGrid.getDataProvider().refreshAll();
                }
            });

        })).setHeader("Deny");

        policyTreeGrid.setColumnReorderingAllowed(true);
    }

    // =====================================================================
    // APPLY ALLOW ALL
    // =====================================================================
    private void applyAllowAll(boolean enable) {
        for (PolicyGroupNode root : policyTreeDc.getItems())
            roleManagerService.applyForAll(root, enable);

        policyTreeGrid.getDataProvider().refreshAll();
    }

    // =====================================================================
    // COLLECT POLICIES
    // =====================================================================
    public List<ResourcePolicyModel> collectPoliciesFromTree() {

        List<ResourcePolicyModel> list = new ArrayList<>();

        if (isAllowAllViewsChecked()) {

            ResourcePolicyModel p = new ResourcePolicyModel();
            p.setId(UUID.randomUUID());
            p.setType("VIEW");
            p.setResource("*");
            p.setAction("view");
            p.setEffect("ALLOW");
            list.add(p);

            ResourcePolicyModel menu = new ResourcePolicyModel();
            menu.setId(UUID.randomUUID());
            menu.setType("MENU");
            menu.setResource("*");
            menu.setAction("menu");
            menu.setEffect("ALLOW");
            list.add(menu);

            return list;
        }

        for (PolicyGroupNode root : policyTreeDc.getItems())
            roleManagerService.collect(root, list);

        return list;
    }
}
