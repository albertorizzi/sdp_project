package Seta;

import AdministratorServer.Model.Position;
import AdministratorServer.Model.Ride;
import AdministratorServer.Model.Taxi;
import com.example.taxis.GrpcServiceGrpc;
import com.example.taxis.GrpcServiceOuterClass;
import io.grpc.stub.StreamObserver;

public class GrpcServiceImpl extends GrpcServiceGrpc.GrpcServiceImplBase {
    @Override
    public void greeting(GrpcServiceOuterClass.HelloRequest request, StreamObserver<GrpcServiceOuterClass.HelloResponse> responseObserver) {

        System.out.println("Sto aggiungendo taxi" + request.getId());

        Position position = new Position(request.getPosition().getX(), request.getPosition().getY());

        Taxi taxi = new Taxi(
                request.getId(),
                request.getPort(),
                request.getIp(),
                request.getBatteryLevel(),
                position
        );

        TaxiIstance.getInstance().addTaxi(taxi);

        System.out.println("Aggiunti taxi" + taxi.getId());


        GrpcServiceOuterClass.HelloResponse response = GrpcServiceOuterClass.HelloResponse
                .newBuilder()
                .setId(
                        request.getId()
                )
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void election(GrpcServiceOuterClass.RideElectionRequest request, StreamObserver<GrpcServiceOuterClass.RideElectionResponse> responseObserver) {
        System.out.println("Ricevo elezione per ride " + request.getIdRide() + " dal taxi " + request.getIdTaxi());

        GrpcServiceOuterClass.RideElectionResponse response = GrpcServiceOuterClass.RideElectionResponse
                .newBuilder()
                .setIdRide(request.getIdRide())
                .setIdTaxi(request.getIdTaxi())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
