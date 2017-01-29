package com.builtbroken.test.data;

import com.builtbroken.builder.html.page.EntryPage;
import com.builtbroken.builder.html.page.Page;
import com.builtbroken.builder.html.theme.PageTheme;
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
    public void testInjectNoData()
    {
        Page page = new EntryPage();
        page.setTheme(getTheme());

        HashMap<String, String> injectionData = new HashMap();
        page.inject(injectionData);

        assertEquals("data:content1", page.pageSegments.get(page.theme.mainTemplate)[1]);
        assertEquals("data:content2", page.pageSegments.get(page.theme.mainTemplate)[3]);
        assertEquals("data:content3", page.pageSegments.get(page.theme.mainTemplate)[5]);
    }

    @Test
    public void testInjectSomeData()
    {
        Page page = new EntryPage();
        page.setTheme(getTheme());

        HashMap<String, String> injectionData = new HashMap();
        injectionData.put("content1", "test");
        page.inject(injectionData);

        assertEquals("test", page.pageSegments.get(page.theme.mainTemplate)[1]);
        assertEquals("data:content2", page.pageSegments.get(page.theme.mainTemplate)[3]);
        assertEquals("data:content3", page.pageSegments.get(page.theme.mainTemplate)[5]);
    }

    @Test
    public void testFull()
    {
        Page page = new EntryPage();
        page.setTheme(getTheme());

        HashMap<String, String> injectionData = new HashMap();
        injectionData.put("content1", "test");
        injectionData.put("content2", "test1");
        injectionData.put("content3", "test2");
        page.inject(injectionData);

        assertEquals("test", page.pageSegments.get(page.theme.mainTemplate)[1]);
        assertEquals("test1", page.pageSegments.get(page.theme.mainTemplate)[3]);
        assertEquals("test2", page.pageSegments.get(page.theme.mainTemplate)[5]);

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

    private PageTheme getTheme()
    {
        PageTheme theme =  new PageTheme(null);
        theme.mainTemplate = PageTemplateTest.getSmallTemplate();
        theme.addTemplate(theme.mainTemplate);
        return theme;
    }
}
