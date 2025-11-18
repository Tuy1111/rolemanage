package com.vn.rm.view.rolemanage.entityfragment;

import com.google.common.base.Strings;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.HeaderRow;
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
import io.jmix.flowui.view.ViewComponent;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.ResourcePolicyEffect;
import io.jmix.security.model.ResourcePolicyModel;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.securityflowui.view.resourcepolicy.AttributeResourceModel;
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
    private CollectionContainer<AttributeResourceModel> attrMatrixDc;
    @ViewComponent
    private DataGrid<AttributeResourceModel> attrMatrixTable;

    @ViewComponent
    private Span attrEntityLabel;

    // ================================ Services ===============================

    @Autowired
    private ResourcePolicyViewUtils resourcePolicyEditorUtils;
    @Autowired
    private Metadata metadata;

    // Header checkbox cho entity grid
    private Checkbox headerAllowAllCb;
    private Checkbox headerCreateCb;
    private Checkbox headerReadCb;
    private Checkbox headerUpdateCb;
    private Checkbox headerDeleteCb;

    // Header checkbox cho attr grid
    private Checkbox headerAttrViewCb;
    private Checkbox headerAttrModifyCb;

    // tránh vòng lặp khi cập nhật từ rows lên header
    private boolean updatingHeaderFromRows = false;
    private boolean updatingAttrHeaderFromRows = false;

    // =========================== Cache & guards ==============================

    // key = entityName, value = danh sách AttributeResourceModel cho entity đó
    private final Map<String, List<AttributeResourceModel>> attrCache = new HashMap<>();
    private boolean syncingAttrSummary = false;

    // ============================== Lifecycle ================================

    @Subscribe
    public void onReady(Fragment.ReadyEvent event) {
        entityMatrixTable.setSelectionMode(DataGrid.SelectionMode.SINGLE);
        buildMatrixSkeleton();
        installMatrixColumns();
        installAttrColumns();
        initEntityHeader();
        initAttrHeader();
    }

    // ========================================================================
    // ============ API để view cha có thể gọi khi cần ========================
    // ========================================================================

    public void initPolicies(Collection<ResourcePolicyModel> policies) {
        if (policies != null) {
            resourcePoliciesDc.setItems(new ArrayList<>(policies));
        } else {
            resourcePoliciesDc.setItems(Collections.emptyList());
        }

        if (entityMatrixDc.getItems().isEmpty()) {
            entityMatrixTable.setSelectionMode(DataGrid.SelectionMode.SINGLE);
            buildMatrixSkeleton();
            installMatrixColumns();
            installAttrColumns();
        }

        refreshMatrixFromPolicies();
    }

    public List<ResourcePolicyModel> buildPoliciesFromMatrix() {

        // Tất cả entity hợp lệ (bỏ null và "*")
        List<EntityMatrixRow> entityRows = entityMatrixDc.getItems().stream()
                .filter(r -> !Strings.isNullOrEmpty(r.getEntityName()))
                .filter(r -> !"*".equals(r.getEntityName()))
                .collect(Collectors.toList());

        // Build policy chi tiết như hiện tại
        List<ResourcePolicyModel> raw = entityRows.stream()
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

                    if (!(T(r.getCanCreate()) || T(r.getCanRead()) || T(r.getCanUpdate()) || T(r.getCanDelete())))
                        return list.stream();

                    // ENTITY_ATTRIBUTE
                    List<AttributeResourceModel> attrs =
                            Optional.ofNullable(attrCache.get(entity)).orElseGet(List::of);

                    if (attrs.isEmpty()) {
                        String pattern = Strings.nullToEmpty(r.getAttributes()).trim();
                        if (!pattern.isEmpty())
                            addAttrPolicy(list, entity, pattern,
                                    EntityAttributePolicyAction.VIEW.getId(),
                                    EntityAttributePolicyAction.MODIFY.getId());
                        return list.stream();
                    }

                    boolean fullView = !attrs.isEmpty() && attrs.stream().allMatch(a -> T(a.getView()));
                    boolean fullModify = !attrs.isEmpty() && attrs.stream().allMatch(a -> T(a.getModify()));

                    if (fullView || fullModify) {
                        if (fullView) {
                            list.add(newAttrPolicy(entity, "*", EntityAttributePolicyAction.VIEW.getId()));
                        }
                        if (fullModify) {
                            list.add(newAttrPolicy(entity, "*", EntityAttributePolicyAction.MODIFY.getId()));
                        }
                    } else {
                        attrs.forEach(a -> {
                            if (T(a.getView()))
                                list.add(newAttrPolicy(entity, a.getName(),
                                        EntityAttributePolicyAction.VIEW.getId()));
                            if (T(a.getModify()))
                                list.add(newAttrPolicy(entity, a.getName(),
                                        EntityAttributePolicyAction.MODIFY.getId()));
                        });

                        boolean allSelected = attrs.stream()
                                .allMatch(a -> T(a.getView()) || T(a.getModify()));
                        if (allSelected) {
                            list.removeIf(p -> isEntityAttributeType(p.getType())
                                    && p.getResource() != null
                                    && p.getResource().startsWith(entity + ".")
                                    && !p.getResource().equals(entity + ".*"));

                            boolean anyView = attrs.stream().anyMatch(a -> T(a.getView()));
                            boolean anyModify = attrs.stream().anyMatch(a -> T(a.getModify()));
                            if (anyView)
                                list.add(newAttrPolicy(entity, "*", EntityAttributePolicyAction.VIEW.getId()));
                            if (anyModify)
                                list.add(newAttrPolicy(entity, "*", EntityAttributePolicyAction.MODIFY.getId()));
                        }
                    }

                    return list.stream();
                })
                .collect(Collectors.toList());

        // =========================
        // NÉN THÀNH "*" CHO ENTITY
        // =========================

        if (!entityRows.isEmpty()) {
            boolean allCreate = entityRows.stream().allMatch(r -> T(r.getCanCreate()));
            boolean allRead   = entityRows.stream().allMatch(r -> T(r.getCanRead()));
            boolean allUpdate = entityRows.stream().allMatch(r -> T(r.getCanUpdate()));
            boolean allDelete = entityRows.stream().allMatch(r -> T(r.getCanDelete()));

            if (allCreate) {
                raw.removeIf(p -> isEntityType(p.getType())
                        && EntityPolicyAction.CREATE.getId().equals(p.getAction()));
                raw.add(newEntityPolicy("*", EntityPolicyAction.CREATE.getId()));
            }
            if (allRead) {
                raw.removeIf(p -> isEntityType(p.getType())
                        && EntityPolicyAction.READ.getId().equals(p.getAction()));
                raw.add(newEntityPolicy("*", EntityPolicyAction.READ.getId()));
            }
            if (allUpdate) {
                raw.removeIf(p -> isEntityType(p.getType())
                        && EntityPolicyAction.UPDATE.getId().equals(p.getAction()));
                raw.add(newEntityPolicy("*", EntityPolicyAction.UPDATE.getId()));
            }
            if (allDelete) {
                raw.removeIf(p -> isEntityType(p.getType())
                        && EntityPolicyAction.DELETE.getId().equals(p.getAction()));
                raw.add(newEntityPolicy("*", EntityPolicyAction.DELETE.getId()));
            }
        }

        // dedup như cũ
        Set<String> seen = new HashSet<>();
        List<ResourcePolicyModel> dedup = new ArrayList<>();
        for (ResourcePolicyModel p : raw) {
            String key = policyKey(p.getType(), p.getResource(), p.getAction(), p.getEffect(), p.getPolicyGroup());
            if (seen.add(key))
                dedup.add(p);
        }

        return compressWildcard(dedup);
    }


    // ========================================================================
    // ========================= Matrix: Entities grid ========================
    // ========================================================================

    private void buildMatrixSkeleton() {
        Map<String, String> entityOptions = resourcePolicyEditorUtils.getEntityOptionsMap();
        List<EntityMatrixRow> rows = entityOptions.entrySet().stream()
                .filter(e -> !"*".equals(e.getKey()))
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

    // ---------------------------- Entity header -----------------------------

    protected void initEntityHeader() {
        // thêm 1 header row nằm dưới hàng tiêu đề mặc định
        HeaderRow row = entityMatrixTable.appendHeaderRow();

        DataGrid.Column<EntityMatrixRow> entityCol   = entityMatrixTable.getColumns().get(0);
        DataGrid.Column<EntityMatrixRow> allowAllCol = entityMatrixTable.getColumnByKey("allowAllCol");
        DataGrid.Column<EntityMatrixRow> createCol   = entityMatrixTable.getColumnByKey("createCol");
        DataGrid.Column<EntityMatrixRow> readCol     = entityMatrixTable.getColumnByKey("readCol");
        DataGrid.Column<EntityMatrixRow> updateCol   = entityMatrixTable.getColumnByKey("updateCol");
        DataGrid.Column<EntityMatrixRow> deleteCol   = entityMatrixTable.getColumnByKey("deleteCol");

        // text "All entities (*)" dưới cột Entity
        if (entityCol != null) {
            row.getCell(entityCol).setText("All entities (*)");
        }

        // ===== HEADER: ALLOW ALL =====
        if (allowAllCol != null) {
            headerAllowAllCb = new Checkbox();
            headerAllowAllCb.addValueChangeListener(e -> {
                if (updatingHeaderFromRows) {
                    return; // đang sync từ rows lên, không đẩy ngược
                }
                boolean v = Boolean.TRUE.equals(e.getValue());
                for (EntityMatrixRow r : entityMatrixDc.getItems()) {
                    r.setAllowAll(v);
                    r.setCanCreate(v);
                    r.setCanRead(v);
                    r.setCanUpdate(v);
                    r.setCanDelete(v);
                    entityMatrixDc.replaceItem(r);
                }
                // sau khi push xuống rows, sync lại header khác
                updateHeaderAllowAllFromRows();
            });
            row.getCell(allowAllCol).setComponent(headerAllowAllCb);
        }

        // ===== HEADER: CREATE =====
        if (createCol != null) {
            headerCreateCb = new Checkbox();
            headerCreateCb.addValueChangeListener(e -> {
                if (updatingHeaderFromRows) return;

                boolean v = Boolean.TRUE.equals(e.getValue());
                for (EntityMatrixRow r : entityMatrixDc.getItems()) {
                    r.setCanCreate(v);
                    syncAllowAll(r);              // cập nhật allowAll của từng row
                    entityMatrixDc.replaceItem(r);
                }
                updateHeaderAllowAllFromRows();
            });
            row.getCell(createCol).setComponent(headerCreateCb);
        }

        // ===== HEADER: READ =====
        if (readCol != null) {
            headerReadCb = new Checkbox();
            headerReadCb.addValueChangeListener(e -> {
                if (updatingHeaderFromRows) return;

                boolean v = Boolean.TRUE.equals(e.getValue());
                for (EntityMatrixRow r : entityMatrixDc.getItems()) {
                    r.setCanRead(v);
                    syncAllowAll(r);
                    entityMatrixDc.replaceItem(r);
                }
                updateHeaderAllowAllFromRows();
            });
            row.getCell(readCol).setComponent(headerReadCb);
        }

        // ===== HEADER: UPDATE =====
        if (updateCol != null) {
            headerUpdateCb = new Checkbox();
            headerUpdateCb.addValueChangeListener(e -> {
                if (updatingHeaderFromRows) return;

                boolean v = Boolean.TRUE.equals(e.getValue());
                for (EntityMatrixRow r : entityMatrixDc.getItems()) {
                    r.setCanUpdate(v);
                    syncAllowAll(r);
                    entityMatrixDc.replaceItem(r);
                }
                updateHeaderAllowAllFromRows();
            });
            row.getCell(updateCol).setComponent(headerUpdateCb);
        }

        // ===== HEADER: DELETE =====
        if (deleteCol != null) {
            headerDeleteCb = new Checkbox();
            headerDeleteCb.addValueChangeListener(e -> {
                if (updatingHeaderFromRows) return;

                boolean v = Boolean.TRUE.equals(e.getValue());
                for (EntityMatrixRow r : entityMatrixDc.getItems()) {
                    r.setCanDelete(v);
                    syncAllowAll(r);
                    entityMatrixDc.replaceItem(r);
                }
                updateHeaderAllowAllFromRows();
            });
            row.getCell(deleteCol).setComponent(headerDeleteCb);
        }

        // lần đầu sync header theo dữ liệu hiện có
        updateHeaderAllowAllFromRows();
    }

    private void refreshMatrixFromPolicies() {
        List<EntityMatrixRow> rows = new ArrayList<>(entityMatrixDc.getItems());

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

        Map<String, EntityMatrixRow> rowByEntity = rows.stream()
                .filter(r -> !Strings.isNullOrEmpty(r.getEntityName()))
                .filter(r -> !"*".equals(r.getEntityName()))
                .collect(Collectors.toMap(EntityMatrixRow::getEntityName, r -> r));

        for (ResourcePolicyModel p : policies) {
            if (p.getResource() == null)
                continue;

            Object effect = p.getEffect();
            boolean allow;
            if (effect == null) {
                allow = true;
            } else if (effect == ResourcePolicyEffect.ALLOW) {
                allow = true;
            } else {
                String effectStr = effect.toString();
                allow = "ALLOW".equalsIgnoreCase(effectStr);
            }
            if (!allow)
                continue;

            Object type = p.getType();
            boolean isEntity = isEntityType(type);
            boolean isEntityAttr = isEntityAttributeType(type);

            if (!isEntity && !isEntityAttr)
                continue;

            if (isEntity) {
                String resource = p.getResource();
                EntityMatrixRow row = rowByEntity.get(resource);
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

            } else if (isEntityAttr) {
                // nếu có wildcard entity.* thì chỉ set summary = "*"
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

        rows.forEach(this::syncAllowAll);
        entityMatrixDc.setItems(rows);
        updateHeaderAllowAllFromRows();

        preloadAllAttributesFromDbAndFillEntitySummary(policies);

        attrMatrixDc.setItems(Collections.emptyList());
        if (attrEntityLabel != null) {
            attrEntityLabel.setText("");
        }
    }


    private void updateHeaderAllowAllFromRows() {
        // nếu tất cả đều null thì khỏi làm gì
        if (headerAllowAllCb == null
                && headerCreateCb == null
                && headerReadCb == null
                && headerUpdateCb == null
                && headerDeleteCb == null) {
            return;
        }

        updatingHeaderFromRows = true;
        try {
            List<EntityMatrixRow> items = entityMatrixDc.getItems();
            if (items == null || items.isEmpty()) {
                // không có row nào -> set false hết
                if (headerAllowAllCb != null) headerAllowAllCb.setValue(false);
                if (headerCreateCb  != null) headerCreateCb.setValue(false);
                if (headerReadCb    != null) headerReadCb.setValue(false);
                if (headerUpdateCb  != null) headerUpdateCb.setValue(false);
                if (headerDeleteCb  != null) headerDeleteCb.setValue(false);
                return;
            }

            boolean allCreate = items.stream().allMatch(r -> T(r.getCanCreate()));
            boolean allRead   = items.stream().allMatch(r -> T(r.getCanRead()));
            boolean allUpdate = items.stream().allMatch(r -> T(r.getCanUpdate()));
            boolean allDelete = items.stream().allMatch(r -> T(r.getCanDelete()));

            // header từng cột
            if (headerCreateCb != null) headerCreateCb.setValue(allCreate);
            if (headerReadCb   != null) headerReadCb.setValue(allRead);
            if (headerUpdateCb != null) headerUpdateCb.setValue(allUpdate);
            if (headerDeleteCb != null) headerDeleteCb.setValue(allDelete);

            // header Allow all = tất cả các quyền đều true cho mọi row
            boolean allFull = items.stream().allMatch(r ->
                    T(r.getCanCreate()) &&
                            T(r.getCanRead()) &&
                            T(r.getCanUpdate()) &&
                            T(r.getCanDelete())
            );
            if (headerAllowAllCb != null) headerAllowAllCb.setValue(allFull);

        } finally {
            updatingHeaderFromRows = false;
        }
    }

    // ========================================================================
    // ======================== Attribute matrix/header =======================
    // ========================================================================

    private void updateAttrHeaderFromRows() {
        if (headerAttrViewCb == null && headerAttrModifyCb == null) {
            return;
        }

        updatingAttrHeaderFromRows = true;
        try {
            List<AttributeResourceModel> items = attrMatrixDc.getItems();
            if (items == null || items.isEmpty()) {
                if (headerAttrViewCb != null) headerAttrViewCb.setValue(false);
                if (headerAttrModifyCb != null) headerAttrModifyCb.setValue(false);
                return;
            }

            boolean allView   = items.stream().allMatch(r -> T(r.getView()));
            boolean allModify = items.stream().allMatch(r -> T(r.getModify()));

            if (headerAttrViewCb != null)   headerAttrViewCb.setValue(allView);
            if (headerAttrModifyCb != null) headerAttrModifyCb.setValue(allModify);
        } finally {
            updatingAttrHeaderFromRows = false;
        }
    }

    private void preloadAllAttributesFromDbAndFillEntitySummary(Collection<ResourcePolicyModel> policies) {
        List<EntityMatrixRow> rows = new ArrayList<>(entityMatrixDc.getItems());
        for (EntityMatrixRow row : rows) {
            String entity = row.getEntityName();
            if (Strings.isNullOrEmpty(entity) || "*".equals(entity))
                continue;

            List<AttributeResourceModel> attrRows = buildAttrRowsForEntity(entity);
            applyAttrPolicies(attrRows, entity, policies);
            attrCache.put(entity, attrRows);
            String summary = computeAttrSummaryFromRows(attrRows);
            row.setAttributes(summary);
        }
        entityMatrixDc.setItems(rows);
    }

    private void initAttrHeader() {
        HeaderRow row = attrMatrixTable.appendHeaderRow();

        DataGrid.Column<AttributeResourceModel> attrCol   = attrMatrixTable.getColumnByKey("name");
        DataGrid.Column<AttributeResourceModel> viewCol   = attrMatrixTable.getColumnByKey("viewCol");
        DataGrid.Column<AttributeResourceModel> modifyCol = attrMatrixTable.getColumnByKey("modifyCol");

        // text "All attributes (*)" dưới cột Attribute
        if (attrCol != null) {
            row.getCell(attrCol).setText("All attributes (*)");
        }

        // ===== HEADER: VIEW ALL =====
        if (viewCol != null) {
            headerAttrViewCb = new Checkbox();
            headerAttrViewCb.addValueChangeListener(e -> {
                if (updatingAttrHeaderFromRows) return;

                boolean v = Boolean.TRUE.equals(e.getValue());
                attrMatrixDc.getItems().forEach(r -> {
                    r.setView(v);
                    if (v) r.setModify(false); // rule: chỉ 1 trong 2
                });
                attrMatrixDc.setItems(new ArrayList<>(attrMatrixDc.getItems()));

                updateAttrHeaderFromRows();

                EntityMatrixRow current = entityMatrixDc.getItemOrNull();
                if (current != null) {
                    updateEntityAttributesSummarySafe(current.getEntityName());
                }
            });
            row.getCell(viewCol).setComponent(headerAttrViewCb);
        }

        // ===== HEADER: MODIFY ALL =====
        if (modifyCol != null) {
            headerAttrModifyCb = new Checkbox();
            headerAttrModifyCb.addValueChangeListener(e -> {
                if (updatingAttrHeaderFromRows) return;

                boolean v = Boolean.TRUE.equals(e.getValue());
                attrMatrixDc.getItems().forEach(r -> {
                    r.setModify(v);
                    if (v) r.setView(false);
                });
                attrMatrixDc.setItems(new ArrayList<>(attrMatrixDc.getItems()));

                updateAttrHeaderFromRows();

                EntityMatrixRow current = entityMatrixDc.getItemOrNull();
                if (current != null) {
                    updateEntityAttributesSummarySafe(current.getEntityName());
                }
            });
            row.getCell(modifyCol).setComponent(headerAttrModifyCb);
        }

        // sync lần đầu theo dữ liệu hiện tại
        updateAttrHeaderFromRows();
    }

    private String computeAttrSummaryFromRows(List<AttributeResourceModel> rows) {
        if (rows == null || rows.isEmpty())
            return null;

        boolean fullView = !rows.isEmpty() && rows.stream().allMatch(a -> T(a.getView()));
        boolean fullModify = !rows.isEmpty() && rows.stream().allMatch(a -> T(a.getModify()));

        if (fullView && fullModify)
            return "*,*";
        if (fullView || fullModify)
            return "*";

        long selected = rows.stream()
                .filter(a -> T(a.getView()) || T(a.getModify()))
                .count();
        if (selected == 0)
            return null;

        return rows.stream()
                .filter(a -> T(a.getView()) || T(a.getModify()))
                .map(AttributeResourceModel::getName)
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

        List<AttributeResourceModel> rows = attrCache.get(entityName);
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
        updateAttrHeaderFromRows();
        updateEntityAttributesSummarySafe(entityName);
    }

    private List<AttributeResourceModel> buildAttrRowsForEntity(String entityName) {
        Map<String, String> attrs = resourcePolicyEditorUtils.getEntityAttributeOptionsMap(entityName);

        if (attrs.isEmpty()) {
            return List.of();
        }

        return attrs.entrySet().stream()
                // BỎ DÒNG "*" Ở ĐÂY
                .filter(e -> !"*".equals(e.getKey()))
                .map(e -> {
                    AttributeResourceModel r = metadata.create(AttributeResourceModel.class);
                    r.setName(e.getKey());
                    r.setCaption(e.getValue());
                    r.setView(false);
                    r.setModify(false);
                    return r;
                })
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .collect(Collectors.toList());
    }


    private void applyAttrPolicies(List<AttributeResourceModel> rows,
                                   String entityName,
                                   Collection<ResourcePolicyModel> policies) {

        Map<String, List<ResourcePolicyModel>> byRes = policies.stream()
                .filter(p -> isEntityAttributeType(p.getType()))
                .filter(p -> p.getResource() != null && p.getResource().startsWith(entityName + "."))
                .filter(p -> p.getEffect() == null || p.getEffect() == ResourcePolicyEffect.ALLOW)
                .collect(Collectors.groupingBy(ResourcePolicyModel::getResource));

        // wildcard entity.*
        String wildcardRes = entityName + ".*";
        List<ResourcePolicyModel> starPolicies = byRes.getOrDefault(wildcardRes, List.of());
        boolean wildcardView = starPolicies.stream()
                .anyMatch(p -> EntityAttributePolicyAction.VIEW.getId().equals(p.getAction()));
        boolean wildcardModify = starPolicies.stream()
                .anyMatch(p -> EntityAttributePolicyAction.MODIFY.getId().equals(p.getAction()));

        rows.forEach(r -> {
            String attr = r.getName();
            if (attr == null)
                return;

            // từ wildcard
            if (wildcardView) r.setView(true);
            if (wildcardModify) r.setModify(true);

            // từ policy chi tiết
            String res = entityName + "." + attr;
            List<ResourcePolicyModel> ps = byRes.getOrDefault(res, List.of());
            ps.forEach(p -> {
                if (EntityAttributePolicyAction.VIEW.getId().equals(p.getAction()))
                    r.setView(true);
                if (EntityAttributePolicyAction.MODIFY.getId().equals(p.getAction()))
                    r.setModify(true);
            });
        });
    }

    // ============================ UI columns ================================

    private void installMatrixColumns() {
        // ===== ALLOW ALL (theo từng entity) =====
        DataGrid.Column<EntityMatrixRow> allowAllCol = entityMatrixTable.getColumnByKey("allowAllCol");
        if (allowAllCol != null) {
            allowAllCol.setRenderer(new ComponentRenderer<>(row -> {
                Checkbox cb = new Checkbox(T(row.getAllowAll()));
                cb.addValueChangeListener(e -> {
                    boolean v = bool(e.getValue());
                    row.setAllowAll(v);

                    // nếu bật Allow all cho entity -> bật hết CRUD
                    if (v) {
                        row.setCanCreate(true);
                        row.setCanRead(true);
                        row.setCanUpdate(true);
                        row.setCanDelete(true);
                    }

                    entityMatrixDc.replaceItem(row);
                    updateHeaderAllowAllFromRows();   // sync header
                });
                return cb;
            }));
        }

        // ===== CREATE =====
        DataGrid.Column<EntityMatrixRow> createCol = entityMatrixTable.getColumnByKey("createCol");
        if (createCol != null) {
            createCol.setRenderer(new ComponentRenderer<>(row -> {
                Checkbox cb = new Checkbox();
                boolean value = T(row.getCanCreate());
                cb.setValue(value);

                // nếu quyền này đang true và entity đang Allow all -> hiển thị dấu trừ
                if (T(row.getAllowAll()) && value) {
                    cb.setIndeterminate(true);
                }

                cb.addValueChangeListener(e -> {
                    boolean v = bool(e.getValue());
                    row.setCanCreate(v);

                    // user chỉnh tay ô này -> không còn trạng thái "kế thừa"
                    cb.setIndeterminate(false);

                    entityMatrixDc.replaceItem(row);
                    syncAllowAll(row);             // xem còn đủ CRUD để giữ Allow all không
                    updateHeaderAllowAllFromRows();
                });
                return cb;
            }));
        }

        // ===== READ =====
        DataGrid.Column<EntityMatrixRow> readCol = entityMatrixTable.getColumnByKey("readCol");
        if (readCol != null) {
            readCol.setRenderer(new ComponentRenderer<>(row -> {
                Checkbox cb = new Checkbox();
                boolean value = T(row.getCanRead());
                cb.setValue(value);

                if (T(row.getAllowAll()) && value) {
                    cb.setIndeterminate(true);
                }

                cb.addValueChangeListener(e -> {
                    boolean v = bool(e.getValue());
                    row.setCanRead(v);
                    cb.setIndeterminate(false);

                    entityMatrixDc.replaceItem(row);
                    syncAllowAll(row);
                    updateHeaderAllowAllFromRows();
                });
                return cb;
            }));
        }

        // ===== UPDATE =====
        DataGrid.Column<EntityMatrixRow> updateCol = entityMatrixTable.getColumnByKey("updateCol");
        if (updateCol != null) {
            updateCol.setRenderer(new ComponentRenderer<>(row -> {
                Checkbox cb = new Checkbox();
                boolean value = T(row.getCanUpdate());
                cb.setValue(value);

                if (T(row.getAllowAll()) && value) {
                    cb.setIndeterminate(true);
                }

                cb.addValueChangeListener(e -> {
                    boolean v = bool(e.getValue());
                    row.setCanUpdate(v);
                    cb.setIndeterminate(false);

                    entityMatrixDc.replaceItem(row);
                    syncAllowAll(row);
                    updateHeaderAllowAllFromRows();
                });
                return cb;
            }));
        }

        // ===== DELETE =====
        DataGrid.Column<EntityMatrixRow> deleteCol = entityMatrixTable.getColumnByKey("deleteCol");
        if (deleteCol != null) {
            deleteCol.setRenderer(new ComponentRenderer<>(row -> {
                Checkbox cb = new Checkbox();
                boolean value = T(row.getCanDelete());
                cb.setValue(value);

                if (T(row.getAllowAll()) && value) {
                    cb.setIndeterminate(true);
                }

                cb.addValueChangeListener(e -> {
                    boolean v = bool(e.getValue());
                    row.setCanDelete(v);
                    cb.setIndeterminate(false);

                    entityMatrixDc.replaceItem(row);
                    syncAllowAll(row);
                    updateHeaderAllowAllFromRows();
                });
                return cb;
            }));
        }

        // ===== ATTRIBUTES SUMMARY =====
        DataGrid.Column<EntityMatrixRow> attrCol = entityMatrixTable.getColumnByKey("attributesCol");
        if (attrCol != null) {
            attrCol.setRenderer(new ComponentRenderer<>(row -> {
                TextField tf = new TextField();
                tf.setWidthFull();
                tf.setReadOnly(true);
                tf.setValue(Objects.toString(displayAttrPattern(row.getAttributes()), ""));
                return tf;
            }));
        }
    }


    private void installAttrColumns() {
        // ===== VIEW =====
        DataGrid.Column<AttributeResourceModel> viewCol = attrMatrixTable.getColumnByKey("viewCol");
        if (viewCol != null) {
            viewCol.setRenderer(new ComponentRenderer<>(row -> {
                Checkbox cb = new Checkbox();

                boolean value = T(row.getView());
                cb.setValue(value);

                // Nếu header "View all" đang bật và ô này đang true -> hiển thị dấu trừ
                if (headerAttrViewCb != null
                        && Boolean.TRUE.equals(headerAttrViewCb.getValue())
                        && value) {
                    cb.setIndeterminate(true);
                }

                cb.addValueChangeListener(e -> {
                    boolean v = T(e.getValue());

                    row.setView(v);
                    if (v) {
                        // bật View thì tắt Modify (rule chỉ 1 trong 2)
                        row.setModify(false);
                    }

                    // user chỉnh tay -> không còn trạng thái "kế thừa"
                    cb.setIndeterminate(false);

                    attrMatrixDc.replaceItem(row);
                    attrMatrixDc.setItems(new ArrayList<>(attrMatrixDc.getItems()));

                    updateAttrHeaderFromRows();

                    EntityMatrixRow current = entityMatrixDc.getItemOrNull();
                    if (current != null) {
                        updateEntityAttributesSummarySafe(current.getEntityName());
                    }
                });
                return cb;
            }));
        }

        // ===== MODIFY =====
        DataGrid.Column<AttributeResourceModel> modifyCol = attrMatrixTable.getColumnByKey("modifyCol");
        if (modifyCol != null) {
            modifyCol.setRenderer(new ComponentRenderer<>(row -> {
                Checkbox cb = new Checkbox();

                boolean value = T(row.getModify());
                cb.setValue(value);

                // Nếu header "Modify all" đang bật và ô này đang true -> hiển thị dấu trừ
                if (headerAttrModifyCb != null
                        && Boolean.TRUE.equals(headerAttrModifyCb.getValue())
                        && value) {
                    cb.setIndeterminate(true);
                }

                cb.addValueChangeListener(e -> {
                    boolean v = T(e.getValue());

                    row.setModify(v);
                    if (v) {
                        // bật Modify thì tắt View
                        row.setView(false);
                    }

                    cb.setIndeterminate(false);

                    attrMatrixDc.replaceItem(row);
                    attrMatrixDc.setItems(new ArrayList<>(attrMatrixDc.getItems()));

                    updateAttrHeaderFromRows();

                    EntityMatrixRow current = entityMatrixDc.getItemOrNull();
                    if (current != null) {
                        updateEntityAttributesSummarySafe(current.getEntityName());
                    }
                });
                return cb;
            }));
        }
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

    private boolean isEntityType(Object type) {
        if (type == null) return false;
        if (type == ResourcePolicyType.ENTITY) return true;
        String typeStr = type.toString().toLowerCase();
        return "entity".equals(typeStr);
    }

    private boolean isEntityAttributeType(Object type) {
        if (type == null) return false;
        if (type == ResourcePolicyType.ENTITY_ATTRIBUTE) return true;
        String typeStr = type.toString().toLowerCase();
        return "entityattribute".equals(typeStr) || "entity_attribute".equals(typeStr);
    }

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

        List<AttributeResourceModel> attrs = new ArrayList<>(attrMatrixDc.getItems());
        if (attrs.isEmpty()) {
            entityRow.setAttributes(null);
            entityMatrixDc.replaceItem(entityRow);
            return;
        }

        boolean fullView = !attrs.isEmpty() && attrs.stream().allMatch(a -> T(a.getView()));
        boolean fullModify = !attrs.isEmpty() && attrs.stream().allMatch(a -> T(a.getModify()));

        String pattern;
        if (fullView && fullModify) {
            pattern = "*,*";
        } else if (fullView || fullModify) {
            pattern = "*";
        } else {
            long selected = attrs.stream()
                    .filter(a -> T(a.getView()) || T(a.getModify()))
                    .count();

            if (selected == 0) {
                pattern = "";
            } else {
                pattern = attrs.stream()
                        .filter(a -> T(a.getView()) || T(a.getModify()))
                        .map(AttributeResourceModel::getName)
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
            if (isEntityAttributeType(p.getType())
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
            if (!isEntityAttributeType(p.getType())
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
