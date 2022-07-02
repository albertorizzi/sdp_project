package Seta;

import AdministratorServer.Model.Position;
import AdministratorServer.Model.Ride;
import AdministratorServer.Model.Taxi;
import Utils.Utils;
import com.example.taxis.GrpcServiceGrpc;
import com.example.taxis.GrpcServiceOuterClass;
import com.sun.javafx.scene.traversal.SubSceneTraversalEngine;
import io.grpc.stub.StreamObserver;

import java.util.Date;
import java.util.PrimitiveIterator;

public class GrpcServiceImpl extends GrpcServiceGrpc.GrpcServiceImplBase {
    @Override
    public void greeting(GrpcServiceOuterClass.HelloRequest request, StreamObserver<GrpcServiceOuterClass.HelloResponse> responseObserver) {

        Position position = new Position(request.getPosition().getX(), request.getPosition().getY());

        Taxi taxi = new Taxi(
                request.getId(),
                request.getPort(),
                request.getIp(),
                request.getBatteryLevel(),
                position
        );

        TaxiIstance.getInstance().addTaxi(taxi);

        System.out.println("\n" + "🚕 AGGIUNTO Taxi " + taxi.getId());

        GrpcServiceOuterClass.HelloResponse response = GrpcServiceOuterClass.HelloResponse
                .newBuilder()
                .setId(request.getId())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void election(GrpcServiceOuterClass.RideElectionRequest request, StreamObserver<GrpcServiceOuterClass.RideElectionResponse> responseObserver) {
        System.out.println("\n" + "🚕 Made election for ride " + request.getIdRide() + " from Taxi " + request.getIdTaxi());

        /*
            Rides Management:
            1. the Taxi must not be already involved in another ride or a recharge process
            2. the Taxi must have the minimum distance from the starting point of the ride
            3. the Taxi must have the highest battery level
            4. the Taxi must have better id
        */
        GrpcServiceOuterClass.RideElectionResponse response = null;

        Position startPositionRide = new Position(request.getStartPositionRide().getX(), request.getStartPositionRide().getY());
        System.out.println("Start district ride " + startPositionRide.getDistrictByPosition());
        System.out.println("Position district taxi " + TaxiIstance.getInstance().getMyTaxi().getPosition().getDistrictByPosition());

        // check if ride request come from district where I'm
        if (startPositionRide.getDistrictByPosition() == TaxiIstance.getInstance().getMyTaxi().getPosition().getDistrictByPosition()) {
            System.out.println("I'm in the SAME district");

            // check if taxi is busy in a ride or in recharging
            if (!TaxiIstance.getInstance().isInRide() && !TaxiIstance.getInstance().isInCharge() && TaxiIstance.getInstance().getIdRideInElection() == request.getIdRide()) {
                System.out.println("ELECTION PARTECIPATE");

                // comparison distances between Taxi position request and my position
                double distanzeOfTaxiRequest = Utils.getDistanceBetweenTwoPosition(startPositionRide,
                        TaxiIstance.getInstance().getPositionOfTaxi(request.getIdTaxi()));

                double distanzeOfMyTaxi = Utils.getDistanceBetweenTwoPosition(startPositionRide,
                        TaxiIstance.getInstance().getMyTaxi().getPosition());

                if (distanzeOfTaxiRequest < distanzeOfMyTaxi) {
                    System.out.println("DISTANCE of Taxi REQUEST < DISTANCE of MY Taxi");


                    // OK because my Taxi is farest than taxi of request
                    //System.out.println("📍 ELECTION for ride " + request.getIdRide() + " WON by Taxi " + request.getIdTaxi());

                    response = GrpcServiceOuterClass.RideElectionResponse
                            .newBuilder()
                            .setIdRide(request.getIdRide())
                            .setIdTaxi(request.getIdTaxi())
                            .setMessageElection("OK")
                            .build();

                } else if (distanzeOfTaxiRequest == distanzeOfMyTaxi) {
                    System.out.println("DISTANCE of Taxi REQUEST === DISTANCE of MY Taxi");

                    // equal distances
                    int batteryLevelOfTaxiRequest = request.getBatteryLevel();
                    int batteryLevelOfMyTaxi = TaxiIstance.getInstance().getMyTaxi().getBatteryLevel();

                    if (batteryLevelOfTaxiRequest > batteryLevelOfMyTaxi) {
                        System.out.println("BATTERY level of Taxi REQUEST is BETTER than BATTERY of MY Taxi");

                        // OK because Taxi request batteryLeval is much than my batteryLevel

                        // System.out.println("📍 ELECTION for ride " + request.getIdRide() + " WON by Taxi " + request.getIdTaxi());

                        response = GrpcServiceOuterClass.RideElectionResponse
                                .newBuilder()
                                .setIdRide(request.getIdRide())
                                .setIdTaxi(request.getIdTaxi())
                                .setMessageElection("OK")
                                .build();

                    } else if (batteryLevelOfTaxiRequest == batteryLevelOfMyTaxi) {
                        System.out.println("BATTERY level of Taxi REQUEST === BATTERY of MY Taxi");

                        // equal batteryLevel, continue checking with IdTaxi
                        // System.out.println("📍 ELECTION for ride " + request.getIdRide() + " WON by Taxi " + request.getIdTaxi());

                        if (request.getIdTaxi() > TaxiIstance.getInstance().getMyTaxi().getId()) {
                            System.out.println("idTaxi request: " + request.getIdTaxi());
                            System.out.println("idTaxi MY: " + TaxiIstance.getInstance().getMyTaxi().getId());

                            response = GrpcServiceOuterClass.RideElectionResponse
                                    .newBuilder()
                                    .setIdRide(request.getIdRide())
                                    .setIdTaxi(request.getIdTaxi())
                                    .setMessageElection("OK")
                                    .build();
                        } else {
                            // NO because my IdTaxi is >
                            response = GrpcServiceOuterClass.RideElectionResponse
                                    .newBuilder()
                                    .setIdRide(request.getIdRide())
                                    .setIdTaxi(request.getIdTaxi())
                                    .setMessageElection("NO")
                                    .build();
                        }
                    } else {
                        System.out.println("BATTERY level of MY Taxi is BETTER than BATTERY of Taxi request");

                        // NO because my batteryLevel is >
                        response = GrpcServiceOuterClass.RideElectionResponse
                                .newBuilder()
                                .setIdRide(request.getIdRide())
                                .setIdTaxi(request.getIdTaxi())
                                .setMessageElection("NO")
                                .build();
                    }
                } else {

                    System.out.println("DISTANCE of Taxi REQUEST > DISTANCE of MY Taxi");

                    // my distance is better
                    response = GrpcServiceOuterClass.RideElectionResponse
                            .newBuilder()
                            .setIdRide(request.getIdRide())
                            .setIdTaxi(request.getIdTaxi())
                            .setMessageElection("NO")
                            .build();
                }
            } else {
                // OK because Taxi is busy in a ride or is in recharging
                // System.out.println("📍 ELECTION for ride " + request.getIdRide() + " WON by Taxi " + request.getIdTaxi());

                System.out.println("I DON'T PARTECIPATE in ELECTION");

                System.out.println(".isInCharge() " + TaxiIstance.getInstance().isInCharge());
                System.out.println(".isInRide() " + TaxiIstance.getInstance().isInRide());
                System.out.println(".isInElection() " + TaxiIstance.getInstance().isInElection());

                // System.out.println("TaxiIstance.getInstance().getIdRideInElection()" + TaxiIstance.getInstance().getIdRideInElection());
                // System.out.println("TaxiIstance.getInstance().getIdRideOnRoad()" + TaxiIstance.getInstance().getIdRideOnRoad());
                // System.out.println("request.idRide" + request.getIdRide());

                if (TaxiIstance.getInstance().getIdRideOnRoad() == request.getIdRide()) {
                    System.out.println("I'm MANAGING RIDE yet");

                    response = GrpcServiceOuterClass.RideElectionResponse
                            .newBuilder()
                            .setIdRide(request.getIdRide())
                            .setIdTaxi(request.getIdTaxi())
                            .setMessageElection("NO")
                            .build();
                } else {
                    System.out.println("I'm NOT MANAGING RIDE");
                    response = GrpcServiceOuterClass.RideElectionResponse
                            .newBuilder()
                            .setIdRide(request.getIdRide())
                            .setIdTaxi(request.getIdTaxi())
                            .setMessageElection("OK")
                            .build();
                }
            }
        } else {
            // OK because startPosition is in another district

            System.out.println("I'm NOT in the SAME district");

            // System.out.println("📍 ELECTION for ride " + request.getIdRide() + " WON by Taxi " + request.getIdTaxi());

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
        System.out.println("\n" + "🗞 COMMUNICATION from Taxi " + request.getIdTaxi() + " to update data after RIDE " + request.getIdRide());
        Position finalPosition = new Position(request.getFinalPosition().getX(), request.getFinalPosition().getY());
        TaxiIstance.getInstance().updateSingleTaxi(request.getIdTaxi(), finalPosition, request.getBatteryLevel());

        GrpcServiceOuterClass.TaxiInfoAfterRideResponse response = GrpcServiceOuterClass.TaxiInfoAfterRideResponse
                .newBuilder()
                .setMessageResponse("OK")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void recharge(GrpcServiceOuterClass.SendRechargeTaxiRequest request, StreamObserver<GrpcServiceOuterClass.ReplyRechargeTaxiResponse> responseObserver) {

        Position positionOfTaxiAfterRide = new Position(request.getRechargeStation().getX(), request.getRechargeStation().getY());

        int idTaxi = request.getIdTaxi();
        long timestampRequest = request.getTimestamp();

        Date date = new Date();
        long actualTime = date.getTime(); // timestamp in ms

        System.out.println("\n" + "⚖️🪫 Made election for STATION RECHARGE of districts " + positionOfTaxiAfterRide.getDistrictByPosition() + " from Taxi " + idTaxi);

        GrpcServiceOuterClass.ReplyRechargeTaxiResponse response = null;

        if (TaxiIstance.getInstance().getMyTaxi().getPosition().getDistrictByPosition() == positionOfTaxiAfterRide.getDistrictByPosition()) {
            if (TaxiIstance.getInstance().getInCharge() == TaxiIstance.RechargeStatus.BATTERY_REQUESTED) {

                // compare actualTime with Timestamp request

                if (actualTime < timestampRequest) {
                    synchronized (TaxiIstance.getInstance().getRechargeLock()) {
                        try {
                            TaxiIstance.getInstance().getRechargeLock().wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    response = GrpcServiceOuterClass.ReplyRechargeTaxiResponse
                            .newBuilder()
                            .setMessageResponse("OK")
                            .build();
                } else {
                    response = GrpcServiceOuterClass.ReplyRechargeTaxiResponse
                            .newBuilder()
                            .setMessageResponse("OK")
                            .build();
                }
            } else if (TaxiIstance.getInstance().getInCharge() == TaxiIstance.RechargeStatus.BATTERY_IN_USED) {
                synchronized (TaxiIstance.getInstance().getRechargeLock()) {
                    try {
                        TaxiIstance.getInstance().getRechargeLock().wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                response = GrpcServiceOuterClass.ReplyRechargeTaxiResponse
                        .newBuilder()
                        .setMessageResponse("OK")
                        .build();

            } else if (TaxiIstance.getInstance().getInCharge() == TaxiIstance.RechargeStatus.BATTERY_NOT_IN_USED) {
                response = GrpcServiceOuterClass.ReplyRechargeTaxiResponse
                        .newBuilder()
                        .setMessageResponse("OK")
                        .build();
            }
        } else {
            // OK because request has a district unequal of my district

            response = GrpcServiceOuterClass.ReplyRechargeTaxiResponse
                    .newBuilder()
                    .setMessageResponse("OK")
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void notifyExit(GrpcServiceOuterClass.SendExitTaxiRequest request, StreamObserver<GrpcServiceOuterClass.ReplyExitTaxiResponse> responseObserver) {

        int idTaxiExit = request.getIdTaxi();

        System.out.println("\n" + "🗑🚖 Request TO EXIT from Taxi with ID: " + idTaxiExit);

        Taxi taxiToRemove = TaxiIstance.getInstance().getTaxiByID(idTaxiExit);

        if (taxiToRemove != null) {
            TaxiIstance.getInstance().removeTaxi(taxiToRemove);
        }

        GrpcServiceOuterClass.ReplyExitTaxiResponse response = null;
        response = GrpcServiceOuterClass.ReplyExitTaxiResponse
                .newBuilder()
                .setMessageResponse("OK")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
