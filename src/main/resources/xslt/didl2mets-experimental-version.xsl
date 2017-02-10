<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:mets="http://www.loc.gov/METS/"
                xmlns="http://i.still.have.a.lot.to.do/ok"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:didl="urn:mpeg:mpeg21:2002:02-DIDL-NS"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">

    <xsl:template match="/">
        <mets:mets>
            <mets:dmdSec ID="dmd1">
                <mets:mdWrap MDTYPE="MODS">
                    <mets:xmlData>
                        <xsl:apply-templates select="/didl:DIDL/didl:Item/didl:Item/didl:Descriptor/didl:Statement/rdf:type[@rdf:resource='info:eu-repo/semantics/descriptiveMetadata']" />
                    </mets:xmlData>
                </mets:mdWrap>
            </mets:dmdSec>
            <mets:fileSec USE="storage/preservation">
                <mets:fileGrp>
                    <xsl:apply-templates select="/didl:DIDL/didl:Item/didl:Item/didl:Descriptor/didl:Statement/rdf:type[@rdf:resource='info:eu-repo/semantics/objectFile']" />
                </mets:fileGrp>
            </mets:fileSec>
        </mets:mets>
    </xsl:template>

    <xsl:template match="/didl:DIDL/didl:Item/didl:Item/didl:Descriptor/didl:Statement/rdf:type[@rdf:resource='info:eu-repo/semantics/descriptiveMetadata']">
        <xsl:copy-of select="../../../didl:Component/didl:Resource/node()" />
    </xsl:template>

    <xsl:template match="/didl:DIDL/didl:Item/didl:Item/didl:Descriptor/didl:Statement/rdf:type[@rdf:resource='info:eu-repo/semantics/objectFile']">
        <mets:file>
            <xsl:attribute name="MIMETYPE">
                <xsl:value-of select="../../../didl:Component/didl:Resource/@mimeType" />
            </xsl:attribute>
            <mets:FLocat LOCTYPE="URL">
                <xsl:attribute name="xlink:href">
                    <xsl:value-of select="../../../didl:Component/didl:Resource/@ref" />
                </xsl:attribute>
            </mets:FLocat>
        </mets:file>
    </xsl:template>

</xsl:stylesheet>