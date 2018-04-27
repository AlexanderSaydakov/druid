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

package io.druid.query.aggregation.datasketches.hll;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yahoo.sketches.hll.HllSketch;
import com.yahoo.sketches.hll.TgtHllType;

import io.druid.query.aggregation.Aggregator;
import io.druid.query.aggregation.AggregatorUtil;
import io.druid.query.aggregation.BufferAggregator;
import io.druid.segment.ColumnSelectorFactory;
import io.druid.segment.ColumnValueSelector;

/**
 * This aggregator factory is for building sketches from raw data.
 * The input column can contain identifiers of type string, char[], byte[] or any numeric type.
 * @author Alexander Saydakov
 */
public class HllSketchBuildAggregatorFactory extends HllSketchAggregatorFactory
{

  @JsonCreator
  public HllSketchBuildAggregatorFactory(
      @JsonProperty("name") final String name,
      @JsonProperty("fieldName") final String fieldName,
      @JsonProperty("lgK") @Nullable final Integer lgK,
      @JsonProperty("tgtHllType") @Nullable final String tgtHllType)
  {
    super(name, fieldName, lgK, tgtHllType);
  }

  @Override
  public String getTypeName()
  {
    return HllSketchModule.BUILD_TYPE_NAME;
  }

  @Override
  protected byte getCacheTypeId()
  {
    return AggregatorUtil.HLL_SKETCH_BUILD_CACHE_TYPE_ID;
  }

  @Override
  public Aggregator factorize(final ColumnSelectorFactory columnSelectorFactory)
  {
    final ColumnValueSelector<Object> selector = columnSelectorFactory.makeColumnValueSelector(getFieldName());
    return new HllSketchBuildAggregator(selector, getLgK(), TgtHllType.valueOf(getTgtHllType()));
  }

  @Override
  public BufferAggregator factorizeBuffered(final ColumnSelectorFactory columnSelectorFactory)
  {
    final ColumnValueSelector<Object> selector = columnSelectorFactory.makeColumnValueSelector(getFieldName());
    return new HllSketchBuildBufferAggregator(
        selector,
        getLgK(),
        TgtHllType.valueOf(getTgtHllType()),
        getMaxIntermediateSize()
    );
  }

  // for the HLL_4 sketch type, this value can be exceeded slightly in extremely rare cases
  @Override
  public int getMaxIntermediateSize()
  {
    return HllSketch.getMaxUpdatableSerializationBytes(getLgK(), TgtHllType.valueOf(getTgtHllType()));
  }

}
