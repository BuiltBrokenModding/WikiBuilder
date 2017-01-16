package com.builtbroken.test.theme;

import com.builtbroken.builder.html.PageTemplate;
import com.builtbroken.builder.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to contain and load all page templates
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/16/2017.
 */
public class Theme
{
    public final String name;
    public final String fileLocation;

    public HashMap<String, PageTemplate> templates;

    public Theme(String name, String fileLocation)
    {
        this.name = name;
        this.fileLocation = fileLocation;
    }

    public void load()
    {
        File settingsFile = new File(fileLocation);
        if (settingsFile.exists() && settingsFile.isFile())
        {
            JsonElement element = Utils.readElement(settingsFile);
            if (element.isJsonObject())
            {
                JsonObject object = element.getAsJsonObject();
                if (object.has("templates"))
                {
                    Gson gson = new Gson();
                    //PageName or injection key, Page disk location
                    Map<String, String> map = new HashMap();
                    map = (Map<String, String>) gson.fromJson(object.get("templates"), map.getClass());

                    for (Map.Entry<String, String> entry : map.entrySet())
                    {
                        templates.put(entry.getKey(), new PageTemplate(entry.getKey(), entry.getValue()));
                    }
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
}
