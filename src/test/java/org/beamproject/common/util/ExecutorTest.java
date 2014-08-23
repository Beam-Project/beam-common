/*
 * Copyright (C) 2013, 2014 beamproject.org
 *
 * This file is part of beam-common.
 *
 * beam-common is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beam-common is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.common.util;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

public class ExecutorTest {

    private Executor executor;
    private ExecutorService service;

    @Before
    public void setUp() {
        executor = new Executor();
        service = createMock(ExecutorService.class);
        executor.service = service;
    }

    @Test
    public void testConstructor() {
        executor = new Executor();
        assertNotNull(executor.service);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRunAsyncOnNull() {
        executor.runAsync(null);
    }

    @Test
    public void testRunAsync() {
        Task task = new Task() {

            @Override
            public void run() {
            }
        };
        service.execute(task);
        expectLastCall();
        replay(service);

        executor.runAsync(task);

        verify(service);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRunAsyncAfterDelay() throws InterruptedException {
        final long timeout = 250;
        Task task = new DelayableTask() {

            @Override
            public void run() {
            }

            @Override
            public long getTimeoutInMilliseconds() {
                return timeout;
            }
        };
        EasyMock.expect(service.invokeAll(anyObject(Collection.class), eq(timeout), eq(TimeUnit.MILLISECONDS))).andReturn(null);
        replay(service);

        executor.runAsync(task);

        verify(service);
    }

    @Test
    public void testShutdown() throws InterruptedException {
        expect(service.awaitTermination(Executor.TERMINATION_TIMEOUT_IN_MILLISECONDS, TimeUnit.MILLISECONDS))
                .andReturn(true);
        replay(service);

        executor.shutdown();

        verify(service);
    }

    @Test
    public void testShutdownHangingThreads() throws InterruptedException {
        expect(service.awaitTermination(Executor.TERMINATION_TIMEOUT_IN_MILLISECONDS, TimeUnit.MILLISECONDS))
                .andThrow(new InterruptedException("interrupted"));
        expect(service.shutdownNow()).andReturn(null);
        replay(service);

        executor.shutdown();

        verify(service);
    }

}
