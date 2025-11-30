package com.boriselec.rimworld.aiart;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@RestController
public class StatusController {
    private final Status status;

    public StatusController(Status status) {
        this.status = status;
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        int queueSize = status.queueSize();
        int queueUserSize = status.queueUserSize();
        String timeAgo = Optional.ofNullable(status.lastSuccess())
            .map(lastSuccess -> Duration.between(lastSuccess, Instant.now()))
            .map(this::formatDuration)
            .orElse("never");

        String styledTimeAgo = (timeAgo.equals("never") || timeAgo.contains("day"))
            ? String.format("<span style=\"font-weight:bold;color:red;\">%s</span>",
                    timeAgo)
            : String.format("<span style=\"font-weight:bold;color:green;\">%s</span>",
                    timeAgo);

        String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>RimWorld AI Art Server Status</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        max-width: 800px;
                        margin: 40px auto;
                        padding: 20px;
                        background: #f5f5f5;
                    }
                    h1 {
                        color: #333;
                        border-bottom: 2px solid #333;
                        padding-bottom: 10px;
                    }
                    table {
                        width: 100%%;
                        border-collapse: collapse;
                        background: white;
                        border: 1px solid #ddd;
                    }
                    td {
                        padding: 12px;
                        text-align: left;
                        border-bottom: 1px solid #ddd;
                    }
                </style>
            </head>
            <body>
                <h1>AI Art Server Status</h1>
                <table>
                    <tr>
                        <td>Last Successful Generation</td>
                        <td>%s</td>
                    </tr>
                    <tr>
                        <td>Queue Size</td>
                        <td>%d</td>
                    </tr>
                    <tr>
                        <td>Players Waiting</td>
                        <td>%d</td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(styledTimeAgo, queueSize, queueUserSize);

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(html);
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
