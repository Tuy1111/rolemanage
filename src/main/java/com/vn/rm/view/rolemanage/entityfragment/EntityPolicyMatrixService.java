package com.vn.rm.view.rolemanage.entityfragment;

import com.google.common.base.Strings;
import io.jmix.core.Metadata;
import io.jmix.security.model.*;
import io.jmix.securityflowui.view.resourcepolicy.AttributeResourceModel;
import io.jmix.securityflowui.view.resourcepolicy.ResourcePolicyViewUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("rm_EntityPolicyMatrixService")
public class EntityPolicyMatrixService {

    @Autowired
    private ResourcePolicyViewUtils resourcePolicyEditorUtils;

    @Autowired
    private Metadata metadata;

    // =========================================================================
    //  PUBLIC API cho Fragment dùng
    // =========================================================================

    /**
     * Tạo skeleton entity matrix (chưa apply policy).
     */
    public List<EntityMatrixRow> buildMatrixSkeleton() {
        Map<String, String> entityOptions = resourcePolicyEditorUtils.getEntityOptionsMap();
        return entityOptions.entrySet().stream()
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
    }

    /**
     * Áp policies vào entity rows, đồng thời preload attr + summary.
     *
     * @param rows      list entity matrix (Fragment đang giữ)
     * @param policies  list policy hiện có
     * @param attrCache cache attr: key = entityName, value = list attr rows
     */
    public void refreshMatrixFromPolicies(List<EntityMatrixRow> rows,
                                          Collection<ResourcePolicyModel> policies,
                                          Map<String, List<AttributeResourceModel>> attrCache) {

        // reset hết flags
        rows.forEach(r -> {
            r.setAllowAll(false);
            r.setCanCreate(false);
            r.setCanRead(false);
            r.setCanUpdate(false);
            r.setCanDelete(false);
            r.setAttributes(null);
        });

        Collection<ResourcePolicyModel> safePolicies =
                Optional.ofNullable(policies).orElseGet(List::of);

        // map entityName -> row của nó (bỏ "*")
        Map<String, EntityMatrixRow> rowByEntity = rows.stream()
                .filter(r -> !Strings.isNullOrEmpty(r.getEntityName()))
                .filter(r -> !"*".equals(r.getEntityName()))
                .collect(Collectors.toMap(EntityMatrixRow::getEntityName, r -> r));

        for (ResourcePolicyModel p : safePolicies) {
            if (p.getResource() == null)
                continue;

            // chỉ lấy policy effect = ALLOW
            if (!isAllowEffect(p.getEffect()))
                continue;

            Object type = p.getType();
            boolean isEntity = isEntityType(type);
            boolean isEntityAttr = isEntityAttributeType(type);

            if (!isEntity && !isEntityAttr)
                continue;

            if (isEntity) {
                String resource = p.getResource();
                String action = p.getAction();

                // wildcard "*" cho tất cả entity
                if ("*".equals(resource)) {
                    for (EntityMatrixRow row : rows) {
                        applyEntityActionToRow(row, action);
                    }
                } else {
                    EntityMatrixRow row = rowByEntity.get(resource);
                    if (row == null)
                        continue;
                    applyEntityActionToRow(row, action);
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

        // đồng bộ AllowAll theo CRUD
        rows.forEach(this::syncAllowAll);

        // load sẵn attr + summary cho từng entity
        preloadAllAttributesFromDbAndFillEntitySummary(rows, safePolicies, attrCache);
    }

    /**
     * Tính summary attributes cho 1 entity dựa trên list attr của entity đó.
     * (Fragment có thể gọi khi user chỉnh attr).
     */
    public void updateEntityAttributesSummary(String entityName,
                                              List<EntityMatrixRow> entityRows,
                                              List<AttributeResourceModel> currentAttrRows,
                                              Map<String, List<AttributeResourceModel>> attrCache) {

        if (Strings.isNullOrEmpty(entityName))
            return;

        EntityMatrixRow entityRow = entityRows.stream()
                .filter(r -> entityName.equals(r.getEntityName()))
                .findFirst()
                .orElse(null);
        if (entityRow == null)
            return;

        List<AttributeResourceModel> attrs = new ArrayList<>(currentAttrRows);
        if (attrs.isEmpty()) {
            entityRow.setAttributes(null);
            return;
        }

        boolean fullView = attrs.stream().allMatch(a -> T(a.getView()));
        boolean fullModify = attrs.stream().allMatch(a -> T(a.getModify()));

        String pattern;
        if (fullView || fullModify) {
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
        // lưu lại cache cho entity này
        attrCache.put(entityName, attrs);
    }

    /**
     * Đồng bộ flag allowAll của 1 row theo CRUD flags.
     */
    public void syncAllowAll(EntityMatrixRow r) {
        boolean all = T(r.getCanCreate()) && T(r.getCanRead())
                && T(r.getCanUpdate()) && T(r.getCanDelete());
        if (T(r.getAllowAll()) != all) {
            r.setAllowAll(all);
        }
    }

    /**
     * Build list ResourcePolicyModel từ matrix + attr cache.
     */
    public List<ResourcePolicyModel> buildPoliciesFromMatrix(List<EntityMatrixRow> entityRows,
                                                             Map<String, List<AttributeResourceModel>> attrCache) {

        // Tất cả entity hợp lệ (bỏ null và "*")
        List<EntityMatrixRow> filtered = entityRows.stream()
                .filter(r -> !Strings.isNullOrEmpty(r.getEntityName()))
                .filter(r -> !"*".equals(r.getEntityName()))
                .collect(Collectors.toList());

        // Build policy chi tiết như hiện tại
        List<ResourcePolicyModel> raw = filtered.stream()
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

                    boolean fullView = attrs.stream().allMatch(a -> T(a.getView()));
                    boolean fullModify = attrs.stream().allMatch(a -> T(a.getModify()));

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

        if (!filtered.isEmpty()) {
            boolean allCreate = filtered.stream().allMatch(r -> T(r.getCanCreate()));
            boolean allRead = filtered.stream().allMatch(r -> T(r.getCanRead()));
            boolean allUpdate = filtered.stream().allMatch(r -> T(r.getCanUpdate()));
            boolean allDelete = filtered.stream().allMatch(r -> T(r.getCanDelete()));

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

    /**
     * Build list attr cho 1 entity (chưa apply policy).
     */
    public List<AttributeResourceModel> buildAttrRowsForEntity(String entityName) {
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

    /**
     * Áp policy attr vào list attr của 1 entity.
     */
    public void applyAttrPolicies(List<AttributeResourceModel> rows,
                                  String entityName,
                                  Collection<ResourcePolicyModel> policies) {
        Map<String, List<ResourcePolicyModel>> byRes = policies.stream()
                .filter(p -> isEntityAttributeType(p.getType()))
                .filter(p -> p.getResource() != null && p.getResource().startsWith(entityName + "."))
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

    /**
     * Dùng khi preload attr + summary cho tất cả entity.
     */
    public void preloadAllAttributesFromDbAndFillEntitySummary(List<EntityMatrixRow> rows,
                                                               Collection<ResourcePolicyModel> policies,
                                                               Map<String, List<AttributeResourceModel>> attrCache) {
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
    }

    /**
     * Tính summary cho list attr (dùng khi preload).
     */
    public String computeAttrSummaryFromRows(List<AttributeResourceModel> rows) {
        if (rows == null || rows.isEmpty())
            return null;

        boolean fullView = rows.stream().allMatch(a -> T(a.getView()));
        boolean fullModify = rows.stream().allMatch(a -> T(a.getModify()));

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

    public String displayAttrPattern(String pattern) {
        if (pattern == null)
            return null;
        String p = pattern.trim();
        if (p.isEmpty())
            return null;
        if ("*,*".equals(p))
            return "*,*";
        return normalizeAttrPattern(p);
    }

    public String normalizeAttrPattern(String pattern) {
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

    // =========================================================================
    //  PRIVATE HELPERS
    // =========================================================================

    private boolean isAllowEffect(Object effect) {
        if (effect == null) {
            return true;
        } else if (effect == ResourcePolicyEffect.ALLOW) {
            return true;
        } else {
            String effectStr = effect.toString();
            return "ALLOW".equalsIgnoreCase(effectStr);
        }
    }

    private void applyEntityActionToRow(EntityMatrixRow row, String action) {
        if (EntityPolicyAction.CREATE.getId().equals(action)) {
            row.setCanCreate(true);
        } else if (EntityPolicyAction.READ.getId().equals(action)) {
            row.setCanRead(true);
        } else if (EntityPolicyAction.UPDATE.getId().equals(action)) {
            row.setCanUpdate(true);
        } else if (EntityPolicyAction.DELETE.getId().equals(action)) {
            row.setCanDelete(true);
        }
    }

    private List<ResourcePolicyModel> compressWildcard(List<ResourcePolicyModel> src) {
        Map<String, Set<String>> entityWildcardActions = new HashMap<>();

        for (ResourcePolicyModel p : src) {
            if (isEntityAttributeType(p.getType())
                    && isAllowEffect(p.getEffect())
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
        if (entityName.endsWith("*")) {
            p.setPolicyGroup("");
        } else {
            p.setPolicyGroup(entityName);
        }
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
        p.setPolicyGroup(entityName);
        return p;
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

    private boolean isEntityType(Object type) {
        if (type == null) return false;
        if (type == ResourcePolicyType.ENTITY) return true;
        String typeStr = type.toString().toLowerCase();
        return "entity".equals(typeStr);
    }

    private boolean isEntityAttributeType(Object type) {
        if (type == null) {
            return false;
        }

        if (type == ResourcePolicyType.ENTITY_ATTRIBUTE) {
            return true;
        }

        String typeStr = type.toString()
                .replace("_", "")
                .toLowerCase();

        return "entityattribute".equalsIgnoreCase(typeStr);
    }

}
