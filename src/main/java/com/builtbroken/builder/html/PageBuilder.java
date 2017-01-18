package com.builtbroken.builder.html;

import com.builtbroken.builder.html.data.CategoryData;
import com.builtbroken.builder.html.page.Page;
import com.builtbroken.builder.html.page.PageData;
import com.builtbroken.builder.html.parts.HTMLPartHeader;
import com.builtbroken.builder.html.parts.HTMLPartParagraph;
import com.builtbroken.builder.html.parts.JsonProcessorHTML;
import com.builtbroken.builder.html.theme.PageTheme;
import com.builtbroken.builder.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main logic class for building all pages
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/14/2017.
 */
public class PageBuilder
{
    public final Logger logger;
    /** Main directory to read and write files inside */
    public final File workingDirectory;
    /** Main directory to read and write files inside */
    public final File outputDirectory;
    /** File for loading settings */
    public final File settingsFile;

    /** Location to access images */
    public File imageDirectory;
    /** Location to get cateogry data from */
    public File categoryFile;
    /** Location of all pages to load */
    public File pageDirectory;

    /** Extra variables loaded from settings to be used later */
    public HashMap<String, String> vars;

    /** Theme/templates used to generate pages. */
    public PageTheme pageTheme;

    /** All categories for the wiki */
    public List<CategoryData> categoryData;
    /** All pages for the wiki */
    public List<PageData> loadedWikiData;
    /** Actual generated pages in data form */
    public List<Page> generatedPages;
    /** List of used images */
    public List<String> usedImages;
    /** Map of image keys to paths on disk (relative to workspace) */
    public HashMap<String, String> images;

    /**
     * Creates a new page builder instance
     *
     * @param workingDirectory - directory to place all files inside
     * @param settingsFile     - location of the primiary settings file
     * @param launchSettings   - launch arguments, overrides settings file in some cases and changes logic
     */
    public PageBuilder(Logger logger, File workingDirectory, File settingsFile, HashMap<String, String> launchSettings)
    {
        this.logger = logger;
        this.workingDirectory = workingDirectory;
        this.settingsFile = settingsFile;
        if (launchSettings.containsKey("outputDirectory"))
        {
            outputDirectory = Utils.getFile(workingDirectory, launchSettings.get("outputDirectory"));
        }
        else
        {
            outputDirectory = new File(workingDirectory, "output");
        }
        if (launchSettings.containsKey("theme"))
        {
            File file = Utils.getFile(workingDirectory, launchSettings.get("theme"));
            if (file.isDirectory())
            {
                file = new File(file, "theme.json");
            }
            pageTheme = new PageTheme(file);
            logger.info("Theme is being set by program arguments! Theme in settings file will not be used.");
            logger.info("Theme : " + pageTheme.themeFile);
        }
        logger.info("Output directory set to " + outputDirectory);
    }

    /**
     * Called to run the page building
     * process from start to finish.
     */
    public void run()
    {
        logger.info("Parsing settings....");
        parseSettings();
        logger.info("Done....\n\n");

        logger.info("Loading HTML templates....");
        loadHTML();
        logger.info("Done....\n\n");

        logger.info("Loading wiki data....");
        loadWikiData();
        logger.info("Done....\n\n");

        logger.info("Parsing wiki data....");
        parseWikiData();
        logger.info("Done....\n\n");

        logger.info("Building wiki data....");
        buildWikiData();
        logger.info("Done....\n\n");

        logger.info("Building pages....");
        buildPages();
        logger.info("Done....\n\n");
    }

