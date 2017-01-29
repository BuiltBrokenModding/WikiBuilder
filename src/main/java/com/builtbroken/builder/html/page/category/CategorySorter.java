package com.builtbroken.builder.html.page.category;

import com.builtbroken.builder.html.data.CategoryData;

import java.util.Comparator;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/29/2017.
 */
public class CategorySorter implements Comparator<CategoryData>
{
    @Override
    public int compare(CategoryData o1, CategoryData o2)
    {
        if (o2.index == -1 && o2.index == -1)
        {
            return Integer.compare(o1.subCategories.size(), o2.subCategories.size());
        }
        return Integer.compare(o1.index, o2.index);
    }
}
