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

        assertEquals(3, template.pageSegments.length);
        assertEquals("<p>", template.pageSegments[0]);
        assertEquals("data:content", template.pageSegments[1]);
        assertEquals("</p>", template.pageSegments[2]);

        assertEquals(1, template.injectionTags.size());
        assertEquals(1, (int) template.injectionTags.get("data:content"));
    }
}
