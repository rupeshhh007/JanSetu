package com.jansetu.backend.source.change;

import com.jansetu.backend.source.SourceSnapshot;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

/**
 * Produces a conservative comparable representation of textual evidence.
 * Binary evidence remains immutable but is not interpreted as policy content.
 */
@Component
public class SourceContentNormalizer {
    public Optional<String> normalizedText(SourceSnapshot snapshot) {
        String contentType = snapshot.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith("text/") && !"application/xhtml+xml".equals(contentType)) {
            return Optional.empty();
        }
        String text = new String(snapshot.getContentBytes(), StandardCharsets.UTF_8);
        if (contentType.contains("html")) {
            text = mainContent(text);
            text = text.replaceAll("(?is)<!--.*?-->", " ")
                    .replaceAll("(?is)<(script|style|noscript|svg|iframe)[^>]*>.*?</\\1>", " ")
                    .replaceAll("(?is)<[^>]+>", " ")
                    .replace("&nbsp;", " ")
                    .replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">");
        }
        text = Normalizer.normalize(text, Normalizer.Form.NFKC).replaceAll("\\s+", " ").trim();
        return text.isBlank() ? Optional.empty() : Optional.of(text);
    }

    private String mainContent(String html) {
        var main = java.util.regex.Pattern.compile("(?is)<main[^>]*>(.*?)</main>").matcher(html);
        if (main.find()) return main.group(1);
        var article = java.util.regex.Pattern.compile("(?is)<article[^>]*>(.*?)</article>").matcher(html);
        return article.find() ? article.group(1) : html;
    }
}
