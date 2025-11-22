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
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.menu.MenuItem;
import io.jmix.security.model.ResourcePolicyModel;
import io.jmix.security.model.ResourceRoleModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.*;

@FragmentDescriptor("user-interface-fragment.xml")
public class UserInterfaceFragment extends Fragment<VerticalLayout> {


    @ViewComponent private CollectionContainer<PolicyGroupNode> policyTreeDc;
    @ViewComponent private TreeDataGrid<PolicyGroupNode> policyTreeGrid;
    @ViewComponent private Checkbox allowAllViews;

    private boolean suppressAllowAllEvent = false;
    private Map<String, List<PolicyGroupNode>> leafIndex = new HashMap<>();
    @Autowired
    private RoleManagerService  roleManagerService;
    // =====================================================================
    // INIT
    // =====================================================================
    public void initUi(ResourceRoleModel model) {

        buildTree(model);
        setupTreeGrid(model.getSource().name());

        boolean hasAllowAll = model.getResourcePolicies().stream()
                .anyMatch(p -> "*".equals(p.getResource()) &&
                        "ALLOW".equalsIgnoreCase(p.getEffect()));

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

        leafIndex.clear();
        roleManagerService.indexLeaves(menuRoot);
        roleManagerService.indexLeaves(viewRoot);

        Set<PolicyGroupNode> initialized = new HashSet<>();
        for (List<PolicyGroupNode> nodes : leafIndex.values()) {
            for (PolicyGroupNode node : nodes) {
                if (initialized.add(node)) {
                    node.resetState();
                }
            }
        }

        // Apply ALLOW from DB only
        for (ResourcePolicyModel p : model.getResourcePolicies()) {

            if (!"ALLOW".equalsIgnoreCase(p.getEffect()))
                continue;

            String key = roleManagerService.buildLeafKey(p.getResource(), p.getAction());
            List<PolicyGroupNode> nodes = key == null ? null : leafIndex.get(key);
            if (nodes == null)
                continue;

            for (PolicyGroupNode n : nodes) {
                roleManagerService.applyState(n, true);
                n.setDenyDefault(false);
            }
        }

        policyTreeDc.setItems(Arrays.asList(viewRoot, menuRoot));
        policyTreeGrid.setItems(Arrays.asList(viewRoot, menuRoot), PolicyGroupNode::getChildren);
    }
//
//    private void indexLeaves(PolicyGroupNode node) {
//        if (node.isLeaf()) {
//            String key = buildLeafKey(node);
//            if (key != null) {
//                leafIndex.computeIfAbsent(key, k -> new ArrayList<>()).add(node);
//            }
//        }
//
//        for (PolicyGroupNode c : node.getChildren())
//            indexLeaves(c);
//    }
//    private String buildLeafKey(PolicyGroupNode node) {
//        return buildLeafKey(node.getResource(), node.getAction());
//    }
//
//    private String buildLeafKey(String resource, String action) {
//        if (resource == null || action == null)
//            return null;
//        return resource + "|" + action;
//    }
//    private PolicyGroupNode compress(PolicyGroupNode node) {
//
//        if (!node.getGroup())
//            return node;
//
//        List<PolicyGroupNode> newChildren = new ArrayList<>();
//        for (PolicyGroupNode c : node.getChildren())
//            newChildren.add(compress(c));
//
//        node.setChildren(newChildren);
//
//        // 🔥 KHÔNG merge nếu node có leaf con → giữ group để leaf nằm bên trong!
//        boolean hasLeaf = node.getChildren().stream().anyMatch(n -> !n.getGroup());
//        if (hasLeaf)
//            return node;
//
//        // merge group 1-1
//        if (node.getChildren().size() == 1 && node.getChildren().get(0).getGroup()) {
//            PolicyGroupNode only = node.getChildren().get(0);
//            if (!"View Access".equals(node.getName()))
//                only.setName(node.getName() + "." + only.getName());
//            return only;
//        }
//
//        return node;
//    }
//
//    // =====================================================================
//    // BUILD VIEWS TREE — Menu-aware
//    // =====================================================================
//    private void buildViewsTree(PolicyGroupNode root, Map<String, List<MenuItem>> viewMenuMap) {
//
//        Map<String, String> classToViewId = new LinkedHashMap<>();
//
//        viewRegistry.getViewInfos().forEach(info -> {
//            if (info.getControllerClass() == null)
//                return;
//
//            String className = info.getControllerClass().getName();
//            String viewId = info.getId();
//
//            classToViewId.compute(className, (key, existing) ->
//                    selectPreferredViewId(existing, viewId));
//        });
//
//        Map<String, String> fragments = scanFragments();
//
//        // Xử lý View
//        for (Map.Entry<String, String> entry : classToViewId.entrySet()) {
//            String className = entry.getKey();
//            String viewId = entry.getValue();
//
//            PolicyGroupNode parent = buildPackageTree(root, className);
//
//            List<MenuItem> menuItems = viewMenuMap.get(viewId);
//            if (menuItems != null && !menuItems.isEmpty()) {
//                boolean singleMenu = menuItems.size() == 1;
//                for (MenuItem menuItem : menuItems) {
//                    PolicyGroupNode menuFolder = ensureMenuFolder(parent, menuItem, singleMenu);
//                    addLeaf(menuFolder, viewId, "(View)", Collections.singletonList(menuItem));
//                }
//                continue;
//            }
//
//            addLeaf(parent, viewId, "(View)", Collections.emptyList());
//        }
//
//        // Xử lý Fragment
//        for (String className : fragments.values()) {
//            PolicyGroupNode parent = buildPackageTree(root, className);
//
//            String simple = className.substring(className.lastIndexOf('.') + 1);
//            addLeaf(parent, simple, "(Fragment)", Collections.emptyList());
//        }
//    }
//
//
//    private Map<String, String> scanFragments() {
//        Map<String, String> result = new HashMap<>();
//        try {
//            String basePackage = applicationContext.getBeansWithAnnotation(SpringBootApplication.class)
//                    .values().iterator().next()
//                    .getClass().getPackageName();
//
//            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//            String path = "classpath*:" + basePackage.replace('.', '/') + "/**/*.class";
//
//            Resource[] resources = resolver.getResources(path);
//            CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory();
//
//            for (Resource r : resources) {
//                MetadataReader reader = factory.getMetadataReader(r);
//                AnnotationMetadata meta = reader.getAnnotationMetadata();
//
//                // fragment nhưng KHÔNG phải view
//                if (meta.hasAnnotation(FragmentDescriptor.class.getName()) &&
//                        !meta.hasAnnotation("io.jmix.flowui.view.ViewController")) {
//
//                    String className = reader.getClassMetadata().getClassName();
//                    result.put(
//                            className.substring(className.lastIndexOf('.') + 1),
//                            className
//                    );
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
//
//    private PolicyGroupNode buildPackageTree(PolicyGroupNode root, String className) {
//        String[] parts = className.split("\\.");
//        PolicyGroupNode cur = root;
//
//        for (int i = 0; i < parts.length - 1; i++) {
//            String folder = parts[i];
//
//            PolicyGroupNode exist = cur.getChildren().stream()
//                    .filter(f -> f.getGroup() && f.getName().equals(folder))
//                    .findFirst().orElse(null);
//
//            if (exist == null) {
//                exist = new PolicyGroupNode(folder, true);
//                exist.setParent(cur);
//                cur.getChildren().add(exist);
//            }
//            cur = exist;
//        }
//        return cur;
//    }
//
//
//    // =====================================================================
//    // ADD LEAF — menu-aware
//    // =====================================================================
//    private void addLeaf(PolicyGroupNode parent, String viewId, String meta, List<MenuItem> menuItems) {
//
//        if (menuItems != null && !menuItems.isEmpty()) {
//
//            boolean singleMenu = menuItems.size() == 1;
//            for (MenuItem menuItem : menuItems) {
//                String caption = singleMenu ? "Allow in menu" : "Allow in menu (" + menuItem.getId() + ")";
//
//                PolicyGroupNode allowMenu = new PolicyGroupNode(caption, false);
//                allowMenu.setType("MENU");
//                allowMenu.setResource(menuItem.getId());
//                allowMenu.setAction("menu");
//                allowMenu.setParent(parent);
//                parent.getChildren().add(allowMenu);
//            }
//
//            PolicyGroupNode allowView = new PolicyGroupNode("View: " + viewId, false);
//            allowView.setType("VIEW");
//            allowView.setResource(viewId);
//            allowView.setAction("view");
//            allowView.setMeta(meta);
//            allowView.setParent(parent);
//            parent.getChildren().add(allowView);
//
//        } else {
//
//            PolicyGroupNode leaf = new PolicyGroupNode(viewId, false);
//            leaf.setType("VIEW");
//            leaf.setResource(viewId);
//            leaf.setAction("view");
//            leaf.setMeta(meta);
//            leaf.setParent(parent);
//            parent.getChildren().add(leaf);
//        }
//    }
//
//    private PolicyGroupNode ensureMenuFolder(PolicyGroupNode parent, MenuItem item, boolean singleMenuGroup) {
//        Deque<MenuItem> stack = new ArrayDeque<>();
//        MenuItem current = item;
//
//        while (current != null) {
//            stack.push(current);
//            current = current.getParent();
//        }
//
//        PolicyGroupNode curNode = parent;
//        while (!stack.isEmpty()) {
//            MenuItem menuItem = stack.pop();
//            String caption = menuItem.getId();
//
//            PolicyGroupNode existing = curNode.getChildren().stream()
//                    .filter(n -> Boolean.TRUE.equals(n.getGroup()) && caption.equals(n.getName()))
//                    .findFirst().orElse(null);
//
//            if (existing == null) {
//                existing = new PolicyGroupNode(caption, true);
//                existing.setType("MENU");
//                existing.setParent(curNode);
//                curNode.getChildren().add(existing);
//            }
//            curNode = existing;
//        }
//
//        if (!singleMenuGroup) {
//            String leafName = item.getId() + " (menu)";
//
//            PolicyGroupNode existingLeaf = curNode.getChildren().stream()
//                    .filter(n -> !Boolean.TRUE.equals(n.getGroup()) && leafName.equals(n.getName()))
//                    .findFirst().orElse(null);
//
//            if (existingLeaf == null) {
//                PolicyGroupNode marker = new PolicyGroupNode(leafName, false);
//                marker.setType("MENU");
//                marker.setResource(item.getId());
//                marker.setAction("menu");
//                marker.setParent(curNode);
//                curNode.getChildren().add(marker);
//                return marker;
//            }
//        }
//
//        return curNode;
//    }
//
//    private String selectPreferredViewId(String existing, String candidate) {
//        if (existing == null)
//            return candidate;
//
//        boolean existingCustom = isCustomViewId(existing);
//        boolean candidateCustom = isCustomViewId(candidate);
//
//        if (candidateCustom && !existingCustom)
//            return candidate;
//
//        return existing;
//    }
//
//    private boolean isCustomViewId(String viewId) {
//        if (viewId == null || viewId.isEmpty())
//            return false;
//        return viewId.contains(".") || viewId.contains("_") || Character.isLowerCase(viewId.charAt(0));
//    }
//
//
//
//
//    // =====================================================================
//    // BUILD MENU TREE
//    // =====================================================================
//    private void buildMenuTree(PolicyGroupNode menuRoot) {
//
//        for (MenuItem root : menuConfig.getRootItems())
//            addMenuNode(menuRoot, root);
//    }
//
//    private void addMenuNode(PolicyGroupNode parentNode, MenuItem item) {
//
//        String caption = item.getView() != null ? item.getView() : item.getId();
//
//        PolicyGroupNode groupNode = new PolicyGroupNode(caption, true);
//        groupNode.setType("MENU");
//        groupNode.setParent(parentNode);
//        parentNode.getChildren().add(groupNode);
//
//        boolean hasView = item.getView() != null;
//        boolean hasChildren = !item.getChildren().isEmpty();
//
//        if (hasView) {
//            PolicyGroupNode allowMenu = new PolicyGroupNode("Allow in menu", false);
//            allowMenu.setType("MENU");
//            allowMenu.setResource(item.getId());
//            allowMenu.setAction("menu");
//            allowMenu.setParent(groupNode);
//            groupNode.getChildren().add(allowMenu);
//
//            PolicyGroupNode allowView = new PolicyGroupNode("View: " + item.getView(), false);
//            allowView.setType("VIEW");
//            allowView.setResource(item.getView());
//            allowView.setAction("view");
//            allowView.setParent(groupNode);
//            groupNode.getChildren().add(allowView);
//        }
//
//        if (hasChildren) {
//            for (MenuItem c : item.getChildren())
//                addMenuNode(groupNode, c);
//            return;
//        }
//
//        if (!hasView && !hasChildren) {
//            PolicyGroupNode leaf = new PolicyGroupNode(caption, false);
//            leaf.setType("MENU");
//            leaf.setResource(item.getId());
//            leaf.setAction("menu");
//            leaf.setParent(groupNode);
//            groupNode.getChildren().add(leaf);
//        }
//    }

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

