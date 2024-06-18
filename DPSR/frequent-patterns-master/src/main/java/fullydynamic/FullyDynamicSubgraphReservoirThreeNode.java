package fullydynamic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import graphpattern.ThreeNodeGraphPattern;
import input.StreamEdge;
import reservoir.SubgraphReservoir;
import struct.LabeledNode;
import struct.NodeMap;
import struct.Triplet;
import topkgraphpattern.Pattern;
import topkgraphpattern.TopkGraphPatterns;
import utility.EdgeHandler;
import utility.SetFunctions;

public class FullyDynamicSubgraphReservoirThreeNode implements TopkGraphPatterns {
    NodeMap nodeMap;
    EdgeHandler utility;
    SubgraphReservoir<Triplet> reservoir;
    THashMap<Pattern,Double> frequentPatterns;
    int N; // total number of subgraphs
    int M; // maximum reservoir size
    int Ncurrent;
    int c1;
    int c2;
    double dis=0.0;
    double beita=1.0;
    double numi=0.0;
    double num2=0.0;
    int num3;
    int flag1=0;
    double ep=10;
    int a[];
    public FullyDynamicSubgraphReservoirThreeNode(int size, int k ) {
        this.nodeMap = new NodeMap();
        utility = new EdgeHandler();
        reservoir = new SubgraphReservoir<Triplet>();
        N = 0;
        M = size;
        c1=0;
        Ncurrent = 0 ;
        c2=0;
        frequentPatterns = new THashMap<>();
    }

    @Override
    public boolean addEdge(StreamEdge edge) {
        if(nodeMap.contains(edge)) {
            return false;
        }
        //System.out.println("+" + edge);
        LabeledNode src = new LabeledNode(edge.getSource(), edge.getSrcLabel());
        LabeledNode dst = new LabeledNode(edge.getDestination(),edge.getDstLabel());

        THashSet<LabeledNode> srcNeighbor = nodeMap.getNeighbors(src);
        THashSet<LabeledNode> dstNeighbor = nodeMap.getNeighbors(dst);

        SetFunctions<LabeledNode> functions = new SetFunctions<LabeledNode>();
        Set<LabeledNode> common = functions.intersectionSet(srcNeighbor, dstNeighbor);

        for(LabeledNode t: srcNeighbor) {
            if(!common.contains(t)) {
                Triplet triplet = new Triplet(src, dst, t,edge, new StreamEdge(src.getVertexId(), src.getVertexLabel(), t.getVertexId(), t.getVertexLabel()));
                addSubgraph(triplet);
            }
        }

        for(LabeledNode t: dstNeighbor) {
            if(!common.contains(t)) {
                Triplet triplet = new Triplet(src, dst, t,edge, new StreamEdge(dst.getVertexId(), dst.getVertexLabel(), t.getVertexId() , t.getVertexLabel()));
                addSubgraph(triplet);
            }else {
                LabeledNode a = src;
                LabeledNode b = dst;
                LabeledNode c = t;
                StreamEdge edgeA = edge;
                StreamEdge edgeB = new StreamEdge(t.getVertexId() , t.getVertexLabel(), src.getVertexId(), src.getVertexLabel());
                StreamEdge edgeC = new StreamEdge(t.getVertexId(), t.getVertexLabel(), dst.getVertexId(), dst.getVertexLabel());

                Triplet tripletWedge = new Triplet(a, b, c, edgeB, edgeC );
                if(reservoir.contains(tripletWedge)) {
                    Triplet tripletTriangle = new Triplet(a, b, c,edgeA, edgeB, edgeC );
                    replaceSubgraphs(tripletWedge, tripletTriangle);
                }
            }
        }
        utility.handleEdgeAddition(edge, nodeMap);
        return false;
    }
    @Override
    public boolean removeEdge(StreamEdge edge) {
        //System.out.println("-" + edge);
        if(!nodeMap.contains(edge)) {
            return false;
        }
        utility.handleEdgeDeletion(edge, nodeMap);

        LabeledNode src = new LabeledNode(edge.getSource(), edge.getSrcLabel());
        LabeledNode dst = new LabeledNode(edge.getDestination(),edge.getDstLabel());

        THashSet<LabeledNode> srcNeighbor = nodeMap.getNeighbors(src);
        THashSet<LabeledNode> dstNeighbor = nodeMap.getNeighbors(dst);

        SetFunctions<LabeledNode> functions = new SetFunctions<LabeledNode>();
        Set<LabeledNode> common = functions.intersectionSet(srcNeighbor, dstNeighbor);

        for(LabeledNode t: srcNeighbor) {
            if(!common.contains(t)) {
                Triplet triplet = new Triplet(src, dst, t,edge, new StreamEdge(src.getVertexId(), src.getVertexLabel(), t.getVertexId(), t.getVertexLabel()));
                removeSubgraph(triplet);
            }
        }

        for(LabeledNode t: dstNeighbor) {
            if(!common.contains(t)) {
                Triplet triplet = new Triplet(src, dst, t,edge, new StreamEdge(dst.getVertexId(),dst.getVertexLabel(), t.getVertexId(), t.getVertexLabel()));
                removeSubgraph(triplet);
            }else {
                LabeledNode a = src;
                LabeledNode b = dst;
                LabeledNode c = t;
                StreamEdge edgeA = edge;
                StreamEdge edgeB = new StreamEdge(c.getVertexId(), c.getVertexLabel(), src.getVertexId(), src.getVertexLabel());
                StreamEdge edgeC = new StreamEdge(c.getVertexId(), c.getVertexLabel(), dst.getVertexId(), dst.getVertexLabel());

                Triplet tripletWedge = new Triplet(a, b, c, edgeB, edgeC );
                Triplet tripletTriangle = new Triplet(a, b, c,edgeA, edgeB, edgeC );
                if(reservoir.contains(tripletTriangle))
                    replaceSubgraphs(tripletTriangle, tripletWedge);
            }
        }
        //System.out.println(reservoir.size());
        return false;
    }
    void removeSubgraph(Triplet t) {
        if(reservoir.contains(t)) {
            //System.out.println("remove called from remove subgraph");
            reservoir.remove(t);
            removeFrequentPattern(t);
            c1++;
        }else
            c2++;

        Ncurrent--;
    }

