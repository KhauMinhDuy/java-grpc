package com.khauminhduy.server;

import com.khauminhduy.grpc.HelloRequest;
import com.khauminhduy.grpc.HelloResponse;
import com.khauminhduy.grpc.HelloServiceGrpc.HelloServiceImplBase;

import io.grpc.stub.StreamObserver;

public class HelloServiceImpl extends HelloServiceImplBase {

	@Override
	public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
		System.out.println("Request received from client:\n" + request);

		String greeting = new StringBuilder().append("Hello, ")
				.append(request.getFirstName())
				.append(" ")
				.append(request.getLastName())
				.toString();
		
		HelloResponse response = HelloResponse.newBuilder().setGreeting(greeting).build();

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

}
