package com.builtbroken.builder.html.data;

import com.builtbroken.builder.utils.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.*;

/**
 * Stores all image reference information
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/17/2017.
 */
public class ImageData
{
    /** List of used images */
    public List<String> usedImages;
    /** Map of image keys to paths on disk (relative to workspace) */
    public HashMap<String, String> images;
    /** Map of replace keys to image paths */
    public HashMap<String, String> imageReplaceKeys;

    public ImageData()
    {
        usedImages = new ArrayList();
        images = new HashMap();
        imageReplaceKeys = new HashMap();
    }

    public void add(String key, String path, String alt, String url)
    {
        imageReplaceKeys.put(key.toLowerCase(), "<a href=\"" + url + "\" target=\"_blank\"><img src=\"" + path + "\" alt=\"" + alt + "\"></a>");
    }

    public void add(String key, String path, String alt)
    {
        imageReplaceKeys.put(key.toLowerCase(), "<img src=\"" + path + "\" alt=\"" + alt + "\">");
    }

    /**
     * Gets the HTML code for the image key
     *
     * @param key - key reference injection code for a image
     * @return HTML code for the image, or an error string if key is not found
     */
    public String get(String key)
    {
        if (imageReplaceKeys.containsKey(key.toLowerCase()))
        {
            return "[Unknown image key -> " + key + "]";
        }
        return imageReplaceKeys.get(key.toLowerCase());
    }



    /**
     * Loads a file from disk as a json file
     * then parses the file looking for an array
     * of links formatted as {"key":{"text":"String","url":"String"}}
     *
     * @param file - file to load, throws an exception if the file is invalid
     */
    public void loadDataFromFile(File file)
    {
        if (file.exists())
        {
            if (file.isFile())
            {
                JsonElement linkJson = Utils.toJsonElement(file);
                if (linkJson.isJsonObject())
                {
                    parseJsonImageArray(linkJson.getAsJsonObject());
                }
                else
                {
                    throw new IllegalArgumentException("Link data file is not a valid json object. File = " + file);
                }
            }
            else
            {
                throw new IllegalArgumentException("Link data file is not a valid file. File = " + file);
            }
        }
        else
        {
            throw new IllegalArgumentException("Link data file is missing. File = " + file);
        }
    }

    /**
     * Parses the json object as an array of links
     * <p>
     * If the object doesn't contain a list of links it will be ignored with no warning
     *
     * @param linkObject - json object
     */
    public void parseJsonImageArray(JsonObject linkObject)
    {
        if (linkObject.has("images"))
        {
            Set<Map.Entry<String, JsonElement>> linkEntrySet = linkObject.entrySet();
            for (Map.Entry<String, JsonElement> entry : linkEntrySet)
            {
                JsonObject linkEntryObject = entry.getValue().getAsJsonObject();
                parseJsonImageEnergy(entry.getKey(), linkEntryObject);
            }
        }
    }

    public void parseJsonImageEnergy(String key, JsonObject linkEntryObject)
    {
        String path = linkEntryObject.getAsJsonPrimitive("path").getAsString();
        String alt = linkEntryObject.getAsJsonPrimitive("alt").getAsString();
        if (linkEntryObject.has("url"))
        {
            add(key, path, alt, linkEntryObject.getAsJsonPrimitive("url").getAsString());
        }
        else
        {
            add(key, path, alt);
        }
    }
}
