package org.enumerable.lambda.enumerable.jruby;

import static java.lang.System.*;
import static java.util.Arrays.*;
import static org.enumerable.lambda.exception.UncheckedException.*;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import org.enumerable.lambda.weaving.Debug;
import org.jruby.threading.DaemonThreadFactory;

public abstract class QueueIterator implements Iterator<Object>, Runnable {
    static boolean debug = Debug.debug;
    static final int LOST_INTEREST_TIME_OUT = 500;

    Executor e = Executors.newCachedThreadPool(new DaemonThreadFactory());
    Object endOfQueue = "<End of Queue>";
    Object noElement = "<No Element>";
    Object nullElement = "<Null>";
    RuntimeException[] exception = new RuntimeException[1];
    BlockingQueue<Object> yieldQueue = new ArrayBlockingQueue<Object>(1);
    Object current = noElement;
    boolean started;

    Object nextElementRequest = "<Next Element Request>";
    BlockingQueue<Object> requestQueue = new ArrayBlockingQueue<Object>(1);

    @SuppressWarnings("serial")
    class LostInterestException extends RuntimeException {
    }

    public void run() {
        started = true;
        try {
            debug("waiting for next request");
            if (null == requestQueue.poll(LOST_INTEREST_TIME_OUT, TimeUnit.MILLISECONDS))
                throw new LostInterestException();
            iterate();
            yieldQueue.offer(endOfQueue, LOST_INTEREST_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            uncheck(e);
        } catch (LostInterestException e) {
            current = endOfQueue;
            debug("client lost interest");
        } catch (RuntimeException e) {
            try {
                exception[0] = e;
                yieldQueue.put(exception);
            } catch (InterruptedException e1) {
                throw uncheck(e1);
            }
        }
    }

    public void debug(String msg) {
        if (debug)
            out.println(msg);
    }

    public boolean hasNext() {
        try {
            if (!started)
                e.execute(this);
            if (current == endOfQueue)
                return false;
            debug("requesting element");
            requestQueue.offer(nextElementRequest);
            current = yieldQueue.take();
            if (current == nullElement)
                current = null;
            debug("got element");
            return current != endOfQueue;
        } catch (InterruptedException e) {
            throw uncheck(e);
        }
    }

    public Object next() {
        if (current == noElement)
            hasNext();
        if (current == exception)
            throw exception[0];
        if (current == endOfQueue)
            throw new NoSuchElementException();
        Object result = current;
        current = noElement;
        return result;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void enque(Object o) throws InterruptedException {
        debug("offering element");
        if (o == null)
            o = nullElement;
        if (!yieldQueue.offer(o, LOST_INTEREST_TIME_OUT, TimeUnit.MILLISECONDS))
            throw new LostInterestException();
        debug("waiting for next request");
        if (null == requestQueue.poll(LOST_INTEREST_TIME_OUT, TimeUnit.MILLISECONDS))
            throw new LostInterestException();
    }

    public abstract void iterate() throws InterruptedException;

    public static class QueueIterable implements Iterable<Object> {
        QueueIterator iterator;

        public QueueIterable(QueueIterator iterator) {
            this.iterator = iterator;
        }

        public Iterator<Object> iterator() {
            return iterator;
        }
    }

    public static void main(String[] args) {
        final Iterable<? extends Object> iterable = asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

        class ForLoopQIterator extends QueueIterator {
            public void iterate() throws InterruptedException {
                for (Object o : iterable)
                    enque(o);
            }
        }

        for (Iterator<?> i = new ForLoopQIterator(); i.hasNext();) {
            Object next = i.next();
            System.out.println(next);
            if (next == (Integer) 5)
                break;
        }

        QueueIterator i = new ForLoopQIterator();
        try {
            while (true)
                System.out.println(i.next());
        } catch (NoSuchElementException done) {
        }

        for (Object o : new QueueIterable(new ForLoopQIterator())) {
            System.out.println(o);
            if (o == (Integer) 5)
                break;

        }
    }
}
