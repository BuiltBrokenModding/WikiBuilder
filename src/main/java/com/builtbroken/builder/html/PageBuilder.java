package com.builtbroken.builder.html;

import com.builtbroken.builder.html.data.CategoryData;
import com.builtbroken.builder.html.data.ImageData;
import com.builtbroken.builder.html.data.LinkData;
import com.builtbroken.builder.html.page.EntryPage;
import com.builtbroken.builder.html.page.PageData;
import com.builtbroken.builder.html.page.category.CategoryPage;
import com.builtbroken.builder.html.theme.PageTheme;
import com.builtbroken.builder.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * Main logic class for building all pages
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/14/2017.
 */
public class PageBuilder
{
    public static final String GITHUB = "https://github.com/BuiltBrokenModding/WikiBuilder";

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
    public HashMap<String, CategoryData> categoryData;
    /** All pages for the wiki */
    public List<PageData> loadedWikiData;
    /** Actual generated pages in data form */
    public List<EntryPage> generatedPages;

    public ImageData imageData;
    public LinkData linkData;

    /**
     * Creates a new page builder instance
     *
     * @param workingDirectory - directory to place all files inside
     * @param settingsFile     - location of the primiary settings file
     * @param launchSettings   - launch arguments, overrides settings file in some cases and changes logic
     */
    public PageBuilder(Logger logger, File workingDirectory, File settingsFile, HashMap<String, String> launchSettings, PageTheme theme, ImageData imageData, LinkData linkData)
    {
        this.logger = logger;
        this.workingDirectory = workingDirectory;
        this.settingsFile = settingsFile;
        this.imageData = imageData;
        this.linkData = linkData;
        this.pageTheme = theme;
        if (launchSettings.containsKey("outputDirectory"))
        {
            outputDirectory = Utils.getFile(workingDirectory, launchSettings.get("outputDirectory"));
        }
        else
        {
            outputDirectory = new File(workingDirectory, "output");
        }
        logger.info("Output directory set to " + outputDirectory);
    }

    /**
     * Called to run the page building
     * process from start to finish.
     */
    public void run()
    {
        //If the following is changed make sure to update the batch processor
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
                    logger.info("Categories:");
                    categoryData = new HashMap();
                    final Set<Map.Entry<String, JsonElement>> entrySet = object.get("categories").getAsJsonObject().entrySet();
                    for (Map.Entry<String, JsonElement> entry : entrySet)
                    {
                        final JsonObject catEntry = entry.getValue().getAsJsonObject();
                        this.categoryData.put(entry.getKey().toLowerCase(), CategoryData.parse(entry.getKey(), catEntry));
                    }
                    logger.info("");
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
        //Recursively load files
        logger.info("\tSearching for pages to load...");
        getFiles(pageDirectory, loadedWikiData);
        logger.info("\tDone...");
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
                    logger.info("\tPageWiki:   " + file);
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
        logger.info("Loading and parsing data from pages");
        Iterator<PageData> it = loadedWikiData.iterator();
        while (it.hasNext())
        {
            PageData data = it.next();
            if (data.type == null || !data.type.equalsIgnoreCase("ignore"))
            {
                data.load(this);
            }
            else
            {
                it.remove();
            }
        }
    }

    /**
     * Finalizes all wiki data and
     * links files together as needed.
     */
    public void buildWikiData()
    {
        logger.info("Injecting link and image data");
        //Loop all pages to replace data
        for (PageData data : loadedWikiData)
        {
            //Replace link keys with link HTML code
            for (Map.Entry<String, Integer> entry : data.pageLinks.entrySet())
            {
                final String key = entry.getKey().toLowerCase();
                if (linkData.linkReplaceKeys.containsKey(key))
                {
                    data.htmlSegments[entry.getValue()] = linkData.linkReplaceKeys.get(key);
                }
                else
                {
                    System.out.println("Warning: " + data.pageName + " is missing a link reference for " + entry.getKey());
                }
            }
            //Replace image keys with image HTML code
            for (Map.Entry<String, Integer> entry : data.imgReferences.entrySet())
            {
                final String key = entry.getKey().toLowerCase();
                if (imageData.imageReplaceKeys.containsKey(key))
                {
                    data.htmlSegments[entry.getValue()] = imageData.imageReplaceKeys.get(key);
                    //Keep track of what images we have used
                    imageData.usedImages.add(key);
                }
                else
                {
                    System.out.println("Warning: " + data.pageName + " is missing an image reference for " + entry.getKey());
                }
            }
            //Map page to category
            if (data.category != null && categoryData.containsKey(data.category.toLowerCase()))
            {
                categoryData.get(data.category.toLowerCase()).pages.add(data);
            }
        }

        //Find children categories
        List<CategoryData> children = new ArrayList();
        for (CategoryData d : categoryData.values())
        {
            if (d.parent != null && categoryData.containsKey(d.parent.toLowerCase()))
            {
                children.add(d);
            }
        }
        //Removed children from main list
        for (CategoryData d : children)
        {
            categoryData.remove(d.name.toLowerCase());
        }
        //Map children to parents
        for (CategoryData d : children)
        {
            categoryData.get(d.parent.toLowerCase()).subCategories.add(d);
        }
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
        String categoryHTML = new CategoryPage(pageTheme).injectData(vars.get("outputPath"), categoryData.values(), vars).buildPage();

        for (PageData data : loadedWikiData)
        {
            if (data.type == null || !"ignore".equalsIgnoreCase(data.type))
            {
                EntryPage page = new EntryPage();
                page.outputFile = new File(outputDirectory, data.getOutput(vars.get("outputPath")));
                page.setTheme(pageTheme);

                //Inject page main content
                page.inject("wikiContentHtml", data.buildHTML());
                page.inject("PageName", data.pageName);
                page.inject("ModCategoryNav", categoryHTML);
                page.inject("time", "" + System.nanoTime());
                page.inject("builderGithub", "" + GITHUB);
                //Inject page data
                page.inject(data.data);
                //Inject global data
                page.inject(vars);
                //Add page to generated pages
                generatedPages.add(page);
            }
        }
        logger.info("Done...");

        logger.info("Saving pages to disk...");
        //Output pages to file
        for (EntryPage page : generatedPages)
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
            for (Map.Entry<String, String> entry : imageData.images.entrySet())
            {
                if (imageData.usedImages.contains(entry.getKey()))
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
        //If changed update batch file
        logger.info("\tLoading theme");
        pageTheme.load();
        pageTheme.loadTemplates();
        logger.info("\tDone");
    }
}
