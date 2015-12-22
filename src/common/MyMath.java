package common;

import java.util.ArrayList;

public class MyMath {

	public static double mean(ArrayList<Double> list){
		double sum = 0;
		int n = list.size();
		for (int i=0; i<n; i++){
			sum = sum + list.get(i);
		}
		return sum / n;
	}
	

	public static double innerProduct(ArrayList<Double> a1, ArrayList<Double> a2){
		double distance = 0;
		if (a1.size() != a2.size()){
			System.out.println("length dismatch");
		}
		for (int i=0; i<a1.size(); i++){
			distance = distance + a1.get(i)*a2.get(i);
		}
		return distance;
	}
	
	public static double innerProduct(double[] a1, double[] a2){
		double distance = 0;
		if (a1.length != a2.length){
			System.out.println("length dismatch");
		}
		for (int i=0; i<a1.length; i++){
			distance = distance + a1[i]*a2[i];
		}
		return distance;
	}
	
	public static double euclideanLength(ArrayList<Double> docWeights){
		double l = 0;
		for (int i=0; i<docWeights.size(); i++){
			l = l + docWeights.get(i);
		}
		return Math.sqrt(l);
	}
	
}
