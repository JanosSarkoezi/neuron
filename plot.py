import pandas as pd
import matplotlib.pyplot as plt

# CSV laden
df = pd.read_csv("loss.csv", header=None, names=["Epoch", "Loss"])

# Plot
plt.figure(figsize=(8, 5))
plt.plot(df["Epoch"], df["Loss"], label="Training Loss")
plt.xlabel("Epoche")
plt.ylabel("Loss (Cross-Entropy)")
plt.title("Loss-Verlauf des Trainings")
plt.legend()
plt.grid(True)
# plt.show()
plt.savefig('my_plot.png')
