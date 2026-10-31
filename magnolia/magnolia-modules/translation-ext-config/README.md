# Translation Extension Configuration Module

## Purpose

This module provides automatic bootstrapping of Google Translation API configuration for Magnolia CMS. It ensures that the Google Translation provider configuration is always up-to-date by re-bootstrapping on every Magnolia startup.

## How It Works

The module uses `AutomaticDefaultDxpVersionHandler` which:
- Bootstraps all module resources on initial installation
- **Re-bootstraps on every update/restart**, ignoring module version checks
- Ensures configuration stays synchronized with the code

## Module Structure

```
translation-ext-config/
├── pom.xml
├── src/main/
│   ├── java/
│   │   └── com/tbscg/doosan/magnolia/translation/setup/
│   │       └── TranslationExtConfigVersionHandler.java
│   └── resources/
│       ├── META-INF/magnolia/
│       │   └── translation-ext-config.xml
│       └── mgnl-bootstrap/translation-ext-config/
│           └── config/modules/content-translation-support-ext-google/
│               └── translationProviders/
│                   └── Google.yaml
```

## Bootstrap File

The module bootstraps the Google Translation provider configuration to:
```
/modules/content-translation-support-ext-google/translationProviders/Google
```

Configuration includes:
- Client ID for Google OAuth
- Client token placeholder: `${jcr.google.translation.api.key}`
- Translation settings (auto-translation, max segments, etc.)

## Dependencies

- `magnolia-core`
- `magnolia-module-dxp-core` (for AutomaticDefaultDxpVersionHandler)
- `content-translation-support-ext-google` (Google Translation extension)

## Why This Approach?

Standard Magnolia bootstrap only runs on initial installation or specific version updates. By using `AutomaticDefaultDxpVersionHandler`, we ensure:
1. Configuration is always applied on every startup
2. Manual configuration in Magnolia UI won't be lost between deployments
3. Environment-specific placeholders (like API keys) are properly resolved
4. Consistent configuration across all environments

## Usage

Simply include this module in your Magnolia webapp. The configuration will be automatically bootstrapped on every Magnolia startup.


