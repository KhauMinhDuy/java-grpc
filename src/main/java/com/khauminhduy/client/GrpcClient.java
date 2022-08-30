package com.khauminhduy.client;

import com.khauminhduy.grpc.HelloRequest;
import com.khauminhduy.grpc.HelloResponse;
import com.khauminhduy.grpc.HelloServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcClient {
	
	public static void main(String[] args) {
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext().build();
		HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel);
		HelloResponse helloResponse = stub.hello(HelloRequest.newBuilder().setFirstName("Khau").setLastName("Duy").build());
		System.out.println("Response received from server:\n" + helloResponse);
		channel.shutdown();
	}

}
