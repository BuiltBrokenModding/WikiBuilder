package com.builtbroken.test.html;

import com.builtbroken.builder.html.PageTemplate;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/16/2017.
 */
public class PageTemplateTest extends TestCase
{
    @Test
    public void testTemplate()
    {
        PageTemplate template = new PageTemplate("template", "template.html");
        String file = "<p>#data:content#</p>";
        template.process(file);

        assertEquals(3, template.htmlSegments.length);
        assertEquals("<p>", template.htmlSegments[0]);
        assertEquals("data:content", template.htmlSegments[1]);
        assertEquals("</p>", template.htmlSegments[2]);

        assertEquals(1, template.injectionTags.size());
        assertEquals(1, (int) template.injectionTags.get("data:content"));
    }

    @Test
    public void testTemplate2()
    {
        PageTemplate template = getSmallTemplate();

        assertEquals(7, template.htmlSegments.length);
        assertEquals("data:content1", template.htmlSegments[1]);
        assertEquals("data:content2", template.htmlSegments[3]);
        assertEquals("data:content3", template.htmlSegments[5]);

        assertEquals(3, template.injectionTags.size());
        assertEquals(1, (int) template.injectionTags.get("data:content1"));
        assertEquals(3, (int) template.injectionTags.get("data:content2"));
        assertEquals(5, (int) template.injectionTags.get("data:content3"));
    }

    public static PageTemplate getSmallTemplate()
    {
        PageTemplate template = new PageTemplate("template", "template.html");
        String file = "" +
                "<html>" +
                "   <div>" +
                "       <p>#data:content1#</p>" +
                "       <p>#data:content2#</p>" +
                "       <p>#data:content3#</p>" +
                "   </div>" +
                "</html>";
        template.process(file);
        return template;
    }
}
