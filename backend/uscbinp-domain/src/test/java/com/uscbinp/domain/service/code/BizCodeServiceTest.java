package com.uscbinp.domain.service.code;

import com.uscbinp.domain.service.code.impl.InMemoryBizCodeService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BizCodeServiceTest {

    @Test
    void shouldGenerateExpectedFormatAndSequence() {
        BizCodeService service = new InMemoryBizCodeService();

        String c1 = service.generate(BizCodeService.BizCategory.NET, "3301", "L1");
        String c2 = service.generate(BizCodeService.BizCategory.NET, "3301", "L1");

        assertTrue(c1.matches("3301-NET-L1-\\d{6}"));
        assertTrue(c2.matches("3301-NET-L1-\\d{6}"));
        assertNotEquals(c1, c2);
        assertEquals("3301-NET-L1-000001", c1);
        assertEquals("3301-NET-L1-000002", c2);
    }
}
