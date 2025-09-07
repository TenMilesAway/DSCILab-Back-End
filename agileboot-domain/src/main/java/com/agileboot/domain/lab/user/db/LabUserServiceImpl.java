package com.agileboot.domain.lab.user.db;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 实验室用户Service实现类
 *
 * @author agileboot
 */
@Service
public class LabUserServiceImpl extends ServiceImpl<LabUserMapper, LabUserEntity> implements LabUserService {

    @Override
    public LabUserEntity getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public LabUserEntity getByStudentNumber(String studentNumber) {
        return baseMapper.selectByStudentNumber(studentNumber);
    }

    @Override
    public LabUserEntity getByEmail(String email) {
        return baseMapper.selectByEmail(email);
    }

    @Override
    public LabUserEntity getByPhone(String phone) {
        return baseMapper.selectByPhone(phone);
    }

    @Override
    public boolean isUsernameDuplicated(String username, Long excludeId) {
        return baseMapper.existsByUsername(username, excludeId);
    }

    @Override
    public boolean isStudentNumberDuplicated(String studentNumber, Long excludeId) {
        return baseMapper.existsByStudentNumber(studentNumber, excludeId);
    }

    @Override
    public boolean isEmailDuplicated(String email, Long excludeId) {
        return baseMapper.existsByEmail(email, excludeId);
    }

    @Override
    public boolean isPhoneDuplicated(String phone, Long excludeId) {
        return baseMapper.existsByPhone(phone, excludeId);
    }

    @Override
    public int hardDeleteById(Long id) {
        return baseMapper.hardDeleteById(id);
    }

    @Override
    public LabUserEntity getUniqueByRealName(String realName) {
        if (realName == null || realName.trim().isEmpty()) return null;
        LambdaQueryWrapper<LabUserEntity> q = new LambdaQueryWrapper<>();
        q.eq(LabUserEntity::getRealName, realName.trim());
        List<LabUserEntity> list = this.list(q);
        return list != null && list.size() == 1 ? list.get(0) : null;
    }

    @Override
    public LabUserEntity getUniqueByEnglishName(String englishName) {
        if (englishName == null || englishName.trim().isEmpty()) return null;
        LambdaQueryWrapper<LabUserEntity> q = new LambdaQueryWrapper<>();
        q.eq(LabUserEntity::getEnglishName, englishName.trim());
        List<LabUserEntity> list = this.list(q);
        return list != null && list.size() == 1 ? list.get(0) : null;
    }

}
