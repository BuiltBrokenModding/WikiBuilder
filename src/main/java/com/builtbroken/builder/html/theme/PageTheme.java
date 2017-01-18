package com.builtbroken.builder.html.theme;

import com.builtbroken.builder.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
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
    /** Main template class */
    public String mainTemplate;
    /** File containing settings for the theme */
    public File themeFile;
    /** Folder containing page templates */
    public File pageDirectory;

    public HashMap<String, PageTemplate> templates;

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
    public void load(File workingDirectory)
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
                if (object.has("main_template"))
                {
                    mainTemplate = object.getAsJsonPrimitive("main_template").getAsString();
                }
                else
                {
                    throw new RuntimeException("File does not define a main template for the theme [" + themeFile + "]");
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
}
