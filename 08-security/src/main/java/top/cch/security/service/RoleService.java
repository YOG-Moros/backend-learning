package top.cch.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.cch.security.entity.SecRole;
import top.cch.security.entity.SecUserRole;
import top.cch.security.mapper.SecRoleMapper;
import top.cch.security.mapper.SecUserRoleMapper;

import java.util.List;

/**
 * @author cch
 * @date 2026/10/28
 * @description RoleService 查用户角色编码（过滤器与登录共用）
 **/
@Service
@RequiredArgsConstructor
public class RoleService {

    private final SecUserRoleMapper userRoleMapper;
    private final SecRoleMapper roleMapper;

    public List<String> loadRoleCodes(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(
                        new LambdaQueryWrapper<SecUserRole>().eq(SecUserRole::getUserId, userId))
                .stream().map(SecUserRole::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectBatchIds(roleIds).stream().map(SecRole::getCode).toList();
    }
}
