import rts.GameState;
import rts.PhysicalGameState;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Sampler;

import java.util.LinkedList;
import java.util.List;

public class Playground {
    public static void main(String[] args) throws Exception {
        List<Double> dist = new LinkedList<>();
        List<Integer> out = new LinkedList<>();
        dist.add(0.9); dist.add(0.2); dist.add(0.3);
        out.add(0); out.add(1); out.add(2);

        int output = (int) Sampler.weighted(dist, out);
        System.out.println(output);


    }
}
