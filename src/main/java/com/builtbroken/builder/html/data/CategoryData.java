package com.builtbroken.builder.html.data;

import com.builtbroken.builder.html.page.PageData;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to store a list of pages to a category. This data is used to find all pages as well create the
 * navigation menu for all wiki pages.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/16/2017.
 */
public class CategoryData
{
    /** Name of the category */
    public final String name;

    /** Pages this category contains */
    public List<PageData> pages;
    /** Sub categories that are appended to this. */
    public List<CategoryData> subCategories;
    /** Name the user sees for the category */
    public String displayName;
    /** Page the user goes to when clicking the category name */
    public String pageID;
    /** Parent category */
    public String parent;

    public CategoryData(String name)
    {
        this.name = name;
        pages = new ArrayList();
        subCategories = new ArrayList();
    }
}
