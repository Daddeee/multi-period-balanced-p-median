import csv
import sys
import numpy as np
import matplotlib.pyplot as plt

points = []

with open(sys.argv[1]) as csv_file:
    reader = csv.reader(csv_file, delimiter=',')
    count = 0
    for row in reader:
        point = [float(row[0]), float(row[1])]
        points.append(point)

points = np.array(points)

fig, ax = plt.subplots()
ax.scatter(points[:,0], points[:,1], s=80)

plt.show()