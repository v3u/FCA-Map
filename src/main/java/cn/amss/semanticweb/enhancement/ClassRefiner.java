/*
 * ClassRefiner.java
 * Copyright (C) 2019 Guowei Chen <icgw@outlook.com>
 *
 * Distributed under terms of the GPL license.
 */

package cn.amss.semanticweb.enhancement;

import cn.amss.semanticweb.alignment.Mapping;

public interface ClassRefiner extends RefinerSetting
{
  public void addInstanceAnchors(Mapping instance_anchors);

  public void validateClassAnchors(Mapping class_anchors, Mapping enhanced_class_mappings);
}
