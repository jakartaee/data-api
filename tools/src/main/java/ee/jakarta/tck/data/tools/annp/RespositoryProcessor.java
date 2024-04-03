/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package ee.jakarta.tck.data.tools.annp;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ee.jakarta.tck.data.tools.qbyn.QueryByNameInfo;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;
import jakarta.persistence.Entity;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;


@SupportedAnnotationTypes("jakarta.data.repository.Repository")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedOptions({"debug"})
public class RespositoryProcessor extends AbstractProcessor {
    private Map<String, RepositoryInfo> repoInfoMap = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.printf("RespositoryProcessor: Processing repositories, over=%s\n", roundEnv.processingOver());
        boolean newRepos = false;
        Set<? extends Element> repositories = roundEnv.getElementsAnnotatedWith(Repository.class);
        for (Element repository : repositories) {
            String fqn = AnnProcUtils.getFullyQualifiedName(repository);
            if(repoInfoMap.containsKey(fqn) || repoInfoMap.containsKey(fqn.substring(0, fqn.length()-1))) {
                System.out.printf("Repository(%s) already processed\n", fqn);
                continue;
            }

            System.out.printf("Repository(%s) as kind:%s\n", repository.asType(), repository.getKind());
            TypeElement entityType = null;
            if(repository instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) repository;
                entityType = getEntityType(typeElement);
                System.out.printf("\tRepository(%s) entityType(%s)\n", repository, entityType);
            }
            // If there
            if(entityType == null) {
                System.out.printf("Repository(%s) does not have an JPA entity type\n", repository);
                continue;
            }
            //
            newRepos = checkRespositoryForQBN(repository, entityType);
        }

        // Generate repository interfaces for QBN methods
        if(newRepos) {
            for (Map.Entry<String, RepositoryInfo> entry : repoInfoMap.entrySet()) {
                RepositoryInfo repoInfo = entry.getValue();
                System.out.printf("Generating repository interface for %s\n", entry.getKey());
                try {
                    AnnProcUtils.writeRepositoryInterface(repoInfo, processingEnv);
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(javax.tools.Diagnostic.Kind.ERROR, e.getMessage());
                }
            }
        }
        return true;
    }

    private TypeElement getEntityType(TypeElement repo) {
        // Check super interfaces for Repository<EntityType>
        for (TypeMirror iface : repo.getInterfaces()) {
            System.out.printf("\tRepository(%s) interface(%s)\n", repo, iface);
            if (iface instanceof DeclaredType) {
                DeclaredType declaredType = (DeclaredType) iface;
                if(!declaredType.getTypeArguments().isEmpty()) {
                    TypeElement candidateType = (TypeElement) processingEnv.getTypeUtils().asElement(declaredType.getTypeArguments().get(0));
                    Entity entity = candidateType.getAnnotation(Entity.class);
                    if (entity != null) {
                        System.out.printf("Repository(%s) entityType(%s)\n", repo, candidateType);
                        return candidateType;
                    }
                }
            }
        }
        // Look for lifecycle methods
        for (Element e : repo.getEnclosedElements()) {
            if (e instanceof ExecutableElement) {
                ExecutableElement ee = (ExecutableElement) e;
                if (isLifeCycleMethod(ee)) {
                    List<? extends VariableElement> params = ee.getParameters();
                    for (VariableElement parameter : params) {
                        // Get the type of the parameter
                        TypeMirror parameterType = parameter.asType();

                        if (parameterType instanceof DeclaredType) {
                            DeclaredType declaredType = (DeclaredType) parameterType;
                            Entity entity = declaredType.getAnnotation(jakarta.persistence.Entity.class);
                            System.out.printf("%s, declaredType: %s\n", ee.getSimpleName(), declaredType, entity);
                            if(entity != null) {
                                System.out.printf("Repository(%s) entityType(%s)\n", repo, declaredType);
                                return (TypeElement) processingEnv.getTypeUtils().asElement(declaredType);
                            }

                            // Get the type arguments
                            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();

                            for (TypeMirror typeArgument : typeArguments) {
                                TypeElement argType = (TypeElement) processingEnv.getTypeUtils().asElement(typeArgument);
                                Entity entity2 = argType.getAnnotation(jakarta.persistence.Entity.class);
                                System.out.printf("%s, typeArgument: %s, entity: %s\n", ee.getSimpleName(), typeArgument, entity2);
                                if(entity2 != null) {
                                    System.out.printf("Repository(%s) entityType(%s)\n", repo, typeArgument);
                                    return (TypeElement) processingEnv.getTypeUtils().asElement(typeArgument);
                                }
                            }
                        }
                    }

                }
            }
        }

        return null;
    }

    private boolean isLifeCycleMethod(ExecutableElement method) {
        return method.getAnnotation(Insert.class) != null
                || method.getAnnotation(Update.class) != null
                || method.getAnnotation(Save.class) != null
                || method.getAnnotation(Delete.class) != null;
    }
    private boolean checkRespositoryForQBN(Element repository, TypeElement entityType) {
        System.out.println("RespositoryProcessor: Checking repository for Query By Name");
        boolean addedRepo = false;

        String entityName = entityType.getQualifiedName().toString();
        List<ExecutableElement> methods = AnnProcUtils.methodsIn(repository.getEnclosedElements());
        RepositoryInfo repoInfo = new RepositoryInfo(repository);
        for (ExecutableElement m : methods) {
            System.out.printf("\t%s\n", m.getSimpleName());
            QueryByNameInfo qbn = AnnProcUtils.isQBN(m);
            if(qbn != null) {
                qbn.setEntity(entityName);
                repoInfo.addQBNMethod(m, qbn);
            }
        }
        if(repoInfo.hasQBNMethods()) {
            System.out.printf("Repository(%s) has QBN(%d) methods\n", repository, repoInfo.qbnMethods.size());
            repoInfoMap.put(AnnProcUtils.getFullyQualifiedName(repository), repoInfo);
            addedRepo = true;
        }
        return addedRepo;
    }

    private void generateQBNRepositoryInterfaces() {
        for (Map.Entry<String, RepositoryInfo> entry : repoInfoMap.entrySet()) {
            RepositoryInfo repoInfo = entry.getValue();
            System.out.printf("Generating repository interface for %s\n", entry.getKey());

        }
    }
}