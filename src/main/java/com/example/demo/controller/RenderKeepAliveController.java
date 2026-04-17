package com.example.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller for Render keep-alive checks.
 */
@RestController
public class RenderKeepAliveController {

    private static final Logger log = LoggerFactory.getLogger(RenderKeepAliveController.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicInteger pingCount = new AtomicInteger(0);

    @Value("${KEEP_ALIVE_URL:${RENDER_EXTERNAL_URL:}}")
    private String keepAliveBaseUrl;

    /**
     * Health check endpoint for external tools or self-ping requests.
     */
    @GetMapping("/health-check")
    public String healthCheck() {
        pingCount.set(0);
        log.info("Health check received. Counter reset.");
        return "OK - Counter Reset (Current: " + pingCount.get() + ")";
    }

    /**
     * Manual endpoint to test a self-ping immediately.
     */
    @GetMapping("/manual-ping")
    public String manualPing() {
        boolean sent = sendPing("Manual");
        return sent ? "Manual Ping Sent!" : "Manual Ping Skipped: KEEP_ALIVE_URL or RENDER_EXTERNAL_URL is empty.";
    }

    /**
     * Runs every 12 minutes. LocalTime follows the server timezone, so set TZ on Render.
     */
    @Scheduled(fixedRate = 720000)
    public void scheduledKeepAlive() {
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.of(9, 30);
        LocalTime end = LocalTime.of(18, 0);

        if (now.isAfter(start) && now.isBefore(end)) {
            sendPing("Auto");
        }
    }

    private boolean sendPing(String type) {
        String targetUrl = buildHealthCheckUrl();
        if (targetUrl == null) {
            log.info("[{}] Keep-alive skipped because no app URL is configured.", type);
            return false;
        }

        try {
            restTemplate.getForObject(targetUrl, String.class);
            int currentCount = pingCount.incrementAndGet();
            log.info("[{}] Keep-alive ping sent. Count: {}", type, currentCount);
            return true;
        } catch (Exception e) {
            log.error("[{}] Keep-alive ping failed: {}", type, e.getMessage());
            return false;
        }
    }

    private String buildHealthCheckUrl() {
        if (keepAliveBaseUrl == null || keepAliveBaseUrl.trim().isEmpty()) {
            return null;
        }

        String trimmed = keepAliveBaseUrl.trim();
        if (trimmed.endsWith("/health-check")) {
            return trimmed;
        }
        if (trimmed.endsWith("/")) {
            return trimmed + "health-check";
        }
        return trimmed + "/health-check";
    }
}
