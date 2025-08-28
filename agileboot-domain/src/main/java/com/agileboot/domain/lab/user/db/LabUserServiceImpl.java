package com.agileboot.domain.lab.user.db;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
