# GraphScope algorithms

[pro-sssp-seq]: src/main/java/com/alibaba/graphscope/example/property/sssp/PropertySSSPVertexData.java
[pro-sssp-pra]: src/main/java/com/alibaba/graphscope/example/property/sssp/ParallelPropertySSSPVertexData.java
[sim-sssp-seq]: src/main/java/com/alibaba/graphscope/example/simple/sssp/SSSPDefault.java
[sim-sssp-par]: src/main/java/com/alibaba/graphscope/example/simple/sssp/SSSPParallel.java

[pro-pr-seq]: src/main/java/com/alibaba/graphscope/example/property/pagerank/PropertyPageRankVertexData.java
[pro-pr-par]: src/main/java/com/alibaba/graphscope/example/property/pagerank/ParallelPropertyPageRankVertexData.java
[sim-pr-seq]: src/main/java/com/alibaba/graphscope/example/simple/pagerank/PageRankDefault.java
[sim-pr-par]: src/main/java/com/alibaba/graphscope/example/simple/pagerank/PageRankParallel.java

[pro-bfs-seq]: src/main/java/com/alibaba/graphscope/example/property/bfs/PropertyBfsVertexData.java
[pro-bfs-par]: src/main/java/com/alibaba/graphscope/example/property/bfs/ParallelPropertyBfsVertexData.java
[sim-bfs-seq]: src/main/java/com/alibaba/graphscope/example/simple/bfs/BFSDefault.java
[sim-bfs-par]: src/main/java/com/alibaba/graphscope/example/simple/bfs/BFSParallel.java

[pro-wcc-seq]: src/main/java/com/alibaba/graphscope/example/property/wcc/PropertyWCCVertexData.java
[pro-wcc-par]: src/main/java/com/alibaba/graphscope/example/property/wcc/ParallelPropertyWCCVertexData.java
[sim-wcc-seq]: src/main/java/com/alibaba/graphscope/example/simple/wcc/WCCDefault.java
[sim-wcc-par]: src/main/java/com/alibaba/graphscope/example/simple/wcc/WCCParallel.java

[pro-tra-seq]: src/main/java/com/alibaba/graphscope/example/property/traverse/PropertyTraverseVertexData.java
[pro-tra-par]: src/main/java/com/alibaba/graphscope/example/property/traverse/ParallelPropertyTraverseVertexData.java
[sim-tra-seq]: src/main/java/com/alibaba/graphscope/example/simple/traverse/TraverseDefault.java
[sim-tra-par]: src/main/java/com/alibaba/graphscope/example/simple/traverse/TraverseParallel.java

This Project contains the Java implementation of sample Graph analytical algorithms like SSSP, PageRank, e.t.c.

## Available Algorithms

| Algorithms | Property Graph-sequential 	               | Property Graph-sequential 	               | Simple Graph-sequential 	                | Simple Graph-sequential 	          |
|------------|---------------------------	               |---------------------------	               |-------------------------	                |-------------------------	          |
| SSSP       | [propertry-sssp-sequential][pro-sssp-seq]   |  [propertry-sssp-parallel][pro-sssp-pra]  | [simple-sssp-sequentail][sim-sssp-seq]  	|[simple-sssp-parallel][sim-sssp-par]  |
| PageRank   |    [propertry-pr-sequential][pro-pr-seq]  | [propertry-pr-parallel][pro-pr-par]       | [simple-pr-sequentail][sim-pr-seq]	        |[simple-pr-parallel][sim-pr-par]  |
| WCC        |[propertry-wcc-sequential][pro-wcc-seq]      | [propertry-wcc-parallel][pro-wcc-par]     | [simple-wcc-sequentail][sim-wcc-seq]	    |[simple-wcc-parallel][sim-wcc-par]  |
| BFS        | [propertry-bfs-sequential][pro-bfs-seq] | [propertry-bfs-parallel][pro-bfs-par]     | [simple-bfs-sequentail][sim-bfs-seq]	    |[simple-bfs-parallel][sim-bfs-par]  |
| Traverse   |[propertry-traverse-sequential][pro-tra-seq] | [propertry-traverse-parallel][pro-tra-par]| [simple-traverse-sequentail][sim-tra-seq]	|[simple-traverse-parallel][sim-tra-par] |


## Build

You need to install `grape-jdk` first.

```bash
git clone https://github.com/alibaba/GraphScope.git
cd analytical_engine/java/grape-jdk
mvn clean install
```

Then build this project with

```bash
mvn clean package
```