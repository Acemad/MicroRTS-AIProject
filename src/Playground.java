import rts.GameState;
import rts.PhysicalGameState;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;

import java.util.LinkedList;
import java.util.List;

public class Playground {
    public static void main(String[] args) throws Exception {
        UnitTypeTable unitTypeTable = new UnitTypeTable();
        PhysicalGameState physicalGameState = PhysicalGameState.load("C:\\Acemad\\dRepos\\microrts-unmodified\\maps\\8x8\\basesWorkers8x8.xml", unitTypeTable);
        GameState gameState = new GameState(physicalGameState, unitTypeTable);

        for (Unit unit : physicalGameState.getUnits()) {
            if (unit.getPlayer() == 0) {
                List<UnitAction> actions = unit.getUnitActionsWithoutWait(gameState);
                System.out.println(actions);
            }
        }

    }
}
