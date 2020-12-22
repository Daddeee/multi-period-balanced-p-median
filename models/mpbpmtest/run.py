import os

d = '.'
folders = [os.path.join(d, o) for o in os.listdir(d)
 if os.path.isdir(os.path.join(d,o)) and o != "./ampl_linux-intel64"]
folders = [x for x in folders if x !=  './ampl_linux-intel64']
folders = sorted(folders, key=lambda x:  [int(k) for k in x.replace("./n", "").replace("p", ",").replace("t", ",").split(",")])
count = 0
for f in folders:
    if count == 5:
        break;

    os.chdir(f + "/")
    os.system("../ampl_linux-intel64/ampl multi-period-balanced-p-median-old.run")
    os.chdir("..")

    file1 = open('time.txt', 'r')
    time = float(file1.readlines()[0])

    if time >=3590:
        print("\ntime limit reached. stop.")
        break

    count += 1