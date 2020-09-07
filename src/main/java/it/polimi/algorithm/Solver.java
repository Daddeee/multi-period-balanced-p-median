package it.polimi.algorithm;

import it.polimi.domain.Problem;
import it.polimi.domain.Solution;

public interface Solver {
    Solution run(Problem problem);
}
