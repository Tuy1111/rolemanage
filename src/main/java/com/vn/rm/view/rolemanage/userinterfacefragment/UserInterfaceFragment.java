package com.vn.rm.view.rolemanage.userinterfacefragment;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
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
import io.jmix.securityflowui.view.resourcepolicy.ResourcePolicyViewUtils;

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

    @Autowired private ResourcePolicyViewUtils resourcePolicyViewUtils;
    @Autowired private ViewRegistry viewRegistry;
    @Autowired private MenuConfig menuConfig;
    @Autowired private ApplicationContext applicationContext;

    @ViewComponent private CollectionContainer<PolicyGroupNode> policyTreeDc;
    @ViewComponent private TreeDataGrid<PolicyGroupNode> policyTreeGrid;
//    @ViewComponent private Checkbox showAssignedOnly;
    @ViewComponent private Checkbox allowAllViews;

    // ====================================================================================
    // INIT
    // ====================================================================================
    public void initUi(ResourceRoleModel model) {

        buildTree(model);
        setupTreeGrid(model.getSource().name());

        boolean hasAllowAll = model.getResourcePolicies().stream()
                .anyMatch(p -> "*".equals(p.getResource())
                        && "ALLOW".equalsIgnoreCase(p.getEffect()));

        allowAllViews.setValue(hasAllowAll);
        if (hasAllowAll) applyAllowAllToTree();

//        showAssignedOnly.addValueChangeListener(e -> refreshTreeWithFilter());
        allowAllViews.addValueChangeListener(e -> applyAllowAllToTree());
    }

    public boolean isAllowAllViewsChecked() {
        return Boolean.TRUE.equals(allowAllViews.getValue());
    }

    // ====================================================================================
    // BUILD TREE
    // ====================================================================================
    private void buildTree(ResourceRoleModel model) {

        PolicyGroupNode viewRoot = new PolicyGroupNode("View Access", true);
        PolicyGroupNode menuRoot = new PolicyGroupNode("Menu Access", true);

        buildViewsTree(viewRoot);
        viewRoot = compress(viewRoot);

        buildMenuTree(menuRoot);

        // Map assigned policies
        Map<String, PolicyGroupNode> map = new HashMap<>();
        collectLeaf(viewRoot, map);
        collectLeaf(menuRoot, map);

        for (ResourcePolicyModel p : model.getResourcePolicies()) {
            if (map.containsKey(p.getResource())) {
                PolicyGroupNode n = map.get(p.getResource());
                n.setEffect(p.getEffect());
                n.setAllow("ALLOW".equalsIgnoreCase(p.getEffect()));
                n.setDeny("DENY".equalsIgnoreCase(p.getEffect()));
            }
        }

        policyTreeDc.setItems(Arrays.asList(viewRoot, menuRoot));
        policyTreeGrid.setItems(Arrays.asList(viewRoot, menuRoot), PolicyGroupNode::getChildren);
    }

    private void collectLeaf(PolicyGroupNode node, Map<String, PolicyGroupNode> map) {
        if (!node.getGroup() && node.getResource() != null)
            map.put(node.getResource(), node);

        for (PolicyGroupNode c : node.getChildren())
            collectLeaf(c, map);
    }

    private PolicyGroupNode compress(PolicyGroupNode node) {
        if (!node.getGroup()) return node;

        List<PolicyGroupNode> list = new ArrayList<>();
        for (PolicyGroupNode c : node.getChildren())
            list.add(compress(c));

        node.setChildren(list);

        if (node.getChildren().size() == 1 && node.getChildren().get(0).getGroup()) {
            PolicyGroupNode only = node.getChildren().get(0);
            if (!"View Access".equals(node.getName()))
                only.setName(node.getName() + "." + only.getName());
            return only;
        }
        return node;
    }

    // ====================================================================================
    // BUILD VIEWS TREE
    // ====================================================================================
    private void buildViewsTree(PolicyGroupNode root) {

        Map<String, String> viewIdToClass = new HashMap<>();

        viewRegistry.getViewInfos().forEach(info -> {
            String id = info.getId();
            String simple = info.getControllerClass().getSimpleName();

            if (id.equals(simple)) return;    // skip fallback ids

            viewIdToClass.put(id, info.getControllerClass().getName());
        });

        Map<String, String> fragments = scanFragments();

        // VIEWS (USE VIEW ID ONLY)
        for (String viewId : viewIdToClass.keySet()) {

            String className = viewIdToClass.get(viewId);
            String[] parts = className.split("\\.");

            PolicyGroupNode cur = root;

            for (int i = 0; i < parts.length - 1; i++) {
                String folder = parts[i];

                PolicyGroupNode folderNode = cur.getChildren()
                        .stream().filter(f -> f.getGroup() && f.getName().equals(folder))
                        .findFirst().orElse(null);

                if (folderNode == null) {
                    folderNode = new PolicyGroupNode(folder, true);
                    folderNode.setParent(cur);
                    cur.getChildren().add(folderNode);
                }
                cur = folderNode;
            }

            addLeaf(cur, viewId, "(View)");
        }

        // FRAGMENTS
        for (String className : fragments.values()) {

            String[] parts = className.split("\\.");
            PolicyGroupNode cur = root;

            for (int i = 0; i < parts.length - 1; i++) {
                String folder = parts[i];

                PolicyGroupNode folderNode = cur.getChildren()
                        .stream().filter(f -> f.getGroup() && f.getName().equals(folder))
                        .findFirst().orElse(null);

                if (folderNode == null) {
                    folderNode = new PolicyGroupNode(folder, true);
                    folderNode.setParent(cur);
                    cur.getChildren().add(folderNode);
                }
                cur = folderNode;
            }

            String simple = parts[parts.length - 1];

            if (cur.getChildren().stream().anyMatch(n -> n.getName().equals(simple)))
                continue;

            addLeaf(cur, simple, "(Fragment)");
        }
    }

    // ====================================================================================
    // SCAN FRAGMENTS
    // ====================================================================================
    private Map<String, String> scanFragments() {
        Map<String, String> result = new HashMap<>();

        try {
            String basePackage =
                    applicationContext.getBeansWithAnnotation(SpringBootApplication.class)
                            .values().iterator().next()
                            .getClass().getPackageName();

            String path = "classpath*:" + basePackage.replace('.', '/') + "/**/*.class";

            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver(getClass().getClassLoader());

            Resource[] resources = resolver.getResources(path);
            CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory();

            for (Resource r : resources) {
                MetadataReader reader = factory.getMetadataReader(r);
                AnnotationMetadata meta = reader.getAnnotationMetadata();

                if (meta.hasAnnotation(FragmentDescriptor.class.getName())) {

                    boolean isView = meta.hasAnnotation("io.jmix.flowui.view.ViewController");
                    if (isView) continue;

                    String className = reader.getClassMetadata().getClassName();

                    result.put(className.substring(className.lastIndexOf('.') + 1), className);
                }
            }

        } catch (Exception e) { e.printStackTrace(); }

        return result;
    }

    // ====================================================================================
    // ADD LEAF
    // ====================================================================================
    private void addLeaf(PolicyGroupNode parent, String resource, String metaText) {

        PolicyGroupNode leaf = new PolicyGroupNode(resource, false);
        leaf.setType("VIEW");
        leaf.setResource(resource);
        leaf.setAction("view");
        leaf.setMeta(metaText);

        leaf.setParent(parent);
        parent.getChildren().add(leaf);
    }

    // ====================================================================================
    // BUILD MENU TREE
    // ====================================================================================
    private void buildMenuTree(PolicyGroupNode menuRoot) {

        for (MenuItem root : menuConfig.getRootItems())
            addMenuNode(menuRoot, root);
    }

    private void addMenuNode(PolicyGroupNode parentNode, MenuItem item) {
        boolean isView = item.getView() != null;
        boolean hasChildren = !item.getChildren().isEmpty();
        boolean isGroup = (!isView && hasChildren);
        // ✔ Caption đúng chuẩn: viewId hoặc id
        String caption = isView
                ? item.getView()
                : item.getId();



        PolicyGroupNode group =new PolicyGroupNode(caption, true);
        group.setType("MENU");
        group.setParent(parentNode);
        parentNode.getChildren().add(group);

        if (isView) {

            // (1) Menu permission
            PolicyGroupNode allowMenu =new PolicyGroupNode("Allow in menu", false);
            allowMenu.setType("MENU");
            allowMenu.setResource(item.getId());
            allowMenu.setAction("menu");
            allowMenu.setParent(group);
            group.getChildren().add(allowMenu);

            // (2) View permission
            PolicyGroupNode allowView =new PolicyGroupNode("View: " + caption, false);
            allowView.setType("VIEW");
            allowView.setResource(item.getView());
            allowView.setAction("view");
            allowView.setParent(group);
            group.getChildren().add(allowView);

            return;
        }

        if (isGroup) {
            for (MenuItem c : item.getChildren())
                addMenuNode(group, c);
            return;
        }

        // Menu leaf ko mở view
        PolicyGroupNode leaf =new PolicyGroupNode(caption, false);
        leaf.setType("MENU");
        leaf.setResource(item.getId());
        leaf.setAction("menu");
        leaf.setParent(group);
        group.getChildren().add(leaf);
    }

    // ====================================================================================
    // UI GRID RENDERING
    // ====================================================================================
    private void setupTreeGrid(String source) {
        boolean editable = "DATABASE".equalsIgnoreCase(source);

        policyTreeGrid.removeAllColumns();

        // Resource column — lớn nhất
        var resourceCol = policyTreeGrid.addHierarchyColumn(node -> {
                    return node.getMeta() != null
                            ? node.getName() + "   " + node.getMeta()
                            : node.getName();
                })
                .setHeader("Resource")
                .setFlexGrow(6)                 // lớn nhất
                .setAutoWidth(true)
                .setResizable(true)
                .setTextAlign(ColumnTextAlign.START);  // trái

        // Type column
        var typeCol = policyTreeGrid.addColumn(PolicyGroupNode::getType)
                .setHeader("Type")
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setResizable(true)
                .setAutoWidth(true);

        // Action column
        var actionCol = policyTreeGrid.addColumn(PolicyGroupNode::getAction)
                .setHeader("Action")
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setResizable(true)
                .setAutoWidth(true);

        // Effect column
        var effectCol = policyTreeGrid.addColumn(PolicyGroupNode::getEffect)
                .setHeader("Effect")
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setResizable(true)
                .setAutoWidth(true);

        // ALLOW
        var allowCol = policyTreeGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (cb, node) -> {
                    cb.setVisible(!node.getGroup());
                    cb.setEnabled(editable);
                    cb.setValue(node.getAllow());
                    cb.addValueChangeListener(e -> {
                        Boolean v = e.getValue();
                        node.setAllow(v);
                        node.setDeny(!v);
                        node.setEffect(v ? "ALLOW" : "DENY");
                    });
                }))
                .setHeader("Allow")
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setResizable(true);

        // DENY
        var denyCol = policyTreeGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (cb, node) -> {
                    cb.setVisible(!node.getGroup());
                    cb.setEnabled(editable);
                    cb.setValue(node.getDeny());
                    cb.addValueChangeListener(e -> {
                        boolean v = e.getValue();
                        node.setDeny(v);
                        node.setAllow(!v);
                        node.setEffect(v ? "DENY" : "ALLOW");
                    });
                }))
                .setHeader("Deny")
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setResizable(true);

        // Cho phép resize toàn grid
        policyTreeGrid.setColumnReorderingAllowed(true);
    }

    // ====================================================================================
    // FILTER
    // ====================================================================================
