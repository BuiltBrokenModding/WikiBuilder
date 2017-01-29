package com.builtbroken.builder.html.page.category;

import com.builtbroken.builder.html.page.PageData;

import java.util.Comparator;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/29/2017.
 */
public class CategoryItemSorter implements Comparator<PageData>
{
    @Override
    public int compare(PageData o1, PageData o2)
    {
        return o1.pageName.compareTo(o2.pageName);
    }
}
