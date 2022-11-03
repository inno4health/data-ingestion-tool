import pickle as pkl
import pandas as pd
with open("labeled_2.pkl", "rb") as f:
    object = pkl.load(f)
    
df = pd.DataFrame(object)
df.to_csv(r'file2.csv')