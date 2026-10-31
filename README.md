# Magnolia Local Environment Set Up 

## Description
Instructions were made from scratch on Windows 10, so after finishing all steps you will be able to run the Magnolia project on any computer with this system. Some of the data you will need to get from admins – some elements are individual and highly protected.

## Requirements
-  VPN access (ask an admin for access)
-  Java
-  Tomcat
-  Git
-  Maven
-  IntelliJ Ultimate (ask an admin for license)
-  GitHub account and access for Doosan repositories (ask an admin for access)

## Magnolia configuration step by step
1) Download Java from https://www.java.com/en/download/

2) Make sure that the Java path is set in your system environment variables
   https://www.qualitestgroup.com/resources/knowledge-center/how-to-guide/add-java-system-path/

3) Download IntelliJ in version Ultimate from JetBrains website https://www.jetbrains.com/idea/

4) If you don't have Git on your computer, please download it from this
   website: https://git-scm.com/downloads

5) Open your local directory where you want to place the projects, it can be
   C:\Users\your_user\IdeaProjects

6) Open your terminal in this directory and clone the repository from https://github.com/DoosanICA/db-dxp-cms.git
   It's highly recommended to use GitHub CLI or SSH key if you are not familiar with these tools, please see links below:
- GitHub CLI https://cli.github.com/
- SSH key https://docs.github.com/en/github/authenticating-to-github/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent

7) Open the project with IntelliJ, you should be able to see that Maven (https://maven.apache.org/) is working on the project dependencies (bottom bar)

8) Download Tomcat from https://tomcat.apache.org/download-90.cgi and unpack the Tomcat repository

9) Select File -> Settings -> Tools -> External Tools -> Click the “+” button and add the Tomcat repository path (if you have already downloaded it from the website, you have it in the downloads directory – better place it somewhere else). To add the Tomcat path you will need to click on the “Program” browse button. Name this as tomcat 9 (or higher depends on the version you have downloaded)

10) Now on top of the bar press edit configuration, then click the “+” button and find Tomcat Server, the select local. Get VM options token from admin and paste it in VM options input. Make sure that Tomcat will run on port 8080 and JMX port is 1099. Find the “before launch” section and click the “+” button, select “Build Artifacts” select the artifact with “war exploded” (it should be the last one).  Don’t close the popup yet – click the “Deployment” tab and make sure that “Application context” is set to “/”. Then click “Ok” and close the popup.

11) Now click Maven on the right side – it should expand the window, find the “reload all maven projects” button and click on it. Wait until Maven will process all dependencies (for the first time it can take a lot, so be patient) and after that, you are ready to go! Click the play button on the top (make sure that the Tomcat which you’ve added before is selected). The project should open in a few seconds.

## Local Environment Variables Setup

For local development, you need to set environment variables for API keys and secrets. These variables are read by the Maven Cargo plugin when running the project locally.

### Required Environment Variables:
- `GOOGLE_TRANSLATION_API_KEY` - Google Translation API key for content translation (author instance only)
- `CLOUDINARY_API_SECRET` - Cloudinary API secret for image management
- `MAGNOLIA_LICENSE_KEY` - Magnolia CMS license key
- `MAGNOLIA_LICENSE_USER` - Email - owner of magnolia CMS license key

### Setup Instructions:

**Option 1: Using .env file (Recommended)**

1. Copy the template file:
   ```bash
   cp magnolia/.env.template magnolia/.env
   ```

2. Edit `magnolia/.env` and fill in your actual values

3. Before running Maven, export the variables:
   ```bash
   cd magnolia
   export $(cat .env | xargs)
   ```
   Or use:
   ```bash
   set -a && source magnolia/.env && set +a
   ```

**Option 2: Manual export**

Export each variable in your terminal before running Maven:
```bash
export GOOGLE_TRANSLATION_API_KEY=your_google_api_key_here
export CLOUDINARY_API_SECRET=your_cloudinary_secret
export MAGNOLIA_LICENSE_KEY=your_license_key
export MAGNOLIA_LICENSE_USER=example@bobcat.com
```

**Note:** The `.env` file is gitignored for security. Never commit actual credentials to the repository. Contact your admin to obtain the necessary API keys and secrets.

### First-Time Setup or After Configuration Changes

The Google Translation API configuration is bootstrapped from a module file. If you're setting up for the first time or the bootstrap configuration has changed:

**Option 1: Clean JCR data (Local Dev Only)**
```bash
rm -rf magnolia/magnolia-webapp/data/repositories
```
Then restart Magnolia - it will bootstrap fresh configuration.

**Option 2: Update Module via AdminCentral**
1. Start Magnolia
2. Go to AdminCentral → Tools → Module Updates
3. Select "localizations" module
4. Click "Update Module"
5. This will re-apply bootstrap files with updated configuration

## Summary
Make sure that every step was made correctly, also, please use the VPN when you are running the Magnolia project – it's required because you will not be able to get an access token after login.

## Magnolia Version Upgrade
- Copy code to a new folder
- Switch to develop branch and take update
- Open `dxp-bom/pom.xml`
- Change `magnoliaSeoVersion` to `6.2.28`
- Click on right Side `Maven` of the project
- Click `Reload All Maven Projects`
- Open `cms-parent/magnolia-parent/Lifecycle/clean`
   - Right click on `clean` then `Modify Run Configuration`
   - Create a configuration with `profile` is empty
   - Run `magnolia-parent [clean]`
- Do same for package
    - Run `magnolia-parent [package]`
- Remove the folder `magnolia/magnolia-webapp/data/repositories/magnolia/*`
- Now Run project
- Check the Magnolia version in About section.