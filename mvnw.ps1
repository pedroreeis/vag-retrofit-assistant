# PowerShell Maven Wrapper for Windows (vag-retrofit-assistant)
if (-not $env:JAVA_HOME -or -not (Test-Path $env:JAVA_HOME)) {
    if (Test-Path "C:\Program Files\Java\jdk-17") {
        $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
    } else {
        $jdkPath = Get-ChildItem "C:\Program Files\Java" -Filter "jdk-*" -ErrorAction SilentlyContinue | 
                   Sort-Object Name -Descending | 
                   Select-Object -First 1 -ExpandProperty FullName
        if ($jdkPath) {
            $env:JAVA_HOME = $jdkPath
        } else {
            $javaPath = Get-Command java.exe -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source
            if ($javaPath -and ($javaPath -notlike "*Common Files*")) {
                $env:JAVA_HOME = Split-Path (Split-Path $javaPath -Parent) -Parent
            } else {
                $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
            }
        }
    }
    Write-Host "Maven Wrapper: JAVA_HOME dynamically set to: $env:JAVA_HOME"
}

$MAVEN_VERSION = "3.9.6"
$MVN_DIR = Join-Path $PSScriptRoot ".maven"
$MVN_HOME = Join-Path $MVN_DIR "apache-maven-$MAVEN_VERSION"
$MVN_BIN = Join-Path $MVN_HOME "bin\mvn.cmd"

if (-not (Test-Path $MVN_BIN)) {
    Write-Host "Maven Wrapper: Maven not found locally. Downloading Apache Maven $MAVEN_VERSION..."
    if (-not (Test-Path $MVN_DIR)) {
        New-Item -ItemType Directory -Path $MVN_DIR | Out-Null
    }
    $ZipPath = Join-Path $MVN_DIR "maven.zip"
    $Url = "https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.zip"
    
    try {
        [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $Url -OutFile $ZipPath -ErrorAction Stop
    } catch {
        Write-Error "Failed to download Maven from $Url. Error: $_"
        exit 1
    }
    
    Write-Host "Maven Wrapper: Extracting Maven archive..."
    try {
        Expand-Archive -Path $ZipPath -DestinationPath $MVN_DIR -Force
        Remove-Item $ZipPath -Force
    } catch {
        Write-Error "Failed to extract Maven archive. Error: $_"
        exit 1
    }
    Write-Host "Maven Wrapper: Installation successful!"
}

# Run Maven
& $MVN_BIN $args
exit $LASTEXITCODE
