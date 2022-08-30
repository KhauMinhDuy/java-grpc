package com.khauminhduy.streaming;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.khauminhduy.grpc.streaming.Stock;
import com.khauminhduy.grpc.streaming.StockQuote;
import com.khauminhduy.grpc.streaming.StockQuoteProviderGrpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class StockServer {
	private final int port;
	private final Server server;

	public StockServer(int port) throws IOException {
		this.port = port;
		server = ServerBuilder.forPort(port)
				.addService(new StockService())
				.build();
	}

	public void start() throws IOException {
		server.start();
		System.out.println("Server started, listening on " + port);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.err.println("shutting down server");
				try {
					StockServer.this.stop();
				} catch (InterruptedException e) {
					e.printStackTrace(System.err);
				}
				System.err.println("server shutted down");
			}
		});
	}

	public void stop() throws InterruptedException {
		if (server != null) {
			server.shutdown()
					.awaitTermination(30, TimeUnit.SECONDS);
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		StockServer stockServer = new StockServer(8990);
		stockServer.start();
		if(stockServer.server != null) {
			stockServer.server.awaitTermination();
		}
	}

	private static class StockService extends StockQuoteProviderGrpc.StockQuoteProviderImplBase {
		StockService() {
		}

		@Override
		public StreamObserver<Stock> bidirectionalStreamingGetListsStockQuotes(
				StreamObserver<StockQuote> responseObserver) {
			return new StreamObserver<Stock>() {

				@Override
				public void onCompleted() {
					responseObserver.onCompleted();
				}

				@Override
				public void onError(Throwable t) {
					System.out.println("error:{}" + t.getMessage());
				}

				@Override
				public void onNext(Stock request) {
					for (int i = 1; i <= 5; i++) {
						StockQuote stockQuote = StockQuote.newBuilder()
								.setPrice(fetchStockPriceBid(request))
								.setOfferNumber(i)
								.setDescription("Price for stock:" + request.getTickerSymbol())
								.build();
						responseObserver.onNext(stockQuote);
					}
				}

			};
		}

		@Override
		public StreamObserver<Stock> clientSideStreamingGetStatisticsOfStocks(StreamObserver<StockQuote> responseObserver) {
			return new StreamObserver<Stock>() {
				int count;
				double price = 0.0;
				StringBuffer sb = new StringBuffer();

				@Override
				public void onCompleted() {
					responseObserver.onNext(StockQuote.newBuilder()
							.setPrice(price / count)
							.setDescription("Statistics-" + sb.toString())
							.build());
					responseObserver.onCompleted();

				}

				@Override
				public void onError(Throwable t) {
					System.out.println("error:{}" + t.getMessage());
				}

				@Override
				public void onNext(Stock stock) {
					count++;
					price = +fetchStockPriceBid(stock);
					sb.append(":").append(stock.getTickerSymbol());
				}

			};
		}

		@Override
		public void serverSideStreamingGetListStockQuotes(Stock request, StreamObserver<StockQuote> responseObserver) {
			for (int i = 1; i <= 5; i++) {
				StockQuote stockQuote = StockQuote.newBuilder()
						.setPrice(fetchStockPriceBid(request))
						.setOfferNumber(i)
						.setDescription("Price for stock:" + request.getTickerSymbol()).build();
				responseObserver.onNext(stockQuote);
			}
			responseObserver.onCompleted();
		}

	}

	private static double fetchStockPriceBid(Stock stock) {
		return stock.getTickerSymbol().length() + ThreadLocalRandom.current().nextDouble(-0.1, 0.1);
	}

}
