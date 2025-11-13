package com.vn.rm.view.rolemanage.entityfragment;

import com.google.common.base.Strings;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.jmix.core.Metadata;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.ResourcePolicyEffect;
import io.jmix.security.model.ResourcePolicyModel;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.securityflowui.view.resourcepolicy.ResourcePolicyViewUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@FragmentDescriptor("entities-fragment.xml")
public class EntitiesFragment extends Fragment<VerticalLayout> {

    // ============================= UI components =============================

    @ViewComponent
    private CollectionContainer<ResourcePolicyModel> resourcePoliciesDc;

    @ViewComponent
    private CollectionContainer<EntityMatrixRow> entityMatrixDc;
    @ViewComponent
    private DataGrid<EntityMatrixRow> entityMatrixTable;

    @ViewComponent
    private CollectionContainer<AttrMatrixRow> attrMatrixDc;
    @ViewComponent
    private DataGrid<AttrMatrixRow> attrMatrixTable;

    @ViewComponent
    private Span attrEntityLabel;

    // ================================ Services ===============================

    @Autowired
    private ResourcePolicyViewUtils resourcePolicyEditorUtils;
    @Autowired
    private Metadata metadata;

    // =========================== Cache & guards ==============================

    private final Map<String, List<AttrMatrixRow>> attrCache = new HashMap<>();
    private boolean syncingAttrSummary = false;

    // ============================== Lifecycle ================================



    @Subscribe
    public void onReady(Fragment.ReadyEvent event) {
        // thay cho onInit + onReady cũ
        entityMatrixTable.setSelectionMode(DataGrid.SelectionMode.SINGLE);
        buildMatrixSkeleton();
        installMatrixColumns();
        installAttrColumns();

        refreshMatrixFromPolicies();
    }

    // ========================================================================
    // ============ API để view cha có thể gọi khi cần ========================
    // ========================================================================

    /**
     * View cha có thể gọi để reload ma trận từ resourcePoliciesDc
     * (khi thay đổi list policies từ bên ngoài).
     */
    public void reloadFromPolicies() {
        refreshMatrixFromPolicies();
    }

