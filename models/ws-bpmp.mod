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

param x_avg := n / p;

param s1 := 1 / ( (n - p) * (max{i in V, j in V} c[i,j]) );

param s2 := 1 / (p * x_avg);

### Variables ###

var x {V,V} binary;

var y {V} >= 0;

### Objective ###

minimize distance_and_mad:
        lambda * s1 * (sum {i in V, j in V} c[i,j] * x[i,j]) + (1 - lambda) * s2 * (sum {i in V} y[i]);

### Constraints ###

subject to exclusive_assignment {j in V}:
        sum {i in V} x[i,j] = 1;

subject to number_of_medians:
        sum {i in V} x[i,i] = p;

subject to assign_to_medians_only {i in V, j in V}:
        x[i,j] <= x[i,i];

subject to abs_1 {i in V}:
        sum {j in V} x[i,j] - x_avg*x[i,i] <= y[i];

subject to abs_2 {i in V}:
        x_avg*x[i,i] - sum {j in V} x[i,j] <= y[i];