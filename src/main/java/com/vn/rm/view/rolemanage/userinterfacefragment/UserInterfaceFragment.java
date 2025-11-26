package com.vn.rm.view.rolemanage.userinterfacefragment;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vn.rm.view.rolemanage.service.RoleManagerService;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.menu.MenuItem;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.security.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@FragmentDescriptor("user-interface-fragment.xml")
public class UserInterfaceFragment extends Fragment<VerticalLayout> {
    @ViewComponent private CollectionContainer<PolicyGroupNode> policyTreeDc;
    @ViewComponent private TreeDataGrid<PolicyGroupNode> policyTreeGrid;
    @ViewComponent private Checkbox allowAllViews;

    @Autowired
    private RoleManagerService roleManagerService;

    private boolean suppressAllowAllEvent = false;



    // ========================================================================
    // INIT UI – FIX CHUẨN NHẤT
    // ========================================================================
    public void initUi(ResourceRoleModel model) {

        // 1) Load annotated role nếu có
        if (model.getSource() == RoleSourceType.ANNOTATED_CLASS) {
            ResourceRole runtimeRole = roleManagerService.getRoleByCode(model.getCode());
            ResourceRoleModel annotatedModel =
                    roleManagerService.convertAnnotatedToModel(runtimeRole);
            roleManagerService.setAnnotatedRole(annotatedModel);
        } else {
            roleManagerService.setAnnotatedRole(null);
        }

        // 2) Build UI tree (quan trọng phải làm trước tick-all)
        buildTree(model);

        // 3) Setup TreeGrid columns
        setupTreeGrid(model.getSource().name());

        // 4) Kiểm tra xem DB role có "*" hay không
        boolean hasAllowAll = model.getResourcePolicies().stream()
                .anyMatch(p -> "*".equals(p.getResource())
                        && ResourcePolicyEffect.ALLOW.equals(p.getEffect()));

        // 5) Đặt giá trị Allow All nhưng không trigger event
        suppressAllowAllEvent = true;
        allowAllViews.setValue(hasAllowAll);
        suppressAllowAllEvent = false;

        // 6) CUỐI CÙNG: đăng ký listener (đặt ở cuối)
        allowAllViews.addValueChangeListener(e -> {
            if (!e.isFromClient() || suppressAllowAllEvent)
                return;

            applyAllowAll(Boolean.TRUE.equals(e.getValue()));
        });
    }

    public void applyForAll(PolicyGroupNode node, boolean enable) {

        if (node.isLeaf()) {
            // chỉ tick UI, không ghi model
            node.setAllow(enable);
            node.setDeny(false);
            policyTreeGrid.getDataProvider().refreshItem(node);
        }

        for (PolicyGroupNode c : node.getChildren()) {
            applyForAll(c, enable);
        }
    }
    private boolean isAllowAllViewsChecked() {
        return Boolean.TRUE.equals(allowAllViews.getValue());
    }
    // ========================================================================
// APPLY ALLOW ALL (KHÔI PHỤC HÀM BỊ MẤT)
// ========================================================================
    private void applyAllowAll(boolean enable) {

        // 1) Tick tất cả leaf theo enable
        for (PolicyGroupNode leaf : roleManagerService.getAllIndexedLeaves()) {
            roleManagerService.applyState(leaf, enable);
            leaf.setDenyDefault(false);
        }

        // 2) Đồng bộ menu <-> screen (tick theo nhau)
        for (PolicyGroupNode leaf : roleManagerService.getAllIndexedLeaves()) {
            roleManagerService.syncLinkedLeaves(leaf, enable);
        }

        // 3) Refresh UI
        policyTreeGrid.getDataProvider().refreshAll();
    }


    // ========================================================================
    // BUILD TREE – (KEEP CODE NHƯ CŨ)
    // ========================================================================

    private void buildTree(ResourceRoleModel model) {

        Map<String, List<MenuItem>> vmMap = roleManagerService.buildViewMenuMap();

        PolicyGroupNode viewRoot = new PolicyGroupNode("View Access", true);
        PolicyGroupNode menuRoot = new PolicyGroupNode("Menu Access", true);

        roleManagerService.buildViewsTree(viewRoot, vmMap);
        viewRoot = roleManagerService.compress(viewRoot);

        roleManagerService.buildMenuTree(menuRoot);

        roleManagerService.clearIndex();
        roleManagerService.indexLeaves(viewRoot);
        roleManagerService.indexLeaves(menuRoot);

        for (PolicyGroupNode leaf : roleManagerService.getAllIndexedLeaves())
            leaf.resetState();
        System.out.println("=== APPLY ANNOTATED - START ===");
        model.getResourcePolicies().forEach(p -> {
            System.out.println("    MODEL POLICY: " + p.getType()
                    + " | " + p.getResource()
                    + " | " + p.getAction()
                    + " | effect=" + p.getEffect());
        });
        System.out.println("=== APPLY ANNOTATED - END ===");

        // 1) apply annotated allow
        applyAnnotated(model);

        // 2) sync menu <-> view
        autoAllowMenuByView();

        // 3) apply DB deny override
        applyDbPolicies(model);

        // final sync
        autoAllowMenuByView();

        policyTreeDc.setItems(Arrays.asList(viewRoot, menuRoot));
        policyTreeGrid.setItems(Arrays.asList(viewRoot, menuRoot), PolicyGroupNode::getChildren);
    }



