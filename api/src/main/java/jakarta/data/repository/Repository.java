/*
 * Copyright (c) 2022,2024 Contributors to the Eclipse Foundation
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
 * <p>Annotates a repository interface to be implemented by the container/runtime.</p>
 *
 * <p>This class is a CDI bean-defining annotation when CDI is available.
 * Regardless of whether CDI or custom dependency injection is used, the
 * repository implementation must be made available to applications via the
 * {@code jakarta.inject.Inject} annotation.</p>
 *
 * <p>For example,</p>
 *
 * <pre>
 * &#64;Repository
 * public interface Products extends DataRepository&lt;Product, Long&gt; {
 *
 *     &#64;Find
 *     &#64;OrderBy("price")
 *     List&lt;Product&gt; named(&#64;By("name") &#64;Is(LIKE_IGNORE_CASE) String namePattern);
 *
 *     &#64;Query("UPDATE Product SET price = price - (price * ?1) WHERE price * ?1 &lt;= ?2")
 *     int putOnSale(float rateOfDiscount, float maxDiscount);
 *
 *     ...
 * }
 * </pre>
 *
 * <pre>
 * &#64;Inject
 * Products products;
 *
 * ...
 * found = products.named("%Printer%");
 * numUpdated = products.putOnSale(0.15f, 20.0f);
 * </pre>
 *
 * <p>The module Javadoc provides an {@link jakarta.data/ overview} of Jakarta Data.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Repository {
    /**
     * Value for the {@link Repository#provider()} attribute that allows the use of any
     * available Jakarta Data provider that supports the type of entity
     * annotation that is present on the repository's entity class.
     */
    String ANY_PROVIDER = "";

    /**
     * <p>Value for the {@link Repository#dataStore()} attribute that indicates to use
     * a default data store.</p>
     *
     * <p>When running in a Jakarta EE profile or platform and the entity annotations
     * indicate a relational database, the default data store is the
     * Jakarta EE default data source, {@code java:comp/DefaultDataSource}.
     * Otherwise, the default data store is determined by the Jakarta Data provider.</p>
     *
     * <p>The default data store might require additional vendor-specific
     * configuration, depending on the vendor.</p>
     */
    String DEFAULT_DATA_STORE = "";

    /**
     * <p>Optionally indicates the data store to use for the repository.</p>
     *
     * <p>Precedence for interpreting the {@code dataStore} value is as follows,
     * from highest precedence to lowest precedence.</p>
     *
     * <ul>
     * <li>If running in an environment where Jakarta Config is available
     * and the value is found in Jakarta Config, an error is raised.
     * Interoperability with Jakarta Config is reserved for future versions
     * of Jakarta Data.
     * </li>
     * <li>If running in a Jakarta EE profile or platform and the entity annotations
     * indicate a relational database and the value begins with {@code java:}
     * and matches the name of a {@code jakarta.annotation.sql.DataSourceDefinition},
     * the JNDI name of a data source, or a resource reference to a data source,
     * then the corresponding {@code javax.sql.DataSource} is used as the data store.
     * If the same conditions are met but the value matches a persistence unit reference,
     * then the corresponding {@code jakarta.persistence.PersistenceUnit} is used as the
     * data store.
     * </li>
     * <li>Otherwise, the value serves as an identifier linking to vendor-specific configuration
     * for the Jakarta Data provider to interpret in a vendor-specific way.
     * Refer to the documentation of the Jakarta Data provider.
     * </li>
     * </ul>
     *
     * <p>The default value of this attribute is {@link #DEFAULT_DATA_STORE}.</p>
     *
     * @return the name of a data store or {@link #DEFAULT_DATA_STORE}.
     */
    String dataStore() default DEFAULT_DATA_STORE;

    /**
     * <p>Restricts the repository implementation to that of a specific
     * Jakarta Data provider.</p>
     *
     * <p>This is useful when multiple Jakarta Data providers support the
     * same type of entity annotation, in which case the provider attribute
     * clarifies which Jakarta Data provider must be used.
     * Jakarta Data providers must ignore {@link Repository} annotations
     * that indicate a different provider's name as the provider.</p>
     *
     * <p>The default value of this attribute is {@link #ANY_PROVIDER},
     * allowing the use of any available Jakarta Data provider that
     * supports the type of entity annotation that is present on the
     * repository's entity class.</p>
     *
     * @return the name of a Jakarta Data provider or {@link #ANY_PROVIDER}.
     */
    String provider() default ANY_PROVIDER;
}