# Metro
A project presenting a concurrently functioning metro.

You can download an exe with bundled JRE or a JAR file from [here](https://drive.google.com/drive/folders/1VDQe5TNDzlfK08I5kl5aVkaSwmxdNDHh?usp=sharing)
You can also screenshots at the end of this file.

## Description

This program simulates a metro net. There are **3 trains**.
Each train rides along its designated route (forward and backward).
All the trains move **at the same time**.
Our goal is to ensure the trains will **not crash** into each other.



## Algorithm

On creation of a new simulation, the program takes the parameters given via the GUI and generates the trains.
Next, it compares trains' routes and creates an array of **shared segments** of the map (parts of the map crossed by more than one train).

When the simulation is launched, the **tunnel's map monitor** object ensures that in each shared segment, there is at most one train at any given time.


## Simulation parameters

For each train, the user can change:
- its speed
- its route via beginning and destination coordinates


## Screenshots
- Initial state of the app:
![Initial state image](https://github.com/MariuszGaljan/MetroProject/blob/master/Images/Init.png?raw=true)

- Running simulation:
![Running simulation image](https://github.com/MariuszGaljan/MetroProject/blob/master/Images/Running.png?raw=true)

- Changing parameters:
![Changing parameters image](https://github.com/MariuszGaljan/MetroProject/blob/master/Images/ParamChange.png?raw=true)
