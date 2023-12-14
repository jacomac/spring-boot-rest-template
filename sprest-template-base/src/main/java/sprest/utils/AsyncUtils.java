package sprest.utils;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * Utility class helping in async task execution.
 *
 */
public abstract class AsyncUtils {

  private static final ScheduledExecutorService scheduler =
      Executors.newScheduledThreadPool(1);

  /**
   * Returns  a future that will complete with a {@link TimeoutException} after given time.
   *
   * @param duration time after future completes
   * @return future that completes with timeout exception
   */
  public static <T> CompletableFuture<T> failAfter(Duration duration) {
    final CompletableFuture<T> promise = new CompletableFuture<>();
    scheduler.schedule(() -> {
      final TimeoutException ex = new TimeoutException("Timeout after " + duration);
      return promise.completeExceptionally(ex);
    }, duration.toMillis(), TimeUnit.MILLISECONDS);
    return promise;
  }

  /**
   * Returns a future that will either complete when given future completes or after given timeout.
   *
   * @param future future to wait for completion
   * @param duration how long to wait for completion
   * @return future
   */
  public static <T> CompletableFuture<T> within(CompletableFuture<T> future, Duration duration) {
    final CompletableFuture<T> timeout = failAfter(duration);
    return future.applyToEither(timeout, Function.identity());
  }
}
