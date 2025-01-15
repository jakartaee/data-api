/*
 * Copyright (c) 2022,2025 Contributors to the Eclipse Foundation
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

import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.metamodel.StaticMetamodel;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.DataRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import jakarta.data.repository.Select;
import jakarta.data.repository.Update;

import java.util.Set;

/**
 * <p>Jakarta Data standardizes a programming model where data is represented by
 * simple Java classes and where operations on data are represented by interface
 * methods.</p>
 *
 * <p>The application defines simple Java objects called entities to represent
 * data in the database. Fields or accessor methods designate each entity attribute.
 * For example,</p>
 *
 * <pre>
 * &#64;Entity
 * public class Product {
 *     &#64;Id
 *     public long id;
 *     public String name;
 *     public float price;
 *     public int yearProduced;
 *     ...
 * }
 * </pre>
 *
 * <p>A repository is an interface annotated with the {@link Repository} annotation.
 * A repository declares methods which perform queries and other operations on entities.
 * For example,</p>
 *
 * <pre>
 * &#64;Repository
 * public interface Products extends BasicRepository&lt;Product, Long&gt; {
 *
 *     &#64;Insert
 *     void create(Product prod);
 *
 *     &#64;OrderBy("price")
 *     List&lt;Product&gt; findByNameIgnoreCaseLikeAndPriceLessThan(String namePattern, float max);
 *
 *     &#64;Query("UPDATE Product SET price = price * (1.0 - ?1) WHERE yearProduced &lt;= ?2")
 *     int discountOldInventory(float rateOfDiscount, int maxYear);
 *
 *     ...
 * }
 * </pre>
 *
 * <p>Repository interfaces are implemented by the container/runtime and are made
 * available to applications via the {@code jakarta.inject.Inject} annotation. For
 * example,</p>
 *
 * <pre>
 * &#64;Inject
 * Products products;
 *
 * ...
 * products.create(newProduct);
 *
 * found = products.findByNameIgnoreCaseLikeAndPriceLessThan("%cell%phone%", 900.0f);
 *
 * numDiscounted = products.discountOldInventory(0.15f, Year.now().getValue() - 1);
 * </pre>
 *
 * <p>Jakarta Persistence and Jakarta NoSQL define programming models for entity
 * classes that may be used with Jakarta Data:</p>
 * <ul>
 * <li>{@code jakarta.persistence.Entity} and the corresponding entity-related
 *    annotations of the Jakarta Persistence specification may be used to
 *    define entities stored in a relational database, or</li>
 * <li>{@code jakarta.nosql.Entity} and the corresponding entity-related
 *     annotations of the Jakarta NoSQL specification may be used to define
 *     entities stored in a NoSQL database.
 * </ul>
 * <p>A Jakarta Data provider may define its own programming model for entity
 *    classes representing some other arbitrary kind of data.</p>
 *
 * <p>Methods of repository interfaces must be styled according to a
 * well-defined set of conventions, which instruct the container/runtime
 * about the desired data access operation to perform. These conventions
 * consist of patterns of reserved keywords within the method name, method
 * parameters with special meaning, method return types, and annotations
 * placed upon the method and its parameters.</p>
 *
 * <p>Built-in repository superinterfaces, such as {@link DataRepository},
 * are provided as a convenient way to inherit commonly used methods and
 * are parameterized by the entity type and by its id type. Other built-in
 * repository interfaces, such as {@link BasicRepository}, may be used in
 * place of {@link DataRepository} and provide a base set of predefined
 * repository operations serving as an optional starting point. The Java
 * application programmer may extend these built-in interfaces, adding
 * custom methods. Alternatively, the programmer may define a repository
 * interface without inheriting the built-in superinterfaces. A programmer
 * may even copy individual method signatures from the built-in repositories
 * to a repository which does not inherit any built-in superinterface. This
 * is possible because the methods of the built-in repository superinterfaces
 * respect the conventions defined for custom repository methods.</p>
 *
* <p>The following example shows an entity class, an embeddable class, and
 * a repository interface:</p>
 * <pre>
 * &#64;Entity
 * public class Purchase {
 *     &#64;Id
 *     public String purchaseId;
 *     &#64;Embedded
 *     public Address address;
 *     ...
 * }
 *
 * &#64;Embeddable
 * public class Address {
 *     public int zipCode;
 *     ...
 * }
 *
 * &#64;Repository
 * public interface Purchases {
 *     &#64;OrderBy("address.zipCode")
 *     List&lt;Purchase&gt; findByAddressZipCodeIn(List&lt;Integer&gt; zipCodes);
 *
 *     &#64;Query("WHERE address.zipCode = ?1")
 *     List&lt;Purchase&gt; forZipCode(int zipCode);
 *
 *     &#64;Save
 *     Purchase checkout(Purchase purchase);
 * }
 * </pre>
 *
 * <h2>Entities</h2>
 *
 * <p>An entity programming model typically specifies an entity-defining
 * annotation that is used to identify entity classes. For Jakarta Persistence,
 * this is {@code jakarta.persistence.Entity}. For Jakarta NoSQL, it is
 * {@code jakarta.nosql.Entity}. A provider may even have no entity-defining
 * annotation and feature a programming model for entity classes where the
 * entity classes are unannotated.</p>
 *
 * <p>Furthermore, an entity programming model must define an annotation which
 * identifies the attribute holding the unique identifier of an entity.
 * For Jakarta Persistence, it is {@code jakarta.persistence.Id} or
 * {@code jakarta.persistence.EmbeddedId}. For Jakarta NoSQL, it is
 * {@code jakarta.nosql.Id}. Alternatively, an entity programming model might
 * allow the identifier attribute to be identified via some convention.
 * Every entity has a unique identifier.</p>
 *
 * <p>An entity has an arbitrary number of attributes.</p>
 *
 * <h3>Entity attribute names</h3>
 *
 * <p>Each attribute of an entity or embeddable class is assigned a
 * name:</p>
 * <ul>
 * <li>when direct field access is used, the name of an entity attribute is
 *     simply the name of the Java field, but</li>
 * <li>when property-based access is used, the name of the entity attribute is derived
 *     from the accessor methods, according to JavaBeans conventions.</li>
 * </ul>
 * <p>Within a given entity class or embeddable class, names assigned to
 * entity attributes must be unique ignoring case.</p>
 * <p>Furthermore, within the context of a given entity, each attribute
 * of an embeddable class reachable by navigation from the entity class may be
 * assigned a compound name. The compound name is obtained by concatenating the
 * names assigned to each attribute traversed by navigation from the entity class
 * to the attribute of the embedded class, optionally joined by a
 * delimiter.</p>
 * <ul>
 * <li>For parameters of a {@link Find} method, the delimiter is {@code _}.
 * <li>For path expressions within a {@linkplain Query query}, the delimiter
 *     is {@code .}.
 * <li>For method names in <em>Query by Method Name</em>, the delimiter is
 *     {@code _} and it is optional. For example, {@code findByAddress_ZipCode}
 *     or {@code findByAddressZipCode} are both legal.
 * <li>For arguments to constructor methods of {@link Sort}, the delimiter
 *     is {@code _} or {@code .}.
 * <li>For the {@code value} member of the {@link OrderBy} or {@link By}
 *     annotation the delimiter is {@code _} or {@code .}.
 * </ul>
 *
 * <p>A entity attribute name used in a Query by Method Name must not contain
 * a keyword reserved by Query by Method Name.</p>
 *
 * <h3>Entity attribute types (basic types)</h3>
 *
 * <p>The following is a list of valid basic entity attribute types.
 * These can be used as the types of repository method parameters
 * for the respective entity attribute.</p>
 *
 * <table style="width: 100%">
 * <caption><b>Basic Types for Entity Attributes</b></caption>
 * <tr style="background-color:#ccc">
 * <td style="vertical-align: top; width:20%"><b>Category</b></td>
 * <td style="vertical-align: top; width:20%"><b>Basic Types</b></td>
 * <td style="vertical-align: top; width:*"><b>Notes</b></td>
 * </tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>Primitives and primitive wrappers</td>
 * <td>{@code boolean} and {@link Boolean}
 * <br>{@code byte} and {@link Byte}
 * <br>{@code char} and {@link Character}
 * <br>{@code double} and {@link Double}
 * <br>{@code float} and {@link Float}
 * <br>{@code int} and {@link Integer}
 * <br>{@code long} and {@link Long}
 * <br>{@code short} and {@link Short}</td>
 * <td></td></tr>
 *
 * <tr style="vertical-align: top"><td>Binary data</td>
 * <td>{@code byte[]}</td>
 * <td>Not sortable.</td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>Enumerated types</td>
 * <td>{@code enum} types</td>
 * <td>It is provider-specific whether sorting is based on
 * {@link Enum#ordinal()} or {@link Enum#name()}.
 * The Jakarta Persistence default of {@code ordinal} can be
 * overridden with the {@code jakarta.persistence.Enumerated}
 * annotation.</td></tr>
 *
 * <tr style="vertical-align: top"><td>Large numbers</td>
 * <td>{@link java.math.BigDecimal}
 * <br>{@link java.math.BigInteger}</td>
 * <td></td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>Textual data</td>
 * <td>{@link String}</td>
 * <td></td></tr>
 *
 * <tr style="vertical-align: top"><td>Time and Dates</td>
 * <td>{@link java.time.Instant}
 * <br>{@link java.time.LocalDate}
 * <br>{@link java.time.LocalDateTime}
 * <br>{@link java.time.LocalTime}</td>
 * <td></td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>Universally unique identifier</td>
 * <td>{@link java.util.UUID}</td>
 * <td></td></tr>
 *
 * </table>
 *
 * <p>All of the basic types are sortable except for {@code byte[]}.
 * A Jakarta Data provider might allow additional entity attribute types.</p>
 *
 * <h2>Lifecycle methods</h2>
 *
 * <p>A lifecycle method makes changes to persistent data in the data store.
 * A lifecycle method must be annotated with a lifecycle annotation such as
 * {@link Insert}, {@link Update}, {@link Save}, or {@link Delete}. The
 * method must accept a single parameter, whose type is either:</p>
 *
 * <ul>
 * <li>the class of the entity, or</li>
 * <li>{@code List<E>} or {@code E[]} where {@code E} is the class of the
 *     entities.</li>
 * </ul>
 *
 * <p>The annotated method must be declared {@code void}, or, except in the
 * case of {@code @Delete}, have a return type that is the same as the type
 * of its parameter.</p>
 *
 * <table style="width: 100%">
 * <caption><b>Lifecycle Annotations</b></caption>
 * <tr style="background-color:#ccc">
 * <td style="vertical-align: top; width: 10%"><b>Annotation</b></td>
 * <td style="vertical-align: top; width: 25%"><b>Description</b></td>
 * <td style="vertical-align: top; width: 65%"><b>Example</b></td>
 * </tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@link Delete}</td>
 * <td>deletes entities</td>
 * <td>{@code @Delete}<br>{@code public void remove(person);}</td></tr>
 *
 * <tr style="vertical-align: top"><td>{@link Insert}</td>
 * <td>creates new entities</td>
 * <td>{@code @Insert}<br>{@code public List<Employee> add(List<Employee> newEmployees);}</td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@link Save}</td>
 * <td>update if exists, otherwise insert</td>
 * <td>{@code @Save}<br>{@code Product[] saveAll(Product... products)}</td></tr>
 *
 * <tr style="vertical-align: top"><td>{@link Update}</td>
 * <td>updates an existing entity</td>
 * <td>{@code @Update}<br>{@code public boolean modify(Product modifiedProduct);}</td></tr>
 * </table>
 *
 * <p>Refer to the API documentation for {@link Insert}, {@link Update}, {@link Delete},
 * and {@link Save} for further information about these annotations.</p>
 *
 * <h2>JDQL query methods</h2>
 *
 * <p>The {@link Query} annotation specifies that a method executes a query written
 * in Jakarta Data Query Language (JDQL) or Jakarta Persistence Query Language (JPQL).
 * A Jakarta Data provider is not required to support the complete JPQL language,
 * which targets relational data stores.</p>
 *
 * <p>Each parameter of the annotated method must either:</p>
 * <ul>
 * <li>have exactly the same name (the parameter name in the Java source, or a name
 *     assigned by {@link Param @Param}) and type as a named parameter of the query,</li>
 * <li>have exactly the same type and position within the parameter list of the method
 *     as a positional parameter of the query, or</li>
 * <li>be of type {@code Limit}, {@code Order}, {@code PageRequest}, or {@code Sort}.</li>
 * </ul>
 *
 * <p>The {@link Param} annotation associates a method parameter with a named parameter.
 * The {@code Param} annotation is unnecessary when the method parameter name matches the
 * name of a named parameter and the application is compiled with the {@code -parameters}
 * compiler option making parameter names available at runtime.</p>
 *
 * <pre>
 * // example using named parameters
 * &#64;Query("where age between :min and :max order by age")
 * List&lt;Person&gt; peopleInAgeRange(int min, int max);
 * </pre>
 *
 * <pre>
 * // example using an ordinal parameter
 * &#64;Query("where ssn = ?1 and deceased = false")
 * Optional&lt;Person&gt; person(String ssn);
 * </pre>
 *
 * <p>Refer to the {@linkplain Query API documentation} for {@code @Query} for further
 * information.</p>
 *
 * <h2>Query by Method Name</h2>
 *
 * <p>The <em>Query by Method Name</em> pattern translates the name of the
 * repository method into a query. The repository method must not include the
 * {@code @Find} annotation, {@code @Query} annotation, or any life cycle
 * annotations on the method, and it must not include any data access related
 * annotations on the method parameters. The method name must be composed of
 * one or more clauses that specify the query's action and optionally any
 * restrictions for matching and ordering of results. The following table lists
 * the method name prefixes available and shows an example query for each.</p>
 *
 * <table id="methodNamePrefixes" style="width: 100%">
 * <caption><b>Query By Method Name</b></caption>
 * <tr style="background-color:#ccc">
 * <td style="vertical-align: top; width: 10%"><b>Prefix</b></td>
 * <td style="vertical-align: top; width: 25%"><b>Description</b></td>
 * <td style="vertical-align: top; width: 65%"><b>Example</b></td>
 * </tr>
 *
 * <tr style="vertical-align: top"><td>{@code count}</td>
 * <td>counts the number of entities</td>
 * <td>{@code countByAgeGreaterThanEqual(ageLimit)}</td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code delete}</td>
 * <td>for delete operations</td>
 * <td>{@code deleteByStatus("DISCONTINUED")}</td></tr>
 *
 * <tr style="vertical-align: top"><td>{@code exists}</td>
 * <td>for determining existence</td>
 * <td>{@code existsByYearHiredAndWageLessThan(2022, 60000)}</td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code find}</td>
 * <td>for find operations</td>
 * <td>{@code findByHeightBetweenOrderByAge(minHeight, maxHeight)}</td></tr>
 * </table>
 *
 * <p>The method name must begin with an <em>action clause</em> that starts with a
 * prefix ({@code count}, {@code delete}, {@code exists}, or {@code find}) that
 * specifies the action of the query. The {@code find} prefix can optionally be
 * followed by the keyword {@code First} and 0 or more digits, which limit the
 * number of results retrieved. The remaining portion of the action clause is
 * ignored text that must not contain reserved keywords.</p>
 *
 * <p>A <em>restriction clause</em> consisting of the {@code By} keyword
 * followed by one or more conditions can optionally follow the action clause.
 * Multiple conditions must be delimited by the {@code And} or {@code Or} keyword.
 * Each condition consists of a case insensitive entity attribute name, optionally
 * followed by the {@code IgnoreCase} keyword (for text-typed attributes), optionally
 * followed by the {@code Not} keyword, optionally followed by a condition operator
 * keyword such as {@code StartsWith}. The equality condition is implied when no
 * condition operator keyword is present. Most of the condition operations, such as
 * {@code Like} or {@code LessThan}, correspond to a single method parameter. The
 * exceptions to this rule are {@code Between}, which corresponds to two method
 * parameters, and {@code Null}, {@code True}, and {@code False}, which require
 * no method parameters.</p>
 *
 * <p>A {@code find} query can optionally end with an <em>order</em>,
 * consisting of the {@code OrderBy} keyword and one or more pairings of case
 * insensitive entity attribute name followed by the {@code Asc} or {@code Desc}
 * keyword, indicating ascending or descending sort. In the case of a single entity
 * attribute, the paired keyword can be omitted, in which case ascending sort is
 * implied.</p>
 *
 * <p>Key-value and Wide-Column databases raise {@link UnsupportedOperationException}
 * for queries on attributes other than the identifier/key.</p>
 *
 * <h3>Reserved keywords for Query by Method Name</h3>
 *
 * <table style="width: 100%">
 * <caption><b>Reserved for Predicate</b></caption>
 * <tr style="background-color:#ccc">
 * <td style="vertical-align: top"><b>Keyword</b></td>
 * <td style="vertical-align: top"><b>Applies to</b></td>
 * <td style="vertical-align: top"><b>Description</b></td>
 * <td style="vertical-align: top"><b>Example</b></td>
 * <td style="vertical-align: top"><b>Unavailable In</b></td>
 * </tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code And}</td>
 * <td>conditions</td>
 * <td>Requires both conditions to be satisfied in order to match an entity.</td>
 * <td>{@code findByNameLikeAndPriceLessThanEqual(namePattern, maxPrice)}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column</td></tr>
 *
 * <tr style="vertical-align: top"><td>{@code Between}</td>
 * <td>sortable basic types</td>
 * <td>Requires that the entity's attribute value be within the range specified by two parameters,
 * inclusive of the parameters. The minimum is listed first, then the maximum.</td>
 * <td>{@code findByAgeBetween(minAge, maxAge)}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column</td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code Contains}</td>
 * <td>strings</td>
 * <td>Requires that a substring of the entity's attribute value
 * matches the parameter value, which can be a pattern.</td>
 * <td>{@code findByNameContains(middleName)}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column<br>Document<br>Graph</td></tr>
 *
 * <tr style="vertical-align: top"><td>{@code EndsWith}</td>
 * <td>strings</td>
 * <td>Requires that the characters at the end of the entity's attribute value
 * match the parameter value, which can be a pattern.</td>
 * <td>{@code findByNameEndsWith(surname)}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column<br>Document<br>Graph</td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code False}</td>
 * <td>boolean</td>
 * <td>Requires that the entity's attribute value has a boolean value of false.</td>
 * <td>{@code findByCanceledFalse()}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column</td></tr>
 *
 * <tr style="vertical-align: top"><td>{@code GreaterThan}</td>
 * <td>sortable basic types</td>
 * <td>Requires that the entity's attribute value be larger than the parameter value.</td>
 * <td>{@code findByStartTimeGreaterThan(startedAfter)}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column</td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code GreaterThanEqual}</td>
 * <td>sortable basic types</td>
 * <td>Requires that the entity's attribute value be at least as big as the parameter value.</td>
 * <td>{@code findByAgeGreaterThanEqual(minimumAge)}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column</td></tr>
 *
 * <tr style="vertical-align: "><td>{@code IgnoreCase}</td>
 * <td>strings</td>
 * <td>Requires case insensitive comparison. For query conditions
 * as well as ordering, the {@code IgnoreCase} keyword can be
 * specified immediately following the entity attribute name.</td>
 * <td>{@code countByStatusIgnoreCaseNotLike("%Delivered%")}
 * <br>{@code findByZipcodeOrderByStreetIgnoreCaseAscHouseNumAsc(55904)}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column<br>Document<br>Graph</td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code In}</td>
 * <td>sortable basic types</td>
 * <td>Requires that the entity attribute value belong to the {@link Set} that is
 * the parameter value.</td>
 * <td>{@code findByMonthIn(Set.of(Month.MAY, Month.JUNE))}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column<br>Document<br>Graph</td></tr>
 *
 * <tr style="vertical-align: top"><td>{@code LessThan}</td>
 * <td>sortable basic types</td>
 * <td>Requires that the entity's attribute value be less than the parameter value.</td>
 * <td>{@code findByStartTimeLessThan(startedBefore)}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column</td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code LessThanEqual}</td>
 * <td>sortable basic types</td>
 * <td>Requires that the entity's attribute value be at least as small as the parameter value.</td>
 * <td>{@code findByAgeLessThanEqual(maximumAge)}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column</td></tr>
 *
 * <tr style="vertical-align: top"><td>{@code Like}</td>
 * <td>strings</td>
 * <td>Requires that the entity's attribute value match the parameter value, which can be a pattern.</td>
 * <td>{@code findByNameLike(namePattern)}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column<br>Document<br>Graph</td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code Not}</td>
 * <td>condition</td>
 * <td>Negates a condition.</td>
 * <td>{@code deleteByNameNotLike(namePattern)}
 * <br>{@code findByStatusNot("RUNNING")}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column</td></tr>
 *
 * <tr style="vertical-align: top"><td>{@code Null}</td>
 * <td>nullable types</td>
 * <td>Requires that the entity's attribute has a null value.</td>
 * <td>{@code findByEndTimeNull()}
 * <br>{@code findByAgeNotNull()}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column<br>Document<br>Graph</td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code Or}</td>
 * <td>conditions</td>
 * <td>Requires at least one of the two conditions to be satisfied in order to match an entity.</td>
 * <td>{@code findByPriceLessThanEqualOrDiscountGreaterThanEqual(maxPrice, minDiscount)}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column</td></tr>
 *
 * <tr style="vertical-align: top"><td>{@code StartsWith}</td>
 * <td>strings</td>
 * <td>Requires that the characters at the beginning of the entity's attribute value
 * match the parameter value, which can be a pattern.</td>
 * <td>{@code findByNameStartsWith(firstTwoLetters)}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column<br>Document<br>Graph</td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code True}</td>
 * <td>boolean</td>
 * <td>Requires that the entity's attribute value has a boolean value of true.</td>
 * <td>{@code findByAvailableTrue()}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column</td></tr>
 *
 * </table>
 *
 * <br><br>
 *
 * <table style="width: 100%">
 * <caption><b>Reserved for Subject</b></caption>
 * <tr style="background-color:#ccc">
 * <td style="vertical-align: top; width: 12%"><b>Keyword</b></td>
 * <td style="vertical-align: top; width: *%"><b>Applies to</b></td>
 * <td style="vertical-align: top; width: *"><b>Description</b></td>
 * <td style="vertical-align: top; width: 48%"><b>Example</b></td>
 * <td style="vertical-align: top; width: *"><b>Unavailable In</b></td>
 * </tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code First}</td>
 * <td>find...By</td>
 * <td>Limits the amount of results that can be returned by the query
 * to the number that is specified after {@code First},
 * or absent that to a single result.</td>
 * <td>{@code findFirst25ByYearHiredOrderBySalaryDesc(int yearHired)}
 * <br>{@code findFirstByYearHiredOrderBySalaryDesc(int yearHired)}</td>
 * <td style="font-family:sans-serif; font-size:0.8em">Key-value<br>Wide-Column<br>Document<br>Graph</td></tr>
 * </table>
 *
 * <br><br>
 *
 * <table style="width: 100%">
 * <caption><b>Reserved for Order Clause</b></caption>
 * <tr style="background-color:#ccc">
 * <td style="vertical-align: top"><b>Keyword</b></td>
 * <td style="vertical-align: top"><b>Description</b></td>
 * <td style="vertical-align: top"><b>Example</b></td>
 * </tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code Asc}</td>
 * <td>Specifies ascending sort order for {@code findBy} queries</td>
 * <td>{@code findByAgeOrderByFirstNameAsc(age)}</td></tr>
 *
 * <tr style="vertical-align: top"><td>{@code Desc}</td>
 * <td>Specifies descending sort order for {@code findBy} queries</td>
 * <td>{@code findByAuthorLastNameOrderByYearPublishedDesc(surname)}</td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code OrderBy}</td>
 * <td>Sorts results of a {@code findBy} query according to one or more entity attributes.
 * Multiple attributes are delimited by {@code Asc} and {@code Desc},
 * which indicate ascending and descending sort direction.
 * Precedence in sorting is determined by the order in which attributes are listed.</td>
 * <td>{@code findByStatusOrderByYearHiredDescLastNameAsc(empStatus)}</td></tr>
 *
 * </table>
 *
 * <p>Key-value and Wide-Column databases raise {@link UnsupportedOperationException}
 * if an order clause is present.</p>
 *
 * <h3>Reserved for future use</h3>
 * <p>
 * The specification does not define behavior for the following keywords, but reserves
 * them as keywords that must not be used as entity attribute names when using
 * Query by Method Name. This gives the specification the flexibility to add them in
 * future releases without introducing breaking changes to applications.
 * </p>
 * <p>
 * Reserved for query conditions: {@code AbsoluteValue}, {@code CharCount},
 * {@code ElementCount}, {@code Empty},
 * {@code Rounded}, {@code RoundedDown}, {@code RoundedUp}, {@code Trimmed},
 * {@code WithDay}, {@code WithHour}, {@code WithMinute}, {@code WithMonth},
 * {@code WithQuarter}, {@code WithSecond}, {@code WithWeek}, {@code WithYear}.
 * </p>
 * <p>
 * Reserved for {@code find} and {@code count}: {@code Distinct}.
 * </p>
 * <p>
 * Reserved for updates: {@code Add}, {@code Divide}, {@code Multiply}, {@code Set}, {@code Subtract}.
 * </p>
 *
 * <h3>Wildcard characters</h3>
 * <p>
 * Wildcard characters for patterns are determined by the data access provider.
 * For Jakarta Persistence providers, {@code _} matches any one character
 * and {@code %} matches 0 or more characters.
 * </p>
 *
 * <h3>Logical operator precedence</h3>
 * <p>
 * For relational databases, the logical operator {@code And}
 * is evaluated on conditions before {@code Or} when both are specified
 * on the same method. Precedence for other database types is limited to
 * the capabilities of the database.
 * </p>
 *
 * <h3>Return types for Query by Method Name</h3>
 *
 * <p>The following is a table of valid return types.
 * The <b>Method</b> column shows name patterns for Query by Method Name.</p>
 *
 * <table style="width: 100%">
 * <caption><b>Return Types for Query by Method Name</b></caption>
 * <tr style="background-color:#ccc">
 * <td style="vertical-align: top"><b>Method</b></td>
 * <td style="vertical-align: top"><b>Return Types</b></td>
 * <td style="vertical-align: top"><b>Notes</b></td>
 * </tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code count}</td>
 * <td>{@code long}</td>
 * <td></td></tr>
 *
 * <tr style="vertical-align: top"><td>{@code delete}</td>
 * <td>{@code void},
 * <br>{@code long},
 * <br>{@code int}</td>
 * <td></td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code exists}</td>
 * <td>{@code boolean}</td>
 * <td>For determining existence</td></tr>
 *
 * <tr style="vertical-align: top"><td>{@code find}</td>
 * <td>{@code E},
 * <br>{@code Optional<E>}</td>
 * <td>For queries returning a single item (or none)</td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code find}</td>
 * <td>{@code E[]},
 * <br>{@code List<E>}</td>
 * <td>For queries where it is possible to return more than 1 item</td></tr>
 *
 * <tr style="vertical-align: top"><td>{@code find}</td>
 * <td>{@code Stream<E>}</td>
 * <td>The caller must call {@link java.util.stream.BaseStream#close() close}
 * for every stream returned by the repository method</td></tr>
 *
 * <tr style="vertical-align: top; background-color:#eee"><td>{@code find} accepting {@link PageRequest}</td>
 * <td>{@code Page<E>}, {@code CursoredPage<E>}</td>
 * <td>For use with pagination</td></tr>
 *
 * </table>
 *
 * <h2>Parameter-based automatic query methods</h2>
 *
 * <p>The {@link Find} annotation indicates that the repository method is
 * a parameter-based automatic query method. The {@link Delete} annotation
 * also indicates a parameter-based automatic query method when the
 * method has no entity type parameters. In these cases, the method name
 * does not determine the semantics of the method, and the query conditions
 * are determined by the method parameters.</p>
 *
 * <p>Each parameter of the annotated method must either:</p>
 * <ul>
 * <li>have exactly the same type and name (the parameter name in the Java
 *     source, or a name assigned by {@link By @By}) as an attribute
 *     of the entity class, or</li>
 * <li>be of type {@link jakarta.data.Limit}, {@link jakarta.data.Sort},
 *     {@link jakarta.data.Order}, or {@link jakarta.data.page.PageRequest}
 *     - if the repository method is annotated with {@link Find}.</li>
 * </ul>
 *
 * <p>A parameter may be annotated with the {@link By} annotation to specify
 * the name of the entity attribute that the argument is to be compared with.
 * If the {@link By} annotation is missing, the method parameter name must
 * match the name of an entity attribute and the repository must be compiled
 * with the {@code -parameters} compiler option so that parameter names are
 * available at runtime.</p>
 *
 * <p>Each parameter determines a query condition, and each such condition
 * is an equality condition. All conditions must match for a record to
 * satisfy the query.</p>
 *
 * <pre>
 * &#64;Find
 * &#64;OrderBy("lastName")
 * &#64;OrderBy("firstName")
 * List&lt;Person&gt; peopleByAgeAndNationality(int age, Country nationality);
 * </pre>
 *
 * <pre>
 * &#64;Find
 * Optional&lt;Person&gt; person(String ssn);
 * </pre>
 *
 * <p>The {@code _} character may be used in a method parameter name to
 * reference an embedded attribute.</p>
 *
 * <pre>
 * &#64;Find
 * &#64;OrderBy("address.zip")
 * Stream&lt;Person&gt; peopleInCity(String address_city);
 * </pre>
 *
 * <p>The following examples illustrate the difference between Query By Method
 * Name and parameter-based automatic query methods. Both methods accept the
 * same parameters and have the same behavior.</p>
 *
 * <pre>
 * // Query by Method Name
 * Vehicle[] findByMakeAndModelAndYear(String makerName, String model, int year, Sort&lt;?&gt;... sorts);
 *
 * // parameter-based conditions
 * &#64;Find
 * Vehicle[] searchFor(String make, String model, int year, Sort&lt;?&gt;... sorts);
 * </pre>
 *
 * <p>For further information, refer to the {@linkplain Find API documentation}
 * for {@code @Find}.</p>
 *
 * <h2>Special parameters</h2>
 *
 * <p>A repository method annotated {@link Query @Query}, {@link Find @Find} or
 * following the <em>Query by Method Name</em> pattern may have <em>special
 * parameters</em> of type {@link Limit}, {@link Order}, {@link Sort}, or
 * {@link PageRequest} if the method return type indicates that the method may
 * return multiple entities. Special parameters occur after parameters related
 * to query conditions and JDQL query parameters, and enable capabilities such
 * as pagination, limits, and sorting.</p>
 *
 * <h3>Limits</h3>
 *
 * <p>The number of results returned by a single invocation of a repository
 * find method may be limited by adding a parameter of type {@link Limit}.
 * The results may even be limited to a positioned range. For example,</p>
 *
 * <pre>
 * &#64;Query("WHERE (fullPrice - salePrice) / fullPrice &gt;= ?1 ORDER BY salePrice DESC, id ASC")
 * Product[] highlyDiscounted(float minPercentOff, Limit limit);
 *
 * ...
 * first50 = products.highlyDiscounted(0.30, Limit.of(50));
 * ...
 * second50 = products.highlyDiscounted(0.30, Limit.range(51, 100));
 * </pre>
 *
 * <h3>Pagination</h3>
 *
 * <p>A repository find method with a parameter of type {@link PageRequest}
 * allows its results to be split and retrieved in pages. For example,</p>
 *
 * <pre>
 * Product[] findByNameLikeOrderByAmountSoldDescIdAsc(
 *                 String pattern, PageRequest pageRequest);
 * ...
 * page1 = products.findByNameLikeOrderByAmountSoldDescIdAsc(
 *                 "%phone%", PageRequest.ofSize(20));
 * </pre>
 *
 * <p>When using pagination, always ensure that the ordering is consistent
 * across invocations. One way to achieve this is to include the unique
 * identifier in the sort criteria.</p>
 *
 * <h3>Sorting</h3>
 *
 * <p>When a page is requested with a {@code PageRequest}, dynamic sorting
 * criteria may be supplied by passing instances of {@link Sort} or {@link Order}.
 * For example,</p>
 *
 * <pre>
 * Product[] findByNameLikeAndPriceBetween(String pattern,
 *                                         float minPrice,
 *                                         float maxPrice,
 *                                         PageRequest pageRequest,
 *                                         Order&lt;Product&gt; order);
 *
 * ...
 * PageRequest page1Request = PageRequest.ofSize(25);
 *
 * page1 = products.findByNameLikeAndPriceBetween(
 *                 namePattern, minPrice, maxPrice, page1Request,
 *                 Order.by(Sort.desc("price"), Sort.asc("id"));
 * </pre>
 *
 * <p>To supply sort criteria dynamically without using pagination, an
 * instance of {@link Order} may be populated with one or more instances
 * of {@link Sort} and passed to the repository find method. For example,</p>
 *
 * <pre>
 * Product[] findByNameLike(String pattern, Limit max, Order&lt;Product&gt; sortBy);
 *
 * ...
 * found = products.findByNameLike(namePattern, Limit.of(25),
 *                                 Order.by(Sort.desc("price"),
 *                                          Sort.desc("amountSold"),
 *                                          Sort.asc("id")));
 * </pre>
 *
 * <p>Generic, untyped {@link Sort} criteria can be supplied directly to a
 * repository method with a variable arguments {@code Sort<?>...} parameter.
 * For example,</p>
 *
 * <pre>
 * Product[] findByNameLike(String pattern, Limit max, {@code Sort<?>...} sortBy);
 *
 * ...
 * found = products.findByNameLike(namePattern, Limit.of(25),
 *                                 Sort.desc("price"),
 *                                 Sort.desc("amountSold"),
 *                                 Sort.asc("name"));
 * </pre>
 *
 * <h2>Returning subsets of entity attributes</h2>
 *
 * <p>In addition to retrieving results that are entities, repository find methods
 * can be written to retrieve single entity attribute results, as well as
 * multiple entity attribute results (represented as a Java record).</p>
 *
 * <h3>Single entity attribute result type</h3>
 *
 * <p>The {@link Select} annotation chooses the entity attribute.
 * The result type within the repository method return type must be consistent
 * with the entity attribute type. For example, if a {@code Person} entity
 * has attributes including {@code year}, {@code month}, {@code day}, and
 * {@code precipitation}, of which the latter is of type {@code float},</p>
 *
 * <pre>
 * &#64;Find(Weather.class)
 * &#64;Select("precipitation")
 * &#64;OrderBy("precipitation")
 * List&lt;Float&gt; precipitationIn(&#64;By("month") Month monthOfYear,
 *                             &#64;By("year") int year);
 * </pre>
 *
 * <h3>Multiple entity attributes result type</h3>
 *
 * <p>The repository method return type includes a Java record to represent a
 * subset of entity attributes. If the record component names do not match the
 * entity attribute names, use the {@link Select} annotation to indicate the
 * entity attribute name. For example, if a {@code Person} entity has attributes
 * {@code ssn}, {@code firstName}, {@code middleName}, and {@code lastName},</p>
 *
 * <pre>
 * public record Name(String firstName,
 *                    String middleName,
 *                    &#64;Select("lastName") String surname) {}
 *
 * &#64;Find(Person.class)
 * Optional&lt;Name&gt; getName(&#64;By("ssn") long socialSecurityNum);
 * </pre>
 *
 * <p>The entity class value that is supplied to the {@link Find} annotation can
 * be omitted if it is the same as the primary entity type of the repository.</p>
 *
 * <h2>Repository default methods</h2>
 *
 * <p>A repository interface may declare any number of {@code default} methods
 * with user-written implementations.</p>
 *
 * <h2>Resource accessor methods</h2>
 *
 * <p>In advanced scenarios, the application program might make direct use of
 * some underlying resource acquired by the Jakarta Data provider, such as a
 * {@code javax.sql.DataSource}, {@code java.sql.Connection}, or even an
 * {@code jakarta.persistence.EntityManager}.</p>
 *
 * <p>To expose access to an instance of such a resource, the repository
 * interface may declare an accessor method, a method with no parameters
 * whose return type is the type of the resource, for example, one of the
 * types listed above. When this method is called, the Jakarta Data provider
 * supplies an instance of the requested type of resource.</p>
 *
 * <p>For example,</p>
 *
 * <pre>
 * &#64;Repository
 * public interface Cars extends BasicRepository&lt;Car, Long&gt; {
 *     ...
 *
 *     EntityManager getEntityManager();
 *
 *     default Car[] advancedSearch(SearchOptions filter) {
 *         EntityManager em = getEntityManager();
 *         ... use entity manager
 *         return results;
 *     }
 * }
 * </pre>
 *
 * <p>If the resource type inherits from {@link AutoCloseable} and the
 * accessor method is called from within an invocation of a default method
 * of the repository, the Jakarta Data provider automatically closes the
 * resource after the invocation of the default method ends. On the other
 * hand, if the accessor method is called from outside the scope of a
 * default method of the repository, it is not automatically closed, and
 * the application programmer is responsible for closing the resource
 * instance.</p>
 *
 * <h2>Precedence of repository methods</h2>
 *
 * <p>The following order, with the lower number having higher precedence,
 * is used to interpret the meaning of repository methods.</p>
 *
 * <ol>
 * <li>If the method is a Java {@code default} method, then the provided
 *     implementation is used.</li>
 * <li>If a method has a <em>resource accessor method</em> return type
 *     recognized by the Jakarta Data provider, then the method is
 *     implemented as a resource accessor method.</li>
 * <li>If a method is annotated with a <em>query annotation</em>
 *     recognized by the Jakarta Data provider, such as {@link Query},
 *     then the method is implemented to execute the query specified by
 *     the query annotation.</li>
 * <li>If the method is annotated with an automatic query annotation,
 *     such as {@link Find}, or with a lifecycle annotation declaring
 *     the type of operation, for example, with {@link Insert},
 *     {@link Update}, {@link Save}, or {@link Delete}, and the provider
 *     recognizes the annotation, then the annotation determines how the
 *     method is implemented.</li>
 * <li>If a method is named according to the conventions of <em>Query by
 *     Method Name</em>, then the implementation follows the Query by
 *     Method Name pattern.</li>
 * </ol>
 *
 * <p>A repository method which does not fit any of the listed patterns
 * and is not handled as a vendor-specific extension must either cause
 * an error at build time or raise {@link UnsupportedOperationException}
 * at runtime.</p>
 *
 * <h2>Identifying the type of entity</h2>
 *
 * <p>Most repository methods perform operations related to a type of entity.
 * In some cases, the entity type is explicit within the signature of the
 * repository method, and in other cases, such as {@code countBy...} and
 * {@code existsBy...} the entity type cannot be determined from the method
 * signature and a primary entity type must be defined for the repository.</p>
 *
 * <h3>Methods where the entity type is explicitly specified</h3>
 *
 * <p>In the following cases, the entity type is determined by the signature
 * of the repository method.</p>
 *
 * <ul>
 * <li>For repository methods annotated with {@link Insert}, {@link Update},
 *     {@link Save}, or {@link Delete} where the method parameter type is a
 *     type, an array of a type, or is parameterized with a type annotated
 *     as an entity, such as {@code MyEntity}, {@code MyEntity[]}, or
 *     {@code List<MyEntity>}, the entity type is determined by the method
 *     parameter type.</li>
 * <li>For repository methods annotated with {@link Find} where the
 *     {@link Find#value value} is a valid entity class.</li>
 * <li>For {@code find} and {@code delete} methods where the return type is
 *     a type, an array of a type, or is parameterized with a type annotated
 *     as an entity, such as {@code MyEntity}, {@code MyEntity[]}, or
 *     {@code Page<MyEntity>}, the entity type is determined by the method
 *     return type.</li>
 * </ul>
 *
 * <h3>Identifying a primary entity type:</h3>
 *
 * <p>The following precedence, from highest to lowest, is used to determine
 * a primary entity type for a repository.</p>
 *
 * <ol>
 * <li>The primary entity type for a repository interface may be specified
 * explicitly by having the repository interface inherit a superinterface
 * like {@link CrudRepository}, where the primary entity type is the argument
 * to the first type parameter of the superinterface. For example,
 * {@code Product}, in,
 * <pre>
 * &#64;Repository
 * public interface Products extends CrudRepository&lt;Product, Long&gt; {
 *     // applies to the primary entity type: Product
 *     long countByPriceLessThan(float max);
 * }
 * </pre>
 * </li>
 * <li>Otherwise, if the repository declares lifecycle methods&mdash;that is,
 * has methods annotated with a lifecycle annotation like {@link Insert},
 * {@link Update}, {@link Save}, or {@link Delete}, where the method
 * parameter type is a type, an array of a type, or is parameterized with a
 * type annotated as an entity&mdash;and all of these methods share the same
 * entity type, then the primary entity type for the repository is that entity
 * type. For example,
 * <pre>
 * &#64;Repository
 * public interface Products {
 *     &#64;Insert
 *     List&lt;Product&gt; add(List&lt;Product&gt; p);
 *
 *     &#64;Update
 *     Product modify(Product p);
 *
 *     &#64;Save
 *     Product[] save(Product... p);
 *
 *     // applies to the primary entity type: Product
 *     boolean existsByName(String name);
 * }
 * </pre>
 * </li>
 * </ol>
 *
 * <h2>Jakarta Validation</h2>
 *
 * <p>When a Jakarta Validation provider is present, constraints that are defined on
 * repository method parameters and return values are validated according to the section,
 * "Method and constructor validation", of the Jakarta Validation specification.</p>
 *
 * <p>The {@code jakarta.validation.Valid} annotation opts in to cascading validation,
 * causing constraints within the objects that are supplied as parameters
 * or returned as results to also be validated.</p>
 *
 * <p>Repository methods raise {@code jakarta.validation.ConstraintViolationException}
 * if validation fails.</p>
 *
 * <p>The following is an example of method validation, where the
 * parameter to {@code findByEmailIn} must not be the empty set,
 * and cascading validation, where the {@code Email} and {@code NotNull} constraints
 * on the entity that is supplied to {@code save} are validated,</p>
 *
 * <pre>
 * import jakarta.validation.Valid;
 * import jakarta.validation.constraints.Email;
 * import jakarta.validation.constraints.NotEmpty;
 * import jakarta.validation.constraints.NotNull;
 * ...
 *
 * &#64;Repository
 * public interface AddressBook extends DataRepository&lt;Contact, Long&gt; {
 *
 *     List&lt;Contact&gt; findByEmailIn(&#64;NotEmpty Set&lt;String&gt; emails);
 *
 *     &#64;Save
 *     void save(&#64;Valid Contact c);
 * }
 *
 * &#64;Entity
 * public class Contact {
 *     &#64;Email
 *     &#64;NotNull
 *     public String email;
 *     &#64;Id
 *     public long id;
 *     ...
 * }
 * </pre>
 *
 *
 * <h2>Jakarta Interceptors</h2>
 *
 * <p>A repository interface or method of a repository interface may be annotated with an
 * interceptor binding annotation. In the Jakarta EE environment, or in any other environment
 * where Jakarta Interceptors is available and integrated with Jakarta CDI, the repository
 * implementation is instantiated by the CDI bean container, and the interceptor binding type
 * is declared {@code @Inherited}, the interceptor binding annotation is inherited by the
 * repository implementation, and the interceptors bound to the annotation are applied
 * automatically by the implementation of Jakarta Interceptors.</p>
 *
 * <h2>Jakarta Transactions</h2>
 *
 * <p>When Jakarta Transactions is available, repository methods can participate in global
 * transactions. If a global transaction is active on the thread of execution in which a
 * repository method is called, and the data source backing the repository is capable of
 * transaction enlistment, then the repository operation is performed within the context of
 * the global transaction.</p>
 *
 * <p>The repository operation must not not commit or roll back a transaction which was
 * already associated with the thread in which the repository operation was called, but it
 * might cause the transaction to be marked for rollback if the repository operation fails,
 * that is, it may set the transaction status to {@code Status.STATUS_MARKED_ROLLBACK}.</p>
 *
 * <p>A repository interface or method of a repository interface may be marked with the
 * annotation {@code jakarta.transaction.Transactional}. When a repository operation marked
 * {@code @Transactional} is called in an environment where both Jakarta Transactions and
 * Jakarta CDI are available, the semantics of this annotation are observed during execution
 * of the repository operation.</p>
 */
module jakarta.data {
    exports jakarta.data;
    exports jakarta.data.metamodel;
    exports jakarta.data.metamodel.impl;
    exports jakarta.data.metamodel.restrict;
    exports jakarta.data.page;
    exports jakarta.data.page.impl;
    exports jakarta.data.repository;
    exports jakarta.data.exceptions;
    opens jakarta.data.repository;
    exports jakarta.data.spi;
}