//    private void refreshTreeWithFilter() {
//        boolean only = showAssignedOnly.getValue();
//
//        if (!only) {
//            policyTreeGrid.setItems(policyTreeDc.getItems(), PolicyGroupNode::getChildren);
//            return;
//        }
//
//        List<PolicyGroupNode> filtered = new ArrayList<>();
//        for (PolicyGroupNode r : policyTreeDc.getItems()) {
//            PolicyGroupNode f = filterAssigned(r);
//            if (f != null) filtered.add(f);
//        }
//
//        policyTreeGrid.setItems(filtered, PolicyGroupNode::getChildren);
//    }

    private PolicyGroupNode filterAssigned(PolicyGroupNode node) {

        if (!node.getGroup())
            return node.getEffect() != null ? node : null;

        List<PolicyGroupNode> children = new ArrayList<>();
        for (PolicyGroupNode c : node.getChildren()) {
            PolicyGroupNode f = filterAssigned(c);
            if (f != null) children.add(f);
        }

        if (children.isEmpty()) return null;

        PolicyGroupNode copy = new PolicyGroupNode(node.getName(), true);
        copy.setChildren(children);
        return copy;
    }

    // ====================================================================================
    // COLLECT POLICIES
    // ====================================================================================
    public List<ResourcePolicyModel> collectPoliciesFromTree() {

        if (isAllowAllViewsChecked()) return Collections.emptyList();

        List<ResourcePolicyModel> list = new ArrayList<>();
        for (PolicyGroupNode root : policyTreeDc.getItems())
            collect(root, list);

        return list;
    }

    private void collect(PolicyGroupNode node, List<ResourcePolicyModel> list) {

        if (!node.getGroup()) {
            ResourcePolicyModel p = new ResourcePolicyModel();
            p.setId(UUID.randomUUID());
            p.setType(node.getType());
            p.setResource(node.getResource());
            p.setAction(node.getAction());
            p.setEffect(node.getEffect() != null ? node.getEffect() : "DENY");
            list.add(p);
        }

        for (PolicyGroupNode c : node.getChildren())
            collect(c, list);
    }

    private void applyAllowAllToTree() {
        boolean enable = allowAllViews.getValue();
        for (PolicyGroupNode root : policyTreeDc.getItems())
            apply(root, enable);
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
