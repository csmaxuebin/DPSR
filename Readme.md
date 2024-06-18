# This code is the source code implementation for the paper "Differential Privacy Frequent Graph Pattern Mining."



# Abstract
With the increase of mobile devices, new edges are added to social networks due to the interaction between users. Whenever the graph changes, by adding or deleting an edge, a large number of new subgraphs can be created, and the existing subgraphs can be modified or destroyed. Analyzing the potential so cial benefits of these databases is vast, but when these real-time changing statis tics contain sensitive information, such behavior also poses a threat to users' pri vacy. This paper proposes a frequent graph pattern mining algorithm DPSR based on differential privacy for incremental graphs. In order to better balance the pri vacy and timeliness of statistical results, the DPSR algorithm combines differen tial privacy in the sliding window model and designs an appropriate privacy budget allocation strategy in the process of mining and publishing. According to the difference between the frequent graph patterns that calculate the privacy min ing of continuous timestamps, judge whether the statistical results of the current timestamps need to be published after adding noise to the support or use the pre vious privacy results to approximate instead of publishing. Finally, this paper makes a experimental evaluation of the DPSR algorithm. It proves that the DPSR algorithm can complete the mining task while meeting data privacy.


# Experimental Environment

```
Operating Environment:Java with Intel Core i5 CPU 1.38 GHz and 16 GB RAM, running Windows.
```

# Datasets

`Patents,Youtube`

# Experimental Setup

### Evaluation Metrics:
- **F-Score**: Measures the harmonic mean of precision and recall, comparing the true frequent closed itemsets with those published by the privacy-preserving algorithm.
- **Relative Error (RE)**: Measures the median relative error in support values after noise addition, relative to the true support values.

### Experiments and Evaluation Parameters:
- **Privacy Budget (ε)**: Explored across a range from 10 to 30.
- **Window Size (w)**: Evaluated from 500 to 2500 timestamps.
- **Probability Threshold (τ)**: Varies from 0.08 to 0.6, depending on the data set and specific experiment.
- **Window Repetition Rate (r)**: Examined in terms of overlap and impact on the results.


# Python Files

1. **Main.java**:
Acts as the entry point for the application, handling initial parameters and setting up the stream processing of edges using a sliding window mechanism. It reads edges from a file, manages them in a sliding window, and employs the `TopkGraphPattern` algorithm for processing graph patterns dynamically. This class handles the graph patterns dynamically using a reservoir-based approach to handle edge additions and deletions, adjusting for changes in graph structure over time .

2. **FixedSizeSlidingWindow.java**:
Manages a sliding window of a fixed size over a stream of edges. It adds new edges to the reservoir and removes the oldest edges when the reservoir exceeds the specified size, thereby maintaining a constant number of edges in the window.

3. **EdgeReservoir.java**:
 Implements a reservoir specifically for edges. This class allows adding and removing edges from a data structure that supports random access and contains checks. It is designed to handle the dynamic aspects of edges in a graph, useful in scenarios where edges need to be sampled or manipulated frequently .

4. **Reservoir.java**:
Defines an interface for a generic reservoir. This interface specifies methods for adding, removing, checking the presence of, and retrieving random elements. It provides a blueprint for the other specific reservoir implementations like edge or subgraph reservoirs .

5. **SubgraphReservoir.java**:
Implements the `Reservoir` interface for subgraphs. This class manages a collection of subgraphs using a structure that allows efficient random access and manipulation, suitable for algorithms that need to sample or modify subgraphs dynamically within a larger graph .

#  Experimental Results

#### Figures (a) to (d): Impact of Window Size (w) and Privacy Budget (ε)
- **YT and PT (F-Score)**: Both graphs (a) and (b) show that the F-Score generally decreases as the window size (w) increases. This trend is observed across different privacy budget (ε) settings. A larger window size means each individual timestamp receives a smaller portion of the privacy budget, leading to more noise and consequently lower F-scores.
- **YT and PT (RE)**: Figures (c) and (d) show that the Relative Error (RE) generally increases with the window size for both datasets. This increase in error correlates with the reduction in the privacy budget per timestamp as the window size increases.

#### Figures (e) to (h): Impact of Threshold (τ) and Privacy Budget (ε)
- **YT and PT (F-Score)**: Figures (e) and (f) illustrate that increasing the threshold (τ) improves the F-score. This improvement suggests that higher thresholds help in reducing noise by focusing on more significant patterns, thus improving data utility.
- **YT and PT (RE)**: Similarly, the RE decreases with an increase in the threshold as shown in figures (g) and (h). This is indicative of more accurate approximations of the frequent graph patterns as the noise level is mitigated.

#### Figures (i) to (l): Impact of Privacy Budget (ε)
- **YT and PT (F-Score)**: Figures (i) and (j) display a clear trend where the F-score improves as the privacy budget (ε) increases. This suggests that a larger budget allows for less noise to be introduced into the data, leading to better utility and accuracy in the frequent graph pattern analysis.
- **YT and PT (RE)**: Figures (k) and (l) further support this by showing a decrease in RE as the privacy budget increases. This reduction in error is consistent with the assumption that higher budgets result in less noisy data.

![输入图片说明](https://github.com/csmaxuebin/DPSR/blob/main/pic/fig1-1.jpg)
![输入图片说明](https://github.com/csmaxuebin/DPSR/blob/main/pic/fig1-2.jpg)







## Update log

```
- {24.06.15} Uploaded overall framework code and readme file
```

