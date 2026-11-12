package com.enterprise.mobile.bdd.plugin;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Logs every Gherkin step as it runs, together with its outcome and duration.
 *
 * <p>Enable by adding {@code com.enterprise.mobile.bdd.plugin.StepLoggingPlugin}
 * to the runner's {@code @CucumberOptions(plugin = ...)} list. Output example:</p>
 * <pre>
 * STEP 3/14 &gt;&gt; When the user opens the Account tab from the footer
 * STEP 3/14 &lt;&lt; PASSED (1.2s) When the user opens the Account tab from the footer
 * </pre>
 */
public class StepLoggingPlugin implements ConcurrentEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(StepLoggingPlugin.class);

    private final Map<String, Long> startedAt = new ConcurrentHashMap<>();

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepStarted.class, this::stepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::stepFinished);
    }

    private void stepStarted(TestStepStarted event) {
        if (!(event.getTestStep() instanceof PickleStepTestStep step)) {
            return;
        }
        startedAt.put(key(event), System.currentTimeMillis());
    }

    private void stepFinished(TestStepFinished event) {
        if (!(event.getTestStep() instanceof PickleStepTestStep step)) {
            return;
        }
        Long start = startedAt.remove(key(event));
        long elapsed = start == null ? 0L : System.currentTimeMillis() - start;
        Status status = event.getResult().getStatus();
        String text = step.getStep().getText();
        if (status == Status.SKIPPED) {
            LOG.info("SKIPPED [{}] {}", keyword(step), text);
        } else if (status == Status.PASSED) {
            LOG.info("STEP PASSED [{}] {} ({} ms)", keyword(step), text, elapsed);
        } else {
            LOG.warn("STEP {} [{}] {} ({} ms) {}", status, keyword(step), text, elapsed,
                    event.getResult().getError() == null ? "" : event.getResult().getError().getMessage());
        }
    }

    private static String key(TestStepStarted event) {
        return event.getTestCase().getId() + ":" + ((PickleStepTestStep) event.getTestStep()).getStep().getLine();
    }

    private static String key(TestStepFinished event) {
        return event.getTestCase().getId() + ":" + ((PickleStepTestStep) event.getTestStep()).getStep().getLine();
    }

    private static String keyword(PickleStepTestStep step) {
        String keyword = step.getStep().getKeyword();
        return keyword == null ? "" : keyword.trim();
    }
}
