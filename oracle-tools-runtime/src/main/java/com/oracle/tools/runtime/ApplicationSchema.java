/*
 * File: ApplicationSchema.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 */

package com.oracle.tools.runtime;

import java.io.File;

import java.util.List;

import java.util.concurrent.TimeUnit;

/**
 * {@link ApplicationSchema} defines a schema encapsulating configuration and
 * operational settings that an {@link ApplicationBuilder} will use to
 * realize an {@link Application}.
 *
 * @param <A>  the type of the {@link Application} that will be realized by an
 *             {@link ApplicationBuilder} using this {@link ApplicationSchema}
 * @param <S>  the type of {@link ApplicationSchema} that will be returned from
 *             setter calls on this interface.  This is to permit extensible
 *             type-safe fluent-style definitions of setter methods in
 *             sub-interfaces.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface ApplicationSchema<A extends Application<A>, S extends ApplicationSchema<A, S>>
{
    /**
     * Obtains the name of the application executable.
     *
     * @return the name of the executable to run
     */
    public String getExecutableName();


    /**
     * Obtains the {@link File} representing the directory in which the
     * application will execute.
     *
     * @return the directory in which the application will run
     */
    public File getWorkingDirectory();


    /**
     * Obtains the {@link PropertiesBuilder} that will be used to determine
     * operating system environment variables to be provided to the
     * {@link Application}.
     *
     * @return {@link PropertiesBuilder}
     */
    public PropertiesBuilder getEnvironmentVariablesBuilder();


    /**
     * Determines the standard error stream will be redirected to the standard
     * output stream.
     *
     * @return <code>true</code> if the standard error stream is redirected
     */
    public boolean isErrorStreamRedirected();


    /**
     * Determines if the environment variables for the {@link ApplicationSchema}
     * should be inherited from the process in which the {@link ApplicationSchema}
     * was created.
     *
     * @return true if the environment variables are inherited
     */
    public boolean isEnvironmentInherited();


    /**
     * Determines if diagnostic information will be logged for {@link Application}s
     * produced using the {@link ApplicationSchema}.
     *
     * @return <code>true</code> to enable diagnostic logging
     */
    public boolean isDiagnosticsEnabled();


    /**
     * Obtains the arguments for the {@link Application}.
     *
     * @return a {@link List} of {@link String}s
     */
    public List<String> getArguments();


    /**
     * Obtains the default duration to used for {@link Application} timeouts.
     * <p>
     * Many programs require timeouts when accessing resources, especially
     * those that are remote or may require waiting for some period of time.
     * In those circumstances, the default timeout is used, unless of course
     * an explicit timeout is specified.
     *
     * @return the timeout duration (measured in {@link #getDefaultTimeoutUnits()})
     */
    public long getDefaultTimeout();


    /**
     * Obtains the default duration timeout {@link TimeUnit}.
     *
     * @return the {@link TimeUnit} for {@link Application} timeouts
     */
    public TimeUnit getDefaultTimeoutUnits();


    /**
     * Obtains the {@link LifecycleEventInterceptor}s that the
     * {@link ApplicationSchema} will attach to {@link Application}s
     * produced by the said {@link ApplicationSchema}.
     * <p>
     * {@link LifecycleEventInterceptor}s are typically used by {@link Application}s
     * and {@link ApplicationBuilder}s to raise {@link LifecycleEvent}s pertaining
     * to the life-cycle of an {@link Application}.
     *
     * @return  the {@link LifecycleEventInterceptor}s
     */
    public Iterable<LifecycleEventInterceptor<A>> getLifecycleInterceptors();
}
