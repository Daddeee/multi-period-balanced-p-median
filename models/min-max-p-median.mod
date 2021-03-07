### Params and sets ###

# number of customers
param n;
# number of medians
param p;

# set of customers
set V := 0..n-1;

# distance between each pair of customers
param c {V,V} >= 0;

param M {V};

### Variables ###

var x {V,V} binary;
var u;
var l;

### Objective ###

minimize max_min:
        u - l;

### Constraints ###

subject to exclusive_assignment {i in V}:
        sum {j in V} x[i,j] = 1;

subject to number_of_medians:
        sum {j in V} x[j,j] = p;

subject to assign_to_medians_only {i in V, j in V}:
        x[i,j] <= x[j,j];

subject to define_u {j in V}:
        u >= sum {i in V} x[i,j];

subject to define_l {j in V}:
        l <= sum {i in V} x[i,j] + n * (1 - x[j,j]);

subject to assign_to_closest {i in V, j in V}:
        sum {k in V} c[i,k]*x[i,k] + (M[i] - c[i,j]) * x[j,j] <= M[i];