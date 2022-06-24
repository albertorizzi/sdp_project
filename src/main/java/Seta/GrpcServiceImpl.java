package Seta;

import AdministratorServer.Model.Position;
import AdministratorServer.Model.Ride;
import AdministratorServer.Model.Taxi;
import Utils.Utils;
import com.example.taxis.GrpcServiceGrpc;
import com.example.taxis.GrpcServiceOuterClass;
import com.sun.javafx.scene.traversal.SubSceneTraversalEngine;
import io.grpc.stub.StreamObserver;

public class GrpcServiceImpl extends GrpcServiceGrpc.GrpcServiceImplBase {
    @Override
    public void greeting(GrpcServiceOuterClass.HelloRequest request, StreamObserver<GrpcServiceOuterClass.HelloResponse> responseObserver) {

        System.out.println("Sto aggiungendo taxi " + request.getId());

        Position position = new Position(request.getPosition().getX(), request.getPosition().getY());

        Taxi taxi = new Taxi(
                request.getId(),
                request.getPort(),
                request.getIp(),
                request.getBatteryLevel(),
                position
        );

        TaxiIstance.getInstance().addTaxi(taxi);

        System.out.println("Aggiunto taxi" + taxi.getId());


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

        /*
            CRITERI IN ORDINE DI RILEVANZA:
            1. Se il taxi è giù in un'altra corsa va scartata
            2. Il taxi deve avere la distanza minima dal punto di partenza della corsa
            3. Il taxi deve avere il più alto livello di batteria
            4. Il taxi deve avere ID più grande
        */
        GrpcServiceOuterClass.RideElectionResponse response = null;

        Position startPositionRide = new Position(request.getStartPositionRide().getX(), request.getStartPositionRide().getY());

        // controllo che la richiesta provengo dal distretto dove mi trovo
        if (startPositionRide.getDistrictByPosition() == TaxiIstance.getInstance().getMyTaxi().getPosition().getDistrictByPosition()) {


            // se taxi è impegnato in corsa e carica
            if (!TaxiIstance.getInstance().getMyTaxi().isInRide() && !TaxiIstance.getInstance().getMyTaxi().isInCharge()) {

                //confronto le distanze tra il taxi che mi ha inviato la richiesta e la mia
                double distanzeOfTaxiRequest = Utils.getDistanceBetweenTwoPosition(startPositionRide,
                        TaxiIstance.getInstance().getPositionOfTaxi(request.getIdTaxi()));

                double distanzeOfMyTaxi = Utils.getDistanceBetweenTwoPosition(startPositionRide,
                        TaxiIstance.getInstance().getMyTaxi().getPosition()); //TODO:

                if (distanzeOfTaxiRequest < distanzeOfMyTaxi) {
                    // rispondo OK perchè taxi è più lontano del taxi che ha fatto la request
                    response = GrpcServiceOuterClass.RideElectionResponse
                            .newBuilder()
                            .setIdRide(request.getIdRide())
                            .setIdTaxi(request.getIdTaxi())
                            .setMessageElection("OK")
                            .build();

                } else if (distanzeOfTaxiRequest == distanzeOfMyTaxi) {
                    // nostra distanza uguale, quindi continuo i controlli

                    int batteryLevelOfTaxiRequest = request.getBatteryLevel();
                    int batteryLevelOfMyTaxi = TaxiIstance.getInstance().getMyTaxi().getBatteryLevel();


                    if (batteryLevelOfTaxiRequest > batteryLevelOfMyTaxi) {
                        // rispondo OK perchè taxi che ha fatto la richiesta ha una batteria maggiore della mia
                        response = GrpcServiceOuterClass.RideElectionResponse
                                .newBuilder()
                                .setIdRide(request.getIdRide())
                                .setIdTaxi(request.getIdTaxi())
                                .setMessageElection("OK")
                                .build();

                    } else if (batteryLevelOfTaxiRequest == batteryLevelOfMyTaxi) {
                        // proseguo con i controlli verificando chi ha id più altro

                        if (request.getIdTaxi() > TaxiIstance.getInstance().getMyTaxi().getId()) {
                            response = GrpcServiceOuterClass.RideElectionResponse
                                    .newBuilder()
                                    .setIdRide(request.getIdRide())
                                    .setIdTaxi(request.getIdTaxi())
                                    .setMessageElection("OK")
                                    .build();
                        } else {
                            response = GrpcServiceOuterClass.RideElectionResponse
                                    .newBuilder()
                                    .setIdRide(request.getIdRide())
                                    .setIdTaxi(request.getIdTaxi())
                                    .setMessageElection("NO")
                                    .build();
                        }

                    } else {
                        // sono migliore io, quindi ti invio NO
                        response = GrpcServiceOuterClass.RideElectionResponse
                                .newBuilder()
                                .setIdRide(request.getIdRide())
                                .setIdTaxi(request.getIdTaxi())
                                .setMessageElection("NO")
                                .build();
                    }


                } else {
                    // sono migliore io, quindi ti invio NO
                    response = GrpcServiceOuterClass.RideElectionResponse
                            .newBuilder()
                            .setIdRide(request.getIdRide())
                            .setIdTaxi(request.getIdTaxi())
                            .setMessageElection("NO")
                            .build();
                }

            } else {
                // rispondo OK perchè taxi è impegnato in una corsa oppure è in carica
                response = GrpcServiceOuterClass.RideElectionResponse
                        .newBuilder()
                        .setIdRide(request.getIdRide())
                        .setIdTaxi(request.getIdTaxi())
                        .setMessageElection("OK")
                        .build();
            }
        } else {
            // rispondo OK perchè la posizione richiesta è in un altro distretto
            response = GrpcServiceOuterClass.RideElectionResponse
                    .newBuilder()
                    .setIdRide(request.getIdRide())
                    .setIdTaxi(request.getIdTaxi())
                    .setMessageElection("OK")
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    public void notifyTaxisAfterRide(GrpcServiceOuterClass.TaxiInfoAfterRideRequest request, StreamObserver<GrpcServiceOuterClass.TaxiInfoAfterRideResponse> responseObserver) {
        System.out.println("Notifica da parte di " + request.getIdTaxi() + "per aggiornare i suoi dati di fine corsa");
        Position finalPosition = new Position(request.getFinalPosition().getX(), request.getFinalPosition().getY());
        TaxiIstance.getInstance().updateSingleTaxi(request.getIdTaxi(), finalPosition, request.getBatteryLevel());

        System.out.println("HO AGGIORNATO I DATI DEL TAXI CHE HA FINITO LA CORSA");
        GrpcServiceOuterClass.TaxiInfoAfterRideResponse response = GrpcServiceOuterClass.TaxiInfoAfterRideResponse
                .newBuilder()
                .setMessageResponse("OK")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
