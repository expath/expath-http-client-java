# Saxon wrapper for the EXPath HTTP Client Module
This is a Java wrapper for Saxon so that it can use the EXPath HTTP Client Module.

Unfortunately there are quite a lot of Jar files that you will need to add to Saxon's classpath. To simplify this we provide several things:

**NOTE:** Where `.sh` files are specified below, the equivalent `.bat` files also exist for Microsoft Windows users.

## 1. A small application for testing

This can also be found in the `examples/example1` sub-directory

After running `mvn package` (from the parent folder) you will now find a folder `http-client-saxon/target/http-client-saxon-1.2.5-SNAPSHOT-dir`. In that folder is a `bin/` folder with some scripts which will run Saxon with the classpath setup for all the Jar files in the `lib/` folder. This uses Saxon-HE.

So for XSLT you can for example run this:
```
bin/transform.sh -config:config.xml -s:input1.xml -xsl:example1.xslt
```

The shorthand for this is `bin/example1-transform.sh`.

or, for XQuery you can run this:
So for XSLT you can for example run this:
```
bin/query.sh -config:config.xml -s:input1.xml -xsl:example1.xq
```

The shorthand for this is `bin/example1-query.sh`.

My config.xml for including the EXPath HTTP Client module looks like:
```xml
<configuration xmlns="http://saxon.sf.net/ns/configuration" edition="HE">
    <global traceExternalFunctions="true"/>
    <resources>
        <extensionFunction>org.expath.httpclient.saxon.SendRequestFunction</extensionFunction>
    </resources>
</configuration>
```

When calling the EXPath HTTP Client, my `input1.xml` looks like this:
```xml
<anything/>
```

When calling the EXPath HTTP Client for XSLT, my `example1.xslt` looks like this:
```xml
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:http="http://expath.org/ns/http-client"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:template match="/">
        <xsl:variable name="my-request" as="element(http:request)">
            <http:request href='https://google.com' method='get'/>
        </xsl:variable>
        <xsl:copy-of select="http:send-request($my-request)"/>
    </xsl:template>
    
</xsl:stylesheet>
```

When calling the EXPath HTTP Client for XQuery, my `example1.xq` looks like this:
```xquery
xquery version "1.0";

declare namespace http = "http://expath.org/ns/http-client";

let $my-request := <http:request href='https://www.google.com' method='get'/>
return
    http:send-request($my-request)
```

## 2. An Uber jar for the Saxon EXPath Http Client
After running `mvn package` you will now find a file: `http-client-saxon/target/http-client-saxon-1.2.5-SNAPSHOT-uber.jar`.

This is a single Jar file that includes all the dependencies (apart from Saxon). You can use it with Saxon for XSLT like so (assuming the two Jars are in the same folder that you run `java` from):

```
java -classpath http-client-saxon-1.2.5-SNAPSHOT-uber.jar:Saxon-HE-9.7.0-15.jar net.sf.saxon.Transform -config:config.xml -s:input1.xml -xsl:example1.xslt
```

or for XQuery:
```
java -classpath http-client-saxon-1.2.5-SNAPSHOT-uber.jar:Saxon-HE-9.7.0-15.jar net.sf.saxon.Query -config:config.xml -s:input1.xml -xsl:example1.xq
```
