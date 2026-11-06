package top.cch.toolkit.service;

import org.junit.jupiter.api.Test;
import top.cch.toolkit.common.BusinessException;
import top.cch.toolkit.config.ToolkitProperties;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

class SingleUseConcurrencyTest {
    private int simultaneous(Runnable action) throws Exception {
        var start=new CountDownLatch(1);
        try(var pool=Executors.newFixedThreadPool(12)) {
            var futures=new java.util.ArrayList<Future<Boolean>>();
            for(int i=0;i<12;i++) futures.add(pool.submit(()->{start.await();try{action.run();return true;}catch(BusinessException e){return false;}}));
            start.countDown();int accepted=0;
            for(var future:futures) if(future.get(5,TimeUnit.SECONDS)) accepted++;
            return accepted;
        }
    }
    @Test void aSignatureNonceIsAcceptedOnlyOnceUnderConcurrency() throws Exception {
        var properties = new ToolkitProperties();
        properties.getSign().setAppSecret("concurrency-test-secret");
        var service=new SignService(properties);service.init();
        try {
            var signature=service.demo(null,null,"concurrent","{}");
            assertThat(simultaneous(()->service.verify(signature.appId(),signature.timestamp(),signature.nonce(),signature.sign(),signature.body()))).isEqualTo(1);
        } finally {service.destroy();}
    }
    @Test void captchaIsAcceptedOnlyOnceUnderConcurrency() throws Exception {
        var properties=new ToolkitProperties();properties.getCaptcha().setDebugCode(true);
        var service=new CaptchaService(properties);service.init();
        try {
            var captcha=service.create("line");
            assertThat(simultaneous(()->service.verify(captcha.captchaId(),captcha.code()))).isEqualTo(1);
        } finally {service.destroy();}
    }
}