    /**
     * View cha gọi lúc save, để build list ResourcePolicyModel
     * tương ứng từ ma trận entity + attributes.
     */
    public List<ResourcePolicyModel> buildPoliciesFromMatrix() {
        List<ResourcePolicyModel> raw = entityMatrixDc.getItems().stream()
                .filter(r -> !Strings.isNullOrEmpty(r.getEntityName()))
                .filter(r -> !"*".equals(r.getEntityName()))
                .flatMap(r -> {
                    String entity = r.getEntityName();

                    if (T(r.getAllowAll())) {
                        r.setCanCreate(true);
                        r.setCanRead(true);
                        r.setCanUpdate(true);
                        r.setCanDelete(true);
                    }

                    List<ResourcePolicyModel> list = new ArrayList<>();

                    // ENTITY CRUD
                    if (T(r.getCanCreate()))
                        list.add(newEntityPolicy(entity, EntityPolicyAction.CREATE.getId()));
                    if (T(r.getCanRead()))
                        list.add(newEntityPolicy(entity, EntityPolicyAction.READ.getId()));
                    if (T(r.getCanUpdate()))
                        list.add(newEntityPolicy(entity, EntityPolicyAction.UPDATE.getId()));
                    if (T(r.getCanDelete()))
                        list.add(newEntityPolicy(entity, EntityPolicyAction.DELETE.getId()));

                    // Không có CRUD => bỏ qua attr
                    if (!(T(r.getCanCreate()) || T(r.getCanRead()) || T(r.getCanUpdate()) || T(r.getCanDelete())))
                        return list.stream();

                    // ENTITY_ATTRIBUTE
                    List<AttrMatrixRow> attrs = Optional.ofNullable(attrCache.get(entity)).orElseGet(List::of);

                    if (attrs.isEmpty()) {
                        String pattern = Strings.nullToEmpty(r.getAttributes()).trim();
                        if (!pattern.isEmpty())
                            addAttrPolicy(list, entity, pattern,
                                    EntityAttributePolicyAction.VIEW.getId(),
                                    EntityAttributePolicyAction.MODIFY.getId());
                        return list.stream();
                    }

                    AttrMatrixRow star = attrs.stream()
                            .filter(a -> "*".equals(a.getAttribute()))
                            .findFirst()
                            .orElse(null);

                    if (star != null && (T(star.getCanView()) || T(star.getCanModify()))) {
                        if (T(star.getCanView()))
                            list.add(newAttrPolicy(entity, "*", EntityAttributePolicyAction.VIEW.getId()));
                        if (T(star.getCanModify()))
                            list.add(newAttrPolicy(entity, "*", EntityAttributePolicyAction.MODIFY.getId()));
                    } else {
                        attrs.stream()
                                .filter(a -> !"*".equals(a.getAttribute()))
                                .forEach(a -> {
                                    if (T(a.getCanView()))
                                        list.add(newAttrPolicy(entity, a.getAttribute(),
                                                EntityAttributePolicyAction.VIEW.getId()));
                                    if (T(a.getCanModify()))
                                        list.add(newAttrPolicy(entity, a.getAttribute(),
                                                EntityAttributePolicyAction.MODIFY.getId()));
                                });

                        boolean allSelected = attrs.stream()
                                .filter(a -> !"*".equals(a.getAttribute()))
                                .allMatch(a -> T(a.getCanView()) || T(a.getCanModify()));
                        if (allSelected) {
                            list.removeIf(p -> p.getType() == ResourcePolicyType.ENTITY_ATTRIBUTE
                                    && p.getResource() != null
                                    && p.getResource().startsWith(entity + ".")
                                    && !p.getResource().equals(entity + ".*"));

                            boolean anyView = attrs.stream().anyMatch(a -> T(a.getCanView()));
                            boolean anyModify = attrs.stream().anyMatch(a -> T(a.getCanModify()));
                            if (anyView)
                                list.add(newAttrPolicy(entity, "*", EntityAttributePolicyAction.VIEW.getId()));
                            if (anyModify)
                                list.add(newAttrPolicy(entity, "*", EntityAttributePolicyAction.MODIFY.getId()));
                        }
                    }

                    return list.stream();
                })
                .collect(Collectors.toList());

        // dedup theo khóa logic
        Set<String> seen = new HashSet<>();
        List<ResourcePolicyModel> dedup = new ArrayList<>();
        for (ResourcePolicyModel p : raw) {
            String key = policyKey(p.getType(), p.getResource(), p.getAction(), p.getEffect(), p.getPolicyGroup());
            if (seen.add(key))
                dedup.add(p);
        }

        // nén wildcard: nếu có entity.* cho VIEW/MODIFY -> bỏ field policies tương ứng
        return compressWildcard(dedup);
    }

    // ========================================================================
    // ========================= Matrix: Entities grid ========================
    // ========================================================================

    private void buildMatrixSkeleton() {
        Map<String, String> entityOptions = resourcePolicyEditorUtils.getEntityOptionsMap();
        List<EntityMatrixRow> rows = entityOptions.entrySet().stream()
                .map(e -> {
                    EntityMatrixRow r = metadata.create(EntityMatrixRow.class);
                    r.setEntityName(e.getKey());
                    r.setEntityCaption(e.getValue());
                    r.setAllowAll(false);
                    r.setCanCreate(false);
                    r.setCanRead(false);
                    r.setCanUpdate(false);
                    r.setCanDelete(false);
                    r.setAttributes(null);
                    return r;
                })
                .collect(Collectors.toList());
        entityMatrixDc.setItems(rows);
    }

