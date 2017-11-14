<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:pom="http://maven.apache.org/POM/4.0.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" indent="no" encoding="utf-8"/>

<xsl:template match="/">
// From properties
<xsl:call-template name="nl" />
	<xsl:for-each select="pom:project/pom:properties">
		<xsl:for-each select="*">
			<xsl:if test="contains(name(), '.version')">
				<xsl:value-of select="name()" />=<xsl:value-of select="text()" />
				<xsl:call-template name="nl" />
			</xsl:if>
		</xsl:for-each>
	</xsl:for-each>
// From dependencies
<xsl:call-template name="nl" />
	<xsl:for-each select="pom:project/pom:dependencyManagement/pom:dependencies/pom:dependency">
		<xsl:if test="not(starts-with(pom:version, '${'))">
			<xsl:value-of select="pom:groupId" />.<xsl:value-of select="pom:artifactId" />.version=<xsl:value-of select="pom:version" />
			<xsl:call-template name="nl" />
		</xsl:if>
	</xsl:for-each>
</xsl:template>

<!-- new line -->
<xsl:template name="nl">
<xsl:text>
</xsl:text>
</xsl:template>

</xsl:stylesheet>
