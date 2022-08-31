package com.amazon.ata.futures;

import com.amazon.ata.futures.apitasks.APITaskRequest;
import com.amazon.ata.futures.apitasks.APITaskResponse;
import com.amazon.ata.futures.apitasks.HttpMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class App {
    public static int TIMEOUT = 4000;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        MockApi mockApi = new MockApi();
        ExecutorService mainExecutorService = Executors.newCachedThreadPool();

        List<APITaskRequest> apiTaskRequests = new ArrayList<>();
            apiTaskRequests.add(new APITaskRequest(HttpMethod.GET, "/categories"));
            apiTaskRequests.add(new APITaskRequest(HttpMethod.GET,"/products/product/1"));
            apiTaskRequests.add(new APITaskRequest(HttpMethod.GET,"/products/all"));

        Future<List<APITaskResponse>> futureResponses =
                mainExecutorService.submit(makeApiCalls(mockApi, apiTaskRequests, TIMEOUT));

        while(!futureResponses.isDone()) {
            Thread.sleep(1000);
            System.out.println("Waiting on API calls...");
        }
        System.out.println("All Futures are done");

        List<APITaskResponse> apiTaskResponses = futureResponses.get();
        for (APITaskResponse response : apiTaskResponses) {
            System.out.println(response);
        }

        mockApi.shutdown();
    }

    public static Callable<List<APITaskResponse>> makeApiCalls(MockApi mockApi, List<APITaskRequest> apiRequests, int timeout) {

        return () -> {
            List<APITaskResponse> responses = new ArrayList<>();
            for (APITaskRequest apiRequest : apiRequests) {
                System.out.println("Making " + apiRequest.getHttpMethod() + " request to " + apiRequest.getPath());
                responses.add(mockApi.performRequest(apiRequest, timeout));
            }

            return responses;
        };
    }
}
