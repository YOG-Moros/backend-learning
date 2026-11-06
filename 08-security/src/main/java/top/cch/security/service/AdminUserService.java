package top.cch.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.cch.security.common.BusinessException;
import top.cch.security.common.ErrorCode;
import top.cch.security.dto.UserCreateRequest;
import top.cch.security.entity.SecRole;
import top.cch.security.entity.SecUser;
import top.cch.security.entity.SecUserRole;
import top.cch.security.mapper.SecRoleMapper;
import top.cch.security.mapper.SecUserMapper;
import top.cch.security.mapper.SecUserRoleMapper;
import top.cch.security.model.LoginUser;
import top.cch.security.security.RolePermissions;
import top.cch.security.vo.UserVO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cch
 * @date 2026/10/28
 * @description AdminUserService 管理员账号管理：列表 / 新增（用户与角色同事务）/ 删除（先删关联）
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final SecUserMapper userMapper;
    private final SecRoleMapper roleMapper;
    private final SecUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    /** 账号列表：带角色与权限点，绝不返回 password */
    public List<UserVO> list() {
        List<SecUser> users = userMapper.selectList(
                new LambdaQueryWrapper<SecUser>().orderByAsc(SecUser::getId));
        Map<Long, String> roleCodeById = roleMapper.selectList(null).stream()
                .collect(Collectors.toMap(SecRole::getId, SecRole::getCode, (a, b) -> a));
        Map<Long, List<String>> rolesByUser = userRoleMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(SecUserRole::getUserId,
                        Collectors.mapping(ur -> roleCodeById.get(ur.getRoleId()),
                                Collectors.toList())));
        return users.stream()
                .map(u -> toVO(u, rolesByUser.getOrDefault(u.getId(), List.of())))
                .toList();
    }

    /** 新增账号：密码 BCrypt 加密；账号表和角色关联表同一事务，roles 支持多个角色 */
    @Transactional(rollbackFor = Exception.class)
    public UserVO create(UserCreateRequest request) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SecUser>()
                .eq(SecUser::getUsername, request.getUsername()));
        if (exists != null && exists > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        List<SecRole> roles = roleMapper.selectList(
                new LambdaQueryWrapper<SecRole>().in(SecRole::getCode, request.getRoles()));
        if (roles.size() != request.getRoles().size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        SecUser user = new SecUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEnabled(true);
        userMapper.insert(user);
        roles.forEach(role -> {
            SecUserRole userRole = new SecUserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());
            userRoleMapper.insert(userRole);
        });
        List<String> roleCodes = roles.stream().map(SecRole::getCode).toList();
        log.info("管理员新增账号 {}，角色 {}", request.getUsername(), roleCodes);
        return toVO(user, roleCodes);
    }

    /** 删除账号：先删关联表再删账号；不能删除当前登录账号 */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String username, LoginUser current) {
        if (current.getUsername().equals(username)) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE_SELF);
        }
        SecUser target = userMapper.selectOne(
                new LambdaQueryWrapper<SecUser>().eq(SecUser::getUsername, username));
        if (target == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SecUserRole>()
                .eq(SecUserRole::getUserId, target.getId()));
        userMapper.deleteById(target.getId());
        log.info("管理员 {} 删除账号 {}", current.getUsername(), username);
    }

    private UserVO toVO(SecUser user, List<String> roles) {
        return new UserVO(user.getId(), user.getUsername(), user.getNickname(),
                user.getEnabled(), roles, RolePermissions.permissionsOf(roles));
    }
}
