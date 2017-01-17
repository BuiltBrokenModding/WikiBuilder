package com.builtbroken.builder.html.parts;

import com.google.gson.JsonElement;

/**
 * Contains the logic needed to convert a json tag into an HTML tag
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/17/2017.
 */
public abstract class HTMLPart
{
    public final String name;
    public final String tag;

    public HTMLPart(String name, String tag)
    {
        this.name = name;
        this.tag = tag;
    }

    /**
     * Called to process a json element
     *
     * @param element - element containing data
     *                about this HTML tag
     * @return string of the converted json to html
     */
    public abstract String process(JsonElement element);
}
