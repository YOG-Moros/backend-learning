package top.cch.security.security;

import java.util.List;
import java.util.Map;

/**
 * @author cch
 * @date 2026/10/28
 * @description RolePermissions 角色 → 权限点映射：
 *              角色给人看，权限点给接口用；新增角色只要在这里配权限点，Controller 不用改
 **/
public final class RolePermissions {

    private RolePermissions() {
    }

    public static final Map<String, List<String>> MAPPING = Map.of(
            "STUDENT", List.of("task:submit", "task:read"),
            "TEACHER", List.of("task:review", "task:read"),
            "ADMIN", List.of("user:read", "user:write", "task:read", "task:review")
    );

    public static List<String> permissionsOf(List<String> roleCodes) {
        return roleCodes.stream()
                .flatMap(code -> MAPPING.getOrDefault(code, List.of()).stream())
                .distinct()
                .toList();
    }
}
