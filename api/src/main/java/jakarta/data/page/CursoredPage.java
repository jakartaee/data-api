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
 *  SPDX-License-Identifier: Apache-2.0
 */
package jakarta.data.page;

import jakarta.data.repository.OrderBy;
import jakarta.data.Order;
import jakarta.data.Sort;
import java.util.NoSuchElementException;

/**
 * <p>A page of data retrieved to satisfy a given page request, with a
 * {@linkplain PageRequest.Cursor cursor} for each result on the page.
 * A repository method which returns the type {@code CursoredPage} uses
 * <em>cursor-based pagination</em> to determine page boundaries.</p>
 *
 * <p>Compared to offset-based pagination, cursor-based pagination reduces
 * the possibility of missed or duplicate results when records are
 * inserted, deleted, or updated in the database between page requests.
 * Cursor-based pagination is possible when a query result set has a
 * well-defined total order, that is, when the results are sorted by a
 * list of entity fields which forms a unique key on the result set.
 * This list of entity fields must be the entity fields of the combined
 * sort criteria of the repository method, in the same order of precedence.
 * This could just be the identifier field of the entity, or it might be
 * some other combination of fields which uniquely identifies each query
 * result.</p>
 *
 * <p>When cursor-based pagination is used, a {@linkplain #nextPageRequest
 * next page request} is made relative to the last entity of the current
 * page and a {@linkplain #previousPageRequest previous page request} is
 * made relative to the first entity of the current page. Alternatively,
 * a page request might be made relative to an arbitrary starting point
 * in the result set, that is, with an arbitrary value of the key.</p>
 *
 * <p>The key for a given element of the result set is represented by an
 * instance of {@link jakarta.data.page.PageRequest.Cursor Cursor}.</p>
 *
 * <p>To use cursor-based pagination, declare a repository method with return
 * type {@code CursoredPage} and with a special parameter (after the normal
 * query parameters) of type {@link PageRequest}, for example:</p>
 *
 * <pre>
 * &#64;Find
 * &#64;OrderBy(_Employee.LASTNAME)
 * &#64;OrderBy(_Employee.FIRSTNAME)
 * &#64;OrderBy(_Employee.ID)
 * CursoredPage&lt;Employee&gt; withOvertime(
 *         &#64;By(_Employee.HOURSWORKED) &#64;Is(GREATER_THAN) int fullTimeHours,
 *         PageRequest pageRequest);
 * </pre>
 *
 * <p>In initial page may be requested using an offset-based page request:</p>
 *
 * <pre>
 * page = employees.withOvertime(40, PageRequest.ofSize(50));
 * </pre>
 *
 * <p>The next page may be requested relative to the end of the current page,
 * as follows:</p>
 *
 * <pre>
 * page = employees.withOvertime(40, page.nextPageRequest());
 * </pre>
 *
 * <p>Here, the instance of {@link PageRequest} returned by
 * {@link CursoredPage#nextPageRequest()} is based on a key value encapsulated
 * in an instance of {@link PageRequest.Cursor Cursor} and identifying the last
 * result on the current page.</p>
 *
 * <p>A {@link PageRequest} based on an explicit cursor may be constructed by
 * calling {@link PageRequest#afterCursor(PageRequest.Cursor)}. The key component
 * values of the cursor supplied to this method must match the list of sorting
 * criteria specified by {@link OrderBy} annotations or {@code OrderBy} name
 * pattern of the repository method and the {@link Sort} and {@link Order}
 * parameters of the repository method. For example:</p>
 *
 * <pre>
 * Employee emp = ...
 * PageRequest pageRequest =
 *         PageRequest.ofPage(5)
 *                    .size(50)
 *                    .afterCursor(Cursor.forKey(emp.lastName, emp.firstName, emp.id));
 * page = employees.withOvertime(40, pageRequest);
 * </pre>
 *
 * <p>By making the query for the next page relative to observed values,
 * instead of to a numerical position, cursor-based pagination is less
 * vulnerable to changes made to data in between page requests. Adding or
 * removing entities is possible without causing unexpected missed or
 * duplicate results. Cursor-based pagination does not prevent misses and
 * duplicates if the entity properties which are the sort criteria for
 * existing entities are modified or if an entity is re-added with different
 * sort criteria after having previously been removed.</p>
 *
 * <h2>Cursor-based Pagination with {@code @Query}</h2>
 *
 * <p>Cursor-based pagination involves generating and appending additional
 * restrictions involving the key fields to the {@code WHERE} clause of the
 * query. For this to be possible, a user-provided JDQL or JPQL query must
 * end with a {@code WHERE} clause to which additional conditions may be
 * appended.</p>
 *
 * <p>Sorting criteria must be specified independently of the user-provided
 * query, either via the {@link OrderBy} annotation or, or by passing
 * {@link Sort} or {@link jakarta.data.Order}. For example:</p>
 *
 * <pre>
 * &#64;Query("WHERE ordersPlaced &gt;= ?1 OR totalSpent &gt;= ?2")
 * &#64;OrderBy("zipcode")
 * &#64;OrderBy("birthYear")
 * &#64;OrderBy("id")
 * CursoredPage&lt;Customer&gt; getTopBuyers(int minOrders, float minSpent,
 *                                     PageRequest pageRequest);
 * </pre>
 *
 * <p>Only queries which return entities may be used with cursor-based pagination
 * because cursors are created from the entity attribute values that
 * form the unique key.</p>
 *
 * <h2>Page Numbers and Totals</h2>
 *
 * <p>Page numbers, total numbers of elements across all pages, and total count
 * of pages are not accurate when cursor-based pagination is used and should not
 * be relied upon.</p>
 *
 * <h2>Database Support for Cursor-based Pagination</h2>
 *
 * <p>A repository method with return type {@code CursoredPage} must raise
 * {@link UnsupportedOperationException} if the database itself is not capable
 * of cursor-based pagination.</p>
 *
 * @param <T> the type of elements in this page
 */
