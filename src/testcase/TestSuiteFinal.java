package testcase;

import game.GameState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.DECK_SIZE;
import model.HaMap;
import ui.UI;
import util.CachedLines;
import util.MapLoader;
import action.Action;
import action.SingletonAction;
import ai.AI;
import ai.RandomHeuristicAI;
import ai.GreedyActionAI;
import ai.GreedyTurnAI;
import ai.HeuristicAI;
import ai.HybridAI;
import ai.RandomAI;
import ai.RandomSwitchAI;
import ai.StatisticAi;
import ai.evaluation.HeuristicEvaluator;
import ai.evaluation.LeafParallelizer;
import ai.evaluation.MaterialBalanceEvaluator;
import ai.evaluation.MeanEvaluator;
import ai.evaluation.OpponentRolloutEvaluator;
import ai.evaluation.RolloutEvaluator;
import ai.evaluation.WinLoseEvaluator;
import ai.evaluation.LeafParallelizer.LEAF_METHOD;
import ai.evolution.IslandHorizonEvolution;
import ai.evolution.RollingHorizonEvolution;
import ai.mcts.Mcts;
import ai.mcts.RootParallelizedMcts;
import ai.util.RAND_METHOD;

public class TestSuiteFinal {

	private static HaMap tiny;
	private static HaMap small;
	private static HaMap standard;
	
	private static AI randomHeuristic = new RandomHeuristicAI(0.5);

	public static void main(String[] args) {

		
		try {
			tiny = MapLoader.get("a-tiny");
			small = MapLoader.get("a-small");
			standard = MapLoader.get("a");
		} catch (final IOException e) {
			e.printStackTrace();
		}
		

		//AAAICount();
		//return;
		
		if (args[0].equals("mcts-rollouts"))
			MctsRolloutDepthTests(Integer.parseInt(args[1]), args[2]);
		
		if (args[0].equals("mcts-c"))
			MctsCTests(Integer.parseInt(args[1]), args[2]);
		
		if (args[0].equals("mcts-depth-1-0"))
			MctsDepth10(Integer.parseInt(args[1]), args[2]);
		
		if (args[0].equals("aaai"))
			AAAItests(Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));
		
