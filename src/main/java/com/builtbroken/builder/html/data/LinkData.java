package com.builtbroken.builder.html.data;

import java.util.HashMap;

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
}