public interface CursoredPage<T> extends Page<T> {
    /**
     * Returns a {@link PageRequest.Cursor Cursor} for key values at the
     * specified position.
     *
     * @param index position (0 is first) of a result on the page.
     * @return cursor for key values at the specified position.
     */
    PageRequest.Cursor cursor(int index);

    /**
     * Returns {@code true} when it is possible to navigate to a previous
     * page of results or if it is necessary to request a previous page in
     * order to determine whether there are more previous results.
     * @return {@code false} if the current page is empty or if it is known
     *         that there is not a previous page.
     */
    @Override
    boolean hasPrevious();

    /**
     * <p>Creates a request for the next page in a forward direction from
     * the current page. This method computes a cursor from the last
     * entity of the current page and includes the cursor in the pagination
     * information so that it can be used to obtain the next page in a
     * forward direction according to the sort criteria and relative to that
     * entity.</p>
     *
     * @return pagination information for requesting the next page.
     * @throws NoSuchElementException if the current page is empty or if
     *         it is known that there is no next page.
     *         To avoid this exception, check for a {@code true} result
     *         of {@link #hasNext()} before invoking this method.
     */
    @Override
    PageRequest nextPageRequest();

    /**
     * <p>Creates a request for the previous page in a reverse direction from
     * the current page. This method computes a cursor from the first
     * entity of the current page and includes the cursor in the pagination
     * information so that it can be used to obtain the previous page in a
     * reverse direction to the sort criteria and relative to that entity.
     * Within a single page, results are not reversed and remain ordered
     * according to the sort criteria.</p>
     *
     * <p>Page numbers are not accurate and should not be relied upon when
     * using cursor-based pagination. Jakarta Data providers should aim to at
     * least avoid returning negative or {@code 0} as page numbers when
     * traversing pages in the previous page direction (this might otherwise occur
     * when matching entities are added prior to the first page and the
     * previous page is requested) by assigning a page number of {@code 1}
     * to such pages. This means that there can be multiple consecutive pages
     * numbered {@code 1} and that navigating to the previous page and then
     * forward again cannot be relied upon to return a page number that is
     * equal to the current page number.</p>
     *
     * @return pagination information for requesting the previous page.
     * @throws NoSuchElementException if the current page is empty or if
     *         it is known that there is no previous page.
     *         To avoid this exception, check for a {@code true} result
     *         of {@link #hasPrevious()} before invoking this method.
     */
    @Override
    PageRequest previousPageRequest();
}