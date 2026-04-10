package com.agileboot.domain.lab.event.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class LabEventServiceImpl extends ServiceImpl<LabEventMapper, LabEventEntity>
    implements LabEventService {
}
