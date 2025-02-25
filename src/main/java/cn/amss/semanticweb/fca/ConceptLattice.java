/*
 * ConceptLattice.java
 * Copyright (C) 2019 Guowei Chen <icgw@outlook.com>
 *
 * Distributed under terms of the GPL license.
 */

package cn.amss.semanticweb.fca;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Iterator;

/**
 * A concept lattice.
 * Here, this algorithm of constructing concept lattice is designed by Guowei Chen.
 *
 * @author Guowei Chen (icgw@outlook.com)
 */
public class ConceptLattice <O, A>
{
  Map<Integer, Concept<O, A>> id2Concept = null;
  Map<Concept<O, A>, Integer> concept2Id = null;

  int topId    = -1;
  int bottomId = -1;

  int numberOfConcepts = 0;

  Map<Integer, Set<Integer>> topDown  = null;
  Map<Integer, Set<Integer>> bottomUp = null;

  /**
   * Initial concept lattice.
   */
  private ConceptLattice() {
    id2Concept = new HashMap<>();
    concept2Id = new HashMap<>();
    topDown    = new HashMap<>();
    bottomUp   = new HashMap<>();
  }

  /**
   * initial concept lattice with the set of formal concepts.
   *
   * @param concepts the set of concepts
   * @param concepts no side effect
   */
  public ConceptLattice(Set<Concept<O, A>> concepts) {
    this();

    Set<O> all_objects    = new HashSet<>();
    Set<A> all_attributes = new HashSet<>();

    for (Concept<O, A> c : concepts) {
      all_objects.addAll(c.getExtent());
      all_attributes.addAll(c.getIntent());
    }

    Set<A> top_attributes = new HashSet<>(all_attributes);
    Set<O> bottom_objects = new HashSet<>(all_objects);
    for (Concept<O, A> c : concepts) {
      top_attributes.retainAll(c.getIntent());
      bottom_objects.retainAll(c.getExtent());
    }

    Concept<O, A> top    = new Concept<>(all_objects, top_attributes);
    Concept<O, A> bottom = new Concept<>(bottom_objects, all_attributes);

    init(concepts, top, bottom);
  }

  /**
   * initial concept lattice with the set of formal concepts, top concept and bottom concept.
   *
   * @param concepts the set of concepts
   * @param concepts no side effect
   * @param top the top concept
   * @param top no side effect
   * @param bottom the bottom concept
   * @param bottom no side effect
   */
  public ConceptLattice(Set<Concept<O, A>> concepts, Concept<O, A> top, Concept<O, A> bottom) {
    this();
    init(concepts, top, bottom);
  }

  /**
   * initial implement method.
   *
   * @param concepts the set of concepts
   * @param concepts no side effect
   * @param top the top concept
   * @param top no side effect
   * @param bottom the bottom concept
   * @param bottom no side effect
   */
  private void init(Set<Concept<O, A>> concepts, Concept<O, A> top, Concept<O, A> bottom) {
    TreeSet<Concept<O, A>> tConcepts = new TreeSet<>(new Comparator<Concept<O, A>>() {
      @Override
      public int compare(Concept<O, A> a, Concept<O, A> b) {
        Set<O> aExtent = a.getExtent();
        Set<O> bExtent = b.getExtent();
        if (aExtent.size() == bExtent.size()) {
          Set<A> aIntent = a.getIntent();
          Set<A> bIntent = b.getIntent();
          if (aIntent.size() == bIntent.size()) {
            return a.toString().compareTo(b.toString());
          }
          return aIntent.size() - bIntent.size();
        }

        return bExtent.size() - aExtent.size();
      }
    });

    tConcepts.addAll(concepts);

    int idx = 0;
    for (Concept<O, A> c : tConcepts) {
      id2Concept.put(idx, c);
      concept2Id.put(c, idx);
      ++idx;
    }
    numberOfConcepts = idx;

    // XXX: check if top or bottom not exist in concepts. (wait to improve)
    topId    = concept2Id.getOrDefault(top,    -1);
    bottomId = concept2Id.getOrDefault(bottom, -2);
  }

  /**
   * Check whether the concepts exist hierarchical direct order.
   *
   * @param up the up concept
   * @param up no side effect
   * @param down the down concept
   * @param down no side effect
   * @return true means the order exist, otherwise false
   */
  private boolean isUpDown(Concept<O, A> up, Concept<O, A> down) {
    if (up == null || down == null || up.equals(down)) return false;
    return up.getExtent().containsAll(down.getExtent());
  }

  /**
   * Check whether the identity (Integer) of concepts exist hierarchical direct order.
   *
   * @param upId the id of up concept
   * @param downId the id of down concept
   * @return true or false
   */
  private boolean isUpDown(int upId, int downId) {
    if (upId == downId) return false;
    return isUpDown(id2Concept.get(upId), id2Concept.get(downId));
  }