    // ========================================================================
    // APPLY ANNOTATED
    // ========================================================================
    private void applyAnnotated(ResourceRoleModel model) {

        for (ResourcePolicyModel p : model.getResourcePolicies()) {

            if (!ResourcePolicyEffect.ALLOW.equalsIgnoreCase(p.getEffect()))
                continue;

            // ================================================
            // 1) Nếu annotated là "*" → ALLOW ALL SCREEN + MENU
            // ================================================
            if ("*".equals(p.getResource())) {
                System.out.println("ANNOTATED * DETECTED → ENABLE ALLOW ALL");

                suppressAllowAllEvent = true;
                allowAllViews.setValue(true);
                suppressAllowAllEvent = false;

                applyAllowAll(true);
                continue;
            }

            // ================================================
            // 2) Các annotated khác (screen, menu, view)
            // ================================================
            String uiAction = mapAnnotatedActionToUiAction(p);
            if (uiAction == null)
                continue;

            String key = roleManagerService.buildLeafKey(p.getResource(), "access");
            List<PolicyGroupNode> nodes = roleManagerService.getNodesByKey(key);

            if (nodes != null) {
                for (PolicyGroupNode n : nodes) {
                    n.setAnnotated(true);
                    roleManagerService.applyState(n, true);
                    n.setDenyDefault(false);
                }
            }
        }
    }
    /**
     * Convert annotated policy → UI policy action
     */
    /**
     * Map annotated policy → UI type + action (screen/menu → view/menu)
     */
    private String mapAnnotatedActionToUiAction(ResourcePolicyModel p) {

        String type = p.getType();

        // screen, menu → access
        if ("screen".equalsIgnoreCase(type)) {
            return "access";
        }
        if ("menu".equalsIgnoreCase(type)) {
            return "access";
        }

        // ignore entity / attribute / specific
        return null;
    }

    // ========================================================================
    // APPLY DB POLICIES – KEEP CODE CŨ
    // ========================================================================
    private void applyDbPolicies(ResourceRoleModel model) {

        for (ResourcePolicyModel p : model.getResourcePolicies()) {
            if (ResourcePolicyEffect.ALLOW.equals(p.getEffect())) {

                List<PolicyGroupNode> nodes = roleManagerService.getNodesByKey(
                        roleManagerService.buildLeafKey(p.getResource(), p.getAction()));

                if (nodes != null) {
                    for (PolicyGroupNode n : nodes) {
                        roleManagerService.applyState(n, true);
                        n.setDenyDefault(false);
                    }
                }
            }
        }

        for (ResourcePolicyModel p : model.getResourcePolicies()) {
            if (ResourcePolicyEffect.DENY.equals(p.getEffect())) {

                List<PolicyGroupNode> nodes = roleManagerService.getNodesByKey(
                        roleManagerService.buildLeafKey(p.getResource(), p.getAction()));

                if (nodes != null) {
                    for (PolicyGroupNode n : nodes) {
                        roleManagerService.applyState(n, false);
                    }
                }
            }
        }
    }


    // ========================================================================
    // MENU SYNC (KEEP CODE CŨ)
    // ========================================================================
    private void autoAllowMenuByView() {
        for (PolicyGroupNode leaf : roleManagerService.getAllIndexedLeaves()) {

            if (!"VIEW".equals(leaf.getType()))
                continue;

            if (!"ALLOW".equals(leaf.getEffect()))
                continue;

            String key = leaf.getResource() + "|menu";

            List<PolicyGroupNode> menus = roleManagerService.getNodesByKey(key);

            if (menus != null) {
                for (PolicyGroupNode node : menus) {
                    roleManagerService.applyState(node, true);
                    node.setDenyDefault(false);
                }
            }
        }
    }

    // ================================================================
    // GRID SETUP
    // ================================================================
    private void setupTreeGrid(String source) {
        boolean editable = "DATABASE".equalsIgnoreCase(source);

        policyTreeGrid.removeAllColumns();

        policyTreeGrid.addHierarchyColumn(n ->
                        n.getMeta() != null ? n.getName() + "  " + n.getMeta() : n.getName())
                .setHeader("Resource")
                .setFlexGrow(4);

        policyTreeGrid.addColumn(PolicyGroupNode::getType)
                .setHeader("Type")
                .setTextAlign(ColumnTextAlign.CENTER);

        policyTreeGrid.addColumn(PolicyGroupNode::getAction)
                .setHeader("Action")
                .setTextAlign(ColumnTextAlign.CENTER);

        // Allow
        policyTreeGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (cb, node) -> {
            cb.setVisible(node.isLeaf());
            cb.setEnabled(editable);
            cb.setValue("ALLOW".equals(node.getEffect()));

            cb.addValueChangeListener(e -> {
                if (!e.isFromClient()) return;
                roleManagerService.syncLinkedLeaves(node, true);
                policyTreeGrid.getDataProvider().refreshAll();
            });
        })).setHeader("Allow");

        // Deny
        policyTreeGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (cb, node) -> {
            cb.setVisible(node.isLeaf());
            cb.setEnabled(editable);
            cb.setValue("DENY".equals(node.getEffect()));

            cb.addValueChangeListener(e -> {
                if (!e.isFromClient()) return;
                if (e.getValue()) {
                    roleManagerService.applyState(node, false); // effect = DENY
                    node.setEffect("DENY");
                } else {
                    // neutral
                    roleManagerService.applyState(node, false);
                    node.setEffect(null);
                }
                policyTreeGrid.getDataProvider().refreshAll();
            });

        })).setHeader("Deny");
    }


    // ================================================================
    // COLLECT POLICIES
    // ================================================================
    public List<ResourcePolicyModel> collectPoliciesFromTree() {

        List<ResourcePolicyModel> result = new ArrayList<>();

        if (isAllowAllViewsChecked()) {
            result.add(roleManagerService.createPolicy("screen", "*", "access"));
            result.add(roleManagerService.createPolicy("menu", "*", "access"));

            return result;
        }


        for (PolicyGroupNode root : policyTreeDc.getItems())
            roleManagerService.collect(root, result);

        return result;
    }
}
