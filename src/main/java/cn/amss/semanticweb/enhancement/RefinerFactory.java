/*
 * RefinerFactory.java
 * Copyright (C) 2019 Guowei Chen <icgw@outlook.com>
 *
 * Distributed under terms of the GPL license.
 */

package cn.amss.semanticweb.enhancement;

import cn.amss.semanticweb.enhancement.impl.ClassRefinerImpl;
import cn.amss.semanticweb.enhancement.impl.InstanceRefinerImpl;

public class RefinerFactory
{
  private RefinerFactory() {
  }

  public static ClassRefiner createClassRefiner() {
    return new ClassRefinerImpl();
  }

  public static InstanceRefiner createInstanceRefiner() {
    return new InstanceRefinerImpl();
  }

  // TODO: property.
}
