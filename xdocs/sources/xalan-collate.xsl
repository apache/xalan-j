<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match ="/">
  <documentation>
    <chapter id="index"><xsl:copy-of select="document('xalan/dtm.xml')"/></chapter>  
    <!--chapter id="index"><xsl:copy-of select="document('xalan/index.xml')"/></chapter>  
    <chapter id="whatsnew"><xsl:copy-of select="document('xalan/whatsnew.xml')"/></chapter>
    <chapter id="overview"><xsl:copy-of select="document('xalan/overview.xml')"/></chapter>
    <chapter id="getstarted"><xsl:copy-of select="document('xalan/getstarted.xml')"/></chapter>
    <chapter id="faq"><xsl:copy-of select="document('xalan/faq.xml')"/></chapter>
    <chapter id="samples"><xsl:copy-of select="document('xalan/samples.xml')"/></chapter>
    <chapter id="commandline"><xsl:copy-of select="document('xalan/commandline.xml')"/></chapter>
    <chapter id="usagepatterns"><xsl:copy-of select="document('xalan/usagepatterns.xml')"/></chapter>
    <chapter id="trax"><xsl:copy-of select="document('xalan/trax.xml')"/></chapter>
    <chapter id="extensions"><xsl:copy-of select="document('xalan/extensions.xml')"/></chapter>
    <chapter id="extensionslib"><xsl:copy-of select="document('xalan/extensionslib.xml')"/></chapter>
    <chapter id="readme"><xsl:copy-of select="document('xalan/readme.xml')"/></chapter>
    <chapter id="api">
      <s1 title="Java API">
        <p>Javadoc for the entire Xalan-Java API:</p>
        <ul>
          <li><jump href="apidocs/index.html">Xalan-Java 2 API</jump></li>
          <li><jump href="compat_apidocs/index.html">Xalan-Java 1 compatibility API</jump></li>
        </ul>         
      </s1>
    </chapter-->
  </documentation>
</xsl:template>
</xsl:stylesheet>


