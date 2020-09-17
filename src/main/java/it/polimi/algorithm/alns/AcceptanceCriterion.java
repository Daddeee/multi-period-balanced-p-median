package it.polimi.algorithm.alns;

public interface AcceptanceCriterion {
    boolean accept(Solution incoming, Solution current);
}
