/*
 * Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
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
 * <p>Annotates a repository method as a parameter-based automatic query method
 * that returns entities or entity attributes.</p>
 *
 * <p>The {@code Find} annotation indicates that the annotated repository
 * method executes a query to retrieve entities (or a subset of entity attributes)
 * based on its parameters and the arguments assigned to those parameters.</p>
 *
 * <p>The method return type identifies one of the following:</p>
 * <ul>
 * <li>the entity type returned by the query,</li>
 * <li>a single entity attribute type returned by the query (requires the
 *     {@link Select} annotation), or</li>
 * <li>a Java record type representing a subset of entity attributes returned
 *     by the query. The names of the record components and entity attributes
 *     must match, or the entity attribute names must be specified by the
 *     {@link Select} annotation.</li>
 * </ul>
 *
 * <p>When returning a single entity attribute or a subset of entity attributes,
 * the attribute types must be single-valued basic types, not multiple-valued
 * types such as collections, arrays, or associations.</p>
 *
 * <p>Each parameter of the annotated method must either:
 * </p>
 * <ul>
 * <li>have exactly the same type and name (the parameter name in the Java source, or a name assigned by {@link By @By})
 *     as an attribute of the entity class, or</li>
 * <li>be of type {@link jakarta.data.Limit}, {@link jakarta.data.Sort}, {@link jakarta.data.Order}, or
 *     {@link jakarta.data.page.PageRequest}.</li>
 * </ul>
 * <p>The query is inferred from the method parameters which match attributes of the entity.
 * </p>
 * <p>There is no specific naming convention for methods annotated with {@code @Find}; they may be named arbitrarily,
 * and their names do not carry any semantic meaning defined by the Jakarta Data specification.
 * </p>
 * <p>For example, consider an interface representing a garage:</p>
 * <pre>
 * &#64;Repository
 * interface Garage {
 *     &#64;Find
 *     List&lt;Car&gt; getCarsWithModel(@By("model") String model);
 * }
 * </pre>
 * <p>The {@code @Find} annotation indicates that the {@code getCarsWithModel(model)} method retrieves {@code Car}
 * instances with the given value of the {@code model} attribute.
 * </p>
 *
 * <p>A method annotated with {@code @Find} must return one of the following types,
 * where {@code E} is the entity type, entity attribute type, or record type inferred or explicitly specified by the {@code @Find} annotation:</p>
 * <ul>
 *     <li>{@code E}, when the method returns a single instance</li>
 *     <li>{@code Optional<E>}, when the method returns at most a single instance,</li>
 *     <li>an array type {@code E[]},
 *     <li>{@code List<E>},</li>
 *     <li>{@code Stream<E>},</li>
 *     <li>{@code Page<E>}, or</li>
 *     <li>{@code CursoredPage<E>} (only allowed when {@code E} is the entity type).</li>
 * </ul>
 *
 * <p>For example, if a {@code Car} entity has attribute names including {@code make},
 * {@code model}, {@code year}, and {@code vin}, a repository can use a Java record
 * to request that only a subset of entity attributes be retrieved,</p>
 * <pre>
 * &#64;Repository
 * public interface Cars extends BasicRepository&lt;Car, String&gt; {
 *     record ModelInfo(String make,
 *                      String model,
 *                      int year) {}
 *
 *     &#64;Find
 *     Optional&lt;ModelInfo&gt; getModelInfo(@By("vin") String vehicleIdNum);
 * }
 * </pre>
 *
 * <p>An automatic query method annotated {@code Find} returns an entity instance,
 * Java record instance, or entity attribute instance for every database record
 * which satisfies the parameter-based conditions.</p>
 * <ul>
 * <li>If the return type of the annotated method is {@code E} (the entity type,
 *     entity attribute type, or record type) or {@code Optional<E>}
 *     and more than one database record satisfies the query conditions, the method
 *     must throw {@link jakarta.data.exceptions.NonUniqueResultException}.</li>
 * <li>If the return type of the annotated method is {@code E} (the entity type,
 *     entity attribute type, or record type) and no database record satisfies
 *     the query conditions, the method must throw
 *     {@link jakarta.data.exceptions.EmptyResultException}.</li>
 * </ul>
 *
 * <p>Annotations such as {@code @Find}, {@code @Query}, {@code @Insert}, {@code @Update}, {@code @Delete}, and
 * {@code @Save} are mutually-exclusive. A given method of a repository interface may have at most one {@code @Find}
 * annotation, lifecycle annotation, or query annotation.
 *
 * @see By
 * @see OrderBy
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Find {
    /**
     * <p>Optionally specifies the queried entity type.</p>
     *
     * <p>The default value, {@code void.class}, has the special meaning of
     * determining the entity type from the method return type if the method
     * returns entities, and otherwise from the primary entity type of the
     * repository.</p>
     *
     * <p>A repository method with the {@code Find} annotation must specify a
     * valid entity class as the value if the method does not return entities
     * and the repository does not otherwise define a primary entity type.</p>
     *
     * <p>For example,</p>
     *
     * <pre>
     * &#64;Repository
     * public interface Vehicles {
     *     &#64;Find(Car.class)
     *     &#64;Select("price")
     *     Optional&lt;Float&gt; getPrice(@By("vin") String vehicleIdNum);
     *
     *     ...
     * }
     * </pre>
     */
    Class<?> value = void.class;
}
