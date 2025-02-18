package LLG;

import domains.Belief;

public class BeliefLLG implements Belief<Double> {

        public BeliefLLG(Double[][] newBeliefArray) {
            this.beliefSets=newBeliefArray;
        }
        public Double[][] beliefSets;

        public Double getMin(int player) {
            return beliefSets[player][0];
        }
        public Double getMax(int player) {
            return beliefSets[player][1];
        }
}
