package com.builtbroken.builder.data;

import com.builtbroken.builder.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores information about a wiki page before it is generated
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/14/2017.
 */
public class PageData extends SegmentedHTML
{
    /** Name of the page */
    public String pageName;
    /** Location of the json data for this page. */
    public final File file;
    /** Category this was loaded for */
    public CategoryData category;
    /** Data unique to just this page, will be injected into the {@link Page} object */
    public final HashMap<String, String> data;


    /** References to other pages in this page's HTML */
    public HashMap<String, Integer> pageReferences;
    /** References to other pages that need to go though linkReplaceKeys */
    public HashMap<String, Integer> pageLinks;
    /** Link replace keys for this page, used to convert '#link#' to html link code with name in other pages. */
    public HashMap<String, String> linkReplaceKeys;

    public PageData(CategoryData category, File file)
    {
        this.category = category;
        this.file = file;
        data = new HashMap();
    }

    /**
     * Called to load the page from json
     */
    public void load()
    {
        JsonElement element = Utils.toJsonElement(file);
        if (element.isJsonObject())
        {
            JsonObject object = element.getAsJsonObject();
            if (object.has("pageName"))
            {
                pageName = object.getAsJsonPrimitive("pageName").getAsString();
            }
            if (object.has("type"))
            {
                String value = object.getAsJsonPrimitive("type").getAsString().toLowerCase();
                if (value.equals("content"))
                {
                    category.pages.add(pageName);
                }
            }
            if (object.has("replaceKeys"))
            {
                Gson gson = new Gson();
                Map<String, String> map = new HashMap();
                map = (Map<String, String>) gson.fromJson(object.get("replaceKeys"), map.getClass());
                linkReplaceKeys.putAll(map);
            }
            if (object.has("content"))
            {
                //Split HTML into segments for injection
                process(toHTML(object.getAsJsonObject("content")));

            }
        }
        else
        {
            throw new RuntimeException("File " + file + " is not a valid json object so can not be parsed into a wiki page.");
        }
    }

    @Override
    public void process(String html)
    {
        pageReferences = new HashMap();
        pageLinks = new HashMap();
        super.process(html);
    }

    @Override
    protected boolean processInjectionTag(String key, int index, String original)
    {
        if (!super.processInjectionTag(key, index, original))
        {
            if (key.startsWith("pageref:"))
            {
                pageReferences.put(key.substring(key.indexOf(":") + 1), index);
            }
            else if (key.startsWith("link:"))
            {
                pageLinks.put(key.substring(key.indexOf(":") + 1), index);
            }
            return false;
        }
        return true;
    }
}
