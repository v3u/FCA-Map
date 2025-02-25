/*
 * MatcherFactory.java
 * Copyright (C) 2019 Guowei Chen <icgw@outlook.com>
 *
 * Distributed under terms of the GPL license.
 */

package cn.amss.semanticweb.matching;

import cn.amss.semanticweb.matching.impl.LexicalMatcherImpl;
import cn.amss.semanticweb.matching.impl.AdditionalPropertyMatcherImpl;

/**
 * The matcher factory for creating a specified matcher model
 *
 * @author Guowei Chen (icgw@outlook.com)
 */
public class MatcherFactory
{
  private MatcherFactory() {
  }

  /**
   * The matcher by FCA based on lexical analysis
   *
   * @return a lexical matcher
   */
  public static LexicalMatcher createLexicalMatcher() {
    return new LexicalMatcherImpl();
  }

  /**
   * The matcher by FCA for identifying more additional property correspondences
   *
   * @return an additional property matcher
   */
  public static AdditionalPropertyMatcher createAdditionalPropertyMatcher() {
    return new AdditionalPropertyMatcherImpl();
  }

  // TODO: more matcher implements are still on developing
}
