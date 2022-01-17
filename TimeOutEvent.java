import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


public abstract class TimeOutEvent extends Thread {
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    abstract void timeOutFunction();

    @Override
    public void run() {
        this.timeOutFunction();
        this.disableWait();
    }

    private void disableWait() {
        try {
            this.lock.lock();
            this.condition.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

    boolean wait(long time, TimeUnit unit) {
        if (!this.isAlive()) {
            return true;
        }
        boolean flag = false;
        try {
            this.lock.lock();
            flag = this.condition.await(time, unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (!flag) {
                this.interrupt();
            }
            this.lock.unlock();
        }
        return flag;
    }

    public static void main(String[] args) {
        TimeOutEvent timeOutEvent = new TimeOutEvent() {
            @Override
            void timeOutFunction() {
                System.out.println("Start function");
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        timeOutEvent.start();
        System.out.println(timeOutEvent.wait(5000, TimeUnit.MILLISECONDS));

        try {
            timeOutEvent.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
