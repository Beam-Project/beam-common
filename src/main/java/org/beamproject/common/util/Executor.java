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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class provides an {@link ExecutorService} for running tasks
 * asynchronously.
 */
public class Executor {

    ExecutorService service = Executors.newCachedThreadPool();

    /**
     * Runs the given {@link Task} instance asynchronously.
     * <p>
     * Objects of the type {@link DelayableTask} will be delayed about the
     * configured time.
     *
     * @param task The task that should be run.
     * @throws IllegalArgumentException If the argument is null.
     * @throws ExecutorException If a problem occurs during execution.
     */
    public void runAsync(final Task task) {
        Exceptions.verifyArgumentsNotNull(task);

        if (task instanceof DelayableTask) {
            try {
                long delay = ((DelayableTask) task).getTimeoutInMilliseconds();
                service.invokeAll(toList(task), delay, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                throw new ExecutorException("Could not complete the delayed task: " + ex.getMessage());
            }
        } else {
            service.execute(task);
        }
    }

    private List<Callable<Object>> toList(final Task task) {
        Callable<Object> callable = new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                task.run();
                return null;
            }
        };

        List<Callable<Object>> tasks = new LinkedList<>();
        tasks.add(callable);

        return tasks;
    }
}
