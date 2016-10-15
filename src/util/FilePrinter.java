package util;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import drugRelated.Item;
import eval.EvaluationResult;
import method.DrugRecommenderParameters;
import recRelated.Recommendation;


public class FilePrinter extends Printer{

	public FilePrinter(boolean debugVal) {
		super(debugVal,PrinterType.FILEPRINTER);
	}


	public void printRecommendeds(String path,
			String target, ArrayList<Recommendation> resultRecs) {
		try{
			//print  list to file
			FileOutputStream fos;
			fos = new FileOutputStream(path,true);
			PrintStream ps = new PrintStream(fos);

			ps.print(target + "\t");
			for(Recommendation rec:resultRecs){
				ps.print(rec.getRecommendedItem().getNames() + "\t");
			}
			ps.println();

			ps.flush();
			ps.close();
			fos.close();

		} catch(Exception e){
			e.printStackTrace();
		}

	}



	public void printMostSimilars(String path, String target,
			ArrayList<String> similars) {
		try{
			//print  list to file
			FileOutputStream fos;
			fos = new FileOutputStream(path,true);
			PrintStream ps = new PrintStream(fos);

			ps.print(target + ",");
			for(String sim:similars){
				ps.print(sim + ",");
			}
			ps.println();

			ps.flush();
			ps.close();
			fos.close();

		} catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void printString(String path, String str) {
		try{
			//print  list to file
			FileOutputStream fos;
			fos = new FileOutputStream(path,true);
			PrintStream ps = new PrintStream(fos);

			ps.println(str );

			ps.flush();
			ps.close();
			fos.close();

		} catch(Exception e){
			e.printStackTrace();
		}

	}


	@Override
	public void printSet(String path, Set<String> targets) {
		try{
			//print  list to file
			FileOutputStream fos;
			fos = new FileOutputStream(path,true);
			PrintStream ps = new PrintStream(fos);

			for(String target:targets){
				ps.println(target);
			}
			ps.flush();
			ps.close();
			fos.close();

		} catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void printEvalResult(String path, HashMap<Item, EvaluationResult> evalResultMap) {
		try{
			//print  list to file
			FileOutputStream fos;
			fos = new FileOutputStream(path,true);
			PrintStream ps = new PrintStream(fos);

			for(Entry<Item, EvaluationResult> e: evalResultMap.entrySet()){
				Item targetDrug = e.getKey();
				EvaluationResult evalResult = e.getValue();

				StringBuilder builder = new StringBuilder();
				builder.append(targetDrug.getNames());
				builder.append("\t");
				builder.append(evalResult.toString());
				builder.append("\t");

				ps.print(builder.toString());
				ps.println();

			}


			ps.flush();
			ps.close();

		} catch(Exception e){
			e.printStackTrace();
		}

	}


	@Override
	public void printOverallEvalResult(String path, 
			DrugRecommenderParameters parameters,
			EvaluationResult evalResult) {
		try{
			//print  list to file
			FileOutputStream fos;
			fos = new FileOutputStream(path,true);
			PrintStream ps = new PrintStream(fos);

			StringBuilder builder = new StringBuilder();
			builder.append(parameters.toString());
			builder.append("\t");
			builder.append(evalResult.toString());
			builder.append("\t");


			ps.print(builder.toString());
			ps.println();
			ps.flush();
			ps.close();
			fos.close();

		} catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void printOverallEvalResult(String path, 
			EvaluationResult evalResult) {
		try{
			//print  list to file
			FileOutputStream fos;
			fos = new FileOutputStream(path,true);
			PrintStream ps = new PrintStream(fos);

			StringBuilder builder = new StringBuilder();
			builder.append(evalResult.toString());
			builder.append("\t");


			ps.print(builder.toString());
			ps.println();
			ps.flush();
			ps.close();
			fos.close();

		} catch(Exception e){
			e.printStackTrace();
		}
		
	}

	@Override
	public void printDataTsv(String path, String[] diseaseNames, 
			Map<String, Integer[]> binaryMatrix) {
		try{
			//print  list to file
			FileOutputStream fos;
			fos = new FileOutputStream(path,true);
			PrintStream ps = new PrintStream(fos);

			StringBuilder builder = new StringBuilder();
			// write disease names
			builder.append("\t");// for the first column add an empty cell
			for(String disease: diseaseNames){
				builder.append(disease);
				builder.append("\t");
			}
			// go to next line
			builder.append("\n");
			
			// write the matrix 
			for(Entry<String, Integer[]> e:binaryMatrix.entrySet()){
				String drug = e.getKey();
				Integer[] vector = e.getValue();
				
				builder.append(drug);
				builder.append("\t");
				
				for(Integer val:vector){
					builder.append(val);
					builder.append("\t");
				}
				
				builder.append("\n");
			}
			


			ps.print(builder.toString());
			ps.println();
			ps.flush();
			ps.close();
			fos.close();

		} catch(Exception e){
			e.printStackTrace();
		}
	}


}
