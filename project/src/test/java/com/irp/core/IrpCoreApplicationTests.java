package com.irp.core;

import com.irp.core.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

/** If the context fails to load - bad bean wiring, a broken Flyway migration, etc. - this is the first thing to fail. */
class IrpCoreApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }
}
