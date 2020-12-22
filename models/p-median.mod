### Params and sets ###

# number of customers
param n;
# number of medians
param p;

# set of customers
set V := 0..n-1;

# distance between each pair of customers
param c {V,V} >= 0;

### Variables ###

var x {V,V} binary;

### Objective ###

minimize cost:
        sum {i in V, j in V} c[i,j] * x[i,j];

### Constraints ###

subject to exclusive_assignment {j in V}:
        sum {i in V} x[i,j] = 1;

subject to number_of_medians:
        sum {i in V} x[i,i] = p;

subject to assign_to_medians_only {i in V, j in V}:
        x[i,j] <= x[i,i];