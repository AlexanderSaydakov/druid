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

package io.druid.query.aggregation.datasketches.quantiles;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.Binder;
import com.yahoo.sketches.quantiles.DoublesSketch;

import io.druid.initialization.DruidModule;
import io.druid.segment.serde.ComplexMetrics;

public class DoublesSketchModule implements DruidModule
{

  public static final String DOUBLES_SKETCH = "quantilesDoublesSketch";

  public static final String DOUBLES_SKETCH_HISTOGRAM_POST_AGG = "quantilesDoublesSketchToHistogram";
  public static final String DOUBLES_SKETCH_QUANTILE_POST_AGG = "quantilesDoublesSketchToQuantile";
  public static final String DOUBLES_SKETCH_QUANTILES_POST_AGG = "quantilesDoublesSketchToQuantiles";
  public static final String DOUBLES_SKETCH_TO_STRING_POST_AGG = "quantilesDoublesSketchToString";

  @Override
  public void configure(final Binder binder)
  {
    if (ComplexMetrics.getSerdeForType(DOUBLES_SKETCH) == null) {
      ComplexMetrics.registerSerde(DOUBLES_SKETCH, new DoublesSketchComplexMetricSerde());
    }
  }

  @Override
  public List<? extends Module> getJacksonModules()
  {
    return Arrays.<Module> asList(
        new SimpleModule("DoublesQuantilesSketchModule").registerSubtypes(
            new NamedType(DoublesSketchAggregatorFactory.class, DOUBLES_SKETCH),
            new NamedType(DoublesSketchHistogramPostAggregator.class, DOUBLES_SKETCH_HISTOGRAM_POST_AGG),
            new NamedType(DoublesSketchQuantilePostAggregator.class, DOUBLES_SKETCH_QUANTILE_POST_AGG),
            new NamedType(DoublesSketchQuantilesPostAggregator.class, DOUBLES_SKETCH_QUANTILES_POST_AGG),
            new NamedType(DoublesSketchToStringPostAggregator.class, DOUBLES_SKETCH_TO_STRING_POST_AGG))
            .addSerializer(DoublesSketch.class, new DoublesSketchJsonSerializer()));
  }

}
