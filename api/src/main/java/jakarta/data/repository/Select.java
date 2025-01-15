/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package jakarta.data.repository;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Determines which entity attribute(s) are returned by a {@link Find}
 * operation.</p>
 *
 * <p>This annotation can be specified on a repository find method or
 * on a record component.</p>
 *
 * <p>When used on a repository find method, the {@link value} of this annotation
 * must be the name(s) of one or more entity attributes to use as query results
 * when an entity matches the restrictions imposed by the method.</p>
 *
 * <p>When used on a record component, the {@link value} of this annotation
 * must be the name of the entity attribute to which the record component
 * corresponds. This value is used for all repository methods that lack this
 * annotation and for which the record is the result type.</p>
 *
 * <p>This annotation must not be used in other locations.</p>
 *
 * @see Find
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.RECORD_COMPONENT })
public @interface Select {
    /**
     * <p>Name or names of an entity attribute(s) that have a single-valued
     * basic type. Multiple-valued types such as collections, arrays, and
     * associations cannot be retrieved independently of the entity.</p>
     *
     * <p>The {@code Select} annotation can be used with repository methods
     * that return a single entity attribute or multiple entity attributes.</p>
     *
     * <h2>Method that returns Single Entity Attributes</h2>
     *
     * <p>Place the {@code Select} annotation on a repository find method and
     * assign the annotation value to be the name of a single entity attribute.
     * The result type that is used in the method return type must be the
     * type of the entity attribute.</p>
     *
     * <p>For example, to return only the {@code price} attribute of the
     * {@code Car} entity that has the supplied {@code vin} attribute value,</p>
     *
     * <pre>
     * &#64;Repository
     * public interface Cars extends BasicRepository&lt;Car, String&gt; {
     *     &#64;Find
     *     &#64;Select(_Car.PRICE)
     *     Optional&lt;Float&gt; getPrice(@By(_Car.VIN) String vehicleIdNum);
     * }
     * </pre>
     *
     * <h2>Method that returns Java Records</h2>
     *
     * <p>A repository method can return a subset of entity attributes per result
     * by having the result type be a Java record. The {@code Select} annotation
     * can be used in the following ways to accommodate this.</p>
     *
     * <h3>Annotating a Repository Method</h3>
     *
     * <p>Place the {@code Select} annotation on a repository find method and
     * assign the annotation value to be the names of multiple entity attributes,
     * corresponding to the order and types of the components of the Java record
     * that is used for the result type.</p>
     *
     * <p>For example, to return only the {@code model}, {@code make}, and
     * {@code year} attributes of a {@code Car} entity that has the supplied
     * {@code vin} attribute value,</p>
     *
     * <pre>
     * &#64;Repository
     * public interface Cars extends BasicRepository&lt;Car, String&gt; {
     *     record ModelInfo(String model,
     *                      String manufacturer,
     *                      int designYear) {}
     *
     *     &#64;Find
     *     &#64;Select({_Car.MODEL, _Car.MAKE, _Car.YEAR})
     *     Optional&lt;ModelInfo&gt; getModelInfo(@By(_Car.VIN) String vehicleIdNum);
     * }
     * </pre>
     *
     * <h3>Annotating a Record Component</h3>
     *
     * <p>Place the {@code Select} annotation on each record component of the
     * Java record that is used as the result type of the repository method.
     * Assign the annotation value to be the name of an entity attribute that
     * has the same type as the record component.</p>
     *
     * <pre>
     * &#64;Repository
     * public interface Cars extends BasicRepository&lt;Car, String&gt; {
     *     record ModelInfo(&#64;Select(_Car.MODEL) String model,
     *                      &#64;Select(_Car.MAKE) String manufacturer,
     *                      &#64;Select(_Car.YEAR) int designYear) {}
     *
     *     &#64;Find
     *     Optional&lt;ModelInfo&gt; getModelInfo(@By(_Car.VIN) String vehicleIdNum);
     * }
     * </pre>
     *
     * <p>For more concise code, the {@code Select} annotation can be omitted from
     * record components that have the same name as the entity attribute name,
     * such as {@code model} in the above example.</p>
     *
     * <p>The examples above use the
     * {@linkplain jakarta.data/jakarta.data.metamodel static metamodel},
     * to avoid hard coding String values for the entity attribute names.</p>
     */
    String[] value();
}
