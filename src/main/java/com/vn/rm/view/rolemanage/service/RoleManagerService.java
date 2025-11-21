package com.vn.rm.view.rolemanage.service;

import com.google.common.base.Strings;
import com.vn.rm.view.rolemanage.entityfragment.EntityMatrixRow;
import io.jmix.core.Metadata;
import io.jmix.security.model.*;
import io.jmix.securityflowui.view.resourcepolicy.AttributeResourceModel;
import io.jmix.securityflowui.view.resourcepolicy.ResourcePolicyViewUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("rm_RoleManagerService")
public class RoleManagerService {

    @Autowired
    private ResourcePolicyViewUtils resourcePolicyEditorUtils;

    @Autowired
    private Metadata metadata;

    private static final String ACT_CREATE = EntityPolicyAction.CREATE.getId();
    private static final String ACT_READ = EntityPolicyAction.READ.getId();
    private static final String ACT_UPDATE = EntityPolicyAction.UPDATE.getId();
    private static final String ACT_DELETE = EntityPolicyAction.DELETE.getId();
    private static final String ACT_ATTR_VIEW = EntityAttributePolicyAction.VIEW.getId();
    private static final String ACT_ATTR_MODIFY = EntityAttributePolicyAction.MODIFY.getId();

    /**
     * Tạo skeleton entity matrix (chưa apply policy).
     */
    public List<EntityMatrixRow> createMatrixEntity() {
        return resourcePolicyEditorUtils.getEntityOptionsMap().entrySet().stream()
                .filter(e -> !"*".equals(e.getKey()))
                .map(e -> {
                    EntityMatrixRow r = metadata.create(EntityMatrixRow.class);
                    r.setEntityName(e.getKey());
                    r.setEntityCaption(e.getValue());
                    return r;
                })
                .collect(Collectors.toList());
    }

    /**
     * Apply tập ResourcePolicyModel vào các dòng entity + preload attribute vào cache.
     */
    public void updateEntityMatrix(List<EntityMatrixRow> rows,
                                   Collection<ResourcePolicyModel> policies,
                                   Map<String, List<AttributeResourceModel>> attrCache) {
        if (rows == null || rows.isEmpty()) {
            return;
        }

        Map<String, Set<String>> entityPolicyMap = new HashMap<>();
        Map<String, Map<String, Set<String>>> attrPolicyMap = new HashMap<>();

        if (policies != null) {
            for (ResourcePolicyModel p : policies) {
                if (p.getResource() == null
                        || !Objects.equals(p.getEffect(), ResourcePolicyEffect.ALLOW)) {
                    continue;
                }

                if (ResourcePolicyType.ENTITY.equals(p.getType())) {
                    entityPolicyMap
                            .computeIfAbsent(p.getResource(), k -> new HashSet<>())
                            .add(p.getAction());
                } else if (ResourcePolicyType.ENTITY_ATTRIBUTE.equals(p.getType())) {
                    String res = p.getResource();
                    int dotIndex = res.lastIndexOf(".");
                    if (dotIndex > 0) {
                        String entity = res.substring(0, dotIndex);
                        String attr = res.substring(dotIndex + 1);
                        attrPolicyMap
                                .computeIfAbsent(entity, k -> new HashMap<>())
                                .computeIfAbsent(attr, k -> new HashSet<>())
                                .add(p.getAction());
                    }
                }
            }
        }

        // Global entity policies: "*"
        Set<String> globalActions = entityPolicyMap.getOrDefault("*", Collections.emptySet());

        for (EntityMatrixRow row : rows) {
            String entity = row.getEntityName();
            Set<String> actions = entityPolicyMap.getOrDefault(entity, Collections.emptySet());

            // Merge quyền riêng của entity và quyền global (*)
            row.setCanCreate(actions.contains(ACT_CREATE) || globalActions.contains(ACT_CREATE));
            row.setCanRead(actions.contains(ACT_READ) || globalActions.contains(ACT_READ));
            row.setCanUpdate(actions.contains(ACT_UPDATE) || globalActions.contains(ACT_UPDATE));
            row.setCanDelete(actions.contains(ACT_DELETE) || globalActions.contains(ACT_DELETE));

            // Đồng bộ allowAll theo CRUD
            syncAllowAll(row);

            // Attribute policies cho entity
            Map<String, Set<String>> entityAttrs = attrPolicyMap.getOrDefault(entity, Collections.emptyMap());

            List<AttributeResourceModel> attrRows;
            if (!attrCache.containsKey(entity)) {
                attrRows = buildAttrRowsForEntity(entity);
                applyAttrPoliciesToRows(attrRows, entityAttrs);
                attrCache.put(entity, attrRows);
            } else {
                attrRows = attrCache.get(entity);
                applyAttrPoliciesToRows(attrRows, entityAttrs);
            }

            row.setAttributes(computeAttrSummaryFromRows(attrRows));
        }
    }

