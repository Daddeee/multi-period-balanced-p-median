import csv
from random import randrange

x_max = 100
y_max = 100

ns = [50, 100, 150, 200]
ps = [5, 10, 20]
ts = [2, 3, 4, 5]

for n in ns:
    for p in ps:
        for t in ts:
            rows = []
            for i in range(0, n):
                x = randrange(0,x_max)
                y = randrange(0,y_max)
                r = randrange(0,t)
                d = randrange(r,t)
                s = 1
                rows.append([x,y,r,d,s])

            depot = [randrange(0, x_max), randrange(0,y_max)]

            filename = 'rand/n' + str(n) + 'p' + str(p) + 't' + str(t) + '.csv'

            with open(filename, mode='w') as file:
                writer = csv.writer(file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
                writer.writerow([n, p, t])
                writer.writerow(depot)
                for row in rows:
                    writer.writerow(row)
