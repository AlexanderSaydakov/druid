/*
 * Licensed to Metamarkets Group Inc. (Metamarkets) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Metamarkets licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.druid.query.aggregation.datasketches.tuple;

import java.util.Comparator;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yahoo.sketches.tuple.ArrayOfDoublesSketch;
import com.yahoo.sketches.tuple.ArrayOfDoublesSketchIterator;

import io.druid.java.util.common.IAE;
import io.druid.query.aggregation.AggregatorUtil;
import io.druid.query.aggregation.PostAggregator;

/**
 * Returns a list of mean values from a given {@link ArrayOfDoublesSketch}.
 * The result will be N double values, where N is the number of double values kept in the sketch per key.
 */
public class ArrayOfDoublesSketchToMeansPostAggregator extends ArrayOfDoublesSketchUnaryPostAggregator
{

  @JsonCreator
  public ArrayOfDoublesSketchToMeansPostAggregator(
      @JsonProperty("name") final String name,
      @JsonProperty("field") final PostAggregator field
  )
  {
    super(name, field);
  }

  @Override
  public Object compute(final Map<String, Object> combinedAggregators)
  {
    final ArrayOfDoublesSketch sketch = (ArrayOfDoublesSketch) getField().compute(combinedAggregators);
    final double[] means = new double[sketch.getNumValues()];
    final ArrayOfDoublesSketchIterator it = sketch.iterator();
    while (it.next()) {
      final double[] values = it.getValues();
      for (int i = 0; i < values.length; i++) {
        means[i] += values[i];
      }
    }
    for (int i = 0; i < means.length; i++) {
      means[i] /= sketch.getRetainedEntries();
    }
    return means;
  }

  @Override
  public Comparator<double[]> getComparator()
  {
    throw new IAE("Comparing arrays of mean values is not supported");
  }

  @Override
  byte getCacheId()
  {
    return AggregatorUtil.ARRAY_OF_DOUBLES_SKETCH_TO_MEANS_CACHE_TYPE_ID;
  }

}
