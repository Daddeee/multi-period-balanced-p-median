import os

for i in range(1,41):
    os.chdir(str(i) + "/")
    os.system("../ampl_linux-intel64/ampl balanced-p-median-old.run")
    os.chdir("..")

    file1 = open('time.txt', 'r')
    time = float(file1.readlines()[0])

    if time >=3550:
        break