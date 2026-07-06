package cn.spirit.go.service.db;

import io.vertx.core.Promise;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManySubscriber<T> implements Subscriber<T> {

    private final List<T> received = new ArrayList<>();

    private final Promise<List<T>> promise;

    public ManySubscriber(Promise<List<T>> promise) {
        Objects.requireNonNull(promise, "promise is null");
        this.promise = promise;
    }

    public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);
    }

    public void onNext(T t) {
        this.received.add(t);
    }

    public void onError(Throwable t) {
        this.promise.fail(t);
    }

    public void onComplete() {
        this.promise.complete(received);
    }

    public Promise<List<T>> promise() {
        return this.promise;
    }
}
