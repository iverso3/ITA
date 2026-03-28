package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.SysUserMapper;
import com.bank.itarch.model.entity.SysUser;
import com.bank.itarch.util.UserContext;
import cn.hutool.crypto.SecureUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    public PageResult<SysUser> pageQuery(PageQuery query, String keyword, String status, Long departmentId) {
        Page<SysUser> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(keyword), w -> w.like(SysUser::getUsername, keyword).or().like(SysUser::getRealName, keyword))
               .eq(StringUtils.hasText(status), SysUser::getStatus, status)
               .eq(departmentId != null, SysUser::getDepartmentId, departmentId)
               .orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = page(page, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return PageResult.of(result.getRecords(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    public SysUser detail(Long id) {
        SysUser user = getById(id);
        if (user == null) throw new BusinessException(1001, "用户不存在");
        user.setPassword(null);
        return user;
    }

    public SysUser create(SysUser entity) {
        if (StringUtils.hasText(entity.getPassword())) {
            entity.setPassword(SecureUtil.md5(entity.getPassword()));
        }
        save(entity);
        entity.setPassword(null);
        return entity;
    }

    public SysUser update(Long id, SysUser entity) {
        if (getById(id) == null) throw new BusinessException(1001, "用户不存在");
        entity.setId(id);
        entity.setPassword(null);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        if (getById(id) == null) throw new BusinessException(1001, "用户不存在");
        removeById(id);
    }

    public void toggleStatus(Long id, String status) {
        SysUser user = getById(id);
        if (user == null) throw new BusinessException(1001, "用户不存在");
        user.setStatus(status);
        updateById(user);
    }

    public void resetPassword(Long id) {
        SysUser user = getById(id);
        if (user == null) throw new BusinessException(1001, "用户不存在");
        user.setPassword(SecureUtil.md5("123456"));
        user.setPasswordExpireTime(LocalDateTime.now().plusDays(90));
        user.setIsFirstLogin(1);
        updateById(user);
    }

    public void changePassword(Long id, String oldPassword, String newPassword) {
        SysUser user = getById(id);
        if (user == null) throw new BusinessException(1001, "用户不存在");
        String encodedOld = SecureUtil.md5(oldPassword);
        if (StringUtils.hasText(user.getPassword()) && !user.getPassword().equals(encodedOld)) {
            throw new BusinessException(1001, "原密码错误");
        }
        user.setPassword(SecureUtil.md5(newPassword));
        user.setPasswordExpireTime(LocalDateTime.now().plusDays(90));
        updateById(user);
    }

    public SysUser findByUsername(String username) {
        return getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    public SysUser getCurrentUser() {
        UserContext.UserInfo userInfo = UserContext.getUser();
        if (userInfo == null) {
            return null;
        }
        SysUser user = getById(userInfo.getUserId());
        if (user != null) user.setPassword(null);
        return user;
    }

    public void updateLoginInfo(Long id, String ip) {
        SysUser user = getById(id);
        if (user != null) {
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(ip);
            user.setIsFirstLogin(0);
            updateById(user);
        }
    }
}
