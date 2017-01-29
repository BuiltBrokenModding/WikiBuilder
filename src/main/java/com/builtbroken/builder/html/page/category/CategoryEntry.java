package com.builtbroken.builder.html.page.category;

import com.builtbroken.builder.html.data.CategoryData;
import com.builtbroken.builder.html.data.SegmentedHTML;
import com.builtbroken.builder.html.page.Page;
import com.builtbroken.builder.html.page.PageData;
import com.builtbroken.builder.html.theme.PageTemplate;
import com.builtbroken.builder.html.theme.PageTheme;

import java.util.*;

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
            Collections.sort(categoryData.subCategories, new CategorySorter());
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
            //Remove pages that need to be inserted
            List<PageData> insertPages = new ArrayList();
            ListIterator<PageData> it = categoryData.pages.listIterator();
            while (it.hasNext())
            {
                PageData data = it.next();
                if (data.categoryDisplayOrder != null)
                {
                    insertPages.add(data);
                    it.remove();
                }
            }

            //Sort abc
            Collections.sort(categoryData.pages, new CategoryItemSorter());

            //Sort by ordering
            for (PageData insert : insertPages)
            {
                final String[] order = insert.categoryDisplayOrder.split(":");
                it = categoryData.pages.listIterator();
                while (it.hasNext())
                {
                    final PageData page = it.next();
                    if (page.pageKey.equalsIgnoreCase(order[1]))
                    {
                        if (order[0].equalsIgnoreCase("after"))
                        {
                            it.add(insert);
                        }
                        //Before
                        else if (order[0].equalsIgnoreCase("before"))
                        {
                            it.previous();
                            it.add(insert);
                            it.next();
                        }
                        else
                        {
                            throw new RuntimeException("Invalid order format '" + order[0] + "' for page " + insert.pageKey);
                        }
                    }
                }
            }

            //Build items
            for (PageData pageData : categoryData.pages)
            {
                //Build item entry
                String[] htmlSegments = theme.categoryItemTemplate.htmlSegments.clone();
                SegmentedHTML.injectData(htmlSegments, theme.categoryItemTemplate.injectionTags, "itemURL", pageData.getOutput(basePath));
                SegmentedHTML.injectData(htmlSegments, theme.categoryItemTemplate.injectionTags, "itemName", pageData.categoryDisplayName != null && !pageData.categoryDisplayName.isEmpty() ? pageData.categoryDisplayName : pageData.pageName);
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
