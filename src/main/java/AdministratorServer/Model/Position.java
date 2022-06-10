package AdministratorServer.Model;

import java.util.ArrayList;
import java.util.List;

public class Position {
    private int x;
    private int y;

    public Position() {
    }

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getDistrictByPosition() {
        int x = this.getX();
        int y = this.getY();

        if (x <= 4) { // D1 o D4
            if (y <= 4) {
                return 1;
            } else {
                return 4;
            }
        } else {
            if (y <= 4) {
                return 2;
            } else {
                return 3;
            }
        }
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
