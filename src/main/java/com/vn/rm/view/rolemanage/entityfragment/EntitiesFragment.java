package com.vn.rm.view.rolemanage.entityfragment;

import com.google.common.base.Strings;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.security.model.ResourcePolicyModel;
import io.jmix.securityflowui.view.resourcepolicy.AttributeResourceModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

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
    private EntityPolicyMatrixService entityMatrixService;

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

        // dùng service build skeleton và set vào data container
        List<EntityMatrixRow> rows = entityMatrixService.buildMatrixSkeleton();
        entityMatrixDc.setItems(rows);

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
            List<EntityMatrixRow> rows = entityMatrixService.buildMatrixSkeleton();
            entityMatrixDc.setItems(rows);
            installMatrixColumns();
            installAttrColumns();
        }

        refreshMatrixFromPolicies();
    }

    /**
     * Wrapper gọi service để áp policies vào matrix + preload attr.
     */
    private void refreshMatrixFromPolicies() {
        List<EntityMatrixRow> rows = new ArrayList<>(entityMatrixDc.getItems());
        Collection<ResourcePolicyModel> policies =
                Optional.ofNullable(resourcePoliciesDc.getItems()).orElseGet(List::of);

        entityMatrixService.refreshMatrixFromPolicies(rows, policies, attrCache);

        entityMatrixDc.setItems(rows);
        updateHeaderAllowAllFromRows();

        // reset bảng attributes bên phải
        attrMatrixDc.setItems(Collections.emptyList());
        if (attrEntityLabel != null) {
            attrEntityLabel.setText("");
        }
    }

    // ========================================================================
    // ========================= Matrix: Entities grid ========================
    // ========================================================================

    // ---------------------------- Entity header -----------------------------

    protected void initEntityHeader() {
        HeaderRow row = entityMatrixTable.appendHeaderRow();

        DataGrid.Column<EntityMatrixRow> entityCol = entityMatrixTable.getColumns().get(0);
        DataGrid.Column<EntityMatrixRow> allowAllCol = entityMatrixTable.getColumnByKey("allowAllCol");
        DataGrid.Column<EntityMatrixRow> createCol = entityMatrixTable.getColumnByKey("createCol");
        DataGrid.Column<EntityMatrixRow> readCol = entityMatrixTable.getColumnByKey("readCol");
        DataGrid.Column<EntityMatrixRow> updateCol = entityMatrixTable.getColumnByKey("updateCol");
        DataGrid.Column<EntityMatrixRow> deleteCol = entityMatrixTable.getColumnByKey("deleteCol");

        // text "All entities (*)" dưới cột Entity
        if (entityCol != null) {
            row.getCell(entityCol).setText("All entities (*)");
        }

        // ===== HEADER: ALLOW ALL =====
        if (allowAllCol != null) {
            headerAllowAllCb = new Checkbox();
            headerAllowAllCb.addValueChangeListener(e -> {
                if (updatingHeaderFromRows) {
                    return;
                }
                boolean v = Boolean.TRUE.equals(e.getValue());

                // lấy snapshot
                List<EntityMatrixRow> items = new ArrayList<>(entityMatrixDc.getItems());
                for (EntityMatrixRow r : items) {
                    r.setAllowAll(v);
                    r.setCanCreate(v);
                    r.setCanRead(v);
                    r.setCanUpdate(v);
                    r.setCanDelete(v);
                }
                // set lại 1 lần, KHÔNG dùng replaceItem trong for-each
                entityMatrixDc.setItems(items);
                if (!v) {
                    resetAllAttributesFlags();
                }
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

                List<EntityMatrixRow> items = new ArrayList<>(entityMatrixDc.getItems());
                for (EntityMatrixRow r : items) {
                    r.setCanCreate(v);
                    syncAllowAll(r);
                }
                entityMatrixDc.setItems(items);

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

                List<EntityMatrixRow> items = new ArrayList<>(entityMatrixDc.getItems());
                for (EntityMatrixRow r : items) {
                    r.setCanRead(v);
                    syncAllowAll(r);
                }
                entityMatrixDc.setItems(items);

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

                List<EntityMatrixRow> items = new ArrayList<>(entityMatrixDc.getItems());
                for (EntityMatrixRow r : items) {
                    r.setCanUpdate(v);
                    syncAllowAll(r);
                }
                entityMatrixDc.setItems(items);

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

                List<EntityMatrixRow> items = new ArrayList<>(entityMatrixDc.getItems());
                for (EntityMatrixRow r : items) {
                    r.setCanDelete(v);
                    syncAllowAll(r);
                }
                entityMatrixDc.setItems(items);

                updateHeaderAllowAllFromRows();
            });
            row.getCell(deleteCol).setComponent(headerDeleteCb);
        }
    }


    private void updateHeaderAllowAllFromRows() {
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
                if (headerAllowAllCb != null) headerAllowAllCb.setValue(false);
                if (headerCreateCb != null) headerCreateCb.setValue(false);
                if (headerReadCb != null) headerReadCb.setValue(false);
                if (headerUpdateCb != null) headerUpdateCb.setValue(false);
                if (headerDeleteCb != null) headerDeleteCb.setValue(false);
                return;
            }

            boolean allCreate = items.stream().allMatch(r -> T(r.getCanCreate()));
            boolean allRead = items.stream().allMatch(r -> T(r.getCanRead()));
            boolean allUpdate = items.stream().allMatch(r -> T(r.getCanUpdate()));
            boolean allDelete = items.stream().allMatch(r -> T(r.getCanDelete()));

            if (headerCreateCb != null) headerCreateCb.setValue(allCreate);
            if (headerReadCb != null) headerReadCb.setValue(allRead);
            if (headerUpdateCb != null) headerUpdateCb.setValue(allUpdate);
            if (headerDeleteCb != null) headerDeleteCb.setValue(allDelete);

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

            boolean allView = items.stream().allMatch(r -> T(r.getView()));
            boolean allModify = items.stream().allMatch(r -> T(r.getModify()));

            if (headerAttrViewCb != null) headerAttrViewCb.setValue(allView);
            if (headerAttrModifyCb != null) headerAttrModifyCb.setValue(allModify);
        } finally {
            updatingAttrHeaderFromRows = false;
        }
    }

    private void initAttrHeader() {
        HeaderRow row = attrMatrixTable.appendHeaderRow();

        DataGrid.Column<AttributeResourceModel> attrCol = attrMatrixTable.getColumnByKey("name");
        DataGrid.Column<AttributeResourceModel> viewCol = attrMatrixTable.getColumnByKey("viewCol");
        DataGrid.Column<AttributeResourceModel> modifyCol = attrMatrixTable.getColumnByKey("modifyCol");

        if (attrCol != null) {
            row.getCell(attrCol).setText("All attributes (*)");
        }

        if (viewCol != null) {
            headerAttrViewCb = new Checkbox();
            headerAttrViewCb.addValueChangeListener(e -> {
                if (updatingAttrHeaderFromRows) return;

                boolean v = Boolean.TRUE.equals(e.getValue());
                attrMatrixDc.getItems().forEach(r -> {
                    r.setView(v);
                    if (v) r.setModify(false);
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
        updateAttrHeaderFromRows();
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
            rows = entityMatrixService.buildAttrRowsForEntity(entityName);
            entityMatrixService.applyAttrPolicies(
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

                    if (v) {
                        row.setCanCreate(true);
                        row.setCanRead(true);
                        row.setCanUpdate(true);
                        row.setCanDelete(true);
                    }
                    entityMatrixDc.replaceItem(row);
                    updateHeaderAllowAllFromRows();
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

                if (T(row.getAllowAll()) && value) {
                    cb.setIndeterminate(true);
                }

                cb.addValueChangeListener(e -> {
                    boolean v = bool(e.getValue());
                    row.setCanCreate(v);
                    cb.setIndeterminate(false);

                    entityMatrixDc.replaceItem(row);
                    syncAllowAll(row);
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

        // ===== ATTRIBUTES SUMMARY ===== (nếu cần renderer sau này)
        DataGrid.Column<EntityMatrixRow> attrCol = entityMatrixTable.getColumnByKey("attributesCol");
        if (attrCol != null) {
            attrCol.setRenderer(new ComponentRenderer<>(row -> {
                // chỉ hiển thị text giống trước đây nếu bạn muốn
                com.vaadin.flow.component.textfield.TextField tf =
                        new com.vaadin.flow.component.textfield.TextField();
                tf.setWidthFull();
                tf.setReadOnly(true);
                tf.setValue(Objects.toString(row.getAttributes(), ""));
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

                if (headerAttrViewCb != null
                        && Boolean.TRUE.equals(headerAttrViewCb.getValue())
                        && value) {
                    cb.setIndeterminate(true);
                }

                cb.addValueChangeListener(e -> {
                    boolean v = T(e.getValue());

                    row.setView(v);
                    if (v) {
                        row.setModify(false);
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

        // ===== MODIFY =====
        DataGrid.Column<AttributeResourceModel> modifyCol = attrMatrixTable.getColumnByKey("modifyCol");
        if (modifyCol != null) {
            modifyCol.setRenderer(new ComponentRenderer<>(row -> {
                Checkbox cb = new Checkbox();

                boolean value = T(row.getModify());
                cb.setValue(value);

                if (headerAttrModifyCb != null
                        && Boolean.TRUE.equals(headerAttrModifyCb.getValue())
                        && value) {
                    cb.setIndeterminate(true);
                }

                cb.addValueChangeListener(e -> {
                    boolean v = T(e.getValue());

                    row.setModify(v);
                    if (v) {
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

    private void syncAllowAll(EntityMatrixRow r) {
        // dùng logic trong service
        entityMatrixService.syncAllowAll(r);
        // entityMatrixDc.replaceItem(r); // đã gọi chỗ khác sau khi set
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
        entityMatrixService.updateEntityAttributesSummary(
                entityName,
                entityMatrixDc.getItems(),
                attrMatrixDc.getItems(),
                attrCache
        );
        // refresh container để UI cập nhật
        entityMatrixDc.setItems(new ArrayList<>(entityMatrixDc.getItems()));
    }

    private static boolean T(Boolean b) {
        return Boolean.TRUE.equals(b);
    }

    private static Boolean bool(Boolean b) {
        return Boolean.TRUE.equals(b);
    }

    // Nếu bạn cần buildPoliciesFromMatrix() cho view cha:
    public List<ResourcePolicyModel> buildPoliciesFromMatrix() {
        return entityMatrixService.buildPoliciesFromMatrix(
                new ArrayList<>(entityMatrixDc.getItems()),
                attrCache
        );
    }

    // reset toàn bộ attribute: view/modify = false
    private void resetAllAttributesFlags() {
        // 1) reset trong cache
        attrCache.values().forEach(list -> {
            for (AttributeResourceModel a : list) {
                a.setView(false);
                a.setModify(false);
            }
        });

        // 2) reset grid đang hiển thị bên phải (nếu có)
        List<AttributeResourceModel> current = new ArrayList<>(attrMatrixDc.getItems());
        if (!current.isEmpty()) {
            current.forEach(a -> {
                a.setView(false);
                a.setModify(false);
            });
            attrMatrixDc.setItems(current);
        }

        // 3) reset summary attributes trên tất cả entity
        List<EntityMatrixRow> entities = new ArrayList<>(entityMatrixDc.getItems());
        for (EntityMatrixRow r : entities) {
            r.setAttributes(null);
        }
        entityMatrixDc.setItems(entities);

        // 4) reset header View/Modify của bảng attr
        if (headerAttrViewCb != null)   headerAttrViewCb.setValue(false);
        if (headerAttrModifyCb != null) headerAttrModifyCb.setValue(false);
    }
}