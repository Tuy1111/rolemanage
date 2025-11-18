package com.vn.rm.view.rolemanage.entityfragment;

import com.google.common.base.Strings;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import groovy.lang.MetaClass;
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

    // Header checkbox cho "Allow all"
    private Checkbox headerAllowAllCb;
    private Checkbox headerCreateCb;
    private Checkbox headerReadCb;
    private Checkbox headerUpdateCb;
    private Checkbox headerDeleteCb;
    private Checkbox headerAttrViewCb;
    private Checkbox headerAttrModifyCb;



    // tránh vòng lặp khi cập nhật từ rows lên header
    private boolean updatingHeaderFromRows = false;
    private boolean updatingAttrHeaderFromRows = false;
    // =========================== Cache & guards ==============================

    private final Map<String, List<AttrMatrixRow>> attrCache = new HashMap<>();
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


//        makeGridHeadersBold();

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
                            list.removeIf(p -> isEntityAttributeType(p.getType())
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

        // dedup
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
        updateHeaderAllowAllFromRows();  // <-- thêm


        preloadAllAttributesFromDbAndFillEntitySummary(policies);

        EntityMatrixRow first = entityMatrixDc.getItems().stream()
                .filter(r -> !Strings.isNullOrEmpty(r.getEntityName()))
                .filter(r -> !"*".equals(r.getEntityName()))
                .findFirst()
                .orElse(null);
        if (first != null) {
            entityMatrixDc.setItem(first);
        } else {
            attrMatrixDc.setItems(Collections.emptyList());
            if (attrEntityLabel != null)
                attrEntityLabel.setText("");
        }
    }
    private void updateAttrHeaderFromRows() {
        if (headerAttrViewCb == null && headerAttrModifyCb == null) {
            return;
        }

        updatingAttrHeaderFromRows = true;
        try {
            List<AttrMatrixRow> items = attrMatrixDc.getItems();
            if (items == null || items.isEmpty()) {
                if (headerAttrViewCb != null) headerAttrViewCb.setValue(false);
                if (headerAttrModifyCb != null) headerAttrModifyCb.setValue(false);
                return;
            }

            boolean allView   = items.stream().allMatch(r -> T(r.getCanView()));
            boolean allModify = items.stream().allMatch(r -> T(r.getCanModify()));

            if (headerAttrViewCb != null)   headerAttrViewCb.setValue(allView);
            if (headerAttrModifyCb != null) headerAttrModifyCb.setValue(allModify);
        } finally {
            updatingAttrHeaderFromRows = false;
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

    private void initAttrHeader() {
        HeaderRow row = attrMatrixTable.appendHeaderRow();

        DataGrid.Column<AttrMatrixRow> attrCol   = attrMatrixTable.getColumnByKey("attribute");
        DataGrid.Column<AttrMatrixRow> viewCol   = attrMatrixTable.getColumnByKey("viewCol");
        DataGrid.Column<AttrMatrixRow> modifyCol = attrMatrixTable.getColumnByKey("modifyCol");

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
                    r.setCanView(v);
                    if (v) r.setCanModify(false);   // chỉ 1 trong 2
                });
                attrMatrixDc.setItems(new ArrayList<>(attrMatrixDc.getItems()));

                // ⭐ sync lại header: View / Modify
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
                    r.setCanModify(v);
                    if (v) r.setCanView(false);     // chỉ 1 trong 2
                });
                attrMatrixDc.setItems(new ArrayList<>(attrMatrixDc.getItems()));

                // ⭐ sync lại header: View / Modify
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

        // Không tạo row "*" nữa, nếu không có attribute thì trả về list rỗng
        if (attrs.isEmpty()) {
            return List.of();
        }

        return attrs.keySet().stream()
                // Phòng trường hợp trong map có key "*" thì cũng bỏ luôn
                .filter(name -> !"*".equals(name))
                .map(name -> {
                    AttrMatrixRow r = metadata.create(AttrMatrixRow.class);
                    r.setEntityName(entityName);
                    r.setAttribute(name);
                    r.setCanView(false);
                    r.setCanModify(false);
                    return r;
                })
                // So sánh bình thường, không ưu tiên "*" nữa
                .sorted((a, b) -> a.getAttribute().compareToIgnoreCase(b.getAttribute()))
                .collect(Collectors.toList());
    }


    private void applyAttrPolicies(List<AttrMatrixRow> rows,
                                   String entityName,
                                   Collection<ResourcePolicyModel> policies) {

        Map<String, List<ResourcePolicyModel>> byRes = policies.stream()
                .filter(p -> isEntityAttributeType(p.getType()))
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
        // allowAll
        DataGrid.Column<EntityMatrixRow> allowAllCol = entityMatrixTable.getColumnByKey("allowAllCol");
        if (allowAllCol != null) {
            allowAllCol.setRenderer(new ComponentRenderer<>(row -> {
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
                    updateHeaderAllowAllFromRows();
                });
                return cb;
            }));
        }

        // create
        DataGrid.Column<EntityMatrixRow> createCol = entityMatrixTable.getColumnByKey("createCol");
        if (createCol != null) {
            createCol.setRenderer(new ComponentRenderer<>(row -> {
                Checkbox cb = new Checkbox(T(row.getCanCreate()));
                cb.addValueChangeListener(e -> {
                    row.setCanCreate(bool(e.getValue()));
                    entityMatrixDc.replaceItem(row);
                    syncAllowAll(row);
                    updateHeaderAllowAllFromRows();  // <-- thêm

                });
                return cb;
            }));
        }

        // read
        DataGrid.Column<EntityMatrixRow> readCol = entityMatrixTable.getColumnByKey("readCol");
        if (readCol != null) {
            readCol.setRenderer(new ComponentRenderer<>(row -> {
                Checkbox cb = new Checkbox(T(row.getCanRead()));
                cb.addValueChangeListener(e -> {
                    row.setCanRead(bool(e.getValue()));
                    entityMatrixDc.replaceItem(row);
                    syncAllowAll(row);
                    updateHeaderAllowAllFromRows();  // <-- thêm

                });
                return cb;
            }));
        }

        // update
        DataGrid.Column<EntityMatrixRow> updateCol = entityMatrixTable.getColumnByKey("updateCol");
        if (updateCol != null) {
            updateCol.setRenderer(new ComponentRenderer<>(row -> {
                Checkbox cb = new Checkbox(T(row.getCanUpdate()));
                cb.addValueChangeListener(e -> {
                    row.setCanUpdate(bool(e.getValue()));
                    entityMatrixDc.replaceItem(row);
                    syncAllowAll(row);
                    updateHeaderAllowAllFromRows();  // <-- thêm

                });
                return cb;
            }));
        }

        // delete
        DataGrid.Column<EntityMatrixRow> deleteCol = entityMatrixTable.getColumnByKey("deleteCol");
        if (deleteCol != null) {
            deleteCol.setRenderer(new ComponentRenderer<>(row -> {
                Checkbox cb = new Checkbox(T(row.getCanDelete()));
                cb.addValueChangeListener(e -> {
                    row.setCanDelete(bool(e.getValue()));
                    entityMatrixDc.replaceItem(row);
                    syncAllowAll(row);
                    updateHeaderAllowAllFromRows();  // <-- thêm

                });
                return cb;
            }));
        }

        // attributes summary
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
        // view
        DataGrid.Column<AttrMatrixRow> viewCol = attrMatrixTable.getColumnByKey("viewCol");
        if (viewCol != null) {
            viewCol.setRenderer(new ComponentRenderer<>(row -> {
                Checkbox cb = new Checkbox(T(row.getCanView()));
                cb.addValueChangeListener(e -> {
                    boolean v = T(e.getValue());

                    row.setCanView(v);
                    if (v) {
                        // bật View thì tắt Modify
                        row.setCanModify(false);
                    }
                    attrMatrixDc.replaceItem(row);
                    // cần refresh lại toàn bộ items để grid re-render checkbox còn lại
                    attrMatrixDc.setItems(new ArrayList<>(attrMatrixDc.getItems()));

                    updateAttrHeaderFromRows();
                    updateEntityAttributesSummarySafe(row.getEntityName());
                });
                return cb;
            }));
        }

        // modify
        DataGrid.Column<AttrMatrixRow> modifyCol = attrMatrixTable.getColumnByKey("modifyCol");
        if (modifyCol != null) {
            modifyCol.setRenderer(new ComponentRenderer<>(row -> {
                Checkbox cb = new Checkbox(T(row.getCanModify()));
                cb.addValueChangeListener(e -> {
                    boolean v = T(e.getValue());

                    row.setCanModify(v);
                    if (v) {
                        // bật Modify thì tắt View
                        row.setCanView(false);
                    }
                    attrMatrixDc.replaceItem(row);
                    // refresh lại -> checkbox View trong cùng row sẽ update
                    attrMatrixDc.setItems(new ArrayList<>(attrMatrixDc.getItems()));

                    updateAttrHeaderFromRows();
                    updateEntityAttributesSummarySafe(row.getEntityName());
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

//    private void makeGridHeadersBold() {
//        // Entity grid
//        entityMatrixTable.getColumns().forEach(col -> {
//            String text = col.getHeaderText();
//            if (text != null && !text.isEmpty()) {
//                Span span = new Span(text);
//                span.getStyle().set("font-weight", "600");
//                col.setHeader(span);
//            }
//        });
//
//        // Attribute grid
//        attrMatrixTable.getColumns().forEach(col -> {
//            String text = col.getHeaderText();
//            if (text != null && !text.isEmpty()) {
//                Span span = new Span(text);
//                span.getStyle().set("font-weight", "600");
//                col.setHeader(span);
//            }
//        });
//    }

}
