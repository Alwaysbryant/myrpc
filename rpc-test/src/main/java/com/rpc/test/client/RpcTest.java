package com.rpc.test.client;


import com.rpc.RpcClient;
import com.rpc.test.service.Dto;
import com.rpc.test.service.HelloService;

/**
 * Created by luxiaoxun on 2016-03-11.
 */
public class RpcTest {

    public static void main(String[] args) throws InterruptedException {
        final RpcClient rpcClient = new RpcClient("localhost:2181");

        int threadNum = 1;
        final int requestNum = 50;
        Thread[] threads = new Thread[threadNum];

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < threadNum; ++i) {
            threads[i] = new Thread(() -> {
                for (int i1 = 0; i1 < requestNum; i1++) {
                    try {
                        final HelloService service = rpcClient.createService(HelloService.class, "1.1.1");
//                        String rpc = service.hello("Rpc");
                        Dto info = service.remoteForDto("Jack", 24, "Engineer");
                        System.out.println("1111111" + info.toString());
                        try {
                            Thread.sleep(5 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception ex) {
                        System.out.println(ex.toString());
                    }
                }
            });
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        long timeCost = (System.currentTimeMillis() - startTime);
        String msg = String.format("Sync call total-time-cost:%sms, req/s=%s", timeCost, ((double) (requestNum * threadNum)) / timeCost * 1000);
        System.out.println(msg);

        rpcClient.stop();
    }
}
