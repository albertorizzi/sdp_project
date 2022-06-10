package Pollution;

import java.util.ArrayList;

public class MeasuramentManager implements Buffer{
    public MeasuramentManager(){} // costruttore

    public ArrayList<Measurement> buffer = new ArrayList<>();

    // produttore
    @Override
    public synchronized void addMeasurement(Measurement m) {
        //System.out.println("Produttore - Insert " + m);
        buffer.add(m);
        if (buffer.size() == 8){
            //System.out.println("Produttore - Notify");
            this.notify();
        }
    }

    // consumatore
    @Override
    public synchronized ArrayList<Measurement> readAllAndClean() {
        // System.out.println("Consumatore - Starting...");
        ArrayList<Measurement> measurementsCopy = new ArrayList<>();

        while(buffer.size() < 8) {
            try {
                //System.out.println("Consumatore - Wait");
                this.wait();
            } catch (InterruptedException e) { e.printStackTrace(); }
        }

        if(buffer.size() == 8){

            measurementsCopy = new ArrayList<>(buffer);

            // cancello buffer a metà - così da garantire overlap al 50%
            for (int i = 0; i < 4; i++)
                buffer.remove(i);

        }

        //System.out.println("measurementsCopy: " + measurementsCopy); // debug
        return measurementsCopy;
    }
}
