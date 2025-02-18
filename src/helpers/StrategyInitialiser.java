package helpers;

import java.util.List;

import domains.Belief;
import strategy.Strategy;

public interface StrategyInitialiser {
	public List<Strategy> init(int subgame, Belief beliefdistribution);
}
