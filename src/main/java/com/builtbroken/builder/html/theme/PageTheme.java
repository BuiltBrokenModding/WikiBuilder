package com.builtbroken.builder.html.theme;

import com.builtbroken.builder.html.data.CategoryData;
import com.builtbroken.builder.html.data.SegmentedHTML;
import com.builtbroken.builder.html.page.PageData;
import com.builtbroken.builder.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to contain and load all page templates for a single page theme.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/16/2017.
 */
public class PageTheme
{
    /** Location of where the theme is located */
    /** Name of the theme */
    public String name;
    /** File containing settings for the theme */
    public File themeFile;
    /** Folder containing page templates */
    public File pageDirectory;
    /** Loaded templates. */
    public HashMap<String, PageTemplate> templates;

    /** Main template class */
    public PageTemplate mainTemplate;

    /** Categories template */
    public PageTemplate categoryTemplate;
    /** Category entry template */
    public PageTemplate categoryEntryTemplate;
    /** Category item template */
    public PageTemplate categoryItemTemplate;
    /** Category entry template for categories with sub categories */
    public PageTemplate categoryChildTemplate;


    /**
     * Creates a new PageTheme instance.
     * Does not load any data or settings
     *
     * @param file - location of theme.json file
     */
    public PageTheme(File file)
    {
        this.themeFile = file;
    }

    /**
     * Called to load the theme data from file. Does
     * not actually load the theme pages. To load
     * the pages call {@link #loadTemplates()} after
     * calling this.
     */
    public void load()
    {
        //Parse settings file
        if (themeFile.exists() && themeFile.isFile())
        {
            final JsonElement element = Utils.toJsonElement(themeFile);
            if (element.isJsonObject())
            {
                final JsonObject object = element.getAsJsonObject();
                if (object.has("templates"))
                {
                    Gson gson = new Gson();
                    //PageName or injection key, Page disk location
                    Map<String, String> map = new HashMap();
                    map = (Map<String, String>) gson.fromJson(object.get("templates"), map.getClass());

                    templates = new HashMap();
                    for (Map.Entry<String, String> entry : map.entrySet())
                    {
                        String key = entry.getKey().toLowerCase();
                        templates.put(key, new PageTemplate(key, entry.getValue()));
                    }
                }
                else
                {
                    throw new RuntimeException("File does not define any templates to load [" + themeFile + "]");
                }
                if (object.has("pageDirectory"))
                {
                    String value = object.getAsJsonPrimitive("pageDirectory").getAsString();
                    pageDirectory = Utils.getFile(themeFile.getParentFile(), value);
                }
                else
                {
                    throw new RuntimeException("File does not define a directory to load template pages from [" + themeFile + "]");
                }
                if (object.has("name"))
                {
                    name = object.getAsJsonPrimitive("name").getAsString();
                }
                else
                {
                    throw new RuntimeException("File does not define the theme's name [" + themeFile + "]");
                }
                if (templates.containsKey("main_template"))
                {
                    mainTemplate = templates.get("main_template");
                }
                else
                {
                    throw new RuntimeException("File does not define a main template for the theme [" + themeFile + "]");
                }
                if (templates.containsKey("category_template"))
                {
                    categoryTemplate = templates.get("category_template");
                }
                else
                {
                    throw new RuntimeException("File does not define a category template for the theme [" + themeFile + "]");
                }
                if (templates.containsKey("category_entry"))
                {
                    categoryEntryTemplate = templates.get("category_entry");
                }
                else
                {
                    throw new RuntimeException("File does not define a category entry template for the theme [" + themeFile + "]");
                }
                if (templates.containsKey("category_item"))
                {
                    categoryItemTemplate = templates.get("category_item");
                }
                else
                {
                    throw new RuntimeException("File does not define a category item template for the theme [" + themeFile + "]");
                }
                if (templates.containsKey("category_parent"))
                {
                    categoryChildTemplate = templates.get("category_parent");
                }
                else
                {
                    throw new RuntimeException("File does not define a category parent template for the theme [" + themeFile + "]");
                }
            }
            else
            {
                throw new RuntimeException("File does not contain a json object [" + themeFile + "]");
            }
        }
        else
        {
            throw new RuntimeException("File is invalid for reading [" + themeFile + "]");
        }
    }

    /**
     * Called to load the templates from file
     */
    public void loadTemplates()
    {
        for (PageTemplate template : templates.values())
        {
            template.load(pageDirectory);
        }
    }

    /**
     * Builds the categories HTML that is placed at the bottom of each Wiki page to allow users
     * to navigate between pages faster.
     *
     * @param basePath - base wiki page path
     * @param vars     - variables to inject into templates
     * @param data     - list of categories to generate
     * @return HTML
     */
    public String buildCategoriesHTML(String basePath, HashMap<String, String> vars, Collection<CategoryData> data)
    {
        //Build categories body
        String categories = "";
        for (CategoryData categoryData : data)
        {
            categories += buildCategory(basePath, categoryData, vars, false);
        }
        //Build html segments
        String[] htmlSegments = categoryTemplate.htmlSegments.clone();
        SegmentedHTML.injectData(htmlSegments, categoryTemplate.injectionTags, "categories", categories);
        SegmentedHTML.injectData(htmlSegments, categoryTemplate.injectionTags, vars);
        //Convert to HTML
        String html = "";
        for (String s : htmlSegments)
        {
            html += s;
        }
        return html;
    }

    /**
     * Generates a single category
     *
     * @param basePath - base wiki page path
     * @param vars     - variables to inject into templates
     * @return HTML
     */
    protected String buildCategory(String basePath, CategoryData categoryData, HashMap<String, String> vars, boolean child)
    {
        String items = "";
        //Build items
        for (PageData pageData : categoryData.pages)
        {
            //Build item entry
            String[] htmlSegments = categoryItemTemplate.htmlSegments.clone();
            SegmentedHTML.injectData(htmlSegments, categoryItemTemplate.injectionTags, "itemURL", pageData.getOutput(basePath));
            SegmentedHTML.injectData(htmlSegments, categoryItemTemplate.injectionTags, "itemName", pageData.pageName);
            SegmentedHTML.injectData(htmlSegments, categoryItemTemplate.injectionTags, vars);
            //Append to items HTML
            for (String s : htmlSegments)
            {
                items += s;
            }
        }
        //Add sub categories
        if (!categoryData.subCategories.isEmpty())
        {
            for (CategoryData data : categoryData.subCategories)
            {
                items += buildCategory(basePath, data, vars, true);
            }
        }

        //Build main body
        String html = "";
        String[] htmlSegments;

        if (!child)
        {
            htmlSegments = categoryTemplate.htmlSegments.clone();
            SegmentedHTML.injectData(htmlSegments, categoryTemplate.injectionTags, "categoryName", categoryData.displayName);
            SegmentedHTML.injectData(htmlSegments, categoryTemplate.injectionTags, "categoryItems", items);
            SegmentedHTML.injectData(htmlSegments, categoryTemplate.injectionTags, vars);
        }
        else
        {
            htmlSegments = categoryChildTemplate.htmlSegments.clone();
            SegmentedHTML.injectData(htmlSegments, categoryChildTemplate.injectionTags, "categoryName", categoryData.displayName);
            SegmentedHTML.injectData(htmlSegments, categoryChildTemplate.injectionTags, "categoryItems", items);
            SegmentedHTML.injectData(htmlSegments, categoryChildTemplate.injectionTags, vars);
        }
        //Append to items HTML
        for (String s : htmlSegments)
        {
            html += s;
        }
        return html;
    }
}
