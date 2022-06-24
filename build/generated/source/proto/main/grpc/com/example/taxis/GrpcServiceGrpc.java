package com.example.taxis;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.25.0)",
    comments = "Source: GrpcService.proto")
public final class GrpcServiceGrpc {

  private GrpcServiceGrpc() {}

  public static final String SERVICE_NAME = "com.example.taxis.GrpcService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.example.taxis.GrpcServiceOuterClass.HelloRequest,
      com.example.taxis.GrpcServiceOuterClass.HelloResponse> getGreetingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "greeting",
      requestType = com.example.taxis.GrpcServiceOuterClass.HelloRequest.class,
      responseType = com.example.taxis.GrpcServiceOuterClass.HelloResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.taxis.GrpcServiceOuterClass.HelloRequest,
      com.example.taxis.GrpcServiceOuterClass.HelloResponse> getGreetingMethod() {
    io.grpc.MethodDescriptor<com.example.taxis.GrpcServiceOuterClass.HelloRequest, com.example.taxis.GrpcServiceOuterClass.HelloResponse> getGreetingMethod;
    if ((getGreetingMethod = GrpcServiceGrpc.getGreetingMethod) == null) {
      synchronized (GrpcServiceGrpc.class) {
        if ((getGreetingMethod = GrpcServiceGrpc.getGreetingMethod) == null) {
          GrpcServiceGrpc.getGreetingMethod = getGreetingMethod =
              io.grpc.MethodDescriptor.<com.example.taxis.GrpcServiceOuterClass.HelloRequest, com.example.taxis.GrpcServiceOuterClass.HelloResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "greeting"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.taxis.GrpcServiceOuterClass.HelloRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.taxis.GrpcServiceOuterClass.HelloResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GrpcServiceMethodDescriptorSupplier("greeting"))
              .build();
        }
      }
    }
    return getGreetingMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.example.taxis.GrpcServiceOuterClass.RideElectionRequest,
      com.example.taxis.GrpcServiceOuterClass.RideElectionResponse> getElectionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "election",
      requestType = com.example.taxis.GrpcServiceOuterClass.RideElectionRequest.class,
      responseType = com.example.taxis.GrpcServiceOuterClass.RideElectionResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.taxis.GrpcServiceOuterClass.RideElectionRequest,
      com.example.taxis.GrpcServiceOuterClass.RideElectionResponse> getElectionMethod() {
    io.grpc.MethodDescriptor<com.example.taxis.GrpcServiceOuterClass.RideElectionRequest, com.example.taxis.GrpcServiceOuterClass.RideElectionResponse> getElectionMethod;
    if ((getElectionMethod = GrpcServiceGrpc.getElectionMethod) == null) {
      synchronized (GrpcServiceGrpc.class) {
        if ((getElectionMethod = GrpcServiceGrpc.getElectionMethod) == null) {
          GrpcServiceGrpc.getElectionMethod = getElectionMethod =
              io.grpc.MethodDescriptor.<com.example.taxis.GrpcServiceOuterClass.RideElectionRequest, com.example.taxis.GrpcServiceOuterClass.RideElectionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "election"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.taxis.GrpcServiceOuterClass.RideElectionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.taxis.GrpcServiceOuterClass.RideElectionResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GrpcServiceMethodDescriptorSupplier("election"))
              .build();
        }
      }
    }
    return getElectionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideRequest,
      com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideResponse> getNotifyTaxisAfterRideMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "notifyTaxisAfterRide",
      requestType = com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideRequest.class,
      responseType = com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideRequest,
      com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideResponse> getNotifyTaxisAfterRideMethod() {
    io.grpc.MethodDescriptor<com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideRequest, com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideResponse> getNotifyTaxisAfterRideMethod;
    if ((getNotifyTaxisAfterRideMethod = GrpcServiceGrpc.getNotifyTaxisAfterRideMethod) == null) {
      synchronized (GrpcServiceGrpc.class) {
        if ((getNotifyTaxisAfterRideMethod = GrpcServiceGrpc.getNotifyTaxisAfterRideMethod) == null) {
          GrpcServiceGrpc.getNotifyTaxisAfterRideMethod = getNotifyTaxisAfterRideMethod =
              io.grpc.MethodDescriptor.<com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideRequest, com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "notifyTaxisAfterRide"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GrpcServiceMethodDescriptorSupplier("notifyTaxisAfterRide"))
              .build();
        }
      }
    }
    return getNotifyTaxisAfterRideMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static GrpcServiceStub newStub(io.grpc.Channel channel) {
    return new GrpcServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static GrpcServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new GrpcServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static GrpcServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new GrpcServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class GrpcServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void greeting(com.example.taxis.GrpcServiceOuterClass.HelloRequest request,
        io.grpc.stub.StreamObserver<com.example.taxis.GrpcServiceOuterClass.HelloResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGreetingMethod(), responseObserver);
    }

