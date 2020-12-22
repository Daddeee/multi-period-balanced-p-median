import csv
import sys
import numpy as np
import matplotlib.pyplot as plt

from scipy.spatial import ConvexHull, convex_hull_plot_2d

available_colors = ["magenta", "cyan", "red", "green", "blue", "orange"]

period_points = []
period_labels = []
period_hulls = []
period_medians = set()

points = []
labels = []

colors_dict = {}

selected_period = int(sys.argv[2])

with open(sys.argv[1]) as csv_file:
    reader = csv.reader(csv_file, delimiter=',')
    for row in reader:
        period = int(row[2])
        l = int(row[4])
        if selected_period == period:
            period_points.append([float(row[0]), float(row[1])])
            period_labels.append(l)
            period_medians.add(l)
        else:
            points.append([float(row[0]), float(row[1])])
            labels.append(l)

        if not l in colors_dict:
            colors_dict[l] = available_colors.pop()

points = np.array(points)
labels = np.array(labels)
colors = [ colors_dict[l] for l in labels ]

period_points = np.array(period_points)
period_labels = np.array(period_labels)
period_colors = [ colors_dict[l] for l in period_labels ]

fig, ax = plt.subplots()
ax.scatter(points[:,0], points[:,1], c=colors, s=80, alpha=0.2)
ax.scatter(period_points[:,0], period_points[:,1], c=period_colors, s=80, alpha=1)

avgArea = 0.
avgCont = 0.
for m in period_medians:
    ps = period_points[period_labels == m]
    hull = ConvexHull(ps)
    area = hull.volume
    avgArea += area
    avgCont += len(ps)
    print("Area", m, ":", area)
    print("Cont", m, ":", len(ps))

    for simplex in hull.simplices:
        plt.plot(ps[simplex, 0], ps[simplex, 1], color=colors_dict[m])

print("AVG AREA:", avgArea/len(period_medians))
print("AVG CONT:", avgCont/len(period_medians))

plt.show()