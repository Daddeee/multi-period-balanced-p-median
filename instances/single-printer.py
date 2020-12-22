import csv
import sys
import numpy as np
import matplotlib.pyplot as plt
from scipy.spatial import ConvexHull, convex_hull_plot_2d

available_colors = ["magenta", "cyan", "red", "green", "blue", "orange"]

points = []
labels = []
colors_dict = {}

medians = []
median_labels = []

with open(sys.argv[1]) as csv_file:
    reader = csv.reader(csv_file, delimiter=',')
    count = 0
    for row in reader:
        point = [float(row[0]), float(row[1])]
        l = int(row[2])
        if l == count:
            medians.append(point)
            median_labels.append(l)
        else:
            points.append(point)
            labels.append(l)
        count = count + 1

for k in sorted(median_labels):
    colors_dict[k] = available_colors.pop()
    print(k, ":", colors_dict[k])

points = np.array(points)
labels = np.array(labels)
colors = [ colors_dict[l] for l in labels ]

medians = np.array(medians)
median_labels = np.array(median_labels)
#median_colors = np.array([ colors_dict[l] for l in median_labels ])
median_colors = np.array([ colors_dict[l] for l in median_labels ])

fig, ax = plt.subplots()
ax.scatter(points[:,0], points[:,1], c=colors, s=80)
ax.scatter(medians[:,0], medians[:,1], c=median_colors, marker="^", s=140)

for m in median_labels:
    #import pdb; pdb.set_trace()
    ps = points[labels == m]
    hull = ConvexHull(ps)
    for simplex in hull.simplices:
        plt.plot(ps[simplex, 0], ps[simplex, 1], color=colors_dict[m])

plt.show()