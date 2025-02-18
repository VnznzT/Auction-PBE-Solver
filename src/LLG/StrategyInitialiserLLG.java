package LLG;
import java.util.ArrayList;
import java.util.List;

import algorithm.PBEContext;
import domains.Belief;
import helpers.StrategyInitialiser;
import strategy.PWCStrategy1Dto2D;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;
public class StrategyInitialiserLLG implements StrategyInitialiser{

    PBEContext context;
    public StrategyInitialiserLLG(PBEContext context){
        this.context=context;
    }

    @Override
    public List<Strategy> init(int subgame, Belief beliefdistribution) {
        double miValLocal = context.getDoubleParameter("minValLocal");
        double maxValLocal = context.getDoubleParameter("maxValLocal");
        double minValGlobal = context.getDoubleParameter("minValGlobal");
        double maxValGlobal = context.getDoubleParameter("maxValGlobal");
        int gridsize = context.getIntParameter("Gridsize");

        List<Strategy> strats = new ArrayList<>();
        
                strats.add(UnivariatePWCStrategy.makeTruthful(minValGlobal,maxValGlobal , gridsize));
                strats.add(UnivariatePWCStrategy.makeTruthful(miValLocal, maxValLocal, gridsize));



        return strats;
    }
}
