package com.builtbroken.builder.html.data;

import com.builtbroken.builder.html.page.PageData;
import com.google.gson.JsonObject;

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
    /**
     * Order in which the page will be listed in the category footer
     * -1 is automatic and normally is the bottom of the list.
     */
    public int index = -1;

    public CategoryData(String name)
    {
        this.name = name;
        pages = new ArrayList();
        subCategories = new ArrayList();
    }

    /**
     * Called to parse and build a category data object
     *
     * @param key      - key the json entry was created with
     * @param catEntry - json entry data
     * @return data object
     */
    public static CategoryData parse(String key, JsonObject catEntry)
    {
        CategoryData categoryData = new CategoryData(key.toLowerCase());
        categoryData.displayName = catEntry.getAsJsonPrimitive("text").getAsString();
        if (catEntry.has("page"))
        {
            categoryData.pageID = catEntry.getAsJsonPrimitive("page").getAsString();
        }
        if (catEntry.has("parent"))
        {
            categoryData.parent = catEntry.getAsJsonPrimitive("parent").getAsString();
        }
        if (catEntry.has("index"))
        {
            categoryData.index = catEntry.getAsJsonPrimitive("index").getAsInt();
        }
        return categoryData;
    }
}
