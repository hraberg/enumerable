package lambda.enumerable.jruby;

import static java.util.Arrays.*;
import static lambda.exception.UncheckedException.*;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class EnumerableQueueIterator {
    public static abstract class QueueIterator implements Iterator<Object> {
        private static final int LOST_INTEREST_TIME_OUT = 500;
        Object endOfQueue = "<End of Queue>";
        Object noElement = "<No Element>";
        Object nullElement = "<Null>";
        RuntimeException[] exception = new RuntimeException[1];
        Object nextElementRequest = "<Next Element Request>";
        BlockingQueue<Object> q = new ArrayBlockingQueue<Object>(1);
        BlockingQueue<Object> rq = new ArrayBlockingQueue<Object>(1);
        Object current = noElement;
        boolean started;

        @SuppressWarnings("serial")
        class LostInterestException extends RuntimeException {
        }

        void run() {
            started = true;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        print("waiting for next request");
                        if (null == rq.poll(LOST_INTEREST_TIME_OUT, TimeUnit.MILLISECONDS))
                            throw new LostInterestException();
                        iterate();
                        q.offer(endOfQueue, LOST_INTEREST_TIME_OUT, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        uncheck(e);
                    } catch (LostInterestException e) {
                        System.out.println("client lost interest");
                    } catch (RuntimeException e) {
                        try {
                            exception[0] = e;
                            q.put(exception);
                        } catch (InterruptedException e1) {
                            throw uncheck(e1);
                        }
                    }
                }
            }).start();
        }

        public synchronized void print(String msg) {
            // System.out.println(msg);
        }

        public boolean hasNext() {
            try {
                if (!started)
                    run();
                if (current == endOfQueue)
                    return false;
                print("requesting element");
                rq.offer(nextElementRequest);
                current = q.take();
                if (current == exception)
                    throw exception[0];
                if (current == nullElement)
                    current = null;
                print("got element");
                return current != endOfQueue;
            } catch (InterruptedException e) {
                throw uncheck(e);
            }
        }

        public Object next() {
            if (current == noElement)
                hasNext();
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
            print("offering element");
            if (o == null)
                o = nullElement;
            if (!q.offer(o, LOST_INTEREST_TIME_OUT, TimeUnit.MILLISECONDS))
                throw new LostInterestException();
            print("waiting for next request");
            if (null == rq.poll(LOST_INTEREST_TIME_OUT, TimeUnit.MILLISECONDS))
                throw new LostInterestException();
        }

        public abstract void iterate() throws InterruptedException;
    }

    public class QueueIterable implements Iterable<Object> {
        QueueIterator iterator;

        public QueueIterable(QueueIterator iterator) {
            this.iterator = iterator;
        }

        public Iterator<Object> iterator() {
            return iterator;
        }
    }

    Iterable<? extends Object> iterable = asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

    public EnumerableQueueIterator() {
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

    public static void main(String[] args) {
        new EnumerableQueueIterator();
    }
}