    /**
     * Called to parse the settings file
     */
    public void parseSettings()
    {
        logger.info("Loading settings for wiki building form " + settingsFile);
        if (!outputDirectory.exists())
        {
            outputDirectory.mkdirs();
        }
        if (settingsFile.exists() && settingsFile.isFile())
        {
            JsonElement element = Utils.toJsonElement(settingsFile);

            if (element.isJsonObject())
            {
                JsonObject object = element.getAsJsonObject();
                if (object.has("vars"))
                {
                    logger.info("Vars:");
                    Gson gson = new Gson();
                    Map<String, String> map = new HashMap();
                    map = (Map<String, String>) gson.fromJson(object.get("vars"), map.getClass());
                    vars = new HashMap();
                    for (Map.Entry<String, String> entry : map.entrySet())
                    {
                        logger.info("\t" + entry.getKey() + " = " + entry.getValue());
                        vars.put(entry.getKey(), entry.getValue());
                    }
                }
                if (object.has("images"))
                {
                    String value = object.getAsJsonPrimitive("images").getAsString();
                    imageDirectory = Utils.getFile(workingDirectory, value);
                    vars.put("imagePath", value);
                    logger.info("Image Path: " + imageDirectory);
                }
                else
                {
                    throw new RuntimeException("Missing image folder location from " + settingsFile);
                }
                if (object.has("categories"))
                {
                    String value = object.getAsJsonPrimitive("categories").getAsString();
                    categoryFile = Utils.getFile(workingDirectory, value);
                    logger.info("Category File: " + imageDirectory);
                }
                else
                {
                    throw new RuntimeException("Missing categories data from " + settingsFile);
                }
                if (object.has("pages"))
                {
                    String value = object.getAsJsonPrimitive("pages").getAsString();
                    pageDirectory = Utils.getFile(workingDirectory, value);
                    logger.info("Pages folder: " + pageDirectory);
                }
                else
                {
                    throw new RuntimeException("Missing pages location data from " + settingsFile);
                }
                if (pageTheme == null)
                {
                    if (object.has("theme"))
                    {
                        File file = Utils.getFile(workingDirectory, object.getAsJsonPrimitive("theme").getAsString());
                        if (file.isDirectory())
                        {
                            file = new File(file, "theme.json");
                        }
                        pageTheme = new PageTheme(file);
                        logger.info("Theme : " + pageTheme.themeFile);
                    }
                    else
                    {
                        throw new RuntimeException("Missing theme data from " + settingsFile);
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
            throw new RuntimeException("File is invalid for reading or missing [" + settingsFile + "]");
        }
    }

    /**
     * Finds all files for parsing.
     */
    public void loadWikiData()
    {
        loadedWikiData = new ArrayList();
        if (categoryFile.exists() && categoryFile.isFile())
        {
            JsonElement element = Utils.toJsonElement(categoryFile);

            if (element.isJsonObject())
            {
                JsonObject object = element.getAsJsonObject();
                if (object.has("categories"))
                {
                    logger.info("Categories:");
                    categoryData = new ArrayList();
                    Gson gson = new Gson();
                    Map<String, String> map = new HashMap();
                    map = (Map<String, String>) gson.fromJson(object.get("categories"), map.getClass());
                    for (Map.Entry<String, String> entry : map.entrySet())
                    {
                        logger.info("\t" + entry.getKey() + " --> " + entry.getValue());
                        CategoryData data = new CategoryData(entry.getKey(), entry.getValue());
                        data.load(workingDirectory);
                    }
                }
                //Recursively load files
                logger.info("");
                logger.info("\tSearching for pages to load...");
                getFiles(pageDirectory, loadedWikiData);
                logger.info("\tDone...");
            }
            else
            {
                throw new RuntimeException("File does not contain a json object [" + categoryFile + "]");
            }
        }
        else
        {
            throw new RuntimeException("File is invalid for reading or missing [" + categoryFile + "]");
        }
    }

    private void getFiles(File folder, List<PageData> wikiPages)
    {
        if (folder != null && folder.exists() && folder.isDirectory())
        {
            File[] files = folder.listFiles();
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    getFiles(file, wikiPages);
                }
                else if (file.getName().endsWith(".json"))
                {
                    logger.info("\tPage:   " + file);
                    wikiPages.add(new PageData(file));
                }
            }
        }
    }

    /**
     * Parses the data from said files
     */
    public void parseWikiData()
    {
        for (PageData data : loadedWikiData)
        {
            data.load();
        }
    }

    /**
     * Finalizes all wiki data and
     * links files together as needed.
     */
    public void buildWikiData()
    {
        usedImages = new ArrayList();
        images = new HashMap();

        //Build up the map of replacement data
        HashMap<String, String> linkReplaceKeys = new HashMap();
        HashMap<String, String> imageReplaceKeys = new HashMap();
        logger.info("Collecting link and image data");
        for (PageData data : loadedWikiData)
        {
            for (Map.Entry<String, String> entry : data.linkReplaceKeys.entrySet())
            {
                final String key = entry.getKey().toLowerCase();
                if (!linkReplaceKeys.containsKey(key))
                {
                    linkReplaceKeys.put(key, "<a href=\"" + data.getOutput(vars.get("outputPath")) + "\">" + entry.getValue() + "</a>");
                }
                else
                {
                    throw new RuntimeException("Duplicate link key[" + entry.getKey() + "] was found for page [" + data.pageName + "] key is linked to " + linkReplaceKeys.get(key));
                }
            }
            for (Map.Entry<String, String> entry : data.imgReplaceKeys.entrySet())
            {
                final String key = entry.getKey().toLowerCase();
                if (!imageReplaceKeys.containsKey(key))
                {
                    String path = vars.get("imagePath") + entry.getValue();
                    imageReplaceKeys.put(key, "<img src=\"" + entry.getValue() + "\">");
                    images.put(key, path);
                }
                else
                {
                    throw new RuntimeException("Duplicate image key[" + entry.getKey() + "] was found for page [" + data.pageName + "] key is linked to " + imageReplaceKeys.get(key));
                }
            }
        }
        logger.info("Injecting link and image data");
        //Loop all pages to replace data
        for (PageData data : loadedWikiData)
        {
            for (Map.Entry<String, Integer> entry : data.pageLinks.entrySet())
            {
                final String key = entry.getKey().toLowerCase();
                if (linkReplaceKeys.containsKey(key))
                {
                    data.htmlSegments[entry.getValue()] = linkReplaceKeys.get(key);
                }
                else
                {
                    System.out.println("Warning: " + data.pageName + " is missing a link reference for " + entry.getKey());
                }
            }
            for (Map.Entry<String, Integer> entry : data.imgReferences.entrySet())
            {
                final String key = entry.getKey().toLowerCase();
                if (imageReplaceKeys.containsKey(key))
                {
                    data.htmlSegments[entry.getValue()] = imageReplaceKeys.get(key);
                    //Keep track of what images we have used
                    usedImages.add(key);
                }
                else
                {
                    System.out.println("Warning: " + data.pageName + " is missing an image reference for " + entry.getKey());
                }
            }
        }
        //TODO generate category footer
    }

    /**
     * Converts the data into
     * HTML files using the provided
     * templates
     */
    public void buildPages()
    {
        logger.info("Creating page objects and injecting data");
        //Inject missing data into
        generatedPages = new ArrayList();
        for (PageData data : loadedWikiData)
        {
            Page page = new Page();
            page.outputFile = new File(outputDirectory, data.getOutput(vars.get("outputPath")));
            page.setTheme(pageTheme);

            //Inject page main content
            page.inject("wikiContentHtml", data.buildHTML());
            page.inject("PageName", data.pageName);
            //Inject page data
            page.inject(data.data);
            //Inject global data
            page.inject(vars);
            //Add page to generated pages
            generatedPages.add(page);
        }
        logger.info("Done...");

        logger.info("Saving pages to disk...");
        //Output pages to file
        for (Page page : generatedPages)
        {
            logger.info("\tOutputting file to " + page.outputFile);
            String html = page.buildPage();

            if (!page.outputFile.getParentFile().exists())
            {
                page.outputFile.getParentFile().mkdirs();
            }
            //TODO validate pages (Check that tags match, that images exist, that links work)
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(page.outputFile)))
            {
                bw.write(html);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        logger.info("Done...");
        List<String> unusedImages = new ArrayList();
        if (!vars.containsKey("doNotCopyImages"))
        {
            logger.info("Copying images to output...");
            for (Map.Entry<String, String> entry : images.entrySet())
            {
                if (usedImages.contains(entry.getKey()))
                {
                    final File file = new File(imageDirectory, entry.getValue());
                    if (file.exists())
                    {
                        copyFile(file, new File(outputDirectory, entry.getValue()));
                    }
                    else
                    {
                        System.out.println("Warning: Image[key='" + entry.getKey() + "' path='" + entry.getValue() + "'] is missing!!");
                    }
                }
                else
                {
                    unusedImages.add(entry.getKey());
                }
            }
            logger.info("Images have been moved.");
        }
        //TODO if in GUI mode show pages to user (Ask user first)
    }

    private void copyFile(File sourceFile, File destFile)
    {
        logger.info("\tCopying " + sourceFile + " to " + destFile);
        //http://stackoverflow.com/questions/106770/standard-concise-way-to-copy-a-file-in-java
        try
        {
            if (!destFile.exists())
            {
                destFile.createNewFile();
            }

            FileChannel source = null;
            FileChannel destination = null;

            try
            {
                source = new FileInputStream(sourceFile).getChannel();
                destination = new FileOutputStream(destFile).getChannel();
                destination.transferFrom(source, 0, source.size());
            }
            catch (Exception e)
            {
                throw new RuntimeException("Error: Failed to copy " + sourceFile + " to " + destFile, e);
            }
            finally
            {
                if (source != null)
                {
                    source.close();
                }
                if (destination != null)
                {
                    destination.close();
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unexpected error while copying " + sourceFile + " to " + destFile, e);
        }
    }

    /**
     * Loads the HTML templates
     * and validates to make sure
     * they will work.
     */
    public void loadHTML()
    {
        logger.info("\tLoading theme");
        pageTheme.load(workingDirectory);
        pageTheme.loadTemplates();
        logger.info("\tDone");

        logger.info("\tLoading HTML processors");
        JsonProcessorHTML.registerPart("h", new HTMLPartHeader());
        JsonProcessorHTML.registerPart("p", new HTMLPartParagraph());
        logger.info("\tDone");
    }
}
