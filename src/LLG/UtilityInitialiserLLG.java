package LLG;

import algorithm.PBEContext;
import domains.Belief;
import helpers.UtilityInitialiser;
import utility.Utility;
import utility.PWCUtility;
import java.util.ArrayList;
import java.util.List;

public class UtilityInitialiserLLG implements UtilityInitialiser {
    PBEContext context;
    public UtilityInitialiserLLG(PBEContext context){
        this.context=context;
    }

    @Override
    public List<Utility> init(int subgame, Belief beliefdistribution) {
        double miValLocal = context.getDoubleParameter("minValLocal");
        double maxValLocal = context.getDoubleParameter("maxValLocal");
        double minValGlobal = context.getDoubleParameter("minValGlobal");
        double maxValGlobal = context.getDoubleParameter("maxValGlobal");

        List<Utility> utils = new ArrayList<>();
        int nBidders = context.getnBidders(subgame);


        utils.add(PWCUtility.makeZero(minValGlobal, maxValGlobal));
        for (int k=1;k<nBidders;k++){
            utils.add(PWCUtility.makeZero(miValLocal, maxValLocal));
        }

        return utils;
    }

}
