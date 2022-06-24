package Utils;

import AdministratorServer.Model.Position;

public final class Utils {
    public static Double getDistanceBetweenTwoPosition(Position startPosition, Position endPosition) {
        return Math.sqrt((endPosition.getX() - startPosition.getX()) ^ 2 + (endPosition.getY() - startPosition.getY()) ^ 2);
    }
}
