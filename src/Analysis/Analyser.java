package Analysis;

import algorithm.SolverCluster;

public interface Analyser {
    GameAnalysis analyse(SolverCluster.Result result);
}
