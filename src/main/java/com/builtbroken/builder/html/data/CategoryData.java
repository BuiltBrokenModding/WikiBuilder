package com.builtbroken.builder.html.data;

import com.builtbroken.builder.utils.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
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
    /** Location of the category data on disk */
    public final String fileLocation;

    /** Pages this category contains */
    public List<String> pages;
    /** Name the user sees for the category */
    public String displayName;
    /** Page the user goes to when clicking the category name */
    public String pageID;

    public CategoryData(String name, String fileLocation)
    {
        this.name = name;
        this.fileLocation = fileLocation;
    }

    public void load(File workingDirectory)
    {
        pages = new ArrayList();
        File file = Utils.getFile(workingDirectory, fileLocation);
        if (file.exists() && file.isFile())
        {
            JsonElement element = Utils.toJsonElement(file);

            if (element.isJsonObject())
            {
                JsonObject object = element.getAsJsonObject();
                if (object.has("name"))
                {
                    displayName = object.getAsJsonPrimitive("name").getAsString();
                }
                if (object.has("page"))
                {
                    pageID = object.getAsJsonPrimitive("page").getAsString();
                }
            }
            else
            {
                throw new RuntimeException("File does not contain a json object [" + file + "]");
            }
        }
        else
        {
            throw new RuntimeException("File is invalid for reading or missing [" + file + "]");
        }
    }
}