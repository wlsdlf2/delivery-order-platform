package com.sparta.deliveryorderplatform;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("DB/Redis 없는 로컬 환경에서는 실행 불가")
@SpringBootTest
class DeliveryOrderPlatformApplicationTests {

    @Test
    void contextLoads() {
    }

}
