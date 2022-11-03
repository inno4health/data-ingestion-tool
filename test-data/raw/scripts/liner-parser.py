import csv

with open("label2-exercises/set1-SL-glute-bridge-rep10.csv", "r") as fi, open('label-2/set1-SL-glute-bridge-rep10-line.csv', 'w', newline='') as fo:

    reader = csv.reader(fi, delimiter=";")
    writer = csv.writer(fo)
    emgdata = []
    header = []
    
    first_row = next(reader)
    col_len = len(first_row)

    for i in range (col_len-1):
        emgdata.append("")
    for i in range (col_len-5):
        header.append("")
    
    for k in range(1,col_len-4):
        header[k-1] = first_row[k].replace(' ', '')
    
    readFirstRow = False
    for i, line in enumerate(reader):
        if (not readFirstRow):
            readFirstRow = True
            emgdata[col_len-5] = line[col_len-4]
            emgdata[col_len-4] = line[col_len-3]
            emgdata[col_len-3] = line[col_len-2]
            emgdata[col_len-2] = line[col_len-1]
        for k in range(1,col_len-4):
            if (line[k]):
                emgdata[k-1] += line[k] + " "
            else:
                # if value not exists in csv file, add 0
                emgdata[k-1] += "0" + " "
            
    header.extend(["label", "set", "exercise", "rep"])
    writer.writerow(header)
    writer.writerow(emgdata)
