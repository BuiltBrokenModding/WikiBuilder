package com.builtbroken.builder.html.parts;

import com.google.gson.JsonElement;

import java.util.HashMap;

/**
 * Handles converting json tags to html tags
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/17/2017.
 */
public final class JsonProcessorHTML
{
    /** Map of json keys to parts that handle processing */
    public static HashMap<String, HTMLPart> parts = new HashMap();

    /**
     * Called to register parts
     *
     * @param tag  - json tag prefix
     * @param part - object that will convert the json
     */
    public static void registerPart(String tag, HTMLPart part)
    {
        parts.put(tag.toLowerCase(), part);
    }

    /**
     * Called to process the json element
     *
     * @param tag   - key that element was assigned to, if a number is included it will be
     *              removed
     * @param value - the contents of the element containing data about the tag
     * @return html as string
     */
    public static String process(String tag, JsonElement value)
    {
        String key = tag.replaceAll("[^A-Za-z]", "");
        if (parts.containsKey(key.toLowerCase()))
        {
            return parts.get(key.toLowerCase()).process(value);
        }
        return "<p>UnknownTag: " + value + " </p>";
    }
}
