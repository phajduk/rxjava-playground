import org.junit.Test;

import java.util.concurrent.TimeUnit;

import rx.Observable;

public class DelayedTest {
    @Test
    public void noDelayTest() throws Exception {
        long start = System.currentTimeMillis();
        Observable.just(1, 2, 3)
                  .subscribe(item -> {
                      System.out.println(String.format("Emitted %d at second %s", item, getElapsedSeconds(start)));
                  });

        Thread.sleep(6000l);
    }

    /**
     * Emitted 1 at second 1
     * Emitted 2 at second 1
     * Emitted 3 at second 1
     */
    @Test
    public void delayTest() throws Exception {
        long start = System.currentTimeMillis();
        Observable.just(1, 2, 3)
                  .delay(1, TimeUnit.SECONDS)
                  .subscribe(item -> {
                      System.out.println(String.format("Emitted %d at second %s", item, getElapsedSeconds(start)));
                  });

        Thread.sleep(6000l);
    }

    /**
     * Delay fun called with arg 1
     * Delay fun called with arg 2
     * Delay fun called with arg 3
     * Emitted 1 at second 1
     * Emitted 2 at second 2
     * Emitted 3 at second 3
     * <p>
     * Note: delay is called for each item separately, we have simple observable created via just so delay function is called immediately
     */
    @Test
    public void delayFooTest() throws Exception {
        long start = System.currentTimeMillis();
        Observable.just(1, 2, 3)
                  .delay(integer -> {
                      System.out.println("Delay fun called with arg " + integer);
                      return Observable.just(integer).delay(integer, TimeUnit.SECONDS);
                  })
                  .subscribe(item -> {
                      System.out.println(String.format("Emitted %d at second %s", item, getElapsedSeconds(start)));
                  });

        Thread.sleep(7000l);
    }

    /**
     * Observable prepared
     * Delay fun called with arg 1
     * Delay fun called with arg 2
     * Delay fun called with arg 3
     * Delay fun called with arg 1
     * Delay fun called with arg 2
     * Delay fun called with arg 3
     * Subscriber#1	Emitted 1 at second 1
     * Subscriber#2	Emitted 1 at second 1
     * Subscriber#1	Emitted 2 at second 2
     * Subscriber#2	Emitted 2 at second 2
     * Subscriber#1	Emitted 3 at second 3
     * Subscriber#2	Emitted 3 at second 3
     * <p>
     * Note: each subscriber have it's own copy of delay function
     */
    @Test
    public void delayFooSeparatedTwoSubscriptionTest() throws Exception {
        long start = System.currentTimeMillis();
        Observable<Integer> observable = Observable.just(1, 2, 3)
                                                   .delay(integer -> {
                                                       System.out.println("Delay fun called with arg " + integer);
                                                       return Observable.just(integer).delay(integer, TimeUnit.SECONDS);
                                                   });
        System.out.printf("Observable prepared\n");
        observable.subscribe(item -> {
            System.out.println(String.format("Subscriber#1\tEmitted %d at second %s", item, getElapsedSeconds(start)));
        });
        observable.subscribe(item -> {
            System.out.println(String.format("Subscriber#2\tEmitted %d at second %s", item, getElapsedSeconds(start)));
        });
        Thread.sleep(7000l);
    }

    /**
     * Observable prepared
     * Delay fun called with arg 1
     * Delay fun called with arg 2
     * Delay fun called with arg 3
     * Subscriber#1	Emitted 1 at second 1
     * Subscriber#2	Emitted 1 at second 1
     * Subscriber#1	Emitted 2 at second 2
     * Subscriber#2	Emitted 2 at second 2
     * Subscriber#1	Emitted 3 at second 3
     * Subscriber#2	Emitted 3 at second 3
     * <p>
     * Note: share() allowed us to share emitter and we have one delay function
     */
    @Test
    public void delayFooSeparatedTwoSubscriptionForSharedObservableTest() throws Exception {
        long start = System.currentTimeMillis();
        Observable<Integer> observable = Observable.just(1, 2, 3)
                                                   .delay(integer -> {
                                                       System.out.println("Delay fun called with arg " + integer);
                                                       return Observable.just(integer).delay(integer, TimeUnit.SECONDS);
                                                   })
                                                   .share();
        System.out.printf("Observable prepared\n");
        observable.subscribe(item -> {
            System.out.println(String.format("Subscriber#1\tEmitted %d at second %s", item, getElapsedSeconds(start)));
        });
        observable.subscribe(item -> {
            System.out.println(String.format("Subscriber#2\tEmitted %d at second %s", item, getElapsedSeconds(start)));
        });
        Thread.sleep(7000l);
    }

    private long getElapsedSeconds(long start) {
        return (System.currentTimeMillis() - start) / 1000;
    }
}
