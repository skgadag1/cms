# Magnolia CMS - Local Development & Debugging Guide

This guide covers how to run and debug the Magnolia CMS application locally.

## Prerequisites

Before starting Magnolia locally, ensure you have the following environment variables configured:

### Required Environment Variables

- `AWS_PROFILE` - Your AWS profile for dev environment
- `CLOUDINARY_API_SECRET` - Cloudinary API secret for media management
- `MAGNOLIA_LICENSE_KEY` - Your Magnolia CMS license key
- `GOOGLE_TRANSLATION_API_KEY` - Google Translation API key for translation features

### Setting Up Environment Variables

You can create a `.env` file in the `magnolia/` directory with these variables:

```bash
AWS_PROFILE=your-aws-profile
CLOUDINARY_API_SECRET=your-cloudinary-secret
MAGNOLIA_LICENSE_KEY=your-magnolia-license
GOOGLE_TRANSLATION_API_KEY=your-google-api-key
```

Or export them in your shell:

```bash
export AWS_PROFILE=your-aws-profile
export CLOUDINARY_API_SECRET=your-cloudinary-secret
export MAGNOLIA_LICENSE_KEY=your-magnolia-license
export GOOGLE_TRANSLATION_API_KEY=your-google-api-key
```

## Starting Magnolia Locally

To start Magnolia locally with debugging enabled:

1. Navigate to the `magnolia/` directory:
   ```bash
   cd magnolia
   ```

2. Run the application using Make:
   ```bash
   make run
   ```

This command will:
- Build the project using Maven
- Start Tomcat 9 server with the Magnolia webapp
- Enable remote debugging on port 8000
- Configure the application with your environment variables

The application will be available at:
- **Main application**: http://localhost:8080
- **Author interface**: http://localhost:8080/.magnolia/admincentral

Default credentials (for local development):
- Username: `superuser`
- Password: `superuser`

## Debugging with VS Code / Cursor

The project is already configured for remote debugging. The debug configuration is located in `.vscode/launch.json`.

### Using the Debug Configuration

1. Start Magnolia locally using `make run` (as described above)

2. In VS Code/Cursor, open the **Run and Debug** panel (Cmd+Shift+D / Ctrl+Shift+D)

3. Select **"Magnolia Remote Debug (decompiled)"** from the dropdown

4. Click the green play button or press F5 to attach the debugger

The debugger will connect to Magnolia running on port 8000 and allow you to:
- Set breakpoints in decompiled Magnolia source code
- Step through code execution
- Inspect variables and call stacks
- Evaluate expressions

### Debug Configuration Details

The launch configuration (`Magnolia Remote Debug (decompiled)`) is set up to:
- Connect to `127.0.0.1:8000` (the JDWP port configured in pom.xml)
- Map source code to decompiled JARs in `magnolia/decompiled/`
- Display debugging output in the integrated terminal

### Sample Launch Configuration

If you need to recreate or customize the debug configuration, here's the existing configuration from `.vscode/launch.json`:

```json
{
  "name": "Magnolia Remote Debug (decompiled)",
  "type": "java",
  "request": "attach",
  "port": 8000,
  "hostName": "127.0.0.1",
  "sourcePaths": [
    "${workspaceFolder}/magnolia/decompiled/magnolia-content-translation-2.5.11",
    "${workspaceFolder}/magnolia/decompiled/magnolia-content-translation-pages-integration-app-2.5.11",
    "${workspaceFolder}/magnolia/decompiled/magnolia-content-translation-support-ext-core-3.6.4",
    "${workspaceFolder}/magnolia/decompiled/magnolia-content-translation-support-ext-google-3.6.4",
    "${workspaceFolder}/magnolia/decompiled/magnolia-content-translation-support-ext-instant-3.6.4"
  ],
  "console": "integratedTerminal",
  "stopOnEntry": false
}
```

**Note**: After decompiling additional Magnolia JARs, update the `sourcePaths` array to include your newly decompiled directories.

## Decompiling Magnolia JARs

To browse and debug Magnolia's source code, you need to decompile the Magnolia JAR files first.

### Prerequisites for Decompilation

- Java Runtime Environment (JRE) installed
- Magnolia must be built first (run `make install` from the `magnolia/` directory)

### Decompiling the JARs

1. Navigate to the debug tools directory:
   ```bash
   cd magnolia/debug_tools
   ```

2. Run the decompilation process:
   ```bash
   make decompile
   ```

This will:
- Process all JAR files with the "magnolia" prefix from the built webapp (`magnolia-webapp/target/.../WEB-INF/lib/`)
- Decompile each JAR to a separate folder in `magnolia/decompiled/`
- Create a folder structure like: `magnolia/decompiled/magnolia-xxx-version/`

**Note**: If you haven't built Magnolia yet, the decompile command will show an error message prompting you to build it first.

Example output structure:
```
magnolia/decompiled/
├── magnolia-content-translation-2.5.11/
├── magnolia-content-translation-pages-integration-app-2.5.11/
├── magnolia-content-translation-support-ext-core-3.5.3/
└── ...
```

### Cleaning Up Decompiled Files

To remove all decompiled files:

```bash
cd magnolia/debug_tools
make clean
```

### Updating Source Paths for Debugging

After decompiling new JARs, you may need to update the `sourcePaths` array in `.vscode/launch.json` to include the newly decompiled directories. Add entries like:

```json
"sourcePaths": [
  "${workspaceFolder}/magnolia/decompiled/magnolia-jar-name-version",
  // ... other paths
]
```

## Troubleshooting

### Debugging Not Working

- Ensure Magnolia is running with the correct JVM arguments (check `pom.xml` line 333 or 413 for the JDWP configuration)
- Verify port 8000 is not being used by another process
- Check that the debugger can connect to `127.0.0.1:8000`

### Missing Source Files During Debug

- Ensure you've decompiled the relevant Magnolia JARs
- Check that the JAR directory names in `sourcePaths` match your decompiled folders
- The decompiled folder name format is: `magnolia-module-name-version`

### Application Won't Start

- Verify all required environment variables are set
- Check if port 8080 is already in use
- Review logs in `magnolia/magnolia-webapp/logs/cargo.log`

## Additional Information

### Maven Profiles

The project uses Maven Cargo plugin with two profiles:

- **tomcat** (default): Uses embedded Derby database at `magnolia-webapp/data`
- **tomcat-postgres**: Uses PostgreSQL database at `magnolia-webapp/data-postgres`

### Useful Commands

```bash
# Clean build
cd magnolia
make clean

# Full install/build
cd magnolia
make install

# View logs
tail -f magnolia/magnolia-webapp/logs/cargo.log
tail -f magnolia/magnolia-webapp/data/logs/magnolia-debug.log
```

### JVM Debug Configuration

The debug agent is configured in `magnolia-webapp/pom.xml`:
```
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000
```

This enables remote debugging on port 8000 without suspending the JVM on startup.