    /**
     * Đọc resourcePoliciesDc và tick CRUD + attributes cho bảng entity.
     * Chấp nhận effect = null hoặc ALLOW.
     */
    private void refreshMatrixFromPolicies() {
        List<EntityMatrixRow> rows = new ArrayList<>(entityMatrixDc.getItems());

        // reset state
        rows.forEach(r -> {
            r.setAllowAll(false);
            r.setCanCreate(false);
            r.setCanRead(false);
            r.setCanUpdate(false);
            r.setCanDelete(false);
            r.setAttributes(null);
        });

        Collection<ResourcePolicyModel> policies =
                Optional.ofNullable(resourcePoliciesDc.getItems()).orElseGet(List::of);

        // map entity -> row cho lookup nhanh
        Map<String, EntityMatrixRow> rowByEntity = rows.stream()
                .filter(r -> !Strings.isNullOrEmpty(r.getEntityName()))
                .filter(r -> !"*".equals(r.getEntityName()))
                .collect(Collectors.toMap(EntityMatrixRow::getEntityName, r -> r));

        // apply policies
        for (ResourcePolicyModel p : policies) {
            if (p.getResource() == null)
                continue;
            boolean allow = (p.getEffect() == null || p.getEffect() == ResourcePolicyEffect.ALLOW);
            if (!allow)
                continue;

            if (p.getType() == ResourcePolicyType.ENTITY) {
                EntityMatrixRow row = rowByEntity.get(p.getResource());
                if (row == null)
                    continue;

                String action = p.getAction();
                if (EntityPolicyAction.CREATE.getId().equals(action)) {
                    row.setCanCreate(true);
                } else if (EntityPolicyAction.READ.getId().equals(action)) {
                    row.setCanRead(true);
                } else if (EntityPolicyAction.UPDATE.getId().equals(action)) {
                    row.setCanUpdate(true);
                } else if (EntityPolicyAction.DELETE.getId().equals(action)) {
                    row.setCanDelete(true);
                }
            } else if (p.getType() == ResourcePolicyType.ENTITY_ATTRIBUTE) {
                String res = p.getResource();
                if (res.endsWith(".*")) {
                    String entity = res.substring(0, res.length() - 2);
                    EntityMatrixRow row = rowByEntity.get(entity);
                    if (row != null) {
                        row.setAttributes("*");
                    }
                }
            }
        }

        // sync allowAll
        rows.forEach(this::syncAllowAll);

        // gán lại vào container
        entityMatrixDc.setItems(rows);

        // preload attr + summary
        preloadAllAttributesFromDbAndFillEntitySummary(policies);

        // chọn entity đầu tiên để hiển thị attr
        EntityMatrixRow first = entityMatrixDc.getItems().stream()
                .filter(r -> !Strings.isNullOrEmpty(r.getEntityName()))
                .filter(r -> !"*".equals(r.getEntityName()))
                .findFirst()
                .orElse(null);
        if (first != null) {
            entityMatrixDc.setItem(first); // sẽ trigger onEntityMatrixItemChange -> loadAttributesForEntity
        } else {
            attrMatrixDc.setItems(Collections.emptyList());
            if (attrEntityLabel != null)
                attrEntityLabel.setText("");
        }
    }

    private void preloadAllAttributesFromDbAndFillEntitySummary(Collection<ResourcePolicyModel> policies) {
        List<EntityMatrixRow> rows = new ArrayList<>(entityMatrixDc.getItems());
        for (EntityMatrixRow row : rows) {
            String entity = row.getEntityName();
            if (Strings.isNullOrEmpty(entity) || "*".equals(entity))
                continue;

            List<AttrMatrixRow> attrRows = buildAttrRowsForEntity(entity);
            applyAttrPolicies(attrRows, entity, policies);
            attrCache.put(entity, attrRows);
            String summary = computeAttrSummaryFromRows(attrRows);
            row.setAttributes(summary);
        }
        entityMatrixDc.setItems(rows);
    }

