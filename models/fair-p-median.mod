### Params and sets ###

# number of customers
param n;
# number of medians
param p;

# set of customers
set V := 0..n-1;

# distance between each pair of customers
param c {V,V} >= 0;

param lambda; # weighted average

param Dbeta;

### Variables ###

var x {V,V} binary;

var u;

var v {V} >= 0;

param s1 := 1 / ( (n - p) * (max{i in V, j in V} c[i,j]) );

### Objective ###

minimize weighted_average_cost_dual_fairness:
        lambda * s1 * (sum {i in V, j in V} c[i,j]*x[i,j])
        + (1 - lambda) * (Dbeta*u + sum {i in V} v[i]);

### Constraints ###

subject to dual_fairness {i in V}:
        u + v[i] >= (1/Dbeta) * (sum {j in V} x[i,j]);

subject to exclusive_assignment {j in V}:
        sum {i in V} x[i,j] = 1;

subject to number_of_medians:
        sum {i in V} x[i,i] = p;

subject to assign_to_medians_only {i in V, j in V}:
        x[i,j] <= x[i,i];