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

import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.repository.OrderBy;

import java.util.List;
import java.util.Optional;

/**
 * <p>A request for a single well-specified page of query results.</p>
 *
 * <p>A query method of a repository may have a parameter of type
 * {@code PageRequest} if its return type indicates that it may return
 * multiple entities, that is, if its return type is an array type,
 * {@link List}, {@code Stream}, {@link Page}, or {@link CursoredPage}.
 * The parameter of type {@code PageRequest} must occur after the method
 * parameters representing regular parameters of the query itself. For
 * example:</p>
 *
 * <pre>
 * &#64;OrderBy("age")
 * &#64;OrderBy("ssn")
 * Person[] findByAgeBetween(int minAge, int maxAge, PageRequest pageRequest);
 * </pre>
 *
 * <p>This method might be called as follows:</p>
 *
 * <pre>
 * var page = people.findByAgeBetween(35, 59,
 *                PageRequest.ofSize(100));
 * var results = page.content();
 * ...
 * while (page.hasNext()) {
 *     page = people.findByAgeBetween(35, 59,
 *                page.nextPageRequest().withoutTotal());
 *     results = page.content();
 *   ...
 * }
 * </pre>
 *
 * <p>A repository method may not be declared with:</p>
 * <ul>
 * <li>more than one parameter of type {@code PageRequest} or {@link Limit},
 * <li>a parameter of type {@code PageRequest} and a parameter of type
 *     {@link Limit}, or</li>
 * <li>a parameter of type {@code PageRequest} in combination with the
 *     keyword {@code First}.</li>
 * </ul>
 */
public interface PageRequest {

    /**
     * Creates a new page request with the given page number and with a default size of 10.
     *
     * @param pageNumber The page number.
     * @return a new instance of {@code PageRequest}. This method never returns {@code null}.
     * @throws IllegalArgumentException when the page number is negative or zero.
     */
    static PageRequest ofPage(long pageNumber) {
        return new Pagination(pageNumber, 10, Mode.OFFSET, null, true);
    }

    /**
     * Creates a new page request without a cursor.
     *
     * @param pageNumber   The page number.
     * @param maxPageSize  The number of query results in a full page.
     * @param requestTotal Indicates whether to retrieve the
     *                     {@linkplain Page#totalElements() total}
     *                     number of elements available across all pages.
     * @return a new instance of {@code PageRequest}. This method never returns {@code null}.
     * @throws IllegalArgumentException when the page number is negative or zero.
     */
    static PageRequest ofPage(long pageNumber, int maxPageSize, boolean requestTotal) {
        return new Pagination(pageNumber, maxPageSize, Mode.OFFSET, null, requestTotal);
    }

    /**
     * Creates a new page request for requesting pages of the specified size,
     * starting with the first page number, which is 1.
     *
     * @param maxPageSize The number of query results in a full page.
     * @return a new instance of {@code PageRequest}. This method never returns {@code null}.
     * @throws IllegalArgumentException when maximum page size is negative or zero.
     */
    static PageRequest ofSize(int maxPageSize) {
        return new Pagination(1, maxPageSize,  Mode.OFFSET, null, true);
    }

    /**
     * <p>Requests {@linkplain CursoredPage cursor-based pagination} in the forward direction,
     * starting after the specified key.</p>
     *
     * @param cursor       cursor with key values, the order and number of
     *                     which must match the {@link OrderBy} annotations or
     *                     {@code OrderBy} name pattern and the {@link Order} and
     *                     {@link Sort} parameters of the repository method to
     *                     which this page request will be supplied.
     * @param pageNumber   The page number.
     * @param maxPageSize  The number of query results in a full page.
     * @param requestTotal Indicates whether to retrieve the
     *                     {@linkplain Page#totalElements() total}
     *                     number of elements available across all pages.
     * @return a new instance of {@code PageRequest} with forward cursor-based pagination.
     *         This method never returns {@code null}.
     * @throws IllegalArgumentException if the cursor is null or has no values.
     */
    static PageRequest afterCursor(Cursor cursor, long pageNumber, int maxPageSize, boolean requestTotal) {
        return new Pagination(pageNumber, maxPageSize, Mode.CURSOR_NEXT, cursor, requestTotal);
    }

    /**
     * <p>Requests {@linkplain CursoredPage cursor-based pagination} in the previous page
     * direction relative to the specified cursor.</p>
     *
     * @param cursor       cursor with key values, the order and number of
     *                     which must match the {@link OrderBy} annotations or
     *                     {@code OrderBy} name pattern and the {@link Order} and
     *                     {@link Sort} parameters of the repository method to
     *                     which this page request will be supplied.
     * @param pageNumber   The page number.
     * @param maxPageSize  The number of query results in a full page.
     * @param requestTotal Indicates whether to retrieve the
     *                     {@linkplain Page#totalElements() total}
     *                     number of elements available across all pages.
     * @return a new instance of {@code PageRequest} with cursor-based pagination
     *         in the previous page direction.
     *         This method never returns {@code null}.
     * @throws IllegalArgumentException if the cursor is null or has no values.
     */
    static PageRequest beforeCursor(Cursor cursor, long pageNumber, int maxPageSize, boolean requestTotal) {
        return new Pagination(pageNumber, maxPageSize, Mode.CURSOR_PREVIOUS, cursor, requestTotal);
    }

    /**
     * <p>Requests {@linkplain CursoredPage cursor-based pagination} in the forward direction,
     * starting after the specified key.</p>
     *
     * @param cursor cursor with key values, the order and number of which must match the
     *        {@link OrderBy} annotations, {@link jakarta.data.Sort} parameters, or
     *        {@code OrderBy} name pattern of the repository method to which
     *        this pagination will be supplied.
     * @return a new instance of {@code PageRequest} with forward cursor-based pagination.
     *         This method never returns {@code null}.
     * @throws IllegalArgumentException if no key values are provided.
     */
    PageRequest afterCursor(Cursor cursor);

