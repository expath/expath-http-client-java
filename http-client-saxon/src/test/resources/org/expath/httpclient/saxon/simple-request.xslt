<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:http="http://expath.org/ns/http-client"
        version="2.0">

    <xsl:param name="request" as="element(http:request)"/>

    <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="no"/>

    <xsl:template name="make-request">
        <result><xsl:sequence select="http:send-request($request)"/></result>
    </xsl:template>

</xsl:stylesheet>