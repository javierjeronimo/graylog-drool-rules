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

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

public class ExecutionTimeRuleTest {
    static final Logger LOG = LoggerFactory.getLogger(ExecutionTimeRuleTest.class);

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
    public void test_no_executionTime_field() {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(Message.FIELD_ID, UUID.randomUUID().toString());

        Message originalMessage = new Message(fields);
        session.insert(originalMessage);
        session.fireAllRules();

        assertEquals(1, session.getObjects().size());
        Message result = (Message) session.getObjects().iterator().next();
        assertNull(result.getField("executionTime"));
    }

    @Test
    public void test_executionTime_input_integer_field() {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(Message.FIELD_ID, UUID.randomUUID().toString());
        fields.put("executionTime", Integer.valueOf(123));

        Message originalMessage = new Message(fields);
        session.insert(originalMessage);
        session.fireAllRules();

        assertEquals(1, session.getObjects().size());
        Message result = (Message) session.getObjects().iterator().next();
        assertNotNull(result.getField("executionTime"));
        assertEquals(Integer.class, result.getField("executionTime").getClass());
        assertEquals(123, result.getField("executionTime"));
    }

    @Test
    public void test_executionTime_input_string_field() {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(Message.FIELD_ID, UUID.randomUUID().toString());
        fields.put("executionTime", "123");

        Message originalMessage = new Message(fields);
        session.insert(originalMessage);
        session.fireAllRules();

        assertEquals(1, session.getObjects().size());
        Message result = (Message) session.getObjects().iterator().next();
        assertNotNull(result.getField("executionTime"));
        assertEquals(Integer.class, result.getField("executionTime").getClass());
        assertEquals(123, result.getField("executionTime"));
    }
}