package com.builtbroken.builder.html.data;

import com.builtbroken.builder.html.parts.JsonProcessorHTML;
import com.builtbroken.builder.utils.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Stores all link reference information
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/17/2017.
 */
public class LinkData
{
    //Build up the map of replacement data
    public HashMap<String, String> linkReplaceKeys;

    public LinkData()
    {
        linkReplaceKeys = new HashMap();
    }

    /**
     * Adds a link to the data set
     * <p>
     * Will convert the text and url into HTML code
     *
     * @param key  - key to look up the link to inject into a page
     * @param text - display text of the link
     * @param url  - location the link goes to
     */
    public void add(String key, String text, String url)
    {
        linkReplaceKeys.put(key.toLowerCase(), JsonProcessorHTML.newLink(text, url));
    }

    /**
     * Gets the HTML code for the link key
     *
     * @param key - key reference injection code for a link
     * @return HTML code for the link, or an error string if key is not found
     */
    public String get(String key)
    {
        if (linkReplaceKeys.containsKey(key.toLowerCase()))
        {
            return "[Unknown link key -> " + key + "]";
        }
        return linkReplaceKeys.get(key.toLowerCase());
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
                    parseJsonLinkArray(linkJson.getAsJsonObject());
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
    public void parseJsonLinkArray(JsonObject linkObject)
    {
        if (linkObject.has("links"))
        {
            Set<Map.Entry<String, JsonElement>> linkEntrySet = linkObject.entrySet();
            for (Map.Entry<String, JsonElement> entry : linkEntrySet)
            {
                JsonObject linkEntryObject = entry.getValue().getAsJsonObject();
                parseJsonLinkEnergy(entry.getKey(), linkEntryObject);
            }
        }
    }

    public void parseJsonLinkEnergy(String key, JsonObject linkEntryObject)
    {
        String text = linkEntryObject.getAsJsonPrimitive("text").getAsString();
        String url = linkEntryObject.getAsJsonPrimitive("url").getAsString();
        add(key, text, url);
    }
}
