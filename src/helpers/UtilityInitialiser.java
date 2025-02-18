package helpers;

import java.util.List;

import domains.Belief;
import utility.Utility;

public interface UtilityInitialiser {
	public List<Utility> init(int subgame, Belief beliefdistribution);
}
