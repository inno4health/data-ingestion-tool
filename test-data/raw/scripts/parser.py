import csv

with open("file2.csv", "r") as fi, open('file2output.csv', 'w', newline='') as fo:
    reader = csv.reader(fi, delimiter=",")
    writer = csv.writer(fo)

    for i, line in enumerate(reader):
        setNumber, exercise, repNumber = "", "", ""
        if (line[7] != "1" and line[7] != "label"):
            vals = line[7].split('_')
            print(vals)
            setNumber = vals[0]
            exercise = vals[1]
            repNumber = vals[2]
        
        writer.writerow([line[0], line[1], line[2], line[3], line[4], line[5], line[6], line[7], setNumber, exercise, repNumber])
        # print('line[{}] = {}'.format(i, line[9]))