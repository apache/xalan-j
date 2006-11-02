<?xml version="1.0"?>

  <!--
   * Licensed to the Apache Software Foundation (ASF) under one
   * or more contributor license agreements. See the NOTICE file
   * distributed with this work for additional information
   * regarding copyright ownership. The ASF licenses this file
   * to you under the Apache License, Version 2.0 (the  "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *     http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- match the root book element -->
  <xsl:template match="book">
    <project>
      <parameter name="copyright" value="{@copyright}"/>
      <xsl:apply-templates/>
    </project>
  </xsl:template>

 <xsl:template match="document">

    <process source="{@source}" producer="parser">
      <processor name="xslt">
        <parameter name="stylesheet" value="sbk:/style/stylesheets/any2project.xsl"/>
      </processor>
    </process>

    <create source="{@source}" target="{@id}.html" producer="parser" printer="html">
      <processor name="xslt">
        <parameter name="id" value="{@id}"/>
        <parameter name="stylesheet" value="sbk:/style/stylesheets/designdoc2html.xsl"/>
      </processor>
    </create>
  </xsl:template>
  
</xsl:stylesheet>