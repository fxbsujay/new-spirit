package cn.spirit.go.service.db;

import io.vertx.core.Promise;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import java.util.Objects;

public class SingleSubscriber<T> implements Subscriber<T> {

    private T received;

    private final Promise<T> promise;

    public SingleSubscriber(Promise<T> promise) {
        Objects.requireNonNull(promise, "promise is null");
        this.promise = promise;
    }

    public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);
    }

    public void onNext(T t) {
        if (this.received == null) {
            this.received = t;
        }
    }

    public void onError(Throwable t) {
        this.promise.fail(t);
    }

    public void onComplete() {
        this.promise.complete(received);
    }

    public Promise<T> promise() {
        return this.promise;
    }
}
