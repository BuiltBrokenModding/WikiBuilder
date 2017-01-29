package com.builtbroken.builder.html.page.category;

import com.builtbroken.builder.html.data.CategoryData;
import com.builtbroken.builder.html.page.Page;
import com.builtbroken.builder.html.theme.PageTemplate;
import com.builtbroken.builder.html.theme.PageTheme;

import java.util.*;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/28/2017.
 */
public class CategoryPage extends Page
{
    public CategoryPage(PageTheme theme)
    {
        setTheme(theme);
    }

    /**
     * Builds the categories data and places it inside of the page
     *
     * @param basePath - base wiki page path
     * @param dataCollection     - list of categories to generate
     * @return HTML
     */
    public CategoryPage injectData(String basePath, Collection<CategoryData> dataCollection, HashMap<String, String> vars)
    {
        //Build categories body
        String categories = "";
        //Copy list and sort
        List<CategoryData> data = new ArrayList();
        data.addAll(dataCollection);
        Collections.sort(data, new CategorySorter());

        for (CategoryData categoryData : data)
        {
            CategoryEntry entry = new CategoryEntry(theme, categoryData, false);
            entry.injectData(basePath, vars);
            categories += entry.buildPage();
        }
        //Build html segments
        inject("categories", categories);
        inject(vars);
        return this;
    }

    @Override
    protected PageTemplate getPrimaryTemplate()
    {
        return theme.categoryTemplate;
    }
}
