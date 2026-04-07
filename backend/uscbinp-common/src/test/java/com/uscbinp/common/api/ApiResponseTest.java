package com.uscbinp.common.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiResponseTest {

    @Test
    void okResponseContainsAllRequiredFields() {
        ApiResponse<String> res = ApiResponse.ok("UP");
        assertEquals("00000", res.getCode());
        assertEquals("OK", res.getMessage());
        assertEquals("UP", res.getData());
        assertNotNull(res.getTimestamp());
    }
}
