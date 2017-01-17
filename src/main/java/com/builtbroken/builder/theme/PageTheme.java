package com.builtbroken.builder.theme;

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
    public final String fileLocation;
    /** Name of the theme */
    public String name;
    /** Main template class */
    public String mainTemplate;
    /** File containing settings for the theme */
    public File settingsFile;
    /** Folder containing page templates */
    public File pageDirectory;

    public HashMap<String, PageTemplate> templates;

    /**
     * Creates a new PageTheme instance.
     * Does not load any data or settings
     *
     * @param fileLocation - location of theme.json file
     */
    public PageTheme(String fileLocation)
    {
        this.fileLocation = fileLocation;
    }

    /**
     * Called to load the theme data from file. Does
     * not actually load the theme pages. To load
     * the pages call {@link #loadTemplates()} after
     * calling this.
     */
    public void load(File workingDirectory)
    {
        //Get settings file location
        if (fileLocation.startsWith("."))
        {
            settingsFile = new File(workingDirectory, fileLocation.replace("." + File.separator, ""));
        }
        else
        {
            settingsFile = new File(fileLocation);
        }

        //Parse settings file
        if (settingsFile.exists() && settingsFile.isFile())
        {
            final JsonElement element = Utils.toJsonElement(settingsFile);
            if (element.isJsonObject())
            {
                final JsonObject object = element.getAsJsonObject();
                if (object.has("templates"))
                {
                    Gson gson = new Gson();
                    //PageName or injection key, Page disk location
                    Map<String, String> map = new HashMap();
                    map = (Map<String, String>) gson.fromJson(object.get("templates"), map.getClass());

                    for (Map.Entry<String, String> entry : map.entrySet())
                    {
                        templates.put(entry.getKey().toLowerCase(), new PageTemplate(entry.getKey(), entry.getValue()));
                    }
                }
                else
                {
                    throw new RuntimeException("File does not define any templates to load [" + settingsFile + "]");
                }
                if (object.has("pageDirectory"))
                {
                    String value = object.getAsJsonPrimitive("pageDirectory").getAsString();
                    if (value.startsWith("."))
                    {
                        pageDirectory = new File(workingDirectory, value.replace("." + File.separator, ""));
                    }
                    else
                    {
                        pageDirectory = new File(value);
                    }
                }
                else
                {
                    throw new RuntimeException("File does not define a directory to load template pages from [" + settingsFile + "]");
                }
                if (object.has("name"))
                {
                    name = object.getAsJsonPrimitive("name").getAsString();
                }
                else
                {
                    throw new RuntimeException("File does not define the theme's name [" + settingsFile + "]");
                }
                if (object.has("main_template"))
                {
                    mainTemplate = object.getAsJsonPrimitive("main_template").getAsString();
                }
                else
                {
                    throw new RuntimeException("File does not define a main template for the theme [" + settingsFile + "]");
                }
            }
            else
            {
                throw new RuntimeException("File does not contain a json object [" + settingsFile + "]");
            }
        }
        else
        {
            throw new RuntimeException("File is invalid for reading [" + settingsFile + "]");
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
