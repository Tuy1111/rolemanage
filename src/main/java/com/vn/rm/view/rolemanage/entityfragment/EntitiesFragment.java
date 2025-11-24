package com.vn.rm.view.rolemanage.entityfragment;

import com.google.common.base.Strings;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
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
import java.util.function.BiConsumer;
import java.util.function.Function;

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

    // Headers Entity
    private Checkbox headerAllowAllCb;
    private Checkbox headerCreateCb;
    private Checkbox headerReadCb;
    private Checkbox headerUpdateCb;
    private Checkbox headerDeleteCb;

    // Headers Attribute
    private Checkbox headerAttrViewCb;
    private Checkbox headerAttrModifyCb;

    // Flags tránh loop
    private boolean updatingHeaderFromRows = false;
    private boolean updatingAttrHeaderFromRows = false;
    private boolean syncingAttrSummary = false;

    // Caches
    private final Map<String, List<AttributeResourceModel>> attrCache = new HashMap<>();
    private final Map<String, Checkbox> entityCellCache = new HashMap<>();

    // Cache text field attributes summary để update ko cần reload grid
    private final Map<String, TextField> entityAttrFieldCache = new HashMap<>();

    // ========================================================================
    // Lifecycle
    // ========================================================================

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

        // CLEAR cache trước khi gán list mới để tránh leak memory/component cũ
        entityCellCache.clear();
        entityAttrFieldCache.clear();

        entityMatrixDc.setItems(rows);
        updateHeaderAllowAllFromRows();

        attrMatrixDc.setItems(Collections.emptyList());
        if (attrEntityLabel != null) {
            attrEntityLabel.setText("");
        }
    }

    // ========================================================================
    // Matrix: Entities grid - Header
    // ========================================================================

    protected void initEntityHeader() {
        HeaderRow row = entityMatrixTable.appendHeaderRow();

        DataGrid.Column<EntityMatrixRow> entityCol = entityMatrixTable.getColumns().isEmpty()
                ? null
                : entityMatrixTable.getColumns().get(0);

        if (entityCol != null) {
            row.getCell(entityCol).setText("All entities (*)");
        }

        // Helper để tạo header checkbox
        headerAllowAllCb = createHeaderCheckbox(row, "allowAllCol", (r, v) -> {
            r.setAllowAll(v);
            r.setCanCreate(v);
            r.setCanRead(v);
            r.setCanUpdate(v);
            r.setCanDelete(v);
        });

        headerCreateCb = createHeaderCheckbox(row, "createCol", (r, v) -> {
            r.setCanCreate(v);
            roleManagerService.syncAllowAll(r);
        });

        headerReadCb = createHeaderCheckbox(row, "readCol", (r, v) -> {
            r.setCanRead(v);
            roleManagerService.syncAllowAll(r);
        });

        headerUpdateCb = createHeaderCheckbox(row, "updateCol", (r, v) -> {
            r.setCanUpdate(v);
            roleManagerService.syncAllowAll(r);
        });

        headerDeleteCb = createHeaderCheckbox(row, "deleteCol", (r, v) -> {
            r.setCanDelete(v);
            roleManagerService.syncAllowAll(r);
        });
    }

    private Checkbox createHeaderCheckbox(HeaderRow headerRow, String colKey,
                                          BiConsumer<EntityMatrixRow, Boolean> logic) {
        DataGrid.Column<EntityMatrixRow> col = entityMatrixTable.getColumnByKey(colKey);
        if (col == null) return null;

        Checkbox cb = new Checkbox();
        cb.addValueChangeListener(e -> {
            if (updatingHeaderFromRows) return;
            if (!e.isFromClient()) return; // QUAN TRỌNG: Chỉ chạy khi user click

            boolean v = Boolean.TRUE.equals(e.getValue());
            List<EntityMatrixRow> items = new ArrayList<>(entityMatrixDc.getItems());

            for (EntityMatrixRow r : items) {
                logic.accept(r, v);
            }

            // Nếu không checkbox "Allow All", ta cần reset thuộc tính con
            if (cb == headerAllowAllCb && !v) {
                resetAllAttributesFlags();
            }

            // Refresh toàn bộ bảng vì dữ liệu thay đổi hàng loạt
            entityMatrixDc.setItems(items);

            // Clear cache vì bảng đã vẽ lại components mới
            entityCellCache.clear();

            updateHeaderAllowAllFromRows();
        });

        headerRow.getCell(col).setComponent(cb);
        return cb;
    }

    private void updateHeaderAllowAllFromRows() {
        if (headerAllowAllCb == null) return;

        updatingHeaderFromRows = true;
        try {
            List<EntityMatrixRow> items = entityMatrixDc.getItems();
            if (items == null || items.isEmpty()) {
                setHeadersValue(false, false, false, false, false);
                return;
            }

            boolean allCreate = items.stream().allMatch(r -> T(r.getCanCreate()));
            boolean allRead = items.stream().allMatch(r -> T(r.getCanRead()));
            boolean allUpdate = items.stream().allMatch(r -> T(r.getCanUpdate()));
            boolean allDelete = items.stream().allMatch(r -> T(r.getCanDelete()));
            boolean allFull = items.stream().allMatch(r -> T(r.getAllowAll()));

            setHeadersValue(allFull, allCreate, allRead, allUpdate, allDelete);

        } finally {
            updatingHeaderFromRows = false;
        }
    }

    private void setHeadersValue(boolean all, boolean c, boolean r, boolean u, boolean d) {
        if (headerAllowAllCb != null) headerAllowAllCb.setValue(all);
        if (headerCreateCb != null) headerCreateCb.setValue(c);
        if (headerReadCb != null) headerReadCb.setValue(r);
        if (headerUpdateCb != null) headerUpdateCb.setValue(u);
        if (headerDeleteCb != null) headerDeleteCb.setValue(d);
    }

    // ========================================================================
    // Attribute matrix - Header
    // ========================================================================

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
                if (updatingAttrHeaderFromRows || !e.isFromClient()) return;

                boolean v = Boolean.TRUE.equals(e.getValue());
                List<AttributeResourceModel> items = new ArrayList<>(attrMatrixDc.getItems());
                items.forEach(r -> {
                    r.setView(v);
                    if (v) r.setModify(false);
                });

                attrMatrixDc.setItems(items); // Refresh grid
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
                if (updatingAttrHeaderFromRows || !e.isFromClient()) return;

                boolean v = Boolean.TRUE.equals(e.getValue());
                List<AttributeResourceModel> items = new ArrayList<>(attrMatrixDc.getItems());
                items.forEach(r -> {
                    r.setModify(v);
                    if (v) r.setView(false);
                });

                attrMatrixDc.setItems(items); // Refresh grid
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

    private void updateAttrHeaderFromRows() {
        if (headerAttrViewCb == null && headerAttrModifyCb == null) return;

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

    // ========================================================================
    // UI columns renderers (ĐÃ TỐI ƯU)
    // ========================================================================

    private void installMatrixColumns() {
        // 1. ALLOW ALL COLUMN
        DataGrid.Column<EntityMatrixRow> allowAllCol = entityMatrixTable.getColumnByKey("allowAllCol");
        if (allowAllCol != null) {
            allowAllCol.setRenderer(new ComponentRenderer<>(row -> {
                String key = entityCellKey("allowAll", row);
                // Dùng computeIfAbsent để cache hiệu quả
                Checkbox cb = entityCellCache.computeIfAbsent(key, k -> {
                    Checkbox newCb = new Checkbox();
                    newCb.addValueChangeListener(e -> {
                        if (!e.isFromClient()) return; // CHẶN LOOP

                        boolean v = bool(e.getValue());
                        row.setAllowAll(v);
                        row.setCanCreate(v);
                        row.setCanRead(v);
                        row.setCanUpdate(v);
                        row.setCanDelete(v);

                        // Update trực tiếp UI checkbox hàng xóm mà KHÔNG reload grid
                        updateSiblingCheckbox(row, "create", v);
                        updateSiblingCheckbox(row, "read", v);
                        updateSiblingCheckbox(row, "update", v);
                        updateSiblingCheckbox(row, "delete", v);

                        updateHeaderAllowAllFromRows();
                    });
                    return newCb;
                });

                // Luôn set value khi render để đảm bảo đúng state
                cb.setValue(T(row.getAllowAll()));
                return cb;
            }));
        }

        // 2. OTHER PERMISSION COLUMNS (Create, Read, Update, Delete)
        installPermissionColumn("createCol", "create", EntityMatrixRow::getCanCreate, EntityMatrixRow::setCanCreate);
        installPermissionColumn("readCol", "read", EntityMatrixRow::getCanRead, EntityMatrixRow::setCanRead);
        installPermissionColumn("updateCol", "update", EntityMatrixRow::getCanUpdate, EntityMatrixRow::setCanUpdate);
        installPermissionColumn("deleteCol", "delete", EntityMatrixRow::getCanDelete, EntityMatrixRow::setCanDelete);

        // 3. ATTRIBUTES SUMMARY COLUMN
        DataGrid.Column<EntityMatrixRow> attrCol = entityMatrixTable.getColumnByKey("attributesCol");
        if (attrCol != null) {
            attrCol.setRenderer(new ComponentRenderer<>(row -> {
                String key = "attrTxt|" + row.getEntityName();
                TextField tf = entityAttrFieldCache.computeIfAbsent(key, k -> {
                    TextField t = new TextField();
                    t.setWidthFull();
                    t.setReadOnly(true);
                    return t;
                });
                tf.setValue(Objects.toString(row.getAttributes(), ""));
                return tf;
            }));
        }
    }

    private void installPermissionColumn(String colId, String keyPrefix,
                                         Function<EntityMatrixRow, Boolean> getter,
                                         BiConsumer<EntityMatrixRow, Boolean> setter) {
        DataGrid.Column<EntityMatrixRow> col = entityMatrixTable.getColumnByKey(colId);
        if (col != null) {
            col.setRenderer(new ComponentRenderer<>(row -> {
                String key = entityCellKey(keyPrefix, row);
                Checkbox cb = entityCellCache.computeIfAbsent(key, k -> {
                    Checkbox newCb = new Checkbox();
                    newCb.addValueChangeListener(e -> {
                        if (!e.isFromClient()) return; // CHẶN LOOP

                        boolean v = bool(e.getValue());
                        setter.accept(row, v); // Cập nhật model

                        // Logic nghiệp vụ: Sync allow all
                        roleManagerService.syncAllowAll(row);

                        // Update checkbox "Allow All" của dòng này nếu nó thay đổi
                        Checkbox allowCb = entityCellCache.get(entityCellKey("allowAll", row));
                        if (allowCb != null) {
                            allowCb.setValue(row.getAllowAll());
                        }

                        newCb.setIndeterminate(false);
                        updateHeaderAllowAllFromRows();
                        // KHÔNG gọi replaceItem(row) -> Giúp UI mượt
                    });
                    return newCb;
                });

                boolean val = T(getter.apply(row));
                cb.setValue(val);

                // Indeterminate logic
                if (T(row.getAllowAll()) && val) {
                    cb.setIndeterminate(true);
                } else {
                    cb.setIndeterminate(false);
                }
                return cb;
            }));
        }
    }

    // Cập nhật checkbox hàng xóm trong cache mà không cần Grid render lại
    private void updateSiblingCheckbox(EntityMatrixRow row, String type, boolean value) {
        Checkbox cb = entityCellCache.get(entityCellKey(type, row));
        if (cb != null) {
            cb.setValue(value);
            cb.setIndeterminate(false);
        }
    }

    private void installAttrColumns() {
        DataGrid.Column<AttributeResourceModel> nameCol = attrMatrixTable.getColumnByKey("attribute");

        if (nameCol != null) {
            nameCol.setRenderer(new ComponentRenderer<>(row -> {
                var span = new Span();

                String caption = Strings.nullToEmpty(row.getName());
                System.out.println(caption);

                span.setText(caption);
                return span;
            }));
        }

        // 2. Cột View (Giữ nguyên)
        installAttrCheckboxColumn("viewCol", AttributeResourceModel::getView, (row, v) -> {
            row.setView(v);
            if (v) row.setModify(false);
        });

        // 3. Cột Modify (Giữ nguyên)
        installAttrCheckboxColumn("modifyCol", AttributeResourceModel::getModify, (row, v) -> {
            row.setModify(v);
            if (v) row.setView(false);
        });
    }

    private void installAttrCheckboxColumn(String colId, Function<AttributeResourceModel, Boolean> getter,
                                           BiConsumer<AttributeResourceModel, Boolean> setter) {
        DataGrid.Column<AttributeResourceModel> col = attrMatrixTable.getColumnByKey(colId);
        if (col != null) {
            col.setRenderer(new ComponentRenderer<>(row -> {
                Checkbox cb = new Checkbox();
                cb.setValue(T(getter.apply(row)));

                cb.addValueChangeListener(e -> {
                    if (!e.isFromClient()) return; // CHẶN LOOP

                    boolean v = T(e.getValue());
                    setter.accept(row, v);

                    // Do Attribute Grid đơn giản hơn và ít dòng hơn,
                    // ta có thể refresh nhẹ hoặc chỉ update model.
                    // Ở đây chỉ cần update header và summary là đủ.

                    // Lưu ý: Nếu muốn update checkbox "đối lập" (View <-> Modify) ngay lập tức
                    // thì nên dùng replaceItem hoặc cache checkbox giống bên EntityGrid.
                    // Tuy nhiên, để đơn giản và nhanh, ta chấp nhận việc này:
                    attrMatrixDc.replaceItem(row);

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

    // ========================================================================
    // Utils & helpers
    // ========================================================================

    private void loadAttributesForEntity(String entityName) {
        if (Strings.isNullOrEmpty(entityName) || "*".equals(entityName.trim())) {
            if (attrEntityLabel != null) attrEntityLabel.setText("");
            attrMatrixDc.setItems(Collections.emptyList());
            return;
        }

        if (attrEntityLabel != null) {
            attrEntityLabel.setText("Entity: " + entityName);
        }

        List<AttributeResourceModel> rows = attrCache.get(entityName);
        if (rows == null) {
            rows = roleManagerService.buildAttrRowsForEntity(entityName);
            attrCache.put(entityName, rows);
        }

        attrMatrixDc.setItems(new ArrayList<>(rows));
        updateAttrHeaderFromRows();
        updateEntityAttributesSummarySafe(entityName);
    }

    private void updateEntityAttributesSummarySafe(String entityName) {
        if (syncingAttrSummary) return;
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

        TextField tf = entityAttrFieldCache.get("attrTxt|" + entityName);
        if (tf != null) {
            // Tìm row data
            entityMatrixDc.getItems().stream()
                    .filter(r -> Objects.equals(r.getEntityName(), entityName))
                    .findFirst()
                    .ifPresent(r -> tf.setValue(Objects.toString(r.getAttributes(), "")));
        }
    }

    private void resetAllAttributesFlags() {
        // 1) reset cache
        attrCache.values().forEach(list -> {
            for (AttributeResourceModel a : list) {
                a.setView(false);
                a.setModify(false);
            }
        });

        // 2) reset grid đang hiển thị
        List<AttributeResourceModel> current = new ArrayList<>(attrMatrixDc.getItems());
        if (!current.isEmpty()) {
            current.forEach(a -> {
                a.setView(false);
                a.setModify(false);
            });
            attrMatrixDc.setItems(current);
        }

        // 3) reset summary attributes
        List<EntityMatrixRow> entities = new ArrayList<>(entityMatrixDc.getItems());
        for (EntityMatrixRow r : entities) {
            r.setAttributes(null);
        }

        // Clear cache components để vẽ lại từ đầu
        entityCellCache.clear();
        entityAttrFieldCache.clear();

        entityMatrixDc.setItems(entities);

        if (headerAttrViewCb != null) headerAttrViewCb.setValue(false);
        if (headerAttrModifyCb != null) headerAttrModifyCb.setValue(false);
    }

    public List<ResourcePolicyModel> buildPoliciesFromMatrix() {
        return roleManagerService.buildPoliciesFromMatrix(
                new ArrayList<>(entityMatrixDc.getItems()),
                attrCache
        );
    }

    private static boolean T(Boolean b) {
        return Boolean.TRUE.equals(b);
    }

    private static Boolean bool(Boolean b) {
        return Boolean.TRUE.equals(b);
    }

    private String entityCellKey(String columnKey, EntityMatrixRow row) {
        return columnKey + "|" + row.getEntityName() + "|" + row.getId();
    }
}