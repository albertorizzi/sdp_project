package Utils;

import AdministratorServer.Model.Position;

public final class Utils {
    public static double getDistanceBetweenTwoPosition(Position startPosition, Position endPosition) {
        double distance =
                Math.sqrt(
                        Math.pow(endPosition.getX() - startPosition.getX(), 2) +
                                Math.pow(endPosition.getY() - startPosition.getY(), 2)
                );
        return distance;
    }
}
