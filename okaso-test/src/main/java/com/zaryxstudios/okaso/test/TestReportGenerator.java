package com.zaryxstudios.okaso.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public final class TestReportGenerator {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private TestReportGenerator() {
    }

    public static String generateTextReport(SimpleTestFramework framework) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("  Okaso Test Report\n");
        sb.append("  ").append(DATE_FORMAT.format(new Date())).append('\n');
        sb.append("========================================\n\n");

        List<SimpleTestFramework.TestResult> results = framework.getResults();
        if (results.isEmpty()) {
            sb.append("  No test results recorded.\n");
        } else {
            for (int i = 0; i < results.size(); i++) {
                SimpleTestFramework.TestResult r = results.get(i);
                sb.append(String.format("  %3d. %s [%s] %s%n",
                    i + 1,
                    r.isPassed() ? "\u2713" : "\u2717",
                    r.getType(),
                    r.getMessage()));
            }
        }

        sb.append('\n');
        sb.append("  Total:  ").append(results.size()).append('\n');
        sb.append("  Passed: ").append(framework.getPassed()).append('\n');
        sb.append("  Failed: ").append(framework.getFailed()).append('\n');
        sb.append("  Status: ").append(framework.isAllPassed() ? "ALL PASSED" : "SOME FAILED").append('\n');
        sb.append("========================================\n");
        return sb.toString();
    }

    public static void generateHtmlReport(SimpleTestFramework framework, File outputFile, String reportTitle)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8));

        try {
            writer.write("<!DOCTYPE html>");
            writer.write("<html lang=\"en\">");
            writer.write("<head><meta charset=\"UTF-8\">");
            writer.write("<title>");
            writer.write(escapeHtml(reportTitle));
            writer.write("</title>");
            writer.write("<style>");
            writer.write("body{font-family:system-ui,sans-serif;max-width:800px;margin:2em auto;padding:0 1em}");
            writer.write("h1{color:#333}.pass{color:#090}.fail{color:#d00}.summary{background:#f5f5f5;padding:1em;border-radius:8px}");
            writer.write("table{width:100%;border-collapse:collapse;margin-top:1em}");
            writer.write("th,td{text-align:left;padding:8px;border-bottom:1px solid #ddd}");
            writer.write("th{background:#eee}");
            writer.write("</style>");
            writer.write("</head><body>");
            writer.write("<h1>");
            writer.write(escapeHtml(reportTitle));
            writer.write("</h1>");
            writer.write("<p>Generated: " + DATE_FORMAT.format(new Date()) + "</p>");

            writer.write("<div class=\"summary\">");
            writer.write("<h2>Summary</h2>");
            writer.write("<p class=\"" + (framework.isAllPassed() ? "pass" : "fail") + "\">");
            writer.write("Total: " + (framework.getPassed() + framework.getFailed()));
            writer.write(" | Passed: " + framework.getPassed());
            writer.write(" | Failed: " + framework.getFailed());
            writer.write(" | Status: " + (framework.isAllPassed() ? "ALL PASSED" : "SOME FAILED"));
            writer.write("</p></div>");

            List<SimpleTestFramework.TestResult> results = framework.getResults();
            if (!results.isEmpty()) {
                writer.write("<table><thead><tr>");
                writer.write("<th>#</th><th>Result</th><th>Type</th><th>Message</th>");
                writer.write("</tr></thead><tbody>");
                for (int i = 0; i < results.size(); i++) {
                    SimpleTestFramework.TestResult r = results.get(i);
                    writer.write("<tr>");
                    writer.write("<td>" + (i + 1) + "</td>");
                    writer.write("<td class=\"" + (r.isPassed() ? "pass" : "fail") + "\">");
                    writer.write(r.isPassed() ? "\u2713" : "\u2717");
                    writer.write("</td>");
                    writer.write("<td>" + escapeHtml(r.getType()) + "</td>");
                    writer.write("<td>" + escapeHtml(r.getMessage()) + "</td>");
                    writer.write("</tr>");
                }
                writer.write("</tbody></table>");
            } else {
                writer.write("<p>No test results recorded.</p>");
            }

            writer.write("</body></html>");
        } finally {
            writer.close();
        }
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&':  sb.append("&amp;");  break;
                case '<':  sb.append("&lt;");   break;
                case '>':  sb.append("&gt;");   break;
                case '"':  sb.append("&quot;"); break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }
}
