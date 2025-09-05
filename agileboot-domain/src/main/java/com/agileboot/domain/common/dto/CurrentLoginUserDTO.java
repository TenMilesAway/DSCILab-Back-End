package com.agileboot.domain.common.dto;

import com.agileboot.domain.system.user.dto.UserDTO;
import java.util.Set;
import lombok.Data;

/**
 * @author valarchie
 */
@Data
public class CurrentLoginUserDTO {

    // 为兼容lab-only返回lab_user字段，这里放宽为Object
    private Object userInfo;
    private String roleKey;
    private Set<String> permissions;

}
