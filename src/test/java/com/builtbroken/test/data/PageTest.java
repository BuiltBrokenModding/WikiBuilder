package com.builtbroken.test.data;

import com.builtbroken.builder.data.Page;
import com.builtbroken.builder.data.PageData;
import com.builtbroken.builder.html.PageTemplate;
import com.builtbroken.test.html.PageTemplateTest;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.HashMap;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/16/2017.
 */
public class PageTest extends TestCase
{
    @Test
    public void testInit()
    {
        PageData data = new PageData("page");
        Page page = new Page(data);

        assertSame(data, page.data);
    }

    @Test
    public void testInjectNoData()
    {
        PageData data = new PageData("page");
        Page page = new Page(data);
        PageTemplate template = PageTemplateTest.getSmallTemplate();

        HashMap<String, String> injectionData = new HashMap();
        page.inject(template, injectionData);

        assertEquals("data:content1", page.pageSegments[1]);
        assertEquals("data:content2", page.pageSegments[3]);
        assertEquals("data:content3", page.pageSegments[5]);
    }

    @Test
    public void testInjectSomeData()
    {
        PageData data = new PageData("page");
        Page page = new Page(data);
        PageTemplate template = PageTemplateTest.getSmallTemplate();

        HashMap<String, String> injectionData = new HashMap();
        injectionData.put("data:content1", "test");
        page.inject(template, injectionData);

        assertEquals("test", page.pageSegments[1]);
        assertEquals("data:content2", page.pageSegments[3]);
        assertEquals("data:content3", page.pageSegments[5]);
    }

    @Test
    public void testFull()
    {
        PageData data = new PageData("page");
        Page page = new Page(data);
        PageTemplate template = PageTemplateTest.getSmallTemplate();

        HashMap<String, String> injectionData = new HashMap();
        injectionData.put("data:content1", "test");
        injectionData.put("data:content2", "test1");
        injectionData.put("data:content3", "test2");
        page.inject(template, injectionData);

        assertEquals("test", page.pageSegments[1]);
        assertEquals("test1", page.pageSegments[3]);
        assertEquals("test2", page.pageSegments[5]);

        final String compare = "" +
                "<html>" +
                "   <div>" +
                "       <p>test</p>" +
                "       <p>test1</p>" +
                "       <p>test2</p>" +
                "   </div>" +
                "</html>";

        assertEquals(compare, page.buildPage());
    }
}
