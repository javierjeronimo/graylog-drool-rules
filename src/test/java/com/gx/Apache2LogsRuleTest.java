package com.gx;

import org.graylog2.plugin.Message;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.Results;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class Apache2LogsRuleTest {
    static final Logger LOG = LoggerFactory.getLogger(Apache2LogsRuleTest.class);

    private KieSession session;

    @Before
    public void setUp() throws Exception {
        KieServices kieServices = KieServices.Factory.get();

        KieContainer kContainer = kieServices.getKieClasspathContainer();
        Results verifyResults = kContainer.verify();
        for (org.kie.api.builder.Message m : verifyResults.getMessages()) {
            LOG.info("{}", m);
        }

        LOG.info("Creating kieBase");
        KieBase kieBase = kContainer.getKieBase();

        LOG.info("There should be rules: ");
        for (KiePackage kp : kieBase.getKiePackages()) {
            for (Rule rule : kp.getRules()) {
                LOG.info("kp " + kp + " rule " + rule.getName());
            }
        }

        LOG.info("Creating kieSession");
        session = kieBase.newKieSession();

        LOG.info("Now running data");
    }

    @Test
    public void test_typical_log() {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(Message.FIELD_ID, UUID.randomUUID().toString());
        fields.put(Message.FIELD_MESSAGE, "\"186.166.232.236\" \"-\" \"-\" \"[06/Sep/2015:06:53:44 +0200]\" \"GET /v2/portals/ve_movistar/zedwebssoauthentication/sessions?uuid=70212b7be4ecbaa27d0dc7f053988423&returnDir=http%3A%2F%2Fve.movistar.genexies.net%2Fsection%2Fpreview%2F0-4-2-15-6-1-9-11%2F7718312%2F0%2F3%2F1%2F0%2Fxhtmlmp%3Fgxs%3D8e53b9ff862cb712eca78af3e991b4a5 HTTP/1.1\" \"302\" \"541\" \"http://ve.movistar.genexies.net/section/search/0/0/xhtmlmp?gxs=8e53b9ff862cb712eca78af3e991b4a5&_q=romeo&_t=0-4-2-15-6-1-9-11&_k=0&_sa=1&_p=0&commit=Buscar\" \"Mozilla/5.0 (BlackBerry; U; BlackBerry 9320; es) AppleWebKit/534.11+ (KHTML, like Gecko) Version/7.1.0.1011 Mobile Safari/534.11+\" \"\\\"http://www.blackberry.net/go/mobile/profiles/uaprof/9320_umts/7.1.0.rdf\\\"\" \"-\" \"-\" \"Time: 1028835\" \"-\" \"541\" \"11064\" \"-\" \"-\" \"-\" \"-\" \"-\" \"-\" \"-\"");
        fields.put("facility", "gnxphp_apache");

        Message originalMessage = new Message(fields);
        session.insert(originalMessage);
        session.fireAllRules();

        assertEquals(1, session.getObjects().size());
        Message result = (Message) session.getObjects().iterator().next();

        assertEquals("186.166.232.236", result.getField("remote_address"));

        assertEquals("GET", result.getField("method"));

        assertEquals("/v2/portals/ve_movistar/zedwebssoauthentication/sessions?uuid=70212b7be4ecbaa27d0dc7f053988423&returnDir=http%3A%2F%2Fve.movistar.genexies.net%2Fsection%2Fpreview%2F0-4-2-15-6-1-9-11%2F7718312%2F0%2F3%2F1%2F0%2Fxhtmlmp%3Fgxs%3D8e53b9ff862cb712eca78af3e991b4a5", result.getField("request_uri"));
        assertEquals("v2", result.getField("api_version"));

        assertEquals(Integer.class, result.getField("response_code").getClass());
        assertEquals(302, result.getField("response_code"));

        assertEquals(Integer.class, result.getField("response_size").getClass());
        assertEquals(541, result.getField("response_size"));

        assertEquals("http://ve.movistar.genexies.net/section/search/0/0/xhtmlmp?gxs=8e53b9ff862cb712eca78af3e991b4a5&_q=romeo&_t=0-4-2-15-6-1-9-11&_k=0&_sa=1&_p=0&commit=Buscar", result.getField("referer"));

        assertEquals("Mozilla/5.0 (BlackBerry; U; BlackBerry 9320; es) AppleWebKit/534.11+ (KHTML, like Gecko) Version/7.1.0.1011 Mobile Safari/534.11+", result.getField("user_agent"));

        assertEquals("http://www.blackberry.net/go/mobile/profiles/uaprof/9320_umts/7.1.0.rdf", result.getField("x-wap-profile"));

        assertNull(result.getField("TM-User-Id"));

        assertNull(result.getField("X-Up-Subno"));

        assertEquals(Integer.class, result.getField("processing_time_seconds").getClass());
        assertEquals(1, result.getField("processing_time_seconds"));

        assertEquals("-", result.getField("connection_status"));

        assertEquals(Integer.class, result.getField("content_length").getClass());
        assertEquals(541, result.getField("content_length"));

        assertEquals(Integer.class, result.getField("thread").getClass());
        assertEquals(11064, result.getField("thread"));

        assertNull(result.getField("X-Up-Calling-Line-Id"));

        assertNull(result.getField("X-Network-Info"));

        assertNull(result.getField("X-Msisdn"));

        assertNull(result.getField("X-Ztgo-BearerAddress"));

        assertNull(result.getField("X-DRUTT-PORTAL-USER-MSISDN"));

        assertNull(result.getField("X-Access-Subnym"));
    }
}