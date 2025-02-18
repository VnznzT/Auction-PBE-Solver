package cluster;

import java.nio.file.Path;
import java.util.List;

import algorithm.PBEContext;
import strategy.Strategy;
import transition.FingerPrint;
import utility.Utility;

public interface Cache<Value,Bid> {
	public void parse();
	public List<Utility<Value>> getUtilities(int ref);
	public List<Strategy<Value,Bid>> getStrategies(int ref);

	public List<Utility<Value>> getUtilities(int ref, int subgame);

	public List<Strategy<Value,Bid>> getStrategies(int ref, int subgame);

	List<Utility<Value>> getPrevUtilities(int ref);

	List<Utility<Value>> getPrevUtilities(int ref, int subgame);


}
