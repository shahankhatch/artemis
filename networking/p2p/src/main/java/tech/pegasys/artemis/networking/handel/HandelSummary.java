package tech.pegasys.artemis.networking.handel;

import java.util.List;

/**
 * This is the aggregation summary structure proposed by Nicolas et al.
 * TODO: change this structure to a graph node-centric approach
 * The change will make the structure more flexible and less corruptible in async/parallel
 * environments (requires local locking only)
 */
public class HandelSummary {

  private int height;
  List<HandelAggregation> aggregations;
}