    private String computeAttrSummaryFromRows(List<AttrMatrixRow> rows) {
        if (rows == null || rows.isEmpty())
            return null;

        AttrMatrixRow star = rows.stream()
                .filter(a -> "*".equals(a.getAttribute()))
                .findFirst()
                .orElse(null);
        List<AttrMatrixRow> normals = rows.stream()
                .filter(a -> !"*".equals(a.getAttribute()))
                .toList();

        boolean starView = star != null && T(star.getCanView());
        boolean starModify = star != null && T(star.getCanModify());

        boolean fullView = !normals.isEmpty() && normals.stream().allMatch(a -> T(a.getCanView()));
        boolean fullModify = !normals.isEmpty() && normals.stream().allMatch(a -> T(a.getCanModify()));

        if ((starView && starModify) || (fullView && fullModify))
            return "*,*";
        if (starView || starModify || fullView || fullModify)
            return "*";

        long selected = normals.stream()
                .filter(a -> T(a.getCanView()) || T(a.getCanModify()))
                .count();
        if (selected == 0)
            return null;

        return normals.stream()
                .filter(a -> T(a.getCanView()) || T(a.getCanModify()))
                .map(AttrMatrixRow::getAttribute)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(","));
    }

    // ========================= Attribute matrix =============================

    private void loadAttributesForEntity(String entityName) {
        if (Strings.isNullOrEmpty(entityName) || "*".equals(entityName.trim())) {
            if (attrEntityLabel != null)
                attrEntityLabel.setText("");
            attrMatrixDc.setItems(Collections.emptyList());
            return;
        }

        if (attrEntityLabel != null) {
            attrEntityLabel.setText("Entity: " + entityName);
        }

        List<AttrMatrixRow> rows = attrCache.get(entityName);
        if (rows == null) {
            rows = buildAttrRowsForEntity(entityName);
            applyAttrPolicies(
                    rows,
                    entityName,
                    Optional.ofNullable(resourcePoliciesDc.getItems()).orElseGet(List::of)
            );
            attrCache.put(entityName, rows);
        }

        attrMatrixDc.setItems(new ArrayList<>(rows));
        updateEntityAttributesSummarySafe(entityName);
    }

    private List<AttrMatrixRow> buildAttrRowsForEntity(String entityName) {
        Map<String, String> attrs = resourcePolicyEditorUtils.getEntityAttributeOptionsMap(entityName);
        if (attrs.isEmpty()) {
            AttrMatrixRow star = metadata.create(AttrMatrixRow.class);
            star.setEntityName(entityName);
            star.setAttribute("*");
            star.setCanView(false);
            star.setCanModify(false);
            return List.of(star);
        }

        return attrs.keySet().stream()
                .map(name -> {
                    AttrMatrixRow r = metadata.create(AttrMatrixRow.class);
                    r.setEntityName(entityName);
                    r.setAttribute(name);
                    r.setCanView(false);
                    r.setCanModify(false);
                    return r;
                })
                .sorted((a, b) -> {
                    if ("*".equals(a.getAttribute()))
                        return -1;
                    if ("*".equals(b.getAttribute()))
                        return 1;
                    return a.getAttribute().compareToIgnoreCase(b.getAttribute());
                })
                .collect(Collectors.toList());
    }

    private void applyAttrPolicies(List<AttrMatrixRow> rows,
                                   String entityName,
                                   Collection<ResourcePolicyModel> policies) {

        Map<String, List<ResourcePolicyModel>> byRes = policies.stream()
                .filter(p -> p.getType() == ResourcePolicyType.ENTITY_ATTRIBUTE)
                .filter(p -> p.getResource() != null && p.getResource().startsWith(entityName + "."))
                .filter(p -> p.getEffect() == null || p.getEffect() == ResourcePolicyEffect.ALLOW)
                .collect(Collectors.groupingBy(ResourcePolicyModel::getResource));

        rows.forEach(r -> {
            String attr = r.getAttribute();
            if (attr == null)
                return;

            if ("*".equals(attr)) {
                String wildcardRes = entityName + ".*";
                List<ResourcePolicyModel> starPolicies = byRes.getOrDefault(wildcardRes, List.of());
                starPolicies.forEach(p -> {
                    if (EntityAttributePolicyAction.VIEW.getId().equals(p.getAction()))
                        r.setCanView(true);
                    if (EntityAttributePolicyAction.MODIFY.getId().equals(p.getAction()))
                        r.setCanModify(true);
                });
            } else {
                String res = entityName + "." + attr;
                List<ResourcePolicyModel> ps = byRes.getOrDefault(res, List.of());
                ps.forEach(p -> {
                    if (EntityAttributePolicyAction.VIEW.getId().equals(p.getAction()))
                        r.setCanView(true);
                    if (EntityAttributePolicyAction.MODIFY.getId().equals(p.getAction()))
                        r.setCanModify(true);
                });
            }
        });

        rows.stream().filter(r -> "*".equals(r.getAttribute())).findFirst().ifPresent(star -> {
            if (T(star.getCanView()) || T(star.getCanModify())) {
                rows.stream().filter(r -> !"*".equals(r.getAttribute())).forEach(r -> {
                    if (T(star.getCanView()))
                        r.setCanView(true);
                    if (T(star.getCanModify()))
                        r.setCanModify(true);
                });
            }
        });
    }

    // ============================ UI columns ================================

    private void installMatrixColumns() {
        entityMatrixTable.getColumnByKey("allowAllCol")
                .setRenderer(new ComponentRenderer<>(row -> {
                    Checkbox cb = new Checkbox(T(row.getAllowAll()));
                    cb.addValueChangeListener(e -> {
                        row.setAllowAll(bool(e.getValue()));
                        if (T(row.getAllowAll())) {
                            row.setCanCreate(true);
                            row.setCanRead(true);
                            row.setCanUpdate(true);
                            row.setCanDelete(true);
                        }
                        entityMatrixDc.replaceItem(row);
                    });
                    return cb;
                }));

        entityMatrixTable.getColumnByKey("createCol")
                .setRenderer(new ComponentRenderer<>(row -> {
                    Checkbox cb = new Checkbox(T(row.getCanCreate()));
                    cb.addValueChangeListener(e -> {
                        row.setCanCreate(bool(e.getValue()));
                        entityMatrixDc.replaceItem(row);
                        syncAllowAll(row);
                    });
                    return cb;
                }));

        entityMatrixTable.getColumnByKey("readCol")
                .setRenderer(new ComponentRenderer<>(row -> {
                    Checkbox cb = new Checkbox(T(row.getCanRead()));
                    cb.addValueChangeListener(e -> {
                        row.setCanRead(bool(e.getValue()));
                        entityMatrixDc.replaceItem(row);
                        syncAllowAll(row);
                    });
                    return cb;
                }));

        entityMatrixTable.getColumnByKey("updateCol")
                .setRenderer(new ComponentRenderer<>(row -> {
                    Checkbox cb = new Checkbox(T(row.getCanUpdate()));
                    cb.addValueChangeListener(e -> {
                        row.setCanUpdate(bool(e.getValue()));
                        entityMatrixDc.replaceItem(row);
                        syncAllowAll(row);
                    });
                    return cb;
                }));

        entityMatrixTable.getColumnByKey("deleteCol")
                .setRenderer(new ComponentRenderer<>(row -> {
                    Checkbox cb = new Checkbox(T(row.getCanDelete()));
                    cb.addValueChangeListener(e -> {
                        row.setCanDelete(bool(e.getValue()));
                        entityMatrixDc.replaceItem(row);
                        syncAllowAll(row);
                    });
                    return cb;
                }));

        entityMatrixTable.getColumnByKey("attributesCol")
                .setRenderer(new ComponentRenderer<>(row -> {
                    TextField tf = new TextField();
                    tf.setWidthFull();
                    tf.setReadOnly(true);
                    tf.setValue(Objects.toString(displayAttrPattern(row.getAttributes()), ""));
                    return tf;
                }));
    }

    private void installAttrColumns() {
        attrMatrixTable.getColumnByKey("viewCol")
                .setRenderer(new ComponentRenderer<>(row -> {
                    Checkbox cb = new Checkbox(T(row.getCanView()));
                    cb.addValueChangeListener(e -> {
                        row.setCanView(T(e.getValue()));

                        if ("*".equals(row.getAttribute())) {
                            attrMatrixDc.getItems().forEach(r -> {
                                if (!"*".equals(r.getAttribute()))
                                    r.setCanView(row.getCanView());
                            });
                            attrMatrixDc.setItems(new ArrayList<>(attrMatrixDc.getItems()));
                        } else {
                            List<AttrMatrixRow> items = new ArrayList<>(attrMatrixDc.getItems());
                            for (AttrMatrixRow r2 : items) {
                                if ("*".equals(r2.getAttribute())) {
                                    if (T(r2.getCanView()) || T(r2.getCanModify())) {
                                        r2.setCanView(false);
                                        r2.setCanModify(false);
                                    }
                                    break;
                                }
                            }
                            attrMatrixDc.setItems(items);
                        }

                        updateEntityAttributesSummarySafe(row.getEntityName());
                    });
                    return cb;
                }));

        attrMatrixTable.getColumnByKey("modifyCol")
                .setRenderer(new ComponentRenderer<>(row -> {
                    Checkbox cb = new Checkbox(T(row.getCanModify()));
                    cb.addValueChangeListener(e -> {
                        row.setCanModify(T(e.getValue()));

                        if ("*".equals(row.getAttribute())) {
                            attrMatrixDc.getItems().forEach(r -> {
                                if (!"*".equals(r.getAttribute()))
                                    r.setCanModify(row.getCanModify());
                            });
                            attrMatrixDc.setItems(new ArrayList<>(attrMatrixDc.getItems()));
                        } else {
                            List<AttrMatrixRow> items = new ArrayList<>(attrMatrixDc.getItems());
                            for (AttrMatrixRow r2 : items) {
                                if ("*".equals(r2.getAttribute())) {
                                    if (T(r2.getCanView()) || T(r2.getCanModify())) {
                                        r2.setCanView(false);
                                        r2.setCanModify(false);
                                    }
                                    break;
                                }
                            }
                            attrMatrixDc.setItems(items);
                        }

                        updateEntityAttributesSummarySafe(row.getEntityName());
                    });
                    return cb;
                }));
    }

    // =============================== Events =================================

    @Subscribe(id = "entityMatrixDc", target = Target.DATA_CONTAINER)
    public void onEntityMatrixItemChange(CollectionContainer.ItemChangeEvent<EntityMatrixRow> e) {
        EntityMatrixRow row = e.getItem();
        if (row == null) {
            if (attrEntityLabel != null)
                attrEntityLabel.setText("");
            attrMatrixDc.setItems(Collections.emptyList());
            return;
        }

        String cap = Optional.ofNullable(row.getEntityCaption()).orElse(row.getEntityName());
        if (attrEntityLabel != null)
            attrEntityLabel.setText("Entity: " + cap + " (" + row.getEntityName() + ")");
        loadAttributesForEntity(row.getEntityName());
    }

    // ======================= Utils & helpers ================================

    private void syncAllowAll(EntityMatrixRow r) {
        boolean all = T(r.getCanCreate()) && T(r.getCanRead())
                && T(r.getCanUpdate()) && T(r.getCanDelete());
        if (T(r.getAllowAll()) != all) {
            r.setAllowAll(all);
            entityMatrixDc.replaceItem(r);
        }
    }

    private void updateEntityAttributesSummarySafe(String entityName) {
        if (syncingAttrSummary)
            return;
        try {
            syncingAttrSummary = true;
            updateEntityAttributesSummary(entityName);
        } finally {
            syncingAttrSummary = false;
        }
    }

    private void updateEntityAttributesSummary(String entityName) {
        if (Strings.isNullOrEmpty(entityName))
            return;

        EntityMatrixRow entityRow = entityMatrixDc.getItems().stream()
                .filter(r -> entityName.equals(r.getEntityName()))
                .findFirst()
                .orElse(null);
        if (entityRow == null)
            return;

        List<AttrMatrixRow> attrs = new ArrayList<>(attrMatrixDc.getItems());
        if (attrs.isEmpty()) {
            entityRow.setAttributes(null);
            entityMatrixDc.replaceItem(entityRow);
            return;
        }

        AttrMatrixRow star = attrs.stream()
                .filter(a -> "*".equals(a.getAttribute()))
                .findFirst()
                .orElse(null);
        List<AttrMatrixRow> normals = attrs.stream()
                .filter(a -> !"*".equals(a.getAttribute()))
                .toList();

        boolean starView = star != null && T(star.getCanView());
        boolean starModify = star != null && T(star.getCanModify());

        boolean fullView = !normals.isEmpty() && normals.stream().allMatch(a -> T(a.getCanView()));
        boolean fullModify = !normals.isEmpty() && normals.stream().allMatch(a -> T(a.getCanModify()));

        String pattern;
        if ((starView && starModify) || (fullView && fullModify)) {
            pattern = "*,*";
        } else if (starView || starModify || fullView || fullModify) {
            pattern = "*";
        } else {
            long selected = normals.stream()
                    .filter(a -> T(a.getCanView()) || T(a.getCanModify()))
                    .count();

            if (selected == 0) {
                pattern = "";
            } else {
                pattern = normals.stream()
                        .filter(a -> T(a.getCanView()) || T(a.getCanModify()))
                        .map(AttrMatrixRow::getAttribute)
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .collect(Collectors.joining(","));
            }
        }

        entityRow.setAttributes(pattern.isBlank() ? null : pattern);
        entityMatrixDc.replaceItem(entityRow);
        attrCache.put(entityName, attrs);
    }

    private List<ResourcePolicyModel> compressWildcard(List<ResourcePolicyModel> src) {
        Map<String, Set<String>> entityWildcardActions = new HashMap<>();

        for (ResourcePolicyModel p : src) {
            if (p.getType() == ResourcePolicyType.ENTITY_ATTRIBUTE
                    && (p.getEffect() == null || p.getEffect() == ResourcePolicyEffect.ALLOW)
                    && p.getResource() != null
                    && p.getResource().endsWith(".*")) {
                String entity = p.getResource().substring(0, p.getResource().length() - 2);
                entityWildcardActions
                        .computeIfAbsent(entity, k -> new HashSet<>())
                        .add(p.getAction());
            }
        }

        if (entityWildcardActions.isEmpty())
            return src;

        return src.stream().filter(p -> {
            if (p.getType() != ResourcePolicyType.ENTITY_ATTRIBUTE
                    || p.getResource() == null
                    || p.getResource().endsWith(".*")) {
                return true;
            }
            int dot = p.getResource().indexOf('.');
            if (dot <= 0)
                return true;

            String entity = p.getResource().substring(0, dot);
            Set<String> w = entityWildcardActions.get(entity);
            return w == null || !w.contains(p.getAction());
        }).collect(Collectors.toList());
    }

    private void addAttrPolicy(List<ResourcePolicyModel> list,
                               String entity,
                               String pattern,
                               String viewId,
                               String modifyId) {
        if ("*".equals(pattern) || "*,*".equals(pattern)) {
            list.add(newAttrPolicy(entity, "*", viewId));
            list.add(newAttrPolicy(entity, "*", modifyId));
        } else {
            for (String attr : pattern.split(",")) {
                String a = attr.trim();
                if (a.isEmpty())
                    continue;
                list.add(newAttrPolicy(entity, a, viewId));
                list.add(newAttrPolicy(entity, a, modifyId));
            }
        }
    }

    private ResourcePolicyModel newEntityPolicy(String entityName, String actionId) {
        ResourcePolicyModel p = metadata.create(ResourcePolicyModel.class);
        p.setType(ResourcePolicyType.ENTITY);
        p.setResource(entityName);
        p.setAction(actionId);
        p.setEffect(ResourcePolicyEffect.ALLOW);
        p.setPolicyGroup("entity");
        return p;
    }

    private ResourcePolicyModel newAttrPolicy(String entityName, String attributePattern, String actionId) {
        ResourcePolicyModel p = metadata.create(ResourcePolicyModel.class);
        p.setType(ResourcePolicyType.ENTITY_ATTRIBUTE);
        String resource = "*".equals(attributePattern)
                ? (entityName + ".*")
                : (entityName + "." + attributePattern);
        p.setResource(resource);
        p.setAction(actionId);
        p.setEffect(ResourcePolicyEffect.ALLOW);
        p.setPolicyGroup("entity-attrs");
        return p;
    }

    private String displayAttrPattern(String pattern) {
        if (pattern == null)
            return null;
        String p = pattern.trim();
        if (p.isEmpty())
            return null;
        if ("*,*".equals(p))
            return "*,*";
        return normalizeAttrPattern(p);
    }

    private String normalizeAttrPattern(String pattern) {
        if (pattern == null)
            return null;
        String trimmed = pattern.trim();
        if (trimmed.isEmpty())
            return null;

        String[] parts = Arrays.stream(trimmed.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        if (parts.length == 0)
            return null;

        for (String p : parts) {
            if ("*".equals(p))
                return "*";
        }

        TreeSet<String> uniq = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        uniq.addAll(Arrays.asList(parts));

        return String.join(",", uniq);
    }

    private static String policyKey(Object type, Object resource, Object action, Object effect, Object group) {
        return (Objects.toString(type, "") + "|"
                + Objects.toString(resource, "") + "|"
                + Objects.toString(action, "") + "|"
                + Objects.toString(effect, "") + "|"
                + Objects.toString(group, ""));
    }

    private static boolean T(Boolean b) {
        return Boolean.TRUE.equals(b);
    }

    private static Boolean bool(Boolean b) {
        return Boolean.TRUE.equals(b);
    }
}
