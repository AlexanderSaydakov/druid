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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yahoo.sketches.hll.HllSketch;
import com.yahoo.sketches.hll.TgtHllType;
import com.yahoo.sketches.hll.Union;

import io.druid.java.util.common.IAE;
import io.druid.query.aggregation.AggregatorFactory;
import io.druid.query.cache.CacheKeyBuilder;

/**
 * Base class for both build and merge factories
 * @author Alexander Saydakov
 */
abstract class HllSketchAggregatorFactory extends AggregatorFactory
{

  static final int DEFAULT_LG_K = 12;
  static final TgtHllType DEFAULT_TGT_HLL_TYPE = TgtHllType.HLL_4;

  static final Comparator<HllSketch> COMPARATOR = Comparator.nullsFirst(Comparator.comparingDouble(HllSketch::getEstimate));

  private final String name;
  private final String fieldName;
  private final int lgK;
  private final TgtHllType tgtHllType;

  HllSketchAggregatorFactory(final String name, final String fieldName, @Nullable final Integer lgK, @Nullable final String tgtHllType)
  {
    if (name == null) {
      throw new IAE("Aggregator name must be specified");
    }
    this.name = name;
    if (fieldName == null) {
      throw new IAE("Parameter fieldName must be specified");
    }
    this.fieldName = fieldName;
    this.lgK = lgK == null ? DEFAULT_LG_K : lgK;
    this.tgtHllType = tgtHllType == null ? DEFAULT_TGT_HLL_TYPE : TgtHllType.valueOf(tgtHllType);
  }

  @Override
  @JsonProperty
  public String getName()
  {
    return name;
  }

  @JsonProperty
  public String getFieldName()
  {
    return fieldName;
  }

  @JsonProperty
  public int getLgK()
  {
    return lgK;
  }

  @JsonProperty
  public String getTgtHllType()
  {
    return tgtHllType.toString();
  }

  @Override
  public List<String> requiredFields()
  {
    return Arrays.asList(fieldName);
  }

  // This is a convoluted way to return a list of input field names this aggregator needs.
  // Currently the returned factories are only used to obtain a field name by calling getName() method.
  @Override
  public List<AggregatorFactory> getRequiredColumns()
  {
    return Arrays.asList(new HllSketchBuildAggregatorFactory(fieldName, fieldName, lgK, tgtHllType.toString()));
  }

  @Override
  public HllSketch deserialize(final Object object)
  {
    return HllSketchMergeComplexMetricSerde.deserializeSketch(object);
  }

  @Override
  public HllSketch combine(final Object objectA, final Object objectB)
  {
    final Union union = new Union(lgK);
    union.update((HllSketch) objectA);
    union.update((HllSketch) objectA);
    return union.getResult(tgtHllType);
  }

  @Override
  public Double finalizeComputation(final Object object)
  {
    final HllSketch sketch = (HllSketch) object;
    return sketch.getEstimate();
  }

  @Override
  public Comparator<HllSketch> getComparator()
  {
    return COMPARATOR;
  }

  @Override
  public AggregatorFactory getCombiningFactory()
  {
    return new HllSketchMergeAggregatorFactory(getName(), getName(), getLgK(), getTgtHllType().toString());
  }

  @Override
  public byte[] getCacheKey()
  {
    return new CacheKeyBuilder(getCacheTypeId()).appendString(name).appendString(fieldName)
        .appendInt(lgK).appendInt(tgtHllType.ordinal()).build();
  }

  @Override
  public boolean equals(final Object object)
  {
    if (this == object) {
      return true;
    }
    if (object == null || !getClass().equals(object.getClass())) {
      return false;
    }
    final HllSketchAggregatorFactory that = (HllSketchAggregatorFactory) object;
    if (!name.equals(that.getName())) {
      return false;
    }
    if (!fieldName.equals(that.getFieldName())) {
      return false;
    }
    if (lgK != that.getLgK()) {
      return false;
    }
    if (!tgtHllType.equals(that.tgtHllType)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(name, fieldName, lgK, tgtHllType);
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + " {"
        + "name=" + name
        + "fieldName=" + fieldName
        + "lgK=" + lgK
        + "tgtHllType=" + tgtHllType
        + "}";
  }

  protected abstract byte getCacheTypeId();

}
