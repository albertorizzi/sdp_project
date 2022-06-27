package AdministratorServer.Model;

import javafx.geometry.Pos;

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

        if (x <= 4) { // D1 or D4
            if (y <= 4) {
                return 1;
            } else {
                return 4;
            }
        } else { // D2 or D3
            if (y <= 4) {
                return 3;
            } else {
                return 2;
            }
        }
    }

    public ArrayList<Integer> getPositionOfRechargeStationByDistrict() {
        int districtByPosition = getDistrictByPosition();
        ArrayList<Integer> stationRechargePosition = new ArrayList<>();


        switch (districtByPosition) {
            case 1:
                stationRechargePosition.add(0);
                stationRechargePosition.add(9);
                break;
            case 2:
                stationRechargePosition.add(9);
                stationRechargePosition.add(9);
                break;
            case 3:
                stationRechargePosition.add(9);
                stationRechargePosition.add(0);

                break;
            case 4:
                stationRechargePosition.add(0);
                stationRechargePosition.add(0);
                break;
        }
        return stationRechargePosition;
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
