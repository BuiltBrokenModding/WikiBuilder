package com.builtbroken.builder.data;

import java.util.HashMap;

/**
 * Stores information about a wiki page before it is generated
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/14/2017.
 */
public class PageData
{
    /** Name of the page */
    public String pageName;
    /** Data unique to just this page, will be combined with global data right before creating the page. */
    public HashMap<String, String> data;

    public PageData(String name)
    {
        this.pageName = name;
        data = new HashMap();
    }
}
