package com.builtbroken.builder.html.parts;

import com.google.gson.JsonElement;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/17/2017.
 */
public class HTMLPartParagraph extends HTMLPart
{
    public HTMLPartParagraph()
    {
        super("paragraph", "p");
    }

    @Override
    public String process(JsonElement element)
    {
        return "<p>" + element + "</p>";
    }
}
