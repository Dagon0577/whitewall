package com.Dagon.whitewall;

import jdk.nashorn.internal.ir.Block;
import org.junit.experimental.theories.Theories;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class MyThread extends Thread{
    private int tid;

    public MyThread(int tid){
        this.tid=tid;
    }
    @Override
    public void run(){
        try{
            for(int i=0;i<10;i++){
                Thread.sleep(1000);
                System.out.println(String.format("%d:%d",tid,i));
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}

class Customer implements Runnable{
    private BlockingQueue<String> queue;

    public Customer(BlockingQueue<String> queue)
    {
        this.queue=queue;
    }
    @Override
    public void run(){
        try{
            while(true){
                System.out.println(Thread.currentThread().getName() + ":"+queue.take());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

class Producer implements Runnable{
    private BlockingQueue<String> queue;

    public Producer(BlockingQueue<String> queue){
        this.queue=queue;
    }

    @Override
    public void run(){
        try {
            for(int i=0;i<100;i++){
                Thread.sleep(1000);
                queue.put(String.valueOf(i));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

public class MultiThreadTests {
    private static Object obj=new Object();

    public static void testSynchronized1(){
        synchronized (obj){
            try{
                for(int i=0;i<10;i++){
                    Thread.sleep(1000);
                    System.out.println(String.format("T3 %d:",i));
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void testSynchronized2(){
        synchronized (obj){
            try{
                for(int i=0;i<10;i++){
                    Thread.sleep(1000);
                    System.out.println(String.format("T4 %d:",i));
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void testSynchronized(){
        for(int i=0;i<10;++i){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    testSynchronized1();
                    testSynchronized2();
                }
            }).start();
        }
    }

    public static void testThread(){
        for(int i=0;i<10;i++){
            new MyThread(i).start();
        }

        for(int i=0;i<10;i++){
            final int qit=i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        for(int i=0;i<10;i++){
                            Thread.sleep(1000);
                            System.out.println(String.format("t2 %d:%d",qit,i));
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static void testBlockingQueue(){
        BlockingQueue<String> queue=new ArrayBlockingQueue<String>(10);
        new Thread(new Producer(queue)).start();
        new Thread(new Customer(queue),"Customer1").start();
        new Thread(new Customer(queue),"Customer2").start();
    }

    private static int userId;
    private static ThreadLocal<Integer> threadLocalUserIds=new ThreadLocal<>();

    public static void testThreadLocal(){
        for(int i=0;i<10;++i){
            final int finalI=i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        userId=finalI;
                        Thread.sleep(1000);
                        System.out.println("userId:"+userId);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static void testExecutor(){
        //ExecutorService service= Executors.newSingleThreadExecutor();
        ExecutorService service=Executors.newFixedThreadPool(2);
        service.submit(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<10;++i){
                    try{
                        Thread.sleep(1000);
                        System.out.println("Executor1:"+i);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        service.submit(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<10;++i){
                    try{
                        Thread.sleep(1000);
                        System.out.println("Executor2:"+i);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        service.shutdown();
        while(!service.isTerminated()){
            try{
                Thread.sleep(1000);
                System.out.println("wait for termination.");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static int counter=0;
    private static AtomicInteger  atomicInteger=new AtomicInteger(0);

    public static void testWithoutAtomic(){
        for(int i=0;i<10;i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Thread.sleep(1000);
                        for(int j=0;j<10;j++){
                            counter++;
                            System.out.println(counter);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static void testWithAtomic(){
        for(int i=0;i<10;i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Thread.sleep(1000);
                        for(int j=0;j<10;j++){
                            System.out.println(atomicInteger.incrementAndGet());
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static void testAtomic(){
        //testWithoutAtomic();
        testWithAtomic();
    }

    public static void testFuture(){
        ExecutorService service=Executors.newSingleThreadExecutor();
        Future<Integer> future=service.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                //Thread.sleep(1000);
                throw new IllegalArgumentException("异常");
                //return 1;
            }
        });

        service.shutdown();
        try{
            System.out.println(future.get());
            //System.out.println(future.get(100,TimeUnit.MILLISECONDS));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        //testThread();
        //testSynchronized();
        //testBlockingQueue();
        //testThreadLocal();
        //testExecutor();
        //testAtomic();
        testFuture();
    }
}
