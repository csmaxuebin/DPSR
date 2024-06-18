package main;

import java.io.BufferedReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;



import fullydynamic.FullyDynamicSubgraphReservoirThreeNode;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;


import java.io.BufferedWriter;
import java.io.FileWriter;


import input.StreamEdge;
import input.StreamEdgeReader;

import slidingwindow.FixedSizeSlidingWindow;
import struct.LabeledNode;
import struct.NodeMap;
import struct.Quadriplet;
import topkgraphpattern.Pattern;
import topkgraphpattern.TopkGraphPatterns;
import utility.EdgeHandler;



public class Main {
	public static void main(String args[]) throws IOException {
		// extract all parameters from the input
		int simulatorType = Integer.parseInt(args[0]);
		String directory = args[1];
		String fileName = args[2];
		int windowSize = Integer.parseInt(args[3]);
		double epsilon = Double.parseDouble(args[4]);
		double delta = Double.parseDouble(args[5]);
		int Tk = Integer.parseInt(args[6]);
		int k = Integer.parseInt(args[7]);
		System.out.println("simulator type: " + simulatorType + " window size: " + windowSize + " epsilon: " + epsilon
				+ " delta: " + delta + " Tk: " + Tk + "k: " + k);

		String sep = ",";
		String inFileName = directory + fileName;

		// input file reader
		BufferedReader in = null;

		try {
			InputStream rawin = new FileInputStream(inFileName);
			if (inFileName.endsWith(".gz"))
				rawin = new GZIPInputStream(rawin);
			in = new BufferedReader(new InputStreamReader(rawin));
		} catch (FileNotFoundException e) {
			System.err.println("File not found");
			e.printStackTrace();
			System.exit(1);
		}

		StreamEdgeReader reader = new StreamEdgeReader(in, sep);
		StreamEdge edge = reader.nextItem();
		FixedSizeSlidingWindow sw = new FixedSizeSlidingWindow(windowSize);

		// declare object of the algorithm interface
		TopkGraphPatterns topkGraphPattern = null;
		long startTime = System.currentTimeMillis();

		if (simulatorType == 0) {
			//double epsilonk = (4 + epsilon) / (epsilon * epsilon);
			//double Tkk = Math.log(Tk / delta);
			//int size = (int) (Tkk * epsilonk);
			double M = (4 * (1 + Math.log(1 / delta))) / (epsilon * epsilon);
			int size = (int) Math.round(M);
			System.out.println("size of the reservoir: " + size);
			topkGraphPattern = new FullyDynamicSubgraphReservoirThreeNode(size, k);
		}

		System.out.println("edges(k)\t\tsecs(s)\t\tpatterns(#)\t\treservoir-size(curr)");

		long edgeCount = 1;
		long PRINT_AFTER = 100000;
		long PRODUCE_SNAPSHOT_AFTER = 1_000_000;
		int index = 0;
		THashMap<Pattern, Double> fp1 = new THashMap<Pattern, Double>();//存上次的结果
		String outFileName2 = "E:\\论文\\data\\results12DP.txt";
		while (edge != null) {
			//if (edgeCount == 1) {
 			topkGraphPattern.addEdge(edge);
			// System.out.println("+ " + edge);
			//} else {
			//topkGraphPattern.addEdge(edge);

			//}

			// slide the window and get the last item if the window is full
			if (isFullyDynamicAlgorithm(simulatorType)) {
				Optional<StreamEdge> oldestEdge = sw.add(edge);
				if (oldestEdge.isPresent()) {
					// System.out.println("- " + oldestEdge);
					//System.out.println("-----------------------");
					topkGraphPattern.removeEdge(oldestEdge.get());
				}
			}
			edge = reader.nextItem();
			edgeCount++;

			if (edgeCount % PRINT_AFTER == 0) {
				// System.out.println(String.format("%d", ((System.currentTimeMillis() -
				// startTime)/1000)));
				System.out.println(String.format("%d\t\t%d\t\t%d\t\t%d", (edgeCount / 1000),
						((System.currentTimeMillis() - startTime) / 1000),
						topkGraphPattern.getFrequentPatterns().size(), topkGraphPattern.getCurrentReservoirSize()));
			}
			if(((FullyDynamicSubgraphReservoirThreeNode) topkGraphPattern).p()[0]==0&&windowSize==edgeCount){
				BufferedWriter bw2 = null;
				FileWriter fw2 = null;
				fw2 = new FileWriter(outFileName2);
				bw2 = new BufferedWriter(fw2);
				topkGraphPattern.removeEdge(edge);
				Set<Pattern> set3 = topkGraphPattern.getFrequentPatterns().keySet();
				THashMap<Pattern, Double> fp5=new THashMap<Pattern, Double>();
				for (Pattern key3 : set3) {
					fp5.put(key3, topkGraphPattern.getFrequentPatterns().get(key3));//fp5现在的
				}
				printMap(fp5 , bw2);
				bw2.close();
				fp5.clear();
			}else{if(((FullyDynamicSubgraphReservoirThreeNode) topkGraphPattern).p()[0]==1&&windowSize==edgeCount) {
				BufferedWriter bw2 = null;
				FileWriter fw2 = null;
				fw2 = new FileWriter(outFileName2);
				bw2 = new BufferedWriter(fw2);
				Set<Pattern> set3 = topkGraphPattern.getFrequentPatterns().keySet();
				THashMap<Pattern, Double> fp5=new THashMap<Pattern, Double>();
				for (Pattern key3 : set3) {
					fp5.put(key3, topkGraphPattern.getFrequentPatterns().get(key3)+laplace(((FullyDynamicSubgraphReservoirThreeNode) topkGraphPattern).p()[1],10));//fp5现在的
				}
				printMap(fp5 , bw2);
				bw2.close();
				fp5.clear();
			}
			}

			/////////////////////////
		}

		long endTime = System.currentTimeMillis();
		System.out.println("execution time: " + (endTime - startTime) / (double) 1000 + " secs.");

		// create the output file name
		String outFileName = "result1234";

		if (simulatorType == 0)
			outFileName = "result1234";

		BufferedWriter bw = null;
		FileWriter fw = null;

		fw = new FileWriter(outFileName);
		bw = new BufferedWriter(fw);

		THashMap<Pattern, Double> correctEstimates = topkGraphPattern.correctEstimates();
		printMap(correctEstimates, bw);
		bw.flush();
		bw.close();
		System.out.println(topkGraphPattern.getNumberofSubgraphs());
	}

	public static void printMap(THashMap<Pattern, Double> mp, BufferedWriter bw) throws IOException {
		Iterator<Entry<Pattern, Double>> it = mp.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Pattern, Double> pair = it.next();
			//支持度隐私保护
			bw.write(pair.getKey() + "\t" + pair.getValue() +"\n");
		}
	}

	private static boolean isFullyDynamicAlgorithm(int simulatorType) {
		return simulatorType == 0 || simulatorType == 1 || simulatorType == 2 || simulatorType == 7
				|| simulatorType == 8 || simulatorType == 10 || simulatorType == 14 || simulatorType == 15
				|| simulatorType == 16;
	}

	public static double laplace(double pro, double k) {
		pro = k / pro;
		double _para = 0.5;
		Random rd = new Random();
		double a = rd.nextDouble();
		double result = 0;
		double temp = 0;
		if (a < _para) {
			temp = pro * Math.log(2 * a);
			result = temp;
		} else if (a > _para) {
			temp = -pro * Math.log(2 - 2 * a);
			result = temp;
		} else
			result = 0;

		return result;
	}



}

