/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotates a parameter of a repository {@link Find} or {@link Delete} method,
 * indicating how a persistent field is compared against the parameter's value.
 * The {@link By} annotation is used on the same parameter to identify the
 * persistent field.</p>
 *
 * <p>For example,</p>
 *
 * <pre>
 * &#64;Repository
 * public interface Products extends CrudRepository&lt;Product, Long&gt; {
 *
 *     // Find all Product entities where the price field is less than a maximum value.
 *     &#64;Find
 *     List&lt;Product&gt; pricedBelow(&#64;By(_Product.PRICE) &#64;Is(LESS_THAN) float max);
 *
 *     // Find a page of Product entities where the name field matches a pattern, ignoring case.
 *     &#64;Find
 *     Page&lt;Product&gt; search(&#64;By(_Product.NAME) &#64;Is(LIKE_IGNORE_CASE) String pattern,
 *                          PageRequest pagination,
 *                          Order&lt;Product&gt; order);
 *
 *     // Remove Product entities with any of the unique identifiers listed.
 *     &#64;Delete
 *     void remove(&#64;By(ID) &#64;Is(IN) List&lt;Long&gt; productIds);
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Is {
    // TODO add JavaDoc with examples to these
    String EQUAL                = "EQUAL";
    String GREATER_THAN         = "GREATER_THAN";
    String GREATER_THAN_EQ      = "GREATER_THAN_EQ";
    String IGNORE_CASE          = "IGNORE_CASE";
    String IN                   = "IN";
    String LESS_THAN            = "LESS_THAN";
    String LESS_THAN_EQ         = "LESS_THAN_EQ";
    String LIKE                 = "LIKE";
    String LIKE_IGNORE_CASE     = "LIKE_IGNORE_CASE";
    String NOT                  = "NOT";
    String NOT_IGNORE_CASE      = "NOT_IGNORE_CASE";
    String NOT_IN               = "NOT_IN";
    String NOT_LIKE             = "NOT_LIKE";
    String NOT_LIKE_IGNORE_CASE = "NOT_LIKE_IGNORE_CASE";

    /**
     * <p>The type of comparison operation to use when comparing a persistent
     * field against a value that is supplied to a repository method.
     * For portable applications, the comparison operation must be one of the
     * constants defined within this class. Jakarta Data providers might choose
     * to provide their own constants as non-portable extensions.</p>
     *
     * <p>The following example compares the year a person was born against
     * a minimum and maximum year that are supplied as parameters to a repository
     * method:</p>
     *
     * <pre>
     * &#64;Find
     * &#64;OrderBy(_Person.YEAR_BORN)
     * List&lt;Person&gt; bornWithin(&#64;By(_Person.YEAR_BORN) &#64;Is(TREATER_THAN_EQ) float minYear,
     *                         &#64;By(_Person.YEAR_BORN) &#64;Is(LESS_THAN_EQ) float maxYear);
     * </pre>
     *
     * <p>The default comparison operation is the {@linkplain #EQUAL equality}
     * comparison.</p>
     *
     * <p>For concise code, it can be convenient for a repository interface to
     * statically import one or more constants from this class. For example:</p>
     *
     * <pre>
     * import static jakarta.data.repository.Is.*;
     * </pre>
     *
     * @return the type of comparison operation.
     */
    String value() default EQUAL;
}