    /**
     */
    public void election(com.example.taxis.GrpcServiceOuterClass.RideElectionRequest request,
        io.grpc.stub.StreamObserver<com.example.taxis.GrpcServiceOuterClass.RideElectionResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getElectionMethod(), responseObserver);
    }

    /**
     */
    public void notifyTaxisAfterRide(com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideRequest request,
        io.grpc.stub.StreamObserver<com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getNotifyTaxisAfterRideMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGreetingMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.example.taxis.GrpcServiceOuterClass.HelloRequest,
                com.example.taxis.GrpcServiceOuterClass.HelloResponse>(
                  this, METHODID_GREETING)))
          .addMethod(
            getElectionMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.example.taxis.GrpcServiceOuterClass.RideElectionRequest,
                com.example.taxis.GrpcServiceOuterClass.RideElectionResponse>(
                  this, METHODID_ELECTION)))
          .addMethod(
            getNotifyTaxisAfterRideMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideRequest,
                com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideResponse>(
                  this, METHODID_NOTIFY_TAXIS_AFTER_RIDE)))
          .build();
    }
  }

  /**
   */
  public static final class GrpcServiceStub extends io.grpc.stub.AbstractStub<GrpcServiceStub> {
    private GrpcServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private GrpcServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GrpcServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new GrpcServiceStub(channel, callOptions);
    }

    /**
     */
    public void greeting(com.example.taxis.GrpcServiceOuterClass.HelloRequest request,
        io.grpc.stub.StreamObserver<com.example.taxis.GrpcServiceOuterClass.HelloResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGreetingMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void election(com.example.taxis.GrpcServiceOuterClass.RideElectionRequest request,
        io.grpc.stub.StreamObserver<com.example.taxis.GrpcServiceOuterClass.RideElectionResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getElectionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void notifyTaxisAfterRide(com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideRequest request,
        io.grpc.stub.StreamObserver<com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getNotifyTaxisAfterRideMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class GrpcServiceBlockingStub extends io.grpc.stub.AbstractStub<GrpcServiceBlockingStub> {
    private GrpcServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private GrpcServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GrpcServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new GrpcServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.example.taxis.GrpcServiceOuterClass.HelloResponse greeting(com.example.taxis.GrpcServiceOuterClass.HelloRequest request) {
      return blockingUnaryCall(
          getChannel(), getGreetingMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.example.taxis.GrpcServiceOuterClass.RideElectionResponse election(com.example.taxis.GrpcServiceOuterClass.RideElectionRequest request) {
      return blockingUnaryCall(
          getChannel(), getElectionMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideResponse notifyTaxisAfterRide(com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideRequest request) {
      return blockingUnaryCall(
          getChannel(), getNotifyTaxisAfterRideMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class GrpcServiceFutureStub extends io.grpc.stub.AbstractStub<GrpcServiceFutureStub> {
    private GrpcServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private GrpcServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GrpcServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new GrpcServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.taxis.GrpcServiceOuterClass.HelloResponse> greeting(
        com.example.taxis.GrpcServiceOuterClass.HelloRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGreetingMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.taxis.GrpcServiceOuterClass.RideElectionResponse> election(
        com.example.taxis.GrpcServiceOuterClass.RideElectionRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getElectionMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideResponse> notifyTaxisAfterRide(
        com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getNotifyTaxisAfterRideMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GREETING = 0;
  private static final int METHODID_ELECTION = 1;
  private static final int METHODID_NOTIFY_TAXIS_AFTER_RIDE = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final GrpcServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(GrpcServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GREETING:
          serviceImpl.greeting((com.example.taxis.GrpcServiceOuterClass.HelloRequest) request,
              (io.grpc.stub.StreamObserver<com.example.taxis.GrpcServiceOuterClass.HelloResponse>) responseObserver);
          break;
        case METHODID_ELECTION:
          serviceImpl.election((com.example.taxis.GrpcServiceOuterClass.RideElectionRequest) request,
              (io.grpc.stub.StreamObserver<com.example.taxis.GrpcServiceOuterClass.RideElectionResponse>) responseObserver);
          break;
        case METHODID_NOTIFY_TAXIS_AFTER_RIDE:
          serviceImpl.notifyTaxisAfterRide((com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideRequest) request,
              (io.grpc.stub.StreamObserver<com.example.taxis.GrpcServiceOuterClass.TaxiInfoAfterRideResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class GrpcServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    GrpcServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.example.taxis.GrpcServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("GrpcService");
    }
  }

  private static final class GrpcServiceFileDescriptorSupplier
      extends GrpcServiceBaseDescriptorSupplier {
    GrpcServiceFileDescriptorSupplier() {}
  }

  private static final class GrpcServiceMethodDescriptorSupplier
      extends GrpcServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    GrpcServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (GrpcServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new GrpcServiceFileDescriptorSupplier())
              .addMethod(getGreetingMethod())
              .addMethod(getElectionMethod())
              .addMethod(getNotifyTaxisAfterRideMethod())
              .build();
        }
      }
    }
    return result;
  }
}