  /**
   * Check whether the concepts exist hierarchical direct order.
   *
   * @param down the down concept
   * @param down no side effect
   * @param up the up concept
   * @param up no side effect
   * @return true/false
   */
  private boolean isDownUp(Concept<O, A> down, Concept<O, A> up) {
    if (down == null || up == null || down.equals(up)) return false;
    return down.getIntent().containsAll(up.getIntent());
  }

  /**
   * Check whether the identity (Integer) of concepts exist hierarchical direct order.
   *
   * @param downId the id of down concept
   * @param upId the id of up concept
   * @return true/false
   */
  private boolean isDownUp(int downId, int upId) {
    if (downId == upId) return false;
    return isDownUp(id2Concept.get(downId), id2Concept.get(upId));
  }

  /**
   * Build concept lattice from top to bottom.
   */
  public void buildTopDown() {
    Queue<Integer> parentIdQueue = new LinkedList<Integer>();
    for (int cId = 0; cId < numberOfConcepts; ++cId) {
      if (cId == topId) continue;

      parentIdQueue.offer(topId);
      while ( !parentIdQueue.isEmpty() ) {
        int pId                 = parentIdQueue.poll();
        Set<Integer> childrenId = topDown.get(pId);

        if (childrenId == null || childrenId.isEmpty()) {
          topDown.put(pId, new HashSet<Integer>(Arrays.asList(cId)));
        } else {
          Set<Integer> childrenIdCopy = new HashSet<>(childrenId);

          boolean bHasExec = false;
          for (int childId : childrenIdCopy) {
            if (cId == childId) continue;

            if (isUpDown(cId, childId)) {
              childrenId.remove(childId);
              childrenId.add(cId);
              Set<Integer> cDownId = topDown.get(cId);
              if (cDownId == null || cDownId.isEmpty()) {
                topDown.put(cId, new HashSet<Integer>(Arrays.asList(childId)));
              } else {
                cDownId.add(childId);
              }
              bHasExec = true;
            }
            else if (isDownUp(cId, childId)) {
              parentIdQueue.offer(childId);
              bHasExec = true;
            }
          }

          if (!bHasExec) {
            childrenId.add(cId);
          }
        }
      }
    }
  }

  /**
   * Build concept lattice from bottom to top.
   */
  public void buildBottomUp() {
    if (topDown == null || topDown.isEmpty()) {
      buildTopDown();
    }

    for (Map.Entry<Integer, Set<Integer>> e : topDown.entrySet()) {
      int v = e.getKey();

      for (int k : e.getValue()) {
        Set<Integer> si = bottomUp.get(k);
        if (si == null) {
          bottomUp.put(k, new HashSet<>(Arrays.asList(v)));
        } else {
          si.add(v);
        }
      }
    }
  }

  /**
   * Output the DOT language of the concept lattice.
   *
   * @return a string of dot language
   */
  public String dotLangFormat() {
    if (topDown == null || topDown.isEmpty()) return "graph { }";

    StringBuilder dotLanguage = new StringBuilder();
    dotLanguage.append(String.format("%s {%n", "graph"));

    for (Map.Entry<Integer, Concept<O, A>> e : id2Concept.entrySet()) {
      // XXX: we should guarantee that e.getValue().toString() not has quotation mark (")
      dotLanguage.append(String.format("  %d [label = \"%s\"];%n", e.getKey(), e.getValue().toString()));
    }

    for (Map.Entry<Integer, Set<Integer>> e : topDown.entrySet()) {
      dotLanguage.append(String.format("  %d -- { ", e.getKey()));

      for (Iterator<Integer> it = e.getValue().iterator(); it.hasNext(); ) {
        dotLanguage.append(String.format("%d", it.next()));

        if (it.hasNext()) {
          dotLanguage.append(", ");
        } else {
          dotLanguage.append(String.format(" };%n"));
        }
      }
    }
    dotLanguage.append("}");

    return dotLanguage.toString();
  }

  /**
   * Get the concept lattice.
   *
   * @return the hash map of concept lattice
   */
  public Map<Concept<O, A>, Set<Concept<O, A>>> getSupSubConcepts() {
    Map<Concept<O, A>, Set<Concept<O, A>>> supSub = new HashMap<>();
    for (Map.Entry<Integer, Set<Integer>> e : topDown.entrySet()) {
      Set<Concept<O, A>> values = new HashSet<>();
      for (int i : e.getValue()) {
        values.add(id2Concept.get(i));
      }
      supSub.put(id2Concept.get(e.getKey()), values);
    }
    return supSub;
  }
}
