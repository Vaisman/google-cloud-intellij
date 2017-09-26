/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.intellij.util;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.application.ApplicationManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Utilities for working with threads. */
public final class ThreadUtil {

  private static final ThreadUtil INSTANCE = new ThreadUtil();

  private ExecutorService backgroundExecutorService;
  private boolean invokeLaterOnEDT = true;

  /** Visibility is restricted internally; use {@link #getInstance()} instead. */
  private ThreadUtil() {
    this.backgroundExecutorService = Executors.newCachedThreadPool();
  }

  /** Returns the static instance of this utility. */
  public static ThreadUtil getInstance() {
    return INSTANCE;
  }

  /**
   * Executes the given {@link Runnable} on the background {@link ExecutorService} and returns the
   * {@link Future} result.
   *
   * @param runnable the {@link Runnable} to run on the background {@link ExecutorService}
   */
  public Future<?> executeInBackground(Runnable runnable) {
    return backgroundExecutorService.submit(runnable);
  }

  /**
   * Executes the given {@link Runnable} on even dispatching thread.
   *
   * <p>If {@link #invokeLaterOnEDT} is {@code true} the runnable will be invoked asynchronously,
   * otherwise it will be invoked synchronously.
   *
   * @param runnable the {@link Runnable} to run on the EDT.
   */
  public void invokeLaterOnEDT(Runnable runnable) {
    if (invokeLaterOnEDT) {
      ApplicationManager.getApplication().invokeLater(runnable);
    } else {
      ApplicationManager.getApplication().invokeAndWait(runnable);
    }
  }

  /**
   * Sets the {@link ExecutorService} used for executing background tasks.
   *
   * <p>This should only be used by unit tests to replace the default {@link ExecutorService} with
   * an executor of your choice. For example, the following snippet replaces the background executor
   * service with a direct one, causing all tasks submitted to it to be executed synchronously on
   * the current thread:
   *
   * <pre>{@code
   * ExecutorService directExecutorService = MoreExecutors.newDirectExecutorService();
   * ThreadUtil.getInstance().setBackgroundExecutorService(directExecutorService);
   * }</pre>
   *
   * @param backgroundExecutorService the new {@link ExecutorService} to use for background tasks
   */
  @VisibleForTesting
  public void setBackgroundExecutorService(ExecutorService backgroundExecutorService) {
    this.backgroundExecutorService = backgroundExecutorService;
  }

  /**
   * Indicates that runnables to be invoked on the EDT should be invoked later in the background.
   */
  @VisibleForTesting
  public void setBackgroundEDTInvocator() {
    this.invokeLaterOnEDT = true;
  }

  /**
   * Indicates that runnables to be invoked on the EDT should be invoked synchronously.
   *
   * <p>This should only be used by unit tests to replace behavior of {@link
   * #executeInBackground(Runnable)} from running Swing EDT tasks in the background to having them
   * block.
   */
  @VisibleForTesting
  public void setSynchronousEDTInvocator() {
    this.invokeLaterOnEDT = false;
  }
}
