<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:http="http://expath.org/ns/http-client"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:template match="/">
        <xsl:variable name="my-request" as="element(http:request)">
            <http:request href='https://www.google.com' method='get'/>
        </xsl:variable>
        <xsl:copy-of select="http:send-request($my-request)"/>
    </xsl:template>
    
</xsl:stylesheet>