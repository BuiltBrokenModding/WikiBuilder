package com.builtbroken.test.data;

import com.builtbroken.builder.data.PageData;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/16/2017.
 */
public class PageDataTest extends TestCase
{
    @Test
    public void testInit()
    {
        PageData data = new PageData("page");
        assertEquals("page", data.pageName);
    }
}
