# Computing Perfect Bayesian Equilibria in Sequential Auctions with Verification 

## Overview
This project is a Java-based application that computes Perfect Bayesian Equilibria (PBEs) in auctions.

## Structure
- `src/cluster': Contains the main classes for the project. In particular, it contains the following entry files:
  - `LLGEntryAsym`: Sets up the context, initializes beliefs, and runs the solver for LLG auction.
  - `KrishnaEntryAsym`: Sets up the context, initializes beliefs, and runs the solver for sequential auction. 
  - `KrishnaReserveEntryAsym`: Sets up the context, initializes beliefs, and runs the solver for sequential auction with reserve price.
  - `KokottEntryAsym`: Sets up the context, initializes beliefs, and runs the solver for split award auction.

## How to Run
To run one of the entry settings you need to pass three parameters:
1. **.config file**: Each setting needs a config file with all necessary parameters. See the config folder for examples. Config/cluster contains the config files run for the experiments. While config contains easy to run examples.
2. **Specify an output Path**:
3. **Specify the index**: The algorithm solves the auctions subgame by subgame, starting with the last round. The index thus specifies which subgame to solve. 

So for example to run a simple version of the sequential auction and solve the first subgame, you would run KrishnaEntryAsym with the arguments: `~/config/krishna_debug.config ~/output/ 0`

The structure of the indices is such that you can run a large number of them in parallel on a cluster. However, you need to run all subgames of a given round first, before moving to the next round. For the sequential auctions, the number of subgames per round corresponds to the grid size. Thus if the grid size is 5 and you want to solve the 3 bidder 2 goods setting, you need to run indices 0-4 (corresponding to different subgames in the second round) before moving to the final index 5. For LLG and split-award it works similar (since the beliefs are of the form 'the belief is larger/smalle than this type'). However, for the reserve price auction the number of subgames per round is double the gridsize, since there are two subgames for each belief. One where the bidders in the first round bid below the reserve price and no good was allocated and one where the bidders bid above the reserve price and the good was allocated and the winner left the auction. 

## Key Classes (grouped by folders)
- `Algorithm`: Contains the main algorithm for computing PBE in auctions, including the context and the solver.
- 'Analysis': Contains classes for analyzing the results of the algorithm.
- 'BR': Contains classes for computing best responses.
- 'Cluster': Contains the entry files for running the algorithm.
- 'distribution': Contains classes for computing the distribution of the types.
- 'domains': Contains interfaces for the auction domain.
- 'helpers': Contains helper classes.
- 'integrator': Contains classes for integrating the beliefs.
- 'pointwiseBR': Contains classes for computing the pointwise best response.
- 'transition': Contains classes for computing the transition to new public belief states.
- 'verification': Contains classes for verifying the results of the algorithm.

## Dependencies
- Java
- commons-math3-3.6.1

## License
This project is part of the paper ["Computing Perfect Bayesian Equilibria in Sequential Auctions with Verification"](https://arxiv.org/abs/2312.04516v1) published at AAAI 2025, which should be cited when using this codebase