import csv
import sys
import numpy as np
import matplotlib.pyplot as plt

available_colors = ["black", "yellow", "magenta", "cyan", "red", "green", "blue"]

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
        if not l in colors_dict:
            colors_dict[l] = available_colors.pop()

points = np.array(points)
labels = np.array(labels)
colors = [ colors_dict[l] for l in labels ]

medians = np.array(medians)
median_labels = np.array(median_labels)
median_colors = np.array([ colors_dict[l] for l in median_labels ])

fig, ax = plt.subplots()
ax.scatter(points[:,0], points[:,1], c=colors)
ax.scatter(medians[:,0], medians[:,1], c=median_colors, marker="^")

plt.show()