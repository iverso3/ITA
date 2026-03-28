package com.bank.itarch.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.mapper.SysUserRoleMapper;
import com.bank.itarch.model.entity.SysUserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserRoleService extends ServiceImpl<SysUserRoleMapper, SysUserRole> {

    public List<Long> getRoleIdsByUserId(Long userId) {
        return baseMapper.selectRoleIdsByUserId(userId);
    }

    public List<String> getRoleCodesByUserId(Long userId) {
        return baseMapper.selectRoleCodesByUserId(userId);
    }
}
