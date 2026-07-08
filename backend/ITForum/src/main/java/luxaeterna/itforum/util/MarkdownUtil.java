package luxaeterna.itforum.util;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class MarkdownUtil {

    private static final Parser PARSER;
    private static final HtmlRenderer RENDERER;

    static {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, java.util.Arrays.asList(
                com.vladsch.flexmark.ext.tables.TablesExtension.create(),
                com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension.create(),
                com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension.create(),
                com.vladsch.flexmark.ext.autolink.AutolinkExtension.create(),
                com.vladsch.flexmark.ext.emoji.EmojiExtension.create(),
                com.vladsch.flexmark.ext.typographic.TypographicExtension.create()
        ));
        PARSER = Parser.builder(options).build();
        RENDERER = HtmlRenderer.builder(options).build();
    }

    public static String toHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        return RENDERER.render(PARSER.parse(markdown));
    }

    public static String stripMarkdown(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        String html = toHtml(markdown);
        return html.replaceAll("<[^>]+>", "")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
