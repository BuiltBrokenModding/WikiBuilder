package com.builtbroken.builder.html.parts;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/17/2017.
 */
public class HTMLPartHeader extends HTMLPart
{
    public HTMLPartHeader()
    {
        super("header", "h");
    }

    @Override
    public String process(JsonElement value)
    {
        if (value.isJsonObject())
        {
            JsonObject h = value.getAsJsonObject();
            String text = h.getAsJsonPrimitive("text").getAsString();
            int size = 2;
            if (h.has("size"))
            {
                JsonPrimitive p = h.getAsJsonPrimitive("size");
                if (p.isString())
                {
                    String s = p.getAsString().toLowerCase();
                    if (s.equals("small"))
                    {
                        size = 3;
                    }
                    else if (s.equals("medium"))
                    {
                        size = 2;
                    }
                    else if (s.equals("large"))
                    {
                        size = 1;
                    }
                }
                else
                {
                    size = p.getAsInt();
                }
            }
            if (h.has("link"))
            {
                String link = h.getAsJsonPrimitive("link").getAsString();
                if (link.startsWith("url"))
                {
                    return "<h" + size + "><a href=\"" + link + "\">" + text + "</a></h" + size + ">";
                }
                else if (link.endsWith(".json"))
                {
                    return "<h" + size + "><a href=\"#PageRef:" + link + "#\">" + text + "</a></h" + size + ">";
                }
                else
                {
                    return "<h" + size + "><a href=\"#" + link + "#\">" + text + "</a></h" + size + ">";
                }
            }
            return "<h" + size + ">" + text + "</h" + size + ">";
        }
        return "<h2>" + value + "</h2>";
    }
}
