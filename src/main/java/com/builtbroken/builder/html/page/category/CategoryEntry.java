package com.builtbroken.builder.html.page.category;

import com.builtbroken.builder.html.data.CategoryData;
import com.builtbroken.builder.html.data.SegmentedHTML;
import com.builtbroken.builder.html.page.Page;
import com.builtbroken.builder.html.page.PageData;
import com.builtbroken.builder.html.theme.PageTemplate;
import com.builtbroken.builder.html.theme.PageTheme;

import java.util.HashMap;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/28/2017.
 */
public class CategoryEntry extends Page
{
    public final CategoryData categoryData;
    public final boolean child;

    public CategoryEntry(PageTheme theme, CategoryData data, boolean child)
    {
        this.categoryData = data;
        this.child = child;
        setTheme(theme);
    }

    public void injectData(String basePath, HashMap<String, String> vars)
    {
        String items = "";
        //Add sub categories
        if (!categoryData.subCategories.isEmpty())
        {
            for (CategoryData subCategory : categoryData.subCategories)
            {
                CategoryEntry entry = new CategoryEntry(theme, subCategory, true);
                entry.injectData(basePath, vars);
                String html = entry.buildPage();
                items += html;
            }
        }
        //If not sub categories add item entries
        else
        {
            //Build items
            for (PageData pageData : categoryData.pages)
            {
                //Build item entry
                String[] htmlSegments = theme.categoryItemTemplate.htmlSegments.clone();
                SegmentedHTML.injectData(htmlSegments, theme.categoryItemTemplate.injectionTags, "itemURL", pageData.getOutput(basePath));
                SegmentedHTML.injectData(htmlSegments, theme.categoryItemTemplate.injectionTags, "itemName", pageData.pageName);
                SegmentedHTML.injectData(htmlSegments, theme.categoryItemTemplate.injectionTags, vars);
                //Append to items HTML
                items += SegmentedHTML.toString(htmlSegments);
            }
        }

        inject("categoryName", categoryData.displayName);
        inject("categoryItems", items);
        inject(vars);
    }

    @Override
    protected PageTemplate getPrimaryTemplate()
    {
        if (child)
        {
            return theme.categoryChildTemplate;
        }
        else if (!categoryData.subCategories.isEmpty())
        {
            return theme.categoryParentTemplate;
        }
        return theme.categoryEntryTemplate;
    }

    @Override
    protected PageTemplate getTemplateToUseFor(String key)
    {
        if (key.equalsIgnoreCase("category_entry"))
        {
            if (!categoryData.subCategories.isEmpty())
            {
                return theme.categoryParentTemplate;
            }
            return theme.categoryEntryTemplate;
        }
        return theme.getTemplate(key);
    }
}
