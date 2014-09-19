Based on the schema.
Here I provide a simple example.

Example:
Nodes: 1 2 3 4

Day  1
edge       Co-occurrence
1 - 2         10
1 - 3         5
1 - 4         3
2 - 4         1
2 - 3         2

Day 2
edge      Co-occurrence
1 - 2         5
1 - 4         2
2 - 3         7
3 - 4         2

Day 3
edge      Co-occurrence
1 - 2         10
3 - 4         15
2 - 4         2
2 - 3         1


Now we start to compute the cluster of these 4 nodes.
Suppose we choose K equals 2, then 2 clusters are chosen.
At first, we compute the degree of these 4 nodes base on all 3 days co-occurrence.
Node         degree
1            18 + 7 + 10 = 35
2            13 + 12 + 13 = 38
3            7 + 9 + 16 = 32
4            4 + 4 + 17 = 25
Note: In the real example I log the degree of nodes. You may figure out a better way.

Suppose we choose 1 and 3 as clusters. I do not choose 2 since it has too many links to 1 and 1 is already the cluster.

Now we can get the cluster(represented as color) for all the nodes.
Node         color
1              1
2              1
3              3
4              3



The next step is to compute the daily data.
At day 1 
node 2 and 4 are more relevant to node 1 according to the co-occurrence data.

node       cluster 1       cluster 3
2             10              5
4             2               0

We do not consider node 1 and 3 here cause they are already clusters. But they should also appear in the nodes json array.


Then we can conclude that
Node        group
1             1
2             1
3             3
4             1



At day 2, the statistic is similar but we sum the co-occurrence to the previous day.

node       cluster 1       cluster 3
2         10 + 5 = 15      5 + 2 = 7
4         2 + 7 = 9        0 + 2 = 2

So we get the group information
Node        group
1             1
2             1
3             3
4             1



Similarly for day 3
node       cluster 1       cluster 3
2          15 + 10 = 25    7 + 0 = 7
4          1 + 9 = 10      2 + 15 = 17

So we get the group information
Node        group
1             1
2             1
3             3
4             3
The last day's group day is same as the cluster day.