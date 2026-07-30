# Copilot Coding Agent - Firewall Configuration for c:geo

This document lists the servers that the Copilot coding agent needs access to when executing gradle tasks for the c:geo project.

## Required Servers for Gradle Tasks

The following servers are required for executing the gradle tasks: `clean`, `assembleBasicDebug`, `test`, and `checkstyle`.

### 1. Gradle Distribution Server
- **URL**: `https://services.gradle.org`
- **Purpose**: Download Gradle wrapper distribution (gradle-8.11.1-all.zip)
- **Used by**: Gradle wrapper initialization
- **Required for tasks**: All gradle tasks (initial setup)

### 2. Maven Central Repository
- **URL**: `https://repo.maven.apache.org` (or `https://repo1.maven.org`)
- **Purpose**: Primary repository for Java/Android dependencies
- **Used by**: Most third-party libraries including:
  - Apache Commons (collections4, compress, lang3, text, io)
  - AssertJ (testing framework)
  - SpotBugs annotations
  - GeographicLib
  - Jackson XML processing
  - Jsoup HTML parsing
  - JUnit
  - OkHttp
  - RxJava
  - AndroidX libraries
  - Many other dependencies
- **Required for tasks**: `assembleBasicDebug`, `test`, `checkstyle`

### 3. Google Maven Repository
- **URL**: `https://dl.google.com/dl/android/maven2/` (or `https://maven.google.com`)
- **Purpose**: Android SDK components, Google Play Services, AndroidX libraries
- **Used by**: 
  - Android Gradle Plugin (com.android.tools.build:gradle:8.9.2)
  - Google Play Services (location, maps)
  - Google Material Design components
  - AndroidX libraries (appcompat, core, recyclerview, etc.)
  - Android build tools and SDKs
  - ML Kit (language detection and translation - basic flavor only)
- **Required for tasks**: `assembleBasicDebug`, `test`, `checkstyle`

### 4. JitPack.io
- **URL**: `https://jitpack.io`
- **Purpose**: Build and host GitHub repositories as Maven artifacts
- **Used by**:
  - k3b-geoHelper (com.github.k3b:k3b-geoHelper:v1.1.12)
  - Mapsforge snapshots (when using snapshot versions from GitHub)
  - VTM snapshots (com.github.mapsforge.vtm:*)
  - Moving-bits libraries (datatables, dbinspection)
  - AndroidChart (com.github.AppDevNext:AndroidChart)
  - UnmockPlugin (com.github.bjoernq:unmockplugin:0.9.0)
- **Required for tasks**: `assembleBasicDebug`, `test`

### 5. Sonatype OSS Snapshots Repository
- **URL**: `https://oss.sonatype.org/content/repositories/snapshots/`
- **Purpose**: Snapshot versions of Mapsforge VTM libraries
- **Used by**: Mapsforge VTM snapshot dependencies (when enabled in build.gradle)
- **Required for tasks**: `assembleBasicDebug` (if using snapshots), `test` (if using snapshots)
- **Note**: Currently used for VTM master-SNAPSHOT versions

### 6. Gradle Plugin Portal
- **URL**: `https://plugins.gradle.org`
- **Purpose**: Download Gradle plugins
- **Used by**:
  - com.github.ben-manes.versions plugin (dependency updates checker)
  - se.ascp.gradle.gradle-versions-filter plugin
  - com.github.spotbugs plugin (code quality analysis)
  - com.gradle.develocity plugin (build scans)
  - com.getkeepsafe.dexcount plugin (method count tracking)
- **Required for tasks**: All gradle tasks (plugin resolution)

## Additional Servers for Development Workflow

These servers are not strictly required for the specified gradle tasks but are useful for a complete development workflow:

### 7. Gradle Build Scans
- **URL**: `https://gradle.com` (specifically `https://scans.gradle.com` or Develocity endpoints)
- **Purpose**: Upload build scan data for analysis
- **Used by**: Develocity Gradle plugin (optional feature)
- **Required for**: Build scan uploads (optional)

### 8. GitHub
- **URL**: `https://github.com` and `https://api.github.com`
- **Purpose**: 
  - Source code repository operations
  - API access for GitHub integrations
  - Issue tracking
  - Pull request management
- **Required for**: Repository operations, CI/CD workflows

### 9. Codacy
- **URL**: `https://codacy.com` and `https://api.codacy.com`
- **Purpose**: Code quality analysis and reporting
- **Used by**: Code quality checks (as referenced in build configuration comments)
- **Required for**: Code quality integration (if enabled)

## Task-Specific Server Requirements

### `clean` task
- **Servers needed**: None (local operation only)
- **Purpose**: Deletes build directories

### `assembleBasicDebug` task
- **Servers needed**: 
  1. Gradle Distribution Server (if wrapper not cached)
  2. Maven Central
  3. Google Maven Repository
  4. JitPack.io
  5. Sonatype OSS Snapshots (if using snapshots)
  6. Gradle Plugin Portal
- **Purpose**: Compiles and packages the debug APK

### `test` task (unit tests)
- **Servers needed**:
  1. Gradle Distribution Server (if wrapper not cached)
  2. Maven Central (for test dependencies like JUnit, AssertJ, MockWebServer)
  3. Google Maven Repository (for Android test libraries)
  4. JitPack.io
  5. Sonatype OSS Snapshots (if using snapshots)
  6. Gradle Plugin Portal
- **Purpose**: Runs local unit tests without Android device

### `checkstyle` task
- **Servers needed**:
  1. Gradle Distribution Server (if wrapper not cached)
  2. Maven Central (for Checkstyle tool: version 10.26.1)
  3. Gradle Plugin Portal
- **Purpose**: Runs Checkstyle code style verification

## Notes

1. **Caching**: Gradle caches downloaded dependencies locally. After the first successful build, subsequent builds will require less network access unless dependencies are updated or cache is cleared.

2. **Proxy Configuration**: If the agent operates behind a corporate proxy, Gradle proxy settings may need to be configured in `gradle.properties` or environment variables.

3. **Build Cache**: The project uses Gradle build cache (`org.gradle.caching=true`), which can speed up builds by reusing outputs from previous builds.

4. **Offline Mode**: Once all dependencies are cached, gradle can operate in offline mode (`--offline` flag), but this is not recommended for initial builds or when dependencies change.

5. **Android SDK**: The agent needs access to download Android SDK components if not already installed. This typically happens through the Google Maven Repository.

6. **API Keys**: Some functionality requires API keys (defined in keys.xml), but these are not required for basic compilation and testing tasks.

## Recommended Firewall Configuration

For a secure yet functional setup, allow HTTPS (port 443) access to:

**Essential (for all gradle tasks):**
- `services.gradle.org`
- `repo.maven.apache.org` or `repo1.maven.org`
- `dl.google.com` or `maven.google.com`
- `plugins.gradle.org`

**Required for full build (assembleBasicDebug, test):**
- `jitpack.io`
- `oss.sonatype.org`

**Optional but recommended:**
- `github.com` and `api.github.com` (for repository operations)
- `gradle.com` and `scans.gradle.com` (for build scans, optional)

All connections use HTTPS (port 443) for secure communication.
