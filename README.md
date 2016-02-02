[![Release](https://jitpack.io/v/com.github.oriley-me/mortein.svg)](https://jitpack.io/#com.github.oriley-me/mortein) [![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0) [![Build Status](https://travis-ci.org/oriley-me/mortein.svg?branch=master)](https://travis-ci.org/oriley-me/mortein) [![Dependency Status](https://www.versioneye.com/user/projects/56b0958d3d82b90032bfff14/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56b0958d3d82b90032bfff14)

# mortein

Debug logging and field value replacement. Initially forked from Hugo by Jake Wharton.

# Gradle Dependency

Firstly, you need to add JitPack.io to your repositories list in the root projects build.gradle:

```gradle
repositories {
    maven { url "https://jitpack.io" }
}
```

Then, add the following to your module dependencies:

```gradle
dependencies {
    compile 'com.github.oriley-me.mortein:mortein-annotations:0.5'
    debugCompile 'com.github.oriley-me.mortein:mortein-runtime:0.5'
}
```
