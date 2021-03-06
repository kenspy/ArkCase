<?xml version="1.0" encoding="UTF-8"?>
<!-- =================================================== -->
<!-- XSL-FO stylesheet to generate Request in PDF format -->
<!-- =================================================== -->
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
    exclude-result-prefixes="fo">
    <xsl:output method="xml" version="1.0"
        omit-xml-declaration="no" indent="yes" />
    <xsl:param name="requestID" />
    <xsl:param name="topic" />
    <xsl:param name="requestorSource" />
    <xsl:param name="requestorName" />
    <xsl:param name="requestorAddress" />
    <xsl:param name="requestorEmailAddress" />
    <xsl:param name="requestorOrganization" />
    <xsl:param name="requestorOrganizationAddress" />
    <xsl:param name="receivedDate" />
    <xsl:param name="requestType" />
    <xsl:param name="requestSubType" />
    <xsl:param name="currentDate" />
    <xsl:param name="category" />
    <xsl:param name="description" />
    <!-- ======================== -->
    <!-- root element: request -->
    <!-- ======================== -->
    <xsl:template match="/">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
            <fo:layout-master-set>
                <fo:simple-page-master
                    master-name="letter" page-height="11.0in"
                    page-width="8.5in" margin-top="0.17in"
                    margin-bottom="0.17in" margin-left="1.0in"
                    margin-right="1.0in">
                    <fo:region-body margin-top="0.5in"
                        margin-bottom="0.5in" />
                </fo:simple-page-master>
            </fo:layout-master-set>

            <fo:page-sequence master-reference="letter">

                <!-- mail cover data goes here -->
                <fo:flow flow-name="xsl-region-body" font-family="Serif">
                    <fo:block font-size="16pt" text-align="center" font-weight="bold" space-after="0.25in">
                        Request Information
                    </fo:block>
                    <fo:block font-size="13pt" text-align="center" space-after="0.75in">
                        <xsl:value-of select="$currentDate"/>
                    </fo:block>
                    <fo:block font-size="12pt" space-after="0.15in">
                        Request ID: <fo:inline font-weight="bold"><xsl:value-of select="$requestID"/></fo:inline>
                    </fo:block>
                    <fo:block font-size="12pt" space-after="0.15in">
                        Topic: <fo:inline font-weight="bold"><xsl:value-of select="$topic"/></fo:inline>
                    </fo:block>
                    <fo:block font-size="12pt" space-after="0.15in">
                        Requester Source: <fo:inline font-weight="bold"><xsl:value-of select="$requestorSource"/></fo:inline>
                    </fo:block>
                    <fo:block font-size="12pt" space-after="0.15in">
                        Requester Name: <fo:inline font-weight="bold"><xsl:value-of select="$requestorName"/></fo:inline>
                    </fo:block>
                    <fo:block font-size="12pt" space-after="0.15in">
                        Requester Address: <fo:inline font-weight="bold"><xsl:value-of select="$requestorAddress"/></fo:inline>
                    </fo:block>
                    <fo:block font-size="12pt" space-after="0.15in">
                        Requester Email Address: <fo:inline font-weight="bold"><xsl:value-of select="$requestorEmailAddress"/></fo:inline>
                    </fo:block>
                    <fo:block font-size="12pt" space-after="0.15in">
                        Requester Organization: <fo:inline font-weight="bold"><xsl:value-of select="$requestorOrganization"/></fo:inline>
                    </fo:block>
                    <fo:block font-size="12pt" space-after="0.15in">
                        Requester Organization Address: <fo:inline font-weight="bold"><xsl:value-of select="$requestorOrganizationAddress"/></fo:inline>
                    </fo:block>
                    <fo:block font-size="12pt" space-after="0.15in">
                        Received Date: <fo:inline font-weight="bold"><xsl:value-of select="$receivedDate"/></fo:inline>
                    </fo:block>
                    <fo:block font-size="12pt" space-after="0.15in">
                        Request Type: <fo:inline font-weight="bold"><xsl:value-of select="$requestType"/></fo:inline>
                    </fo:block>
                    <fo:block font-size="12pt" space-after="0.15in">
                        Request SubType: <fo:inline font-weight="bold"><xsl:value-of select="$requestSubType"/></fo:inline>
                    </fo:block>
                    <fo:block font-size="12pt" space-after="0.15in">
                        Category: <fo:inline font-weight="bold"><xsl:value-of select="$category"/></fo:inline>
                    </fo:block>
                    <fo:block font-size="12pt" space-after="0.15in">
                        Description: <fo:inline font-weight="bold"><xsl:value-of select="$description"/></fo:inline>
                    </fo:block>
                </fo:flow>
            </fo:page-sequence>

        </fo:root>
    </xsl:template>
</xsl:stylesheet>
