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

import jakarta.data.exceptions.OptimisticLockingFailureException;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static jakarta.data.repository.By.ID;

/**
 * <p>A built-in repository supertype for performing basic operations on entities.</p>
 *
 * <p>The type parameters of {@code BasicRepository<T,K>} capture the primary entity type ({@code T}) for the repository
 * and the type of the unique identifier attribute ({@code K}) of the primary entity type.</p>
 *
 * <p>The primary entity type is used for repository methods, such as {@code countBy...} and {@code deleteBy...}, which
 * do not explicitly specify an entity type.</p>
 *
 * <p>Example entity:</p>
 *
 * <pre>
 * {@code @Entity}
 * public class Employee {
 *     {@code @Id}
 *     public int badgeNumber;
 *     public String firstName;
 *     public String lastName;
 *     ...
 * }
 * </pre>
 *
 * <p>Example repository:</p>
 *
 * <pre>
 * {@code @Repository}
 * public interface Employees extends BasicRepository{@code <Employee, Integer>} {
 *
 *     boolean deleteByBadgeNumber(int badgeNum);
 *
 *     ...
 * }
 * </pre>
 *
 * <p>Example usage:</p>
 *
 * <pre>
 * {@code @Inject}
 * Employees employees;
 *
 * ...
 *
 * Employee emp = ...
 * emp = employees.save(emp);
 *
 * boolean deleted = employees.deleteByBadgeNumber(emp.badgeNum);
 *
 * PageRequest{@code <Employee>} pageRequest = PageRequest.of(Employee.class).size(25).sortBy(Sort.asc("name"));
 * Page{@code <Employee>} page = people.findAll(pageRequest);
 * </pre>
 *
 * <p>The module Javadoc provides an {@link jakarta.data/ overview} of Jakarta Data.</p>
 *
 * @param <T> the type of the primary entity class of the repository.
 * @param <K> the type of the unique identifier attribute of the primary entity.
 */
public interface BasicRepository<T, K> extends DataRepository<T, K> {

    /**
     * Saves a given entity to the database. If the entity has an Id or key that exists in the database,
     * the method will update the existing record. Otherwise, it will insert a new record.
     *
     * <p>If the entity has a non-null Id, the method will attempt to
     * update the existing record in the database. If the entity does not exist in the database or has a null Id,
     * then this method will insert a new record into the database.</p>
     *
     * <p>The entity instance that is returned as a result value of this method
     * must be updated with all automatically generated values and incremented values
     * that changed due to the save. After invoking this method, do not continue to use
     * the entity value that is supplied as a parameter. This method makes no guarantees
     * about the state of the entity value that is supplied as a parameter.</p>
     *
     * <p>If the entity uses optimistic locking and the version differs from the version in the database,
     * an {@link OptimisticLockingFailureException} will be thrown.</p>
     *
     * @param entity The entity to be saved. Must not be {@code null}.
     * @param <S> Type of the entity to save.
     * @return The saved entity; never {@code null}.
     * @throws OptimisticLockingFailureException If the entity uses optimistic locking and the version in the
     *         database differs from the version in the entity.
     * @throws NullPointerException If the provided entity is {@code null}.
     */
    @Save
    <S extends T> S save(S entity);

    /**
     * Saves all given entities to the database. If an entity has a non-null Id that exists in the database,
     * the method will update the existing record. Otherwise, it will insert a new record.
     *
     * <p>If an entity has a non-null Id, this method will attempt to update the
     * existing record in the database. If an entity does not exist in the database
     * or has a null Id, then this method inserts a new record into the database.</p>
     *
     * <p>The entity instances that are returned as a result of this method
     * must be updated with all automatically generated values and incremented values
     * that changed due to the save. After invoking this method, do not continue to use
     * the entity values that are supplied in the parameter. This method makes no guarantees
     * about the state of the entity values that are supplied in the parameter.</p>
     *
     * @param entities An iterable of entities.
     * @param <S> Type of entity to save.
     * @return The saved entities; will never be {@code null}.
     * @throws OptimisticLockingFailureException If an entity has a version for optimistic locking
     *         that differs from the version in the database.
     * @throws NullPointerException If either the iterable is null or any element is null.
     */
    @Save
    <S extends T> List<S> saveAll(List<S> entities);