    /**
     * <p>Requests {@linkplain CursoredPage cursor-based pagination} in the previous page
     * direction relative to the specified key values.</p>
     *
     * @param cursor cursor with key values, the order and number of which must match the
     *        {@link OrderBy} annotations, {@link jakarta.data.Sort} parameters, or
     *        {@code OrderBy} name pattern of the repository method to which
     *        this pagination will be supplied.
     * @return a new instance of {@code PageRequest} with cursor-based pagination
     *         in the previous page direction. This method never returns {@code null}.
     * @throws IllegalArgumentException if no key values are provided.
     */
    PageRequest beforeCursor(Cursor cursor);

    /**
     * Compares with another instance to determine if both represent the same
     * pagination information.
     *
     * @return true if both instances are of the same class and
     *         represent the same pagination information. Otherwise false.
     */
    @Override
    boolean equals(Object o);

    /**
     * Returns the key values which are the starting point for
     * cursor-based pagination.
     *
     * @return the cursor; {@link Optional#empty()} if using offset pagination.
     */
    Optional<Cursor> cursor();

    /**
     * Returns the type of pagination.
     *
     * @return the type of pagination.
     */
    Mode mode();

    /**
     * Returns the page to be returned.
     *
     * @return the page to be returned.
     */
    long page();

    /**
     * Returns the requested size of each page
     *
     * @return the requested size of each page
     */
    int size();

    /**
     * Indicates that a query method which returns a {@link Page}
     * should retrieve the {@linkplain Page#totalElements() total
     * number of elements} available across all pages. This behavior
     * is enabled by default. To obtain a page request with total
     * retrieval disabled, call {@link #withoutTotal()}.
     * @return {@code true} if the total number of elements should
     *         be retrieved from the database.
     */
    boolean requestTotal();

    /**
     * <p>Creates a new page request with the same pagination information,
     * but with the specified maximum page size. When a page is retrieved
     * from the database, the number of elements in the page must be equal
     * to the maximum page size unless there is an insufficient number of
     * elements to retrieve from the database from the start of the page.</p>
     *
     * @param maxPageSize the number of query results in a full page.
     * @return a new instance of {@code PageRequest}.
     *         This method never returns {@code null}.
     */
    PageRequest size(int maxPageSize);

    /**
     * Returns an otherwise-equivalent page request with
     * {@link #requestTotal()} set to {@code false}, so that
     * totals will not be retrieved from the database. Note
     * that when totals are not retrieved by a repository
     * method with return type {@link Page}, the operations
     * {@link Page#totalElements()} and {@link Page#totalPages()}
     * throw an {@link IllegalStateException} when called.
     * @return a page request with {@link #requestTotal()}
     *         set to {@code false}.
     */
    PageRequest withoutTotal();

    /**
     * Returns an otherwise-equivalent page request with
     * {@link #requestTotal()} set to {@code true}, so that
     * totals will be retrieved from the database.
     * @return a page request with {@link #requestTotal()}
     *         set to {@code true}.
     */
    PageRequest withTotal();

    /**
     * The type of pagination: offset-based or cursor-based, which includes
     * a direction.
     */
    enum Mode {
        /**
         * Indicates forward cursor-based pagination, which follows the
         * direction of the sort criteria, using a cursor that is
         * formed from the key of the last entity on the current page.
         */
        CURSOR_NEXT,

        /**
         * Indicates a request for a page with cursor-based pagination
         * in the previous page direction to the sort criteria, using a cursor
         * that is formed from the key of first entity on the current page.
         * The order of results on each page follows the sort criteria
         * and is not reversed.
         */
        CURSOR_PREVIOUS,

        /**
         * Indicates a request for a page using offset pagination.
         * The starting position for pages is computed as an offset from
         * the first result based on the page number and maximum page size.
         * Offset pagination is used when a cursor is not supplied.
         */
        OFFSET
    }

    /**
     * A cursor that is formed from a key, relative to which a next
     * or previous page can be requested.
     */
    interface Cursor {
        /**
         * Returns whether or not the values that make up the key of this cursor
         * are equal to those of the supplied cursor.
         * Both instances must also have the same cursor implementation class
         * in order to be considered equal.
         *
         * @param cursor a cursor against which to compare.
         * @return true or false.
         */
        @Override
        boolean equals(Object cursor);

        /**
         * Returns the key value at the specified position.
         *
         * @param  index position (0 is first) of the key value to obtain.
         * @return the key value at the specified position.
         * @throws IndexOutOfBoundsException if the index is negative
         *         or greater than or equal to the {@link #size}.
         */
        Object get(int index);

        /**
         * Returns a hash code based on the key values.
         *
         * @return a hash code based on the key values.
         */
        @Override
        int hashCode();

        /**
         * Returns the number of values in the key.
         *
         * @return the number of values in the key.
         */
        int size();

        /**
         * An unmodifiable list of values in the key.
         *
         * @return an unmodifiable list containing the
         *         ordered values
         */
        List<?> elements();

        /**
         * String representation of the cursor, including the number of
         * key values in the cursor but not the values themselves.
         *
         * @return String representation of the cursor.
         */
        @Override
        String toString();

        /**
         * Obtain an instance of {@code Cursor} for the given key.
         * @param key the key
         * @return a new instance of {@code Cursor}
         */
        static Cursor forKey(Object... key) {
            return new PageRequestCursor(key);
        }
    }
}
