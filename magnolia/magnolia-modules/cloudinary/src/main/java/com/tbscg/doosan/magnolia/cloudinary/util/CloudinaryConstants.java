package com.tbscg.doosan.magnolia.cloudinary.util;

import java.util.regex.Pattern;

public interface CloudinaryConstants {

    // General
    String FORMAT_PLUGIN_DROPDOWN_NAME = "Format";
    String TOOLBAR_STYLES = "styles";
    String ASSET_PROVIDER_ID = "cloudinary";
    String THUMBNAIL_RENDITION_NAME = "thumbnail";

    // i18n
    String LABEL_UNABLE_TO_ACCESS_SELECTION = "ui-framework.richtexteditorexception.cannotaccessselecteditem";
    String LABEL_UNABLE_TO_OPEN_CHOOSER = "ui-form.richtexteditorexception.opentargetappfailure";
    String LABEL_NON_EXITING_DIRECTORY = "ui-form.richtexteditorexception.nonexitingdirectory";

    // Magnolia Apps
    String APP_CLOUDINARY_ASSETS = "dam-external-app";
    String APP_PAGES = "pages-app";
    String APP_PAGES_OLD = "pages";

    // Plugins
    String CLOUDINARY_LINK_PLUGIN_PATH = "/VAADIN/js/cloudinarylink/";
    String CLOUDINARY_FILE_BROWSER_PLUGIN_PATH = "/VAADIN/js/cloudinarybrowser/";
    String CLOUDINARY_VIDEO_PLAYER_PLUGIN = "cloudinaryVideoPlayer";
    String CLOUDINARY_VIDEO_PLAYER_PLUGIN_PATH = "/VAADIN/js/cloudinaryvideo/";
    String FILE_BROWSER_PLUGIN = "magnoliaFileBrowser";
    String YOUTUBE_PLUGIN = "youtube";
    String YOUTUBE_PLUGIN_PATH = "/VAADIN/js/youtube/";
    String COLOR_BUTTON_PLUGIN_PATH = "/VAADIN/js/colorbutton/";
    String COLOR_BUTTON_PLUGIN = "colorbutton";
    String COLOR_DIALOG_PLUGIN_PATH = "/VAADIN/js/colordialog/";
    String COLOR_DIALOG_PLUGIN = "colordialog";
    // Mime Types
    String IMAGE_ASSET_TYPE = "image";
    String VIDEO_ASSET_TYPE = "video";
    String TEXT_ASSET_TYPE = "text";
    Pattern IMAGE_MIME_TYPE_PATTERN = Pattern.compile("image.(?!tiff$)(?:[^.\\s]+)");
    Pattern VIDEO_MIME_TYPE_PATTERN = Pattern.compile("video.*");
    Pattern TEXT_MIME_TYPE_PATTERN = Pattern.compile("text.*");
    String DEFAULT_IMAGE_EXTENSION = ".jpg";

    //Events
    String EVENT_CHOOSE_ASSET = "chooseAsset";
    String EVENT_ASSET_CHOSEN = "assetChosen";
    String EVENT_VIDEO_CHOSEN = "videoChosen";
    String EVENT_VTT_CHOSEN = "chapterChosen";
    String EVENT_CLOUDINARY_VIDEO = "getCloudinaryVideo";
    String EVENT_CLOUDINARY_CHAPTERS_FILE = "getCloudinaryChaptersFile";
    String EVENT_CLOUDINARY_LINK = "getCloudinaryLink";
    String EVENT_CLOUDINARY_LINK_SELECTED = "cloudinaryLinkSelected";

    // Properties
    String PROPERTY_TITLE = "title";

    // Folder Modes
    String FOLDER_MODE_FIXED = "fixed";
    String FOLDER_MODE_DYNAMIC = "dynamic";

    // Colors
    String COLORS_BUTTON_CONFIG_NAME = "colorButton_colors";
    String CKEDITOR_COLORS = "'ff3600,D5DEE3,c8c8c8,6c757d,ffffff'";

    // Font sizes
    String FONT_SIZES_CONFIG_NAME = "fontSize_sizes";
    String CKEDITOR_FONT_SIZES = "'H1/56px;H2/44px;H3/36px;H4/24px;H5/18px;Caption/14px'";
    String CKEDITOR_FONT_SIZES_DEFAULT_LABEL = "'Text Style'";
    String FONT_SIZES_DEFAULT_LABEL = "fontSize_defaultLabel";

    // Cloudinary Methos
    String CLOUDINARY_METHOD_HANDLER = "handleCloudinarySelection";
    String CLOUDINARY_ERROR_METHOD_HANDLER = "handleCloudinaryError";
}
