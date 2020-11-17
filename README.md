[![Build Status](https://travis-ci.com/expath/expath-http-client-java.svg?branch=master)](https://travis-ci.com/expath/expath-http-client-java)
[![Build status](https://ci.appveyor.com/api/projects/status/o090g9b807036qh6/branch/master?svg=true)](https://ci.appveyor.com/project/AdamRetter/expath-http-client-java/branch/master)
[![Java 7+](https://img.shields.io/badge/java-7%2B-blue.svg)](https://adoptopenjdk.net/)
[![License](https://img.shields.io/badge/license-MPL%201.0-blue.svg)](https://www-archive.mozilla.org/mpl/MPL-1.0.txt)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.expath.http.client/http-client-parent/badge.svg)](https://search.maven.org/search?q=g:org.expath.http.client)

# Java implementation of EXPath HTTP Client Module 

This is a Java implementation of the EXPath [HTTP Client Module](http://expath.org/spec/http-client) specification.

We provide a Java library that may be used as the basis for specific product implementations, and an RI (Reference
Implementation) developed for [Saxon](https://www.saxonica.com) which demonstrates how to use
the library. 

# Building from source

Requires:
* Java 1.7 or newer
* Maven 3 or newer

```bash
$ git clone https://github.com/expath/expath-http-client-java.git
$ cd expath-http-client-java
$ mvn clean package
```

**NOTE:** Where `.sh` files are specified below, the equivalent `.bat` files also exist for Microsoft Windows users.

# Using the Saxon RI

**Compatibility with Saxon Versions**

| http-client-saxon Version | Saxon Versions |
|---------------------------|----------------|
| 1.3.0                     | 9.9+           |
| &lt;= 1.2.4               | 9.7+           |

*Additional Saxon specific examples can be found in [http-client-saxon/README.md](http-client-saxon/README.md)*.

To use the RI for Saxon, you require several Jar files to be present on the classpath with Saxon:
`http-client-saxon-VERSION.jar`, `http-client-java-VERSION.jar`, and the dependencies of `http-client-java`; to make this easier we provide an Uber Jar, whereby you can just place `http-client-saxon-VERSION-uber.jar` onto Saxon's classpath.
If you have built from source these can be found in the respective folders: `http-client-saxon/target/`,
and `http-client-java/target/`, alternatively you may download the releases from
[Maven Central](https://search.maven.org/search?q=g:org.expath.http.client).

Saxon also needs to have the EXPath HTTP Client Module's functions registered with it. Depending on how you are
using Saxon, will depend on how this is done.

If you are using Saxon's classic API from Java, you can do something like:
```java
import org.expath.httpclient.saxon.SendRequestFunction;

...

Configuration configuration = new Configuration();
configuration.registerExtensionFunction(new SendRequestFunction());

...
```

If you are using Saxon from the command line, you may specify a Saxon
[configuration file](https://www.saxonica.com/html/documentation/configuration/configuration-file/), to which you
need to add an `<extensionFunction>` to the `<resources>` section, for example:

```xml
<configuration xmlns="http://saxon.sf.net/ns/configuration"
               edition="EE"
               licenseFileLocation="saxon-license.lic"
               label="Some label">

...

  <resources>
    <extensionFunction>org.expath.httpclient.saxon.SendRequestFunction</extensionFunction>

    ...
```

More information if needed about extension functions for Saxon can be found
[here](https://www.saxonica.com/html/documentation/extensibility/integratedfunctions/ext-full-J.html).