		if (args[0].equals("tciaig"))
			TCIAIG(Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));
	
		if (args[0].equals("tciaig_timebudget"))
			TCIAIG_timebudget(Integer.parseInt(args[1]), args[2]);
	
		if (args[0].equals("tciaig_actions"))
			TCIAIG_actions(Integer.parseInt(args[1]), args[2]);
		
		
		// YES
		if (args[0].equals("mcts-cut-random"))
			MctsCutRandom(Integer.parseInt(args[1]), args[2]);
		
		if (args[0].equals("mcts-collapse-random1"))
			MctsCollapseRandom1(Integer.parseInt(args[1]), args[2]);
		
		if (args[0].equals("mcts-collapse-random05"))
			MctsCollapseRandom05(Integer.parseInt(args[1]), args[2]);
		
		if (args[0].equals("mcts-c0"))
			MctsC0(Integer.parseInt(args[1]), args[2]);
		
		// YES
		if (args[0].equals("mcts-random"))
			MctsRandom(Integer.parseInt(args[1]), args[2]);
		
		// YES
		if (args[0].equals("mcts-ne-random"))
			MctsNeRandom(Integer.parseInt(args[1]), args[2]);
		
		if (args[0].equals("mcts-trans"))
			MctsTransTests(Integer.parseInt(args[1]), args[2]);
		
		if (args[0].equals("rolling"))
			RollingTests(Integer.parseInt(args[1]), args[2]);
		
		if (args[0].equals("rolling-para"))
			RollingParaTests(Integer.parseInt(args[1]), args[2]);
		
		if (args[0].equals("rolling-greedy"))
			RollingGreedy(Integer.parseInt(args[1]), args[2]);
		
		if (args[0].equals("mcts-greedy"))
			MctsGreedy(Integer.parseInt(args[1]), args[2]);
		
		if (args[0].equals("mcts-vs-rolling"))
			MctsVsRolling(Integer.parseInt(args[1]), args[2]);
		
		if (args[0].equals("last-time"))
			LastTime(Integer.parseInt(args[1]), args[2]);
		
		if (args[0].equals("greedyturn"))
			GreedyTurn(Integer.parseInt(args[1]), args[2]);
		
		if (args[0].equals("baselines"))
			BaseLines(Integer.parseInt(args[1]), args[2]);
		
		if (args[0].equals("ap"))
			AP(Integer.parseInt(args[1]), args[2]);
		
	}
	
	private static void AAAICount(){
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 6000;
		
		final Mcts mcts = new Mcts(budget, new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(true)));
		
		final AI greedyTurn = new GreedyTurnAI(new HeuristicEvaluator(true), budget, false);
		final RollingHorizonEvolution rolling = new RollingHorizonEvolution(true, 100, .1, .5, budget, new HeuristicEvaluator(false));
		
		tests.add(new TestCase(new StatisticAi(mcts), new StatisticAi(rolling),
			1, "mcts-rolling-count", map("standard"), deck("standard")));
		
		tests.add(new TestCase(new StatisticAi(mcts), new StatisticAi(greedyTurn),
				1, "mcts-greedyturn-count", map("standard"), deck("standard")));
		
		TestCase.GFX = true;
		
		for (final TestCase test : tests)
			test.run();
		
	}
	
	private static void TCIAIG(int runs, String size, int num){
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 6000;
		
		final Mcts mcts = new Mcts(budget, new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(true)));
		
		final Mcts nonexpl = new Mcts(budget, new RolloutEvaluator(1, 1, new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		nonexpl.c = 0;
		
		final Mcts cutting = new Mcts(budget, new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(true)));
		cutting.cut = true;
		
		final AI greedyAction = new GreedyActionAI(new HeuristicEvaluator(true));
		final AI greedyTurn = new GreedyTurnAI(new HeuristicEvaluator(true), budget, false);
		final RollingHorizonEvolution rolling = new RollingHorizonEvolution(true, 100, .1, .5, budget, new HeuristicEvaluator(false));
		
		if (num==1 || num==0)
			tests.add(new TestCase(new StatisticAi(nonexpl), new StatisticAi(greedyAction),
				runs, "nonexpl-greedyaction", map(size), deck(size)));
		if (num==2 || num==0)
			tests.add(new TestCase(new StatisticAi(nonexpl), new StatisticAi(greedyTurn),
					runs, "nonexpl-greedyturn", map(size), deck(size)));
		if (num==3 || num==0)
			tests.add(new TestCase(new StatisticAi(nonexpl), new StatisticAi(mcts),
					runs, "nonexpl-mcts", map(size), deck(size)));
		if (num==4 || num==0) 
			tests.add(new TestCase(new StatisticAi(nonexpl), new StatisticAi(rolling),
				runs, "nonexpl-rolling", map(size), deck(size)));
			
		
		if (num==5  || num==0 || num==-1)
			tests.add(new TestCase(new StatisticAi(cutting), new StatisticAi(greedyAction),
				runs, "cutting-greedyaction", map(size), deck(size)));
		if (num==6  || num==0 || num==-1)
			tests.add(new TestCase(new StatisticAi(cutting), new StatisticAi(greedyTurn),
					runs, "cutting-greedyturn", map(size), deck(size)));
		if (num==7  || num==0 || num==-1)
			tests.add(new TestCase(new StatisticAi(cutting), new StatisticAi(mcts),
					runs, "cutting-mcts", map(size), deck(size)));
		if (num==8  || num==0 || num==-1)
			tests.add(new TestCase(new StatisticAi(cutting), new StatisticAi(rolling),
				runs, "cutting-rolling", map(size), deck(size)));
		if (num==9  || num==0 || num==-1)
			tests.add(new TestCase(new StatisticAi(cutting), new StatisticAi(nonexpl),
				runs, "cutting-nonexpl", map(size), deck(size)));
		
		TestCase.GFX = true;
		
		for (final TestCase test : tests)
			test.run();
		
	}
	
	private static void TCIAIG_timebudget(int runs, String size){
		
		TestCase.GFX = true;
		
		final Mcts nonexpl = new Mcts(2000, new RolloutEvaluator(1, 1, new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		nonexpl.c = 0;
		
		final Mcts cutting = new Mcts(2000, new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(true)));
		cutting.cut = true;
		
		final RollingHorizonEvolution rolling = new RollingHorizonEvolution(true, 100, .1, .5, 2000, new HeuristicEvaluator(false));
		
		/*
		TestCase test1 = new TestCase(new StatisticAi(cutting), new StatisticAi(rolling),
				runs, "cutting-rolling-2000ms", map(size), deck(size));
		TestCase test2 = new TestCase(new StatisticAi(nonexpl), new StatisticAi(rolling),
				runs, "nonexpl-rolling-2000ms", map(size), deck(size));
		test1.run();
		test2.run();
		nonexpl.budget = 500;
		cutting.budget = 500;
		rolling.budget = 500;
		TestCase test3 = new TestCase(new StatisticAi(cutting), new StatisticAi(rolling),
				runs, "cutting-rolling-500ms", map(size), deck(size));
		TestCase test4 = new TestCase(new StatisticAi(nonexpl), new StatisticAi(rolling),
				runs, "nonexpl-rolling-500ms", map(size), deck(size));
		test3.run();
		test4.run();
		nonexpl.budget = 100;
		cutting.budget = 100;
		rolling.budget = 100;
		TestCase test5 = new TestCase(new StatisticAi(cutting), new StatisticAi(rolling),
				runs, "cutting-rolling-100ms", map(size), deck(size));
		TestCase test6 = new TestCase(new StatisticAi(nonexpl), new StatisticAi(rolling),
				runs, "nonexpl-rolling-100ms", map(size), deck(size));
		test5.run();
		test6.run();
		*/
		
		TestCase test7 = new TestCase(new StatisticAi(cutting), new StatisticAi(nonexpl),
				runs, "cutting-rolling-100ms", map(size), deck(size));
		TestCase test8 = new TestCase(new StatisticAi(cutting), new StatisticAi(nonexpl),
				runs, "cutting-rolling-500ms", map(size), deck(size));
		TestCase test9 = new TestCase(new StatisticAi(cutting), new StatisticAi(nonexpl),
				runs, "cutting-rolling-2000ms", map(size), deck(size));
		nonexpl.budget = 100;
		cutting.budget = 100;
		test7.run();
		nonexpl.budget = 500;
		cutting.budget = 500;
		test8.run();
		nonexpl.budget = 2000;
		cutting.budget = 2000;
		test9.run();
		
	}
	
	private static void TCIAIG_actions(int runs, String size){
		
		TestCase.GFX = true;
		
		final Mcts nonexpl = new Mcts(2000, new RolloutEvaluator(1, 1, new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		nonexpl.c = 0;
		
		final Mcts cutting = new Mcts(2000, new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(true)));
		cutting.cut = true;
		
		final RollingHorizonEvolution rolling = new RollingHorizonEvolution(true, 100, .1, .5, 2000, new HeuristicEvaluator(false));
		/*
		TestCase test1 = new TestCase(new StatisticAi(cutting), new StatisticAi(rolling),
				runs, "cutting-rolling-2000ms-ap10", map(size), deck(size));
		TestCase test2 = new TestCase(new StatisticAi(nonexpl), new StatisticAi(rolling),
				runs, "nonexpl-rolling-2000ms-ap10", map(size), deck(size));
		test1.ap = 10;
		test2.ap = 10;
		test1.run();
		test2.run();
		
		TestCase test3 = new TestCase(new StatisticAi(cutting), new StatisticAi(rolling),
				runs, "cutting-rolling-2000ms-ap20", map(size), deck(size));
		TestCase test4 = new TestCase(new StatisticAi(nonexpl), new StatisticAi(rolling),
				runs, "nonexpl-rolling-2000ms-ap20", map(size), deck(size));
		test3.ap = 20;
		test4.ap = 20;
		test3.run();
		test4.run();
		
		TestCase test5 = new TestCase(new StatisticAi(cutting), new StatisticAi(rolling),
				runs, "cutting-rolling-2000ms-ap30", map(size), deck(size));
		TestCase test6 = new TestCase(new StatisticAi(nonexpl), new StatisticAi(rolling),
				runs, "nonexpl-rolling-2000ms-ap30", map(size), deck(size));
		test5.ap = 30;
		test6.ap = 30;
		test5.run();
		test6.run();
		*/
		TestCase test7 = new TestCase(new StatisticAi(cutting), new StatisticAi(nonexpl),
				runs, "cutting-nonexpl-2000ms-ap10", map(size), deck(size));
		TestCase test8 = new TestCase(new StatisticAi(cutting), new StatisticAi(nonexpl),
				runs, "nonexpl-nonexpl-2000ms-ap20", map(size), deck(size));
		TestCase test9 = new TestCase(new StatisticAi(cutting), new StatisticAi(nonexpl),
				runs, "nonexpl-nonexpl-2000ms-ap30", map(size), deck(size));
		test7.ap = 10;
		test8.ap = 20;
		test9.ap = 30;
		test7.run();
		test8.run();
		test9.run();
		
	}
	
	private static void AAAItests(int runs, String size, int num) {
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 6000;
		
		final Mcts mcts = new Mcts(budget, new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(true)));
		
		final AI greedyAction = new GreedyActionAI(new HeuristicEvaluator(true));
		final AI greedyTurn = new GreedyTurnAI(new HeuristicEvaluator(true), budget, false);
		final RollingHorizonEvolution rolling = new RollingHorizonEvolution(true, 100, .1, .5, budget, new HeuristicEvaluator(false));
		
		if (num==1)
			tests.add(new TestCase(new StatisticAi(rolling), new StatisticAi(greedyAction),
				runs, "rolling-greedyaction", map(size), deck(size)));
		if (num==2)
			tests.add(new TestCase(new StatisticAi(rolling), new StatisticAi(greedyTurn),
				runs, "rolling-greedyturn", map(size), deck(size)));
		if (num==3)
			tests.add(new TestCase(new StatisticAi(mcts), new StatisticAi(greedyAction),
				runs, "mcts-greedyaction", map(size), deck(size)));
		if (num==4)
			tests.add(new TestCase(new StatisticAi(mcts), new StatisticAi(greedyTurn),
				runs, "mcts-greedyturn", map(size), deck(size)));
		if (num==5)
			tests.add(new TestCase(new StatisticAi(mcts), new StatisticAi(rolling),
				runs, "mcts-rolling", map(size), deck(size)));
		if (num==6)
			tests.add(new TestCase(new StatisticAi(greedyAction), new StatisticAi(greedyTurn),
				runs, "greedyaction-greedyturn", map(size), deck(size)));
		
		TestCase.GFX = true;
		
		for (final TestCase test : tests)
			test.run();
		
		
	}

	private static void MctsDepth10(int runs, String size) {
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 6000;
		
		final Mcts mctsD0 = new Mcts(budget, new RolloutEvaluator(1, 0, new RandomHeuristicAI(0.5), new HeuristicEvaluator(true)));
		
		final Mcts mctsD1 = new Mcts(budget, new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(true)));
		
		tests.add(new TestCase(new StatisticAi(mctsD0), new StatisticAi(mctsD1),
				runs, "mcts-d-0-vs-mcts-d-1", map(size), deck(size)));
		
		TestCase.GFX = true;
		
		for (final TestCase test : tests)
			test.run();
		
	}

	private static void MctsCollapseRandom05(int runs, String size) {
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 6000;
		
		final Mcts mctsCollapseR0 = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(0), new HeuristicEvaluator(true)));
		mctsCollapseR0.collapse = true;
		
		final Mcts mctsCollapseR05 = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(.5), new HeuristicEvaluator(true)));
		mctsCollapseR05.collapse = true;
		
		tests.add(new TestCase(new StatisticAi(mctsCollapseR0), new StatisticAi(mctsCollapseR05),
				runs, "mcts-collapse-r0-vs-mcts-collapse-r05", map(size), deck(size)));
		
		TestCase.GFX = true;
		
		for (final TestCase test : tests)
			test.run();
		
	}
	
	private static void MctsCollapseRandom1(int runs, String size) {
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 6000;
		
		final Mcts mctsCollapseR0 = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(0), new HeuristicEvaluator(true)));
		mctsCollapseR0.collapse = true;
		
		final Mcts mctsCollapseR1 = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		mctsCollapseR1.collapse = true;
		
		tests.add(new TestCase(new StatisticAi(mctsCollapseR0), new StatisticAi(mctsCollapseR1),
				runs, "mcts-collapse-r0-vs-mcts-collapse-r1", map(size), deck(size)));
		
		TestCase.GFX = true;
		
		for (final TestCase test : tests)
			test.run();
		
	}
	
	private static void MctsCutRandom(int runs, String size) {
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 6000;
		
		final Mcts mctsCutR0 = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(0), new HeuristicEvaluator(true)));
		mctsCutR0.cut = true;
		
		final Mcts mctsCutR1 = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		mctsCutR1.cut = true;
		
		final Mcts mctsCutR05 = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(.5), new HeuristicEvaluator(true)));
		mctsCutR05.cut = true;
		
		tests.add(new TestCase(new StatisticAi(mctsCutR0), new StatisticAi(mctsCutR1),
				runs, "mcts-cut-r0-vs-mcts-cut-r1", map(size), deck(size)));
		
		TestCase.GFX = true;
		
		for (final TestCase test : tests)
			test.run();
		
	}
	
	private static void MctsC0(int runs, String size) {
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 6000;
		
		final AI greedyaction = new GreedyActionAI(new HeuristicEvaluator(false));
		
		//final Mcts mcts = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(0), new HeuristicEvaluator(true)));
		
		final Mcts mctsc0 = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(0), new HeuristicEvaluator(true)));
		mctsc0.c = 0;
		
		tests.add(new TestCase(new StatisticAi(mctsc0), new StatisticAi(greedyaction),
				runs, "mcts-c0-r0-vs-greedyaction", map(size), deck(size)));
		
		//TestCase.GFX = true;
		
		for (final TestCase test : tests)
			test.run();
		
	}
	
	private static void AP(int runs, String size) {
		
		final List<TestCase> testsAP1 = new ArrayList<TestCase>();
		final List<TestCase> testsAP3 = new ArrayList<TestCase>();
		final List<TestCase> testsAP5 = new ArrayList<TestCase>();
		final List<TestCase> testsAP10 = new ArrayList<TestCase>();
		final List<TestCase> testsAP20 = new ArrayList<TestCase>();
		final List<TestCase> testsAP40 = new ArrayList<TestCase>();
		
		int budget = 1000;
		final AI greedyaction = new GreedyActionAI(new HeuristicEvaluator(true));

		final Mcts mcts = new Mcts(budget, new RolloutEvaluator(1, 1, new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		final Mcts mctsc0 = new Mcts(budget, new RolloutEvaluator(1, 1, new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		mctsc0.c = 0;
		final Mcts mctscut = new Mcts(budget, new RolloutEvaluator(1, 1, new RandomHeuristicAI(.5), new HeuristicEvaluator(true)));
		mctscut.cut = true;
		final IslandHorizonEvolution rollingisland = new IslandHorizonEvolution(true, 100, .1, .5, budget, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		testsAP1.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mcts),
			runs, "greedyaction-vs-mcts-AP1", map(size), deck(size)));
		testsAP1.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mctsc0),
				runs, "greedyaction-vs-mctsc0-AP1", map(size), deck(size)));
		testsAP1.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mctscut),
				runs, "greedyaction-vs-mctscut-AP1", map(size), deck(size)));
		testsAP1.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(rollingisland),
				runs, "greedyaction-vs-rollingisland-AP1", map(size), deck(size)));
		
		testsAP3.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mcts),
				runs, "greedyaction-vs-mcts-AP3", map(size), deck(size)));
		testsAP3.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mctsc0),
				runs, "greedyaction-vs-mctsc0-AP3", map(size), deck(size)));
		testsAP3.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mctscut),
				runs, "greedyaction-vs-mctscut-AP3", map(size), deck(size)));
		testsAP3.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(rollingisland),
				runs, "greedyaction-vs-rollingisland-AP3", map(size), deck(size)));
		
		testsAP5.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mcts),
				runs, "greedyaction-vs-mcts-AP5", map(size), deck(size)));
		testsAP5.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mctsc0),
				runs, "greedyaction-vs-mctsc0-AP5", map(size), deck(size)));
		testsAP5.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mctscut),
				runs, "greedyaction-vs-mctscut-AP5", map(size), deck(size)));
		testsAP5.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(rollingisland),
				runs, "greedyaction-vs-rollingisland-AP5", map(size), deck(size)));	
		
		testsAP10.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mcts),
				runs, "greedyaction-vs-mcts-AP10", map(size), deck(size)));
		testsAP10.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mctsc0),
				runs, "greedyaction-vs-mctsc0-AP10", map(size), deck(size)));
		testsAP10.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mctscut),
				runs, "greedyaction-vs-mctscut-AP10", map(size), deck(size)));
		testsAP10.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(rollingisland),
				runs, "greedyaction-vs-rollingisland-AP10", map(size), deck(size)));
		
		testsAP20.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mcts),
				runs, "greedyaction-vs-mcts-AP20", map(size), deck(size)));
		testsAP20.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mctsc0),
				runs, "greedyaction-vs-mctsc0-AP20", map(size), deck(size)));
		testsAP20.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mctscut),
				runs, "greedyaction-vs-mctscut-AP20", map(size), deck(size)));
		testsAP20.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(rollingisland),
				runs, "greedyaction-vs-rollingisland-AP20", map(size), deck(size)));
		
		testsAP40.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mcts),
				runs, "greedyaction-vs-mcts-AP40", map(size), deck(size)));
		testsAP40.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mctsc0),
				runs, "greedyaction-vs-mctsc0-AP40", map(size), deck(size)));
		testsAP40.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(mctscut),
				runs, "greedyaction-vs-mctscut-AP40", map(size), deck(size)));
		testsAP40.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(rollingisland),
				runs, "greedyaction-vs-rollingisland-AP40", map(size), deck(size)));
		
		for (final TestCase test : testsAP1){
			test.ap = 1;
			test.run();
		}
		
		for (final TestCase test : testsAP3){
			test.ap = 3;
			test.run();
		}
		
		for (final TestCase test : testsAP5){
			test.ap = 5;
			test.run();
		}
		
		for (final TestCase test : testsAP10){
			test.ap = 10;
			test.run();
		}
		
		for (final TestCase test : testsAP20){
			test.ap = 20;
			test.run();
		}
		
		for (final TestCase test : testsAP40){
			test.ap = 40;
			test.run();
		}
		
	}
	
	private static void BaseLines(int runs, String size) {
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 6000;
		
		final AI greedyturn = new GreedyTurnAI(new HeuristicEvaluator(true), budget);
		final AI greedyaction = new GreedyActionAI(new HeuristicEvaluator(true));
		
		final AI random = new RandomAI(RAND_METHOD.TREE);
		
		tests.add(new TestCase(new StatisticAi(greedyturn), new StatisticAi(greedyaction),
				runs, "greedyturn-vs-greedyaction", map(size), deck(size)));
		
		tests.add(new TestCase(new StatisticAi(greedyturn), new StatisticAi(random),
				runs, "greedyturn-vs-random", map(size), deck(size)));
		
		tests.add(new TestCase(new StatisticAi(greedyaction), new StatisticAi(random),
				runs, "greedyaction-vs-random", map(size), deck(size)));
		
		for (final TestCase test : tests)
			test.run();
		
	}

	private static void GreedyTurn(int runs, String size) {
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 6000;
		
		final Mcts mcts = new Mcts(budget, new RolloutEvaluator(1, 1, new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		mcts.c = 0;
		
		final IslandHorizonEvolution rollingisland = new IslandHorizonEvolution(true, 100, .1, .5, budget, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		final AI greedtyurn = new GreedyTurnAI(new HeuristicEvaluator(true), budget);
		
		tests.add(new TestCase(new StatisticAi(mcts), new StatisticAi(greedtyurn),
				runs, "mcts-vs-greedtyurn", map(size), deck(size)));
		
		tests.add(new TestCase(new StatisticAi(rollingisland), new StatisticAi(greedtyurn),
				runs, "rollingisland-vs-greedtyurn", map(size), deck(size)));
		
		for (final TestCase test : tests)
			test.run();
	}



	private static void LastTime(int runs, String size) {
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 3000;
		
		final Mcts mcts_23_4375 = new Mcts(budget/32, new RolloutEvaluator(1, 1, new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		mcts_23_4375.c = 0;
		
		final Mcts mcts_11_71875 = new Mcts(budget/64, new RolloutEvaluator(1, 1, new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		mcts_11_71875.c = 0;
		
		final IslandHorizonEvolution rollingisland_46_875 = new IslandHorizonEvolution(true, 100, .1, .5, budget/64, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		final IslandHorizonEvolution rollingisland_23_4375 = new IslandHorizonEvolution(true, 100, .1, .5, budget/128, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		final IslandHorizonEvolution rollingisland_11_71875 = new IslandHorizonEvolution(true, 100, .1, .5, budget/256, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		final AI greedtyurn = new GreedyTurnAI(new HeuristicEvaluator(true), 3000);
		
		tests.add(new TestCase(new StatisticAi(mcts_23_4375), new StatisticAi(greedtyurn),
				runs, "mcts_23_4375-vs-greedtyurn", map(size), deck(size)));
		
		tests.add(new TestCase(new StatisticAi(mcts_11_71875), new StatisticAi(greedtyurn),
				runs, "mcts_11_71875-vs-greedtyurn", map(size), deck(size)));
		
		tests.add(new TestCase(new StatisticAi(rollingisland_46_875), new StatisticAi(greedtyurn),
				runs, "rollingisland_46_875-vs-greedtyurn", map(size), deck(size)));
		
		tests.add(new TestCase(new StatisticAi(rollingisland_23_4375), new StatisticAi(greedtyurn),
				runs, "rollingisland_23_4375-vs-greedtyurn", map(size), deck(size)));
		
		tests.add(new TestCase(new StatisticAi(rollingisland_11_71875), new StatisticAi(greedtyurn),
				runs, "rollingisland_11_71875-vs-greedtyurn", map(size), deck(size)));
		
		for (final TestCase test : tests)
			test.run();
		
	}
	
	private static void MctsNeRandom(int runs, String size) {
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 6000;
		
		final Mcts mcts0 = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(0), new HeuristicEvaluator(true)));
		mcts0.c = 0;
		final Mcts mcts1 = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		mcts1.c = 0;
		final Mcts mcts05 = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(.5), new HeuristicEvaluator(true)));
		mcts05.c = 0;
		
		tests.add(new TestCase(new StatisticAi(mcts0), new StatisticAi(mcts05),
				runs, "ne-mcts-r0-vs-ne-mcts-r05", map(size), deck(size)));
		
		TestCase.GFX = true;
		
		for (final TestCase test : tests)
			test.run();
		
	}
	
	private static void MctsRandom(int runs, String size) {
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 6000;
		
		final Mcts mcts0 = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(0), new HeuristicEvaluator(true)));
		final Mcts mcts1 = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		final Mcts mcts05 = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(.5), new HeuristicEvaluator(true)));
		
		tests.add(new TestCase(new StatisticAi(mcts0), new StatisticAi(mcts05),
				runs, "mcts-r0-vs-mcts-r05", map(size), deck(size)));
		
		TestCase.GFX = true;
		
		for (final TestCase test : tests)
			test.run();
		
	}

	private static void MctsVsRolling(int runs, String size) {
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 6000;
		
		final Mcts mcts = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		mcts.c = 0;
		
		final IslandHorizonEvolution rollingisland = new IslandHorizonEvolution(true, 100, .1, .5, budget, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		tests.add(new TestCase(new StatisticAi(mcts), new StatisticAi(rollingisland),
				runs, "mcts-vs-rollingisland", map(size), deck(size)));
		
		TestCase.GFX = true;
		
		for (final TestCase test : tests)
			test.run();
		
	}

	private static void MctsGreedy(int runs, String size) {
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 3000;
		
		final Mcts mcts14c0_3000 = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		mcts14c0_3000.c = 0;
		
		final Mcts mcts14c0_1500 = new Mcts(budget/2, new RolloutEvaluator(1, 1,new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		mcts14c0_1500.c = 0;
		
		final Mcts mcts14c0_750 = new Mcts(budget/4, new RolloutEvaluator(1, 1,new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		mcts14c0_750.c = 0;
		
		final Mcts mcts14c0_375 = new Mcts(budget/8, new RolloutEvaluator(1, 1,new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		mcts14c0_375.c = 0;
		
		final Mcts mcts14c0_187_5 = new Mcts(budget/16, new RolloutEvaluator(1, 1,new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		mcts14c0_187_5.c = 0;
		
		final AI greedtyurn = new GreedyTurnAI(new HeuristicEvaluator(true), 3000);
		
		tests.add(new TestCase(new StatisticAi(mcts14c0_3000), new StatisticAi(greedtyurn),
				runs, "mcts14c0_3000-vs-greedtyurn_3000", map(size), deck(size)));
		
		tests.add(new TestCase(new StatisticAi(mcts14c0_1500), new StatisticAi(greedtyurn),
				runs, "mcts14c0_1500-vs-greedtyurn_3000", map(size), deck(size)));
		
		tests.add(new TestCase(new StatisticAi(mcts14c0_750), new StatisticAi(greedtyurn),
				runs, "mcts14c0_750-vs-greedtyurn_3000", map(size), deck(size)));
		
		tests.add(new TestCase(new StatisticAi(mcts14c0_375), new StatisticAi(greedtyurn),
				runs, "mcts14c0_375-vs-greedtyurn_3000", map(size), deck(size)));
		
		tests.add(new TestCase(new StatisticAi(mcts14c0_187_5), new StatisticAi(greedtyurn),
				runs, "mcts14c0_87_5-vs-greedtyurn_3000", map(size), deck(size)));
		
		
		for (final TestCase test : tests)
			test.run();
		
	}

	private static void RollingGreedy(int runs, String size) {
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 6000;
		
		final IslandHorizonEvolution rollingisland_6000 = new IslandHorizonEvolution(true, 100, .1, .5, budget, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		final IslandHorizonEvolution rollingisland_1500 = new IslandHorizonEvolution(true, 100, .1, .5, budget/2, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		final IslandHorizonEvolution rollingisland_750 = new IslandHorizonEvolution(true, 100, .1, .5, budget/4, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		final IslandHorizonEvolution rollingisland_375 = new IslandHorizonEvolution(true, 100, .1, .5, budget/8, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		final IslandHorizonEvolution rollingisland_187_5 = new IslandHorizonEvolution(true, 100, .1, .5, budget/16, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		final AI greedtyurn = new GreedyTurnAI(new HeuristicEvaluator(true), 3000);
		final AI greedyaction = new GreedyActionAI(new HeuristicEvaluator(true));
		
		tests.add(new TestCase(new StatisticAi(rollingisland_6000), new StatisticAi(greedyaction),
				runs, "rollingisland-vs-greedyaction", map(size), deck(size)));
		/*
		tests.add(new TestCase(new StatisticAi(rollingisland_1500), new StatisticAi(greedtyurn),
				runs, "rollingisland_1500-vs-greedtyurn_3000", map(size), deck(size)));
		
		tests.add(new TestCase(new StatisticAi(rollingisland_750), new StatisticAi(greedtyurn),
				runs, "rollingisland_750-vs-greedtyurn_3000", map(size), deck(size)));
		
		tests.add(new TestCase(new StatisticAi(rollingisland_375), new StatisticAi(greedtyurn),
				runs, "rollingisland_375-vs-greedtyurn_3000", map(size), deck(size)));
		
		tests.add(new TestCase(new StatisticAi(rollingisland_187_5), new StatisticAi(greedtyurn),
				runs, "rollingisland_187_5-vs-greedtyurn_3000", map(size), deck(size)));
		*/
		for (final TestCase test : tests)
			test.run();
		
	}

	private static void RollingTests(int runs, String size) {
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 1000;
		
		final RollingHorizonEvolution rolling0 = new RollingHorizonEvolution(true, 100, .1, .5, budget, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0), new HeuristicEvaluator(false)));
		
		final RollingHorizonEvolution rolling05 = new RollingHorizonEvolution(true, 100, .1, .5, budget, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		final RollingHorizonEvolution rolling1 = new RollingHorizonEvolution(true, 100, .1, .5, budget, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(1), new HeuristicEvaluator(false)));
		
		// ---
		
		final RollingHorizonEvolution rolling05r1 = new RollingHorizonEvolution(true, 100, .1, .5, budget, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		final RollingHorizonEvolution rolling05r2 = new RollingHorizonEvolution(true, 100, .1, .5, budget, 
				new RolloutEvaluator(2, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		final RollingHorizonEvolution rolling05r5 = new RollingHorizonEvolution(true, 100, .1, .5, budget, 
				new RolloutEvaluator(5, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		final RollingHorizonEvolution rolling05r10 = new RollingHorizonEvolution(true, 100, .1, .5, budget, 
				new RolloutEvaluator(10, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		final RollingHorizonEvolution rolling05r50 = new RollingHorizonEvolution(true, 100, .1, .5, budget, 
				new RolloutEvaluator(50, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		// --
		
		final RollingHorizonEvolution rolling05r1noHistory = new RollingHorizonEvolution(true, 100, .1, .5, budget, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		rolling05r1noHistory.useHistory = false;
		
		final RollingHorizonEvolution rollingheuristic = new RollingHorizonEvolution(true, 100, .1, .5, budget, 
				new HeuristicEvaluator(false));
		
		final AI greedyaction = new GreedyActionAI(new HeuristicEvaluator(true));
		
		final AI greedyturn = new GreedyTurnAI(new HeuristicEvaluator(true), budget);
		
		tests.add(new TestCase(new StatisticAi(rolling05r1), new StatisticAi(rolling05r1noHistory),
				runs, "rolling05r1-vs-rolling05r1nohis", map(size), deck(size)));
		
		tests.add(new TestCase(new StatisticAi(rolling05r1), new StatisticAi(rollingheuristic),
				runs, "rolling05r1-vs-rollingheuristic", map(size), deck(size)));
		
		
		for (final TestCase test : tests)
			test.run();
		
	}

	private static void RollingParaTests(int runs, String size) {
		
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 1000;
		
		final RollingHorizonEvolution rolling05r1_1000 = new RollingHorizonEvolution(true, 100, .1, .5, budget, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		final IslandHorizonEvolution rollingisland05r1_1000 = new IslandHorizonEvolution(true, 100, .1, .5, budget, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		final RollingHorizonEvolution rolling05r1_2000 = new RollingHorizonEvolution(true, 100, .1, .5, budget*2, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		final IslandHorizonEvolution rollingisland05r1_2000 = new IslandHorizonEvolution(true, 100, .1, .5, budget*2, 
				new RolloutEvaluator(1, 1, new RandomHeuristicAI(0.5), new HeuristicEvaluator(false)));
		
		tests.add(new TestCase(new StatisticAi(rolling05r1_1000), new StatisticAi(rollingisland05r1_1000),
				runs, "rolling05r1_1000-vs-rollingisland05r1_1000", map(size), deck(size)));
		
		tests.add(new TestCase(new StatisticAi(rolling05r1_2000), new StatisticAi(rollingisland05r1_2000),
				runs, "rolling05r1_2000-vs-rollingisland05r1_2000", map(size), deck(size)));
		
		for (final TestCase test : tests)
			test.run();
		
	}


	private static void MctsCTests(int runs, String size) {
		final List<TestCase> tests = new ArrayList<TestCase>();
		final Mcts mcts0 = new Mcts(6000, new RolloutEvaluator(1, 1,randomHeuristic, new HeuristicEvaluator(true)));
		
		final Mcts mcts1 = new Mcts(6000, new RolloutEvaluator(1, 1,randomHeuristic, new HeuristicEvaluator(true)));
		mcts1.c = mcts1.c / 2;
		final Mcts mcts2 = new Mcts(6000, new RolloutEvaluator(1, 1,randomHeuristic, new HeuristicEvaluator(true)));
		mcts2.c = mcts2.c / 4;
		final Mcts mcts3 = new Mcts(6000, new RolloutEvaluator(1, 1,randomHeuristic, new HeuristicEvaluator(true)));
		mcts3.c = mcts3.c / 8;
		
		final Mcts mcts4 = new Mcts(6000, new RolloutEvaluator(1, 1,randomHeuristic, new HeuristicEvaluator(true)));
		mcts4.c = mcts4.c / 16;
		
		final Mcts mcts5 = new Mcts(6000, new RolloutEvaluator(1, 1,randomHeuristic, new HeuristicEvaluator(true)));
		mcts5.c = mcts5.c / 24;
		
		final Mcts mcts6 = new Mcts(6000, new RolloutEvaluator(1, 1,randomHeuristic, new HeuristicEvaluator(true)));
		mcts6.c = mcts6.c / 32;
		
		final Mcts mcts7 = new Mcts(6000, new RolloutEvaluator(1, 1,randomHeuristic, new HeuristicEvaluator(true)));
		mcts7.c = mcts7.c / 48;
		
		final Mcts mcts8 = new Mcts(6000, new RolloutEvaluator(1, 1,randomHeuristic, new HeuristicEvaluator(true)));
		mcts8.c = mcts8.c / 64;
		
		final Mcts mcts9 = new Mcts(6000, new RolloutEvaluator(1, 1,randomHeuristic, new HeuristicEvaluator(true)));
		mcts9.c = mcts9.c / 92;
		
		final Mcts mcts10 = new Mcts(6000, new RolloutEvaluator(1, 1,randomHeuristic, new HeuristicEvaluator(true)));
		mcts10.c = mcts10.c / 192;
		
		final Mcts mcts11 = new Mcts(6000, new RolloutEvaluator(1, 1,randomHeuristic, new HeuristicEvaluator(true)));
		mcts11.c = 0;
		
		final Mcts mcts11nonrandom = new Mcts(6000, new RolloutEvaluator(1, 1,new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		mcts11nonrandom.c = 0;
		
		final Mcts mcts11random = new Mcts(6000, new RolloutEvaluator(1, 1,new RandomHeuristicAI(0), new HeuristicEvaluator(true)));
		mcts11random.c = 0;
		
		final Mcts mcts12cut = new Mcts(6000, new RolloutEvaluator(1, 1,new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		//mcts12cut.c = 0;
		mcts12cut.cut = true;
		
		final Mcts mcts12cut05parallel = new Mcts(6000, new LeafParallelizer(new RolloutEvaluator(1, 1,new RandomHeuristicAI(.5), new HeuristicEvaluator(true)), LEAF_METHOD.AVERAGE));
		//mcts12cut.c = 0;
		mcts12cut05parallel.cut = true;
		
		final Mcts mcts1305 = new Mcts(6000, new RolloutEvaluator(1, 1,new RandomHeuristicAI(.5), new HeuristicEvaluator(true)));
		mcts1305.c = 0;
		//mcts1305.cut = true;
		
		final Mcts mcts1305parallel = new Mcts(6000, new LeafParallelizer(new RolloutEvaluator(1, 1,new RandomHeuristicAI(.5), new HeuristicEvaluator(true)), LEAF_METHOD.AVERAGE));
		mcts1305parallel.c = 0;
		//mcts1305parallel.cut = true;
		
		final Mcts mcts12collapse = new Mcts(6000, new RolloutEvaluator(1, 1,new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		//mcts12collapse.c = 0;
		mcts12collapse.collapse = true;
		
		final AI greedyaction = new GreedyActionAI(new HeuristicEvaluator(true));
		
		final Mcts mcts14c0 = new Mcts(6000, new RolloutEvaluator(1, 1,new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		mcts14c0.c = 0;

		final Mcts mcts14cut05 = new Mcts(6000, new RolloutEvaluator(1, 1,new RandomHeuristicAI(.5), new HeuristicEvaluator(true)));
		//mcts12cut.c = 0;
		mcts14cut05.cut = true;
		
		final RootParallelizedMcts mcts14cut05rootparallel = new RootParallelizedMcts(6000, new RolloutEvaluator(1, 1,new RandomHeuristicAI(.5), new HeuristicEvaluator(true)));
		//mcts12cut.c = 0;
		mcts14cut05rootparallel.cut = true;
		
		tests.add(new TestCase(new StatisticAi(mcts14cut05), new StatisticAi(mcts14cut05rootparallel),
				runs, "mcts-cut05-vs-mcts-cut05-rootparallel", map(size), deck(size)));
		/*
		tests.add(new TestCase(new StatisticAi(mcts1305), new StatisticAi(mcts1305parallel),
				runs, "mcts-c0-05-vs-mcts-c0-05-parallel", map(size), deck(size)));
		*/
		for (final TestCase test : tests)
			test.run();

	}
	
	private static void MctsTransTests(int runs, String size) {
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		int budget = 2000;
		
		final Mcts mcts14c0 = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		mcts14c0.c = 0;
		
		final Mcts mcts14c0notrans = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(1), new HeuristicEvaluator(true)));
		mcts14c0notrans.c = 0;
		mcts14c0notrans.useTrans = false;
		
		final Mcts mcts14cut05 = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(.5), new HeuristicEvaluator(true)));
		mcts14cut05.cut = true;
		
		final Mcts mcts14cut05notrans = new Mcts(budget, new RolloutEvaluator(1, 1,new RandomHeuristicAI(.5), new HeuristicEvaluator(true)));
		mcts14cut05notrans.cut = true;
		mcts14cut05notrans.useTrans = false;
		
		tests.add(new TestCase(new StatisticAi(mcts14c0), new StatisticAi(mcts14c0notrans),
				runs, "mcts14c0-vs-mcts14c0notrans", map(size), deck(size)));
		
		tests.add(new TestCase(new StatisticAi(mcts14cut05), new StatisticAi(mcts14cut05notrans),
				runs, "mcts14cut05-vs-mcts14cut05notrans", map(size), deck(size)));
		
		for (final TestCase test : tests)
			test.run();

	}
	
	private static void MctsRolloutDepthTests(int runs, String size) {
		final List<TestCase> tests = new ArrayList<TestCase>();
		
		final Mcts mcts10000 = new Mcts(6000, new RolloutEvaluator(1, 10000,
				randomHeuristic, new HeuristicEvaluator(true)));
		final Mcts mcts10 = new Mcts(6000, new RolloutEvaluator(1, 10,
				randomHeuristic, new HeuristicEvaluator(true)));
		final Mcts mcts5 = new Mcts(6000, new RolloutEvaluator(1, 5,
				randomHeuristic, new HeuristicEvaluator(true)));
		
		final Mcts mcts1 = new Mcts(6000, new RolloutEvaluator(1, 1,randomHeuristic, new HeuristicEvaluator(true)));
		final AI greedyaction = new GreedyActionAI(new HeuristicEvaluator(true));
		  
		tests.add(new TestCase(new StatisticAi(mcts10000), new StatisticAi(greedyaction),
				runs, "mcts-nodepth-vs-greedyaction-2", map(size), deck(size)));
		tests.add(new TestCase(new StatisticAi(mcts10), new StatisticAi(greedyaction),
				runs, "mcts-10depth-vs-greedyaction-2", map(size), deck(size)));
		tests.add(new TestCase(new StatisticAi(mcts5), new StatisticAi(greedyaction),
				runs, "mcts-5depth-vs-greedyaction-2", map(size), deck(size)));
		
		//tests.add(new TestCase(new StatisticAi(mcts1), new StatisticAi(greedyaction),
		//		runs, "mcts-1depth-vs-greedyaction", map(size), deck(size)));
		
		for (final TestCase test : tests)
			test.run();

	}

	private static DECK_SIZE deck(String size) {
		if (size.equals("tiny"))
			return DECK_SIZE.TINY;
		if (size.equals("small"))
			return DECK_SIZE.SMALL;

		return DECK_SIZE.STANDARD;
	}

	private static HaMap map(String size) {
		if (size.equals("tiny"))
			return tiny;
		if (size.equals("small"))
			return small;
		return standard;
	}

}
