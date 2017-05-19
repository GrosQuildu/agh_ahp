## Analytic Hierarchy Process

Project for Operational Research and Theory of Computational Complexity at AGH.

AHP is a technique for analyzing complex decisions. Given a set of alternatives, we have to choose "the best" one.
We do so by comparing every alternative with each other in terms of some criteria.

Consider a problem of choosing the best of three cars (it's taken from "Introduction to the Analytic Hierarchy Process" by Matteo Brunelli). Firstly, we have to choose criteria, for example horse power, color, shape...

![Criteria tree](https://cloud.githubusercontent.com/assets/6371919/26248815/24c5ccb6-3ca4-11e7-92fb-e48a03a9e657.png "Criteria tree")

Then we make "pairwise" decisions: how many times car X is better than car Y in respect of criterion Z or how many times criterion A is better than criterion B. Superiority is estimated by rational numbers.

![App](https://cloud.githubusercontent.com/assets/6371919/26250371/0f4e56fe-3caa-11e7-89a9-16d386c780d8.png "app")

When all decisions were made (composing matrix of decisions), we have to compute "priority vector" which describes which alternative/criterion is the best. There are two methods supported by application: eigenvector/eigenvalue and geometric mean.


#### Inconsistiency
It is possible to decide that (according to some criterion) car X is 3 times better than car Y, 0.5 times better than Z (or 2 times worse than) and car Y is 5 times better than Z. Or that car X is 9 times better than car Y, and car Y is 2 times worse than car X. Clearly, these decisions are inconsistient.

Inconsistency can be described by plenty of indices: consistency index, consistency ratio, index of determinants, geometric index, harmonic index. What do we do with inconsistient decisions? Most common approach is to reject them when ratio index is greater than 0.1 and accept when is smaller. You can change this limit in the application.

#### Run
To start application run:
```java -jar ahp.jar```
You can load some examples from resources/ dir.

Code uses [JAMA](http://math.nist.gov/javanumerics/jama/).