    void addSubgraph(Triplet t) {
        N++;
        Ncurrent++;

        boolean flag = false;
        if (c1+c2 ==0) {
            if(reservoir.size() < M ) {
                flag = true;
            }else if (0.28  < (M/(double)N)) {
                //Math.random()是阈值
                //Math.random()+laplace(ep/2,1) < (M/(double)N)+laplace(ep/2,1)//改进的地方
                // 在此处加入差分隐私判断其是否为频繁诱导子图
                flag = true;
                //System.out.println("remove called from add subgraph");
                Triplet temp = reservoir.getRandom();
                reservoir.remove(temp);
                removeFrequentPattern(temp);
            }
        }else {
            int d = c1+c2;
            if (Math.random() < (c1/(double)(d))) {
                flag = true;
                c1--;
            }else {
                c2--;
            }
        }

        if(flag) {
            reservoir.add(t);
            addFrequentPattern(t);
            //System.out.println("reservoir size after add method " + reservoir.size());
        }
    }

    //remove a and add b
    void replaceSubgraphs(Triplet a, Triplet b) {
        reservoir.remove(a);
        removeFrequentPattern(a);
        reservoir.add(b);
        addFrequentPattern(b);

    }

    int addFrequentPattern(Triplet t) {
        //int flag1=0;
        numi=0;
        num3=0;
        THashMap<Pattern, Double> FP2 = new THashMap<Pattern, Double>();
        List<Pattern> patterns2 = new ArrayList<Pattern>(frequentPatterns.keySet());
        for (Pattern p2 : patterns2) {
            FP2.put(p2, frequentPatterns.get(p2));
        }
        ThreeNodeGraphPattern p = new ThreeNodeGraphPattern(t);
        if (frequentPatterns.containsKey(p)) {
            Double count = frequentPatterns.get(p);
            frequentPatterns.put(p, count + 1);
        } else {
            frequentPatterns.put(p, 1.0);
        }
        THashMap<Pattern, Double> FP1 = new THashMap<Pattern, Double>();
        List<Pattern> patterns1 = new ArrayList<Pattern>(frequentPatterns.keySet());
        for (Pattern p1 : patterns1) {
            FP1.put(p1, frequentPatterns.get(p1));
        }

        //计算dis
        List<Pattern> patterns3 = new ArrayList<Pattern>(FP2.keySet());
        for(Pattern p3: patterns3) {
            if(FP1.containsKey(p3)){
                num2=num2+Math.abs(FP1.get(p3)-FP2.get(p3));
                num3++;
            }else{
                num2=num2+FP2.get(p3)+FP1.get(p3);
            }
        }

        FP1.putAll(FP2);
        List<Pattern> patterns4 = new ArrayList<Pattern>(FP1.keySet());
        for(Pattern p4: patterns4) {
            numi++;
        }

        if(numi!=0) {
            dis = div(num2, numi, 2).doubleValue();
            if(dis<beita){
                flag1=0;
                //removeFrequentPattern(t);
            }else {flag1=1;}
        }

        return flag1;
    }
    public int [] p(){
        int pp=0;
        if(flag1==0){
            pp=0;
        }else{
            pp=1;
        }
        int pp1[]={pp,num3};//覆盖
        return pp1;
    }
    void removeFrequentPattern(Triplet t) {
        ThreeNodeGraphPattern p = new ThreeNodeGraphPattern(t);
        if(frequentPatterns.containsKey(p)) {
            Double count = frequentPatterns.get(p);
            if(count >1)
                frequentPatterns.put(p, count-1);
            else
                frequentPatterns.remove(p);
        }
    }

    @Override
        public THashMap<Pattern, Double> getFrequentPatterns() {
        return this.frequentPatterns;
    }
    @Override
    public THashMap<Pattern, Double> correctEstimates() {
        THashMap<Pattern, Double> correctFrequentPatterns = new THashMap<Pattern, Double>();
        double correctFactor = correctFactor();
        List<Pattern> patterns = new ArrayList<Pattern>(frequentPatterns.keySet());
        for(Pattern p: patterns) {
            Double count = frequentPatterns.get(p);
            double value = count*correctFactor;

            correctFrequentPatterns.put(p, (Double) value);
        }
        return correctFrequentPatterns;
    }

    private double  correctFactor() {
        return Math.max(1.0, ((double) Ncurrent/M));
    }

    @Override
    public int getNumberofSubgraphs() {
        return Ncurrent;
    }

    @Override
    public int getCurrentReservoirSize() {
        // TODO Auto-generated method stub
        return 0;
    }
    public static double laplace ( double pro, double k){//pro隐私预算，k敏感度
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
    //求除法
    public static BigDecimal div(double d1, double d2, int c) {// 进行除法运算
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.divide(b2,c,BigDecimal.ROUND_HALF_UP);
    }
}

