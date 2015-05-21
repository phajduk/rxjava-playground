import org.junit.Test;
import rx.Observable;
import rx.Subscriber;
import rx.observables.BlockingObservable;
import rx.schedulers.Schedulers;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class BlockingAndThreadingPlaygroundTest {
    /*
    Thread[main,5,main]		Method end
    Thread[RxComputationThreadPool-1,5,main]		Value 0
    Thread[RxComputationThreadPool-1,5,main]		Value 1
    Thread[RxComputationThreadPool-1,5,main]		Value 2
    Thread[RxComputationThreadPool-1,5,main]		Value 3
    Thread[RxComputationThreadPool-1,5,main]		Value 4
    Thread[RxComputationThreadPool-1,5,main]		Value 5

    Process finished with exit code 0
     */
    @Test
    public void computationScheduler() throws Exception {
        endlessObservable()
                .subscribe(this::logWithThreadInfo);

        logMethodEnding();
        Thread.sleep(6000l);
    }

    /*
    Note: method end is launched first as Observable.interval is called on computation scheduler by default.

    Thread[main,5,main]		Method end
    Thread[RxNewThreadScheduler-1,5,main]		Value 0
    Thread[RxNewThreadScheduler-1,5,main]		Value 1
    Thread[RxNewThreadScheduler-1,5,main]		Value 2
    Thread[RxNewThreadScheduler-1,5,main]		Value 3
    Thread[RxNewThreadScheduler-1,5,main]		Value 4
    Thread[RxNewThreadScheduler-1,5,main]		Value 5

    Process finished with exit code 0
     */
    @Test
    public void computationSchedulerWithObserveOnSet() throws Exception {
        endlessObservable()
                .observeOn(Schedulers.newThread())
                .subscribe(this::logWithThreadInfo);

        logMethodEnding();
        Thread.sleep(6000l);
    }

    /*
    Thread[main,5,main]		Value 0
    Thread[main,5,main]		Value 1
    Thread[main,5,main]		Value 2
    Thread[main,5,main]		Value 3
    Thread[main,5,main]		Value 4
    Thread[main,5,main]		Value 5
    Thread[main,5,main]		Value 6
    Thread[main,5,main]		Value 7
    Thread[main,5,main]		Value 8
    ...

    Process finished with exit code 130
     */
    @Test
    public void blockedComputationScheduler() throws Exception {
        BlockingObservable<String> blockingObservable = endlessObservable().toBlocking();
        for (String value : blockingObservable.toIterable()) {
            logWithThreadInfo(value);
        }

        logMethodEnding();
        Thread.sleep(6000l);
    }

    /*
    10:34:27.712
    10:34:27.794
    10:34:27.796
    Thread[main,5,main]		Value 0
    Thread[main,5,main]		Value 1
    Thread[main,5,main]		Value 2
    Thread[main,5,main]		Value 3
    Thread[main,5,main]		Value 4
    Thread[main,5,main]		Method end

    Process finished with exit code 0
     */
    @Test
    public void blockedComputationSchedulerWithLimitedNumberOfEmittedValues() throws Exception {
        logCurrentTime();
        BlockingObservable<String> blockingObservable = endlessObservable().limit(5).toBlocking();
        logCurrentTime();
        Iterable<String> iterable = blockingObservable.toIterable(); //LinkedBlockingQueue under the hood
        logCurrentTime();
        for (String value : iterable) {
            logWithThreadInfo(value);
        }
        logMethodEnding();
        Thread.sleep(6000l);
    }

    /*
    Thread[main,5,main]		Value 0
    Thread[main,5,main]		Method end

    Process finished with exit code 0
     */
    @Test
    public void firstOrDefaultInBlockingObservable() throws Exception {
        BlockingObservable<String> blockingObservable = endlessObservable().limit(5).toBlocking();
        logWithThreadInfo(blockingObservable.firstOrDefault("default"));
        logMethodEnding();
        Thread.sleep(6000l);
    }

    /*
    Thread[main,5,main]		default
    Thread[main,5,main]		Method end

    Process finished with exit code 0
     */
    @Test
    public void emptySchedulerDefaultValue() throws Exception {
        BlockingObservable<Object> blockingObservable = Observable.empty().toBlocking();
        logWithThreadInfo(blockingObservable.firstOrDefault("default").toString());
        logMethodEnding();
        Thread.sleep(6000l);
    }


    /*
    Note: Changed scheduler to immediate (curr thread) so end method called on the end

    Thread[RxNewThreadScheduler-1,5,main]		Value 0
    Thread[RxNewThreadScheduler-1,5,main]		Value 1
    Thread[RxNewThreadScheduler-1,5,main]		Value 2
    Thread[main,5,main]		Method end

    Process finished with exit code 0
     */
    @Test
    public void immediateSchedulerForInterval() throws Exception {
        Observable.interval(1, TimeUnit.SECONDS, Schedulers.immediate())
                .limit(3)
                .observeOn(Schedulers.newThread())
                .map(value -> "Value " + value)
                .subscribe(value -> {
                    logWithThreadInfo(value);
                });

        logMethodEnding();
        Thread.sleep(6000l);
    }

    /*
    Thread[main,5,main]		Call from custom observable
    Thread[RxNewThreadScheduler-1,5,main]		Value 0
    Thread[RxNewThreadScheduler-1,5,main]		Value 1
    Thread[RxNewThreadScheduler-1,5,main]		Value 2
    Thread[main,5,main]		Method end

    Process finished with exit code 0
     */
    @Test
    public void customObservableWithoutSubscribeOn() throws Exception {
        myIntervalObservable
                .limit(3)
                .observeOn(Schedulers.newThread())
                .map(value -> "Value " + value)
                .subscribe(value -> {
                    logWithThreadInfo(value);
                });

        logMethodEnding();
        Thread.sleep(6000l);
    }

    /*
    Thread[main,5,main]		Method end
    Thread[RxNewThreadScheduler-2,5,main]		Call from custom observable
    Thread[RxNewThreadScheduler-1,5,main]		Value 0
    Thread[RxNewThreadScheduler-1,5,main]		Value 1
    Thread[RxNewThreadScheduler-1,5,main]		Value 2
     */
    @Test
    public void customObservableWithSubscribeOn() throws Exception {
        myIntervalObservable
                .limit(3)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .map(value -> "Value " + value)
                .subscribe(value -> {
                    logWithThreadInfo(value);
                });

        logMethodEnding();
        Thread.sleep(6000l);
    }


    private Observable<String> endlessObservable() {
        return Observable.interval(1, TimeUnit.SECONDS).map(value -> "Value " + value);
    }

    private void logMethodEnding() {
        logWithThreadInfo("Method end");
    }

    private void logWithThreadInfo(String msg) {
        System.out.printf("%s\t\t%s%n", Thread.currentThread().toString(), msg);
    }

    private void logCurrentTime() {
        System.out.println(LocalDateTime.now().toLocalTime().toString());
    }

    private Observable<Integer> myIntervalObservable = Observable.create(subscriber -> {
        logWithThreadInfo("Call from custom observable");
        for (int i = 0; i < 5; i++) {
            subscriber.onNext(i);
            try {
                Thread.sleep(1000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        subscriber.onCompleted();
    });
}
