package com.uscbinp.infra.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MybatisPlusConfigTest {

    @Test
    void interceptorContainsPaginationPlugin() {
        MybatisPlusInterceptor interceptor = new MybatisPlusConfig().mybatisPlusInterceptor();
        assertNotNull(interceptor);
        assertTrue(interceptor.getInterceptors().stream().anyMatch(PaginationInnerInterceptor.class::isInstance));
    }
}
