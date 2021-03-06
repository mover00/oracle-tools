/*
 * File: AbstractJavaApplicationBuilderTest.java
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

package com.oracle.tools.runtime.java;

import com.oracle.tools.deferred.DeferredAssert;

import com.oracle.tools.deferred.listener.DeferredCompletionListener;

import com.oracle.tools.junit.AbstractTest;

import com.oracle.tools.lang.StringHelper;

import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.DummyApp;
import com.oracle.tools.runtime.DummyClassPathApp;

import com.oracle.tools.runtime.console.PipedApplicationConsole;
import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.applications.SleepingApplication;
import com.oracle.tools.runtime.java.container.ContainerClassLoader;

import org.junit.Test;

import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.core.StringContains.containsString;

import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.Serializable;

import java.util.UUID;

import java.util.concurrent.Callable;

/**
 * Functional Tests for {@link JavaApplicationBuilder}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public abstract class AbstractJavaApplicationBuilderTest extends AbstractTest
{
    /**
     * Creates a new {@link JavaApplicationBuilder} to use for a tests in this
     * class and/or sub-classes.
     *
     * @return the {@link JavaApplicationBuilder}
     */
    public abstract JavaApplicationBuilder<SimpleJavaApplication,
                                           SimpleJavaApplicationSchema> newJavaApplicationBuilder();


    /**
     * Obtains the internal Application Process Id for the specified Application
     * (without using the application.getId() method)
     *
     * @param application  the application
     *
     * @return the process id
     */
    public long getProcessIdFor(JavaApplication<?> application)
    {
        return ((AbstractJavaApplication) application).getJavaProcess().getId();
    }


    /**
     * Ensure that we can start and terminate a {@link JavaApplication}.
     *
     * @throws Exception
     */
    @Test
    public void shouldRunApplication() throws Exception
    {
        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(DummyApp.class.getCanonicalName()).setArgument("arg1").setArgument("arg2")
                .setSystemProperty("test.prop.1",
                                   "value.1").setSystemProperty("test.prop.2", "value.2").setDiagnosticsEnabled(true);

        JavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema> builder =
            newJavaApplicationBuilder();

        PipedApplicationConsole console     = new PipedApplicationConsole();
        JavaApplication<?>      application = builder.realize(schema, "java-app", console);

        String                  stdout      = console.getOutputReader().readLine();

        assertThat(stdout.startsWith("[java-app:"), is(true));
        assertThat(stdout, containsString("arg1,arg2"));

        stdout = console.getOutputReader().readLine();

        assertThat(stdout, containsString("test.prop.1=value.1"));

        assertThat(application.getId(), is(getProcessIdFor(application)));

        application.destroy();
    }


    /**
     * Ensure that we can start and terminate an external Java-based
     * Application with a customized ClassPath.
     *
     * @throws Exception
     */
    @Test
    public void shouldRunApplicationWithRestrictedClasspath() throws Exception
    {
        ClassPath   knownJarClassPath = ClassPath.ofResource("asm-license.txt");
        Class<Mock> knownClass        = Mock.class;

        ClassPath   path1             = ClassPath.ofClass(DummyClassPathApp.class);
        ClassPath   path2             = ClassPath.ofClass(ContainerClassLoader.class);
        ClassPath   path3             = ClassPath.ofClass(StringHelper.class);
        ClassPath   classPath         = new ClassPath(knownJarClassPath, path1, path2, path3);

        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(DummyClassPathApp.class.getCanonicalName()).setClassPath(classPath)
                .setArgument(knownClass.getCanonicalName()).setDiagnosticsEnabled(true);

        JavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema> builder =
            newJavaApplicationBuilder();

        PipedApplicationConsole console     = new PipedApplicationConsole();
        JavaApplication<?>      application = builder.realize(schema, "java-app", console);

        String                  stdout      = console.getOutputReader().readLine();

        assertThat(stdout, containsString(knownJarClassPath.iterator().next()));

        stdout = console.getOutputReader().readLine();

        assertThat(stdout, containsString(path1.iterator().next()));

        stdout = console.getOutputReader().readLine();

        assertThat(stdout, containsString(path2.iterator().next()));

        assertThat(application.getId(), is(getProcessIdFor(application)));

        application.destroy();
    }


    /**
     * Ensure that {@link NativeJavaApplicationBuilder}s create applications that
     * can have {@link java.util.concurrent.Callable}s submitted to them and executed.
     */
    @Test
    public void shouldExecuteCallable() throws InterruptedException
    {
        SimpleJavaApplication application = null;

        try
        {
            // define and start the SleepingApplication
            SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

            // set a System-Property for the SleepingApplication (we'll request it back)
            String uuid = UUID.randomUUID().toString();

            schema.setSystemProperty("uuid", uuid);

            JavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema> builder =
                newJavaApplicationBuilder();

            ApplicationConsole console = new SystemApplicationConsole();

            application = builder.realize(schema, "sleeping", console);

            DeferredAssert.assertThat(application, new GetSystemProperty("uuid"), is(uuid));
        }
        catch (IOException e)
        {
        }
        finally
        {
            if (application != null)
            {
                application.destroy();
            }
        }
    }


    /**
     * A {@link java.util.concurrent.Callable} that returns a {@link System#getProperty(String)}.
     */
    public static class GetSystemProperty implements Callable<String>, Serializable
    {
        /**
         * The name of the {@link System#getProperty(String)} to return.
         */
        private String propertyName;


        /**
         * Constructs a {@link GetSystemProperty}.
         *
         * (for serialization)
         */
        public GetSystemProperty()
        {
        }


        /**
         * Constructs a {@link GetSystemProperty}.
         *
         * @param propertyName the name of the system property
         */
        public GetSystemProperty(String propertyName)
        {
            this.propertyName = propertyName;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public String call() throws Exception
        {
            return System.getProperty(propertyName);
        }
    }
}
