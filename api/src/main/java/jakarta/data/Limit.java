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
package jakarta.data;

import jakarta.data.page.PageRequest;

/**
 * <p>Specifies a limit on the number of results retrieved by a repository
 * method. The results of a single invocation of a repository method may
 * be limited to a given {@linkplain #of(int) maximum number of results}
 * or to a given {@linkplain #range(long, long) positioned range} defined
 * in terms of an offset and maximum number of results.</p>
 *
 * <p>A query method of a repository may have a parameter of type
 * {@code Limit} if its return type indicates that it may return multiple
 * entities. The parameter of type {@code Limit} must occur after the
 * method parameters representing regular parameters of the query itself.
 * For example,</p>
 *
 * <pre>
 * &#64;Find
 * Product[] named(&#64;By(_Product.NAME) &#64;Is(LikeIgnoreCase) String namePattern,
 *                 Limit limit,
 *                 Sort&lt;Product&gt;... sorts);
 * 
 * ...
 * mostExpensive50 = products.named(pattern, Limit.of(50), Sort.desc("price"));
 * ...
 * secondMostExpensive50 = products.named(pattern, Limit.range(51, 100), Sort.desc("price"));
 * </pre>
 *
 * <p>A repository method may not be declared with:
 * <ul>
 * <li>more than one parameter of type {@code Limit},</li>
 * <li>a parameter of type {@code Limit} and a parameter of type
 *     {@link PageRequest}, or</li>
 * <li>a parameter of type {@code Limit} in combination with the
 *     {@code First} keyword.
 * </ul>
 *
 * @param maxResults maximum number of results for a query.
 * @param startAt    starting position for query results (1 is the first result).
 */
public record Limit(int maxResults, long startAt) {

    private static final long DEFAULT_START_AT = 1L;

    /**
     * <p>Limits query results. For more descriptive code, use:</p>
     * <ul>
     * <li>{@link #of(int) Limit.of(maxResults)} to cap the number of results.</li>
     * <li>{@link #range(long, long) Limit.range(startAt, endAt)} to limit to a range.</li>
     * </ul>
     *
     * @param maxResults maximum number of results for a query.
     * @param startAt    starting position for query results (1 is the first result).
     */
    public Limit {
        if (startAt < 1) {
            throw new IllegalArgumentException("startAt: " + startAt);
        }
        if (maxResults < 1) {
            throw new IllegalArgumentException("maxResults: " + maxResults);
        }
    }

    // Override to provide method documentation:
    /**
     * <p>Maximum number of results that can be returned for a
     * single invocation of the repository method.</p>
     *
     * @return maximum number of results for a query.
     */
    public int maxResults() {
        return maxResults;
    }

    // Override to provide method documentation:
    /**
     * <p>Offset at which to start when returning query results.
     * The first query result is position {@code 1}.</p>
     *
     * @return offset of the first result.
     */
    public long startAt() {
        return startAt;
    }

    /**
     * <p>Create a limit that caps the number of results at the
     * specified maximum, starting from the first result.</p>
     *
     * @param maxResults maximum number of results.
     * @return limit that can be supplied to a find method
     *         or {@code @Query} method that performs a find operation; will never be {@code null}.
     * @throws IllegalArgumentException if maxResults is less than 1.
     */
    public static Limit of(int maxResults) {
        return new Limit(maxResults, DEFAULT_START_AT);
    }

    /**
     * <p>Create a limit that restricts the results to a range,
     * beginning with the {@code startAt} position and
     * ending after the {@code endAt} position or the
     * position of the final result, whichever comes first.</p>
     *
     * @param startAt position at which to start including results.
     *                The first query result is position 1.
     * @param endAt   position after which to cease including results.
     * @return limit that can be supplied to a find method or
     *         or a {@code @Query} method that performs a find operation; will never be {@code null}.
     * @throws IllegalArgumentException if {@code startAt} is less than 1
     *         or {@code endAt} is less than {@code startAt},
     *         or the range from {@code startAt} to {@code endAt}
     *         exceeds {@link Integer#MAX_VALUE}.
     */
    public static Limit range(long startAt, long endAt) {
        if (endAt < startAt)
            throw new IllegalArgumentException("startAt: " + startAt + ", endAt: " + endAt);

        if (endAt - startAt >= Integer.MAX_VALUE)
            throw new IllegalArgumentException("startAt: " + startAt + ", endAt: " + endAt + ", maxResults > " + Integer.MAX_VALUE);

        return new Limit((int) (endAt - startAt + 1), startAt);
    }
}
