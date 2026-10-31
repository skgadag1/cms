# versioncleaner

The version cleaner module provides commands to remove old or duplicated versions from your version store. Which versions are selected for removal can be configured through voters.

Analyzing and removing unneeded versions can take a lot of time, the version cleanup command can be configured to examine a set number of versions (actually nodes in the version workspace) and then stop. The next run of the version cleanup command will resume from the node last analyzed.

# Important safety note


## Installation

Add a dependency for the version cleaner module to your web app Maven pom file:

```
<dependency>
  <groupId>info.magnolia.services</groupId>
  <artifactId>versioncleaner</artifactId>
  <version>(current version cleaner module version)</version>
</dependency>
```

## Usage

The version cleaner module will create these commands:

cleanup-cleanVersions
cleanup-cleanAllVersions
cleanup-cleanNodeVersions
diagnostic-findBadVersions

cleanup-cleanVersions will remove unneeded versions according to its configuration.

cleanup-cleanAllVersions will remove all versions found except root and leaf versions.

cleanup-cleanNodeVersions will remove unneeded versions in a workspace and path according to its configuration.

diagnostic-findBadVersions will locate corrupted versions in the version workspace.

## Information on Magnolia CMS


## License


## Contributors

Andrew Warinner  
Senior Solution Architect  
Magnolia