    /**
     * Retrieves an entity by its Id.
     *
     * @param id must not be {@code null}.
     * @return the entity with the given Id or {@link Optional#empty()} if none is found.
     * @throws NullPointerException when the Id is {@code null}.
     */
    @Find
    Optional<T> findById(@By(ID) K id);

    /**
     * Returns whether an entity with the given Id exists.
     *
     * @param id must not be {@code null}.
     * @return {@code true} if an entity with the given Id exists, {@code false} otherwise.
     * @throws NullPointerException when the Id is {@code null}.
     */
    boolean existsById(K id);

    /**
     * Retrieves all persistent entities of the specified type from the database.
     *
     * @return a stream of all entities; will never be {@code null}.
     * @throws UnsupportedOperationException  for Key-Value and Wide-Column databases that are not capable
     * of the {@code findAll} operation.
     */
    @Find
    Stream<T> findAll();

    /**
     * Returns a {@link Page} of entities according to the page request that is provided as the {@link PageRequest} parameter.
     *
     * @param pageRequest the request for a paginated result; must not be {@code null}.
     * @return a page of entities; will never be {@code null}.
     * @throws NullPointerException when {@code pageRequest} is {@code null}.
     * @throws UnsupportedOperationException for Key-Value and Wide-Column databases when the {@link PageRequest.Mode#CURSOR_NEXT}
     * or {@link PageRequest.Mode#CURSOR_PREVIOUS} pagination mode is selected.
     * @see PageRequest.Mode
     */
    @Find
    Page<T> findAll(PageRequest<T> pageRequest);

    /**
     * Returns all instances of the type {@code T} with the given Ids.
     * <p>
     * If some or all Ids are not found, no entities are returned for these Ids.
     * <p>
     * Note that the order of elements in the result is not guaranteed.
     *
     * @param ids must not be {@code null} nor contain any {@code null} values.
     * @return guaranteed to be not {@code null}. The size can be equal or less than the number of given
     * ids.
     * @throws NullPointerException in case the given {@link Iterable ids} or one of its items is {@code null}.
     */
    @Find
    Stream<T> findByIdIn(@By(ID) List<K> ids);

    /**
     * Deletes the entity with the given Id.
     * <p>
     * If the entity is not found in the persistence store it is silently ignored.
     *
     * @param id must not be {@code null}.
     * @throws NullPointerException when the Id is {@code null}.
     */
    @Delete
    void deleteById(@By(ID) K id);

    /**
     * Deletes a given entity. Deletion is performed by matching the Id, and if the entity is
     * versioned (for example, with {@code jakarta.persistence.Version}), then also the version.
     * Other properties of the entity do not need to match.
     *
     * @param entity must not be {@code null}.
     * @throws OptimisticLockingFailureException if the entity is not found in the database for deletion
     *         or has a version for optimistic locking that is inconsistent with the version in the database.
     * @throws NullPointerException when the entity is null
     */
    @Delete
    void delete(T entity);

    /**
     * Deletes all instances of the type {@code T} with the given Ids.
     * <p>
     * Entities that are not found in the persistent store are silently ignored.
     *
     * @param ids must not be {@code null}. Must not contain {@code null} elements.
     * @throws NullPointerException when the iterable is {@code null} or contains {@code null} elements.
     */
    @Delete
    void deleteByIdIn(@By(ID) List<K> ids);

    /**
     * Deletes the given entities. Deletion of each entity is performed by matching the unique identifier,
     * and if the entity is versioned (for example, with {@code jakarta.persistence.Version}), then also
     * the version. Other properties of the entity do not need to match.
     *
     * @param entities Must not be {@code null}. Must not contain {@code null} elements.
     * @throws OptimisticLockingFailureException If an entity is not found in the database for deletion
     *         or has a version for optimistic locking that is inconsistent with the version in the database.
     * @throws NullPointerException If the iterable is {@code null} or contains {@code null} elements.
     */
    @Delete
    void deleteAll(List<? extends T> entities);

}
