package com.boriselec.rimworld.aiart;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Controller
public class StatusController {
    private final Status status;

    public StatusController(Status status) {
        this.status = status;
    }

    @GetMapping("/status")
    public String getStatus(Model model) {
        int queueSize = status.queueSize();
        int queueUserSize = status.queueUserSize();
        String timeAgo = Optional.ofNullable(status.lastSuccess())
            .map(lastSuccess -> Duration.between(lastSuccess, Instant.now()))
            .map(this::formatDuration)
            .orElse("never");

        boolean isTimeWarning = timeAgo.equals("never") || timeAgo.contains("day");

        model.addAttribute("timeAgo", timeAgo);
        model.addAttribute("isTimeWarning", isTimeWarning);
        model.addAttribute("queueSize", queueSize);
        model.addAttribute("queueUserSize", queueUserSize);

        return "status";
    }

    private String formatDuration(Duration d) {
        long days = d.toDays();
        if (days > 0) {
            return days + " day" + (days != 1 ? "s" : "") + " ago";
        }

        long hours = d.toHours();
        if (hours > 0) {
            return hours + " hour" + (hours != 1 ? "s" : "") + " ago";
        }

        long minutes = d.toMinutes();
        if (minutes > 0) {
            return minutes + " minute" + (minutes != 1 ? "s" : "") + " ago";
        }

        return d.getSeconds() + " second" + (d.getSeconds() != 1 ? "s" : "") + " ago";
    }
}