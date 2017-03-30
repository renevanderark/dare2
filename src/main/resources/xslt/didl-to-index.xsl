<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dcterms="http://purl.org/dc/terms/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:didl="urn:mpeg:mpeg21:2002:02-DIDL-NS"
                xmlns:dii="urn:mpeg:mpeg21:2002:01-DII-NS"
                xmlns:wmp="http://www.surfgroepen.nl/werkgroepmetadataplus"
                exclude-result-prefixes="didl mods dc dcterms dii wmp"
>
    <xsl:param name="source_set" />
    <xsl:param name="source" />
    <xsl:template match="/">
        <add>
            <doc>
                <xsl:apply-templates select="/didl:DIDL/didl:Item/didl:Descriptor/didl:Statement[@mimeType='application/xml']/dii:Identifier" />
                <xsl:apply-templates select="//mods:mods/mods:genre" />
                <xsl:apply-templates select="//mods:mods/mods:originInfo/mods:dateOther[@type='embargo']" />
                <xsl:apply-templates select="//mods:mods/mods:originInfo/mods:dateIssued[@encoding='w3cdtf']" />
                <xsl:apply-templates select="//mods:mods/mods:originInfo/mods:publisher" />
                <xsl:apply-templates select="//dcterms:accessRights" />
                <xsl:apply-templates select="//wmp:rights/dc:description" />
                <xsl:apply-templates select="//wmp:rights/dc:rights" />
                <field name="source_s">
                    <xsl:value-of select="$source" />
                </field>
                <field name="sourceSet_s">
                    <xsl:value-of select="$source_set" />
                </field>
            </doc>
        </add>
    </xsl:template>

    <xsl:template match="/didl:DIDL/didl:Item/didl:Descriptor/didl:Statement[@mimeType='application/xml']/dii:Identifier">
        <field name="id"><xsl:value-of select="." /></field>
    </xsl:template>

    <xsl:template match="//mods:mods/mods:genre">
        <field name="genre_ss"><xsl:value-of select="." /></field>
    </xsl:template>

    <xsl:template match="//mods:mods/mods:originInfo/mods:dateOther[@type='embargo']">
        <field name="embargoDate_ss"><xsl:value-of select="." /></field>
    </xsl:template>

    <xsl:template match="//mods:mods/mods:originInfo/mods:dateIssued[@encoding='w3cdtf']">
        <field name="dateIssued_ss"><xsl:value-of select="." /></field>
    </xsl:template>

    <xsl:template match="//mods:mods/mods:originInfo/mods:publisher">
        <field name="publisher_ss"><xsl:value-of select="." /></field>
    </xsl:template>


    <xsl:template match="//dcterms:accessRights">
        <field name="accessRights_ss"><xsl:value-of select="." /></field>
    </xsl:template>

    <xsl:template match="//wmp:rights/dc:description" >
        <field name="wmpRightsDescription_ss"><xsl:value-of select="." /></field>
    </xsl:template>

    <xsl:template match="//wmp:rights/dc:rights" >
        <field name="wmpRights_ss"><xsl:value-of select="." /></field>
    </xsl:template>
</xsl:stylesheet>