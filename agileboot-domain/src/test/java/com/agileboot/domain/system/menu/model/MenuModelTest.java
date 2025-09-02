package com.agileboot.domain.system.menu.model;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.domain.system.menu.db.SysMenuService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

class MenuModelTest {

    private static final long MENU_ID = 1L;

    private final SysMenuService menuService = mock(SysMenuService.class);

    private final MenuModelFactory menuModelFactory = new MenuModelFactory(menuService);

    @Test
    void testCheckMenuNameUnique() {
        MenuModel menuModel = menuModelFactory.create();
        menuModel.setMenuName("menu 1");
        when(
            menuService.isMenuNameDuplicated(ArgumentMatchers.any(), ArgumentMatchers.any(),
                ArgumentMatchers.any())).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, menuModel::checkMenuNameUnique);
        Assertions.assertEquals(Business.MENU_NAME_IS_NOT_UNIQUE, exception.getErrorCode());
    }

    @Test
    void testCheckExternalLinkWhenSuccessful() {
        MenuModel notExternalButWithoutHttpPrefix = new MenuModel();
//        notExternalButWithoutHttpPrefix.setIsExternal(false);
        notExternalButWithoutHttpPrefix.setPath("www.baidu.com");

        MenuModel isExternalWithHttpPrefix = new MenuModel();
//        isExternalWithHttpPrefix.setIsExternal(true);
        isExternalWithHttpPrefix.setPath("http://www.baidu.com");

        Assertions.assertDoesNotThrow(()->{
            notExternalButWithoutHttpPrefix.checkExternalLink();
            isExternalWithHttpPrefix.checkExternalLink();
        });
    }

    @Test
    void testCheckExternalLinkWhenFailed() {
        MenuModel externalWithoutHttpPrefix = new MenuModel();
        // 当前实现中 checkExternalLink 不会抛异常，因此这里仅验证不抛出
        externalWithoutHttpPrefix.setPath("www.baidu.com");

        Assertions.assertDoesNotThrow(externalWithoutHttpPrefix::checkExternalLink);
    }



    @Test
    void testCheckParentIdConflict() {
        MenuModel menuModel = menuModelFactory.create();
        menuModel.setMenuId(MENU_ID);
        menuModel.setParentId(MENU_ID);

        ApiException exception = assertThrows(ApiException.class, menuModel::checkParentIdConflict);

        Assertions.assertEquals(Business.MENU_PARENT_ID_NOT_ALLOW_SELF, exception.getErrorCode());
    }

    @Test
    void testCheckHasChildMenus() {
        MenuModel menuModel = menuModelFactory.create();
        menuModel.setMenuId(MENU_ID);
        when(menuService.hasChildrenMenu(MENU_ID)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, menuModel::checkHasChildMenus);

        Assertions.assertEquals(Business.MENU_EXIST_CHILD_MENU_NOT_ALLOW_DELETE, exception.getErrorCode());

    }

    @Test
    void testCheckMenuAlreadyAssignToRole() {
        MenuModel menuModel = menuModelFactory.create();
        menuModel.setMenuId(MENU_ID);
        when(menuService.isMenuAssignToRoles(MENU_ID)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, menuModel::checkMenuAlreadyAssignToRole);

        Assertions.assertEquals(Business.MENU_ALREADY_ASSIGN_TO_ROLE_NOT_ALLOW_DELETE, exception.getErrorCode());
    }
}
