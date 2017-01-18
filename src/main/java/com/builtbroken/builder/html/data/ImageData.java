package com.builtbroken.builder.html.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
}
