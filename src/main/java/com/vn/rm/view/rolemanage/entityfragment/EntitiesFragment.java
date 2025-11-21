package com.vn.rm.view.rolemanage.entityfragment;

import com.google.common.base.Strings;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vn.rm.view.rolemanage.service.RoleManagerService;
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


    @Autowired
    private RoleManagerService roleManagerService;

    private Checkbox headerAllowAllCb;
    private Checkbox headerCreateCb;
    private Checkbox headerReadCb;
    private Checkbox headerUpdateCb;
    private Checkbox headerDeleteCb;

    private Checkbox headerAttrViewCb;
    private Checkbox headerAttrModifyCb;

    private boolean updatingHeaderFromRows = false;
    private boolean updatingAttrHeaderFromRows = false;


    private final Map<String, List<AttributeResourceModel>> attrCache = new HashMap<>();
    private boolean syncingAttrSummary = false;


    @Subscribe
    public void onReady(Fragment.ReadyEvent event) {
        entityMatrixTable.setSelectionMode(DataGrid.SelectionMode.SINGLE);

        List<EntityMatrixRow> rows = roleManagerService.createMatrixEntity();
        entityMatrixDc.setItems(rows);

        installMatrixColumns();
        installAttrColumns();
        initEntityHeader();
        initAttrHeader();
    }

    @Subscribe(id = "entityMatrixDc", target = Target.DATA_CONTAINER)
    public void onEntityMatrixItemChange(CollectionContainer.ItemChangeEvent<EntityMatrixRow> e) {
        EntityMatrixRow row = e.getItem();
        if (row == null) {
            if (attrEntityLabel != null) {
                attrEntityLabel.setText("");
            }
            attrMatrixDc.setItems(Collections.emptyList());
            return;
        }

        String cap = Optional.ofNullable(row.getEntityCaption()).orElse(row.getEntityName());
        if (attrEntityLabel != null) {
            attrEntityLabel.setText("Entity: " + cap + " (" + row.getEntityName() + ")");
        }
        loadAttributesForEntity(row.getEntityName());
    }


    public void initPolicies(Collection<ResourcePolicyModel> policies) {
        if (policies != null) {
            resourcePoliciesDc.setItems(new ArrayList<>(policies));
        } else {
            resourcePoliciesDc.setItems(Collections.emptyList());
        }

        if (entityMatrixDc.getItems().isEmpty()) {
            entityMatrixTable.setSelectionMode(DataGrid.SelectionMode.SINGLE);
            List<EntityMatrixRow> rows = roleManagerService.createMatrixEntity();
            entityMatrixDc.setItems(rows);
            installMatrixColumns();
            installAttrColumns();
            initEntityHeader();
            initAttrHeader();
        }

        refreshMatrixFromPolicies();
    }


    private void refreshMatrixFromPolicies() {
        List<EntityMatrixRow> rows = new ArrayList<>(entityMatrixDc.getItems());
        Collection<ResourcePolicyModel> policies =
                Optional.ofNullable(resourcePoliciesDc.getItems()).orElseGet(List::of);

        roleManagerService.updateEntityMatrix(rows, policies, attrCache);

        entityMatrixDc.setItems(rows);
        updateHeaderAllowAllFromRows();

        attrMatrixDc.setItems(Collections.emptyList());
        if (attrEntityLabel != null) {
            attrEntityLabel.setText("");
        }
    }

    // ========================= Matrix: Entities grid ========================

    // ---------------------------- Entity header -----------------------------

    protected void initEntityHeader() {
        HeaderRow row = entityMatrixTable.appendHeaderRow();

        DataGrid.Column<EntityMatrixRow> entityCol = entityMatrixTable.getColumns().isEmpty()
                ? null
                : entityMatrixTable.getColumns().get(0);
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

                // snapshot
                List<EntityMatrixRow> items = new ArrayList<>(entityMatrixDc.getItems());
                for (EntityMatrixRow r : items) {
                    r.setAllowAll(v);
                    r.setCanCreate(v);
                    r.setCanRead(v);
                    r.setCanUpdate(v);
                    r.setCanDelete(v);
                }
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
                if (updatingHeaderFromRows) {
                    return;
                }

                boolean v = Boolean.TRUE.equals(e.getValue());
                List<EntityMatrixRow> items = new ArrayList<>(entityMatrixDc.getItems());
                for (EntityMatrixRow r : items) {
                    r.setCanCreate(v);
                    roleManagerService.syncAllowAll(r);
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
                if (updatingHeaderFromRows) {
                    return;
                }

                boolean v = Boolean.TRUE.equals(e.getValue());
                List<EntityMatrixRow> items = new ArrayList<>(entityMatrixDc.getItems());
                for (EntityMatrixRow r : items) {
                    r.setCanRead(v);
                    roleManagerService.syncAllowAll(r);
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
                if (updatingHeaderFromRows) {
                    return;
                }

                boolean v = Boolean.TRUE.equals(e.getValue());
                List<EntityMatrixRow> items = new ArrayList<>(entityMatrixDc.getItems());
                for (EntityMatrixRow r : items) {
                    r.setCanUpdate(v);
                    roleManagerService.syncAllowAll(r);
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
                if (updatingHeaderFromRows) {
                    return;
                }

                boolean v = Boolean.TRUE.equals(e.getValue());
                List<EntityMatrixRow> items = new ArrayList<>(entityMatrixDc.getItems());
                for (EntityMatrixRow r : items) {
                    r.setCanDelete(v);
                    roleManagerService.syncAllowAll(r);
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
                if (updatingAttrHeaderFromRows) {
                    return;
                }

                boolean v = Boolean.TRUE.equals(e.getValue());
                attrMatrixDc.getItems().forEach(r -> {
                    r.setView(v);
                    if (v) {
                        r.setModify(false);
                    }
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
                if (updatingAttrHeaderFromRows) {
                    return;
                }

                boolean v = Boolean.TRUE.equals(e.getValue());
                attrMatrixDc.getItems().forEach(r -> {
                    r.setModify(v);
                    if (v) {
                        r.setView(false);
                    }
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
            if (attrEntityLabel != null) {
                attrEntityLabel.setText("");
            }
            attrMatrixDc.setItems(Collections.emptyList());
            return;
        }

        if (attrEntityLabel != null) {
            attrEntityLabel.setText("Entity: " + entityName);
        }

        // Dữ liệu attributes đã được RoleManagerService.updateEntityMatrix()
        // preload vào attrCache dựa trên policies (nếu initPolicies đã chạy)
        List<AttributeResourceModel> rows = attrCache.get(entityName);

        // Trường hợp chưa có trong cache (chưa initPolicies hoặc entity mới)
        if (rows == null) {
            rows = roleManagerService.buildAttrRowsForEntity(entityName);
            attrCache.put(entityName, rows);
        }

        // Luôn truyền bản copy cho container
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
                    roleManagerService.syncAllowAll(row);
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
                    roleManagerService.syncAllowAll(row);
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
                    roleManagerService.syncAllowAll(row);
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
                    roleManagerService.syncAllowAll(row);
                    updateHeaderAllowAllFromRows();
                });
                return cb;
            }));
        }

        // ===== ATTRIBUTES SUMMARY =====
        DataGrid.Column<EntityMatrixRow> attrCol = entityMatrixTable.getColumnByKey("attributesCol");
        if (attrCol != null) {
            attrCol.setRenderer(new ComponentRenderer<>(row -> {
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
        DataGrid.Column<AttributeResourceModel> nameCol = attrMatrixTable.getColumnByKey("name");
        if (nameCol != null) {
            nameCol.setRenderer(new ComponentRenderer<>(row -> {
                Span span = new Span();

                String caption = Optional.ofNullable(row.getCaption()).orElse("");
                String name = Optional.ofNullable(row.getName()).orElse("");

                String text = !caption.isEmpty() ? caption : name;
                span.setText(text);
                return span;
            }));
        }

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

    // ======================= Utils & helpers ================================

    private void updateEntityAttributesSummarySafe(String entityName) {
        if (syncingAttrSummary) {
            return;
        }
        try {
            syncingAttrSummary = true;
            updateEntityAttributesSummary(entityName);
        } finally {
            syncingAttrSummary = false;
        }
    }

    private void updateEntityAttributesSummary(String entityName) {
        roleManagerService.updateEntityAttributesSummary(
                entityName,
                entityMatrixDc.getItems(),
                attrMatrixDc.getItems(),
                attrCache
        );
        entityMatrixDc.setItems(new ArrayList<>(entityMatrixDc.getItems()));
    }

    private static boolean T(Boolean b) {
        return Boolean.TRUE.equals(b);
    }

    private static Boolean bool(Boolean b) {
        return Boolean.TRUE.equals(b);
    }

    /**
     * Cho view cha gọi để build lại tập ResourcePolicyModel từ matrix.
     */
    public List<ResourcePolicyModel> buildPoliciesFromMatrix() {
        return roleManagerService.buildPoliciesFromMatrix(
                new ArrayList<>(entityMatrixDc.getItems()),
                attrCache
        );
    }

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
        if (headerAttrViewCb != null) headerAttrViewCb.setValue(false);
        if (headerAttrModifyCb != null) headerAttrModifyCb.setValue(false);
    }
}