    /**
     * Đồng bộ cờ allowAll = true nếu và chỉ nếu 4 CRUD đều true.
     * Cho fragment gọi được nên để public.
     */
    public void syncAllowAll(EntityMatrixRow r) {
        boolean all = Boolean.TRUE.equals(r.getCanCreate())
                && Boolean.TRUE.equals(r.getCanRead())
                && Boolean.TRUE.equals(r.getCanUpdate())
                && Boolean.TRUE.equals(r.getCanDelete());
        r.setAllowAll(all);
    }

    /**
     * Tạo danh sách attribute cho một entity (chưa apply policy, tất cả view/modify = false).
     */
    public List<AttributeResourceModel> buildAttrRowsForEntity(String entityName) {
        return resourcePolicyEditorUtils.getEntityAttributeOptionsMap(entityName).entrySet().stream()
                .filter(e -> !"*".equals(e.getKey()))
                .map(e -> {
                    AttributeResourceModel r = metadata.create(AttributeResourceModel.class);
                    r.setName(e.getKey());
                    r.setCaption(e.getValue());
                    return r;
                })
                .sorted(Comparator.comparing(AttributeResourceModel::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    /**
     * Apply attr policies (wildcard + từng attribute) vào danh sách rows.
     */
    private void applyAttrPoliciesToRows(List<AttributeResourceModel> rows,
                                         Map<String, Set<String>> attrPolicies) {
        Set<String> wildCardActions = attrPolicies.getOrDefault("*", Collections.emptySet());
        boolean wildView = wildCardActions.contains(ACT_ATTR_VIEW);
        boolean wildModify = wildCardActions.contains(ACT_ATTR_MODIFY);

        for (AttributeResourceModel r : rows) {
            Set<String> specificActions = attrPolicies.getOrDefault(r.getName(), Collections.emptySet());
            r.setView(wildView || specificActions.contains(ACT_ATTR_VIEW));
            r.setModify(wildModify || specificActions.contains(ACT_ATTR_MODIFY));
        }
    }

    /**
     * Tính summary attributes: null / "*" / "attr1, attr2, ...".
     */
    public String computeAttrSummaryFromRows(List<AttributeResourceModel> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        boolean allView = true;
        boolean allModify = true;
        List<String> selected = new ArrayList<>();

        for (AttributeResourceModel r : rows) {
            boolean v = Boolean.TRUE.equals(r.getView());
            boolean m = Boolean.TRUE.equals(r.getModify());

            if (!v) {
                allView = false;
            }
            if (!m) {
                allModify = false;
            }

            if (v || m) {
                selected.add(r.getName());
            }
        }

        if (allView || allModify) {
            return "*";
        }
        if (selected.isEmpty()) {
            return null;
        }

        selected.sort(String.CASE_INSENSITIVE_ORDER);
        return String.join(", ", selected);
    }

    /**
     * Build lại danh sách ResourcePolicyModel từ entity matrix + attr cache.
     */
    public List<ResourcePolicyModel> buildPoliciesFromMatrix(List<EntityMatrixRow> entityRows,
                                                             Map<String, List<AttributeResourceModel>> attrCache) {
        List<ResourcePolicyModel> result = new ArrayList<>();

        // Lọc các dòng hợp lệ
        List<EntityMatrixRow> activeRows = entityRows.stream()
                .filter(r -> !Strings.isNullOrEmpty(r.getEntityName()) && !"*".equals(r.getEntityName()))
                .collect(Collectors.toList());

        // 1. Kiểm tra điều kiện "All Entities" (*)
        boolean allCreate = !activeRows.isEmpty()
                && activeRows.stream().allMatch(r -> Boolean.TRUE.equals(r.getCanCreate()) || Boolean.TRUE.equals(r.getAllowAll()));
        boolean allRead = !activeRows.isEmpty()
                && activeRows.stream().allMatch(r -> Boolean.TRUE.equals(r.getCanRead()) || Boolean.TRUE.equals(r.getAllowAll()));
        boolean allUpdate = !activeRows.isEmpty()
                && activeRows.stream().allMatch(r -> Boolean.TRUE.equals(r.getCanUpdate()) || Boolean.TRUE.equals(r.getAllowAll()));
        boolean allDelete = !activeRows.isEmpty()
                && activeRows.stream().allMatch(r -> Boolean.TRUE.equals(r.getCanDelete()) || Boolean.TRUE.equals(r.getAllowAll()));

        if (allCreate) {
            result.add(createPolicy(ResourcePolicyType.ENTITY, "*", ACT_CREATE));
        }
        if (allRead) {
            result.add(createPolicy(ResourcePolicyType.ENTITY, "*", ACT_READ));
        }
        if (allUpdate) {
            result.add(createPolicy(ResourcePolicyType.ENTITY, "*", ACT_UPDATE));
        }
        if (allDelete) {
            result.add(createPolicy(ResourcePolicyType.ENTITY, "*", ACT_DELETE));
        }

        // 2. Duyệt từng Entity Row
        for (EntityMatrixRow row : activeRows) {
            String entity = row.getEntityName();
            boolean allowAll = Boolean.TRUE.equals(row.getAllowAll());

            // 2.1 Entity Policies (CRUD) - bỏ qua những action đã gom vào "*"
            if (!allCreate && (allowAll || Boolean.TRUE.equals(row.getCanCreate()))) {
                result.add(createPolicy(ResourcePolicyType.ENTITY, entity, ACT_CREATE));
            }
            if (!allRead && (allowAll || Boolean.TRUE.equals(row.getCanRead()))) {
                result.add(createPolicy(ResourcePolicyType.ENTITY, entity, ACT_READ));
            }
            if (!allUpdate && (allowAll || Boolean.TRUE.equals(row.getCanUpdate()))) {
                result.add(createPolicy(ResourcePolicyType.ENTITY, entity, ACT_UPDATE));
            }
            if (!allDelete && (allowAll || Boolean.TRUE.equals(row.getCanDelete()))) {
                result.add(createPolicy(ResourcePolicyType.ENTITY, entity, ACT_DELETE));
            }

            // 2.2 Attribute Policies
            List<AttributeResourceModel> attrs = attrCache.getOrDefault(entity, Collections.emptyList());
            if (attrs.isEmpty()) {
                continue;
            }

            boolean fullAttrView = attrs.stream().allMatch(a -> Boolean.TRUE.equals(a.getView()));
            boolean fullAttrModify = attrs.stream().allMatch(a -> Boolean.TRUE.equals(a.getModify()));

            if (fullAttrView) {
                result.add(createAttrPolicy(entity, "*", ACT_ATTR_VIEW));
            }
            if (fullAttrModify) {
                result.add(createAttrPolicy(entity, "*", ACT_ATTR_MODIFY));
            }

            // Nếu không phải full (*), tạo lẻ từng attribute
            if (!fullAttrView && !fullAttrModify) {
                for (AttributeResourceModel attr : attrs) {
                    if (Boolean.TRUE.equals(attr.getView())) {
                        result.add(createAttrPolicy(entity, attr.getName(), ACT_ATTR_VIEW));
                    }
                    if (Boolean.TRUE.equals(attr.getModify())) {
                        result.add(createAttrPolicy(entity, attr.getName(), ACT_ATTR_MODIFY));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Cập nhật summary attributes cho 1 entity + sync lại cache.
     */
    public void updateEntityAttributesSummary(String entityName,
                                              List<EntityMatrixRow> entityRows,
                                              List<AttributeResourceModel> currentAttrRows,
                                              Map<String, List<AttributeResourceModel>> attrCache) {
        if (Strings.isNullOrEmpty(entityName) || entityRows == null) {
            return;
        }

        entityRows.stream()
                .filter(r -> entityName.equals(r.getEntityName()))
                .findFirst()
                .ifPresent(row -> {
                    String summary = computeAttrSummaryFromRows(currentAttrRows);
                    row.setAttributes(summary);
                    attrCache.put(entityName, new ArrayList<>(currentAttrRows != null ? currentAttrRows : List.of()));
                });
    }

    // --- Helpers ---

    private ResourcePolicyModel createPolicy(String type, String resource, String action) {
        ResourcePolicyModel p = metadata.create(ResourcePolicyModel.class);
        p.setType(type);
        p.setResource(resource);
        p.setAction(action);
        p.setEffect(ResourcePolicyEffect.ALLOW);
        p.setPolicyGroup(resource.endsWith("*") ? null : resource); // Group theo Entity
        return p;
    }

    private ResourcePolicyModel createAttrPolicy(String entity, String attr, String action) {
        ResourcePolicyModel p = metadata.create(ResourcePolicyModel.class);
        p.setType(ResourcePolicyType.ENTITY_ATTRIBUTE);
        p.setResource(entity + "." + attr);
        p.setAction(action);
        p.setEffect(ResourcePolicyEffect.ALLOW);
        p.setPolicyGroup(entity);
        return p;
    }
}