        // ALLOW checkbox
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

        // DENY checkbox (UI only)
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

//    private void applyForAll(PolicyGroupNode node, boolean enable) {
//
//        if (node.isLeaf()) {
//
//            applyState(node, enable);
//        }
//
//        for (PolicyGroupNode c : node.getChildren())
//            applyForAll(c, enable);
//    }
//
//    // =====================================================================
//    // COLLECT POLICIES — only ALLOW saved
//    // =====================================================================
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
//
//    private void collect(PolicyGroupNode node, List<ResourcePolicyModel> list) {
//
//        if (node.isLeaf() && "ALLOW".equals(node.getEffect())) {
//
//            ResourcePolicyModel p = new ResourcePolicyModel();
//            p.setId(UUID.randomUUID());
//            p.setType(node.getType());
//            p.setResource(node.getResource());
//            p.setAction(node.getAction());
//            p.setEffect("ALLOW");
//
//            list.add(p);
//        }
//
//        for (PolicyGroupNode c : node.getChildren())
//            collect(c, list);
//    }
//    private void syncLinkedLeaves(PolicyGroupNode node, boolean allow) {
//        String key = buildLeafKey(node);
//        if (key == null) {
//            applyState(node, allow);
//            return;
//        }
//
//        List<PolicyGroupNode> linked = leafIndex.get(key);
//        if (linked == null) {
//            applyState(node, allow);
//            return;
//        }
//
//        for (PolicyGroupNode target : linked)
//            applyState(target, allow);
//    }
//
//    private void applyState(PolicyGroupNode node, boolean allow) {
//        node.setEffect(allow ? "ALLOW" : null);
//        node.setAllow(allow);
//        node.setDeny(!allow);
//    }
//    private Map<String, List<MenuItem>> buildViewMenuMap() {
//        Map<String, List<MenuItem>> map = new HashMap<>();
//        for (MenuItem root : menuConfig.getRootItems()) {
//            collectMenuItems(root, map);
//        }
//        return map;
//    }
//
//    private void collectMenuItems(MenuItem item, Map<String, List<MenuItem>> map) {
//        if (item.getView() != null) {
//            map.computeIfAbsent(item.getView(), k -> new ArrayList<>()).add(item);
//        }
//
//        for (MenuItem child : item.getChildren()) {
//            collectMenuItems(child, map);
//        }
//    }

}
