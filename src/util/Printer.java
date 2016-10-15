/**
 * 
 */
package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import drugRelated.Item;
import eval.EvaluationResult;
import method.DrugRecommenderParameters;
import recRelated.Recommendation;


/**
 * @author mg
 *
 */
public abstract class Printer {
	public enum PrinterType
	{
		FILEPRINTER,
		CONSOLEPRINTER
	};
	
	// true --> print, false--> do not print
	protected boolean debugPrint = true;
	protected PrinterType type = null;
	
	
	/**
	 * @param debugPrint
	 * @param type
	 */
	public Printer(boolean debugPrint, PrinterType type ) {
		super();
		this.debugPrint = debugPrint;
		this.type = type;
	}

	abstract public void printString(String path, String str) ;
	
	abstract public void printMostSimilars(String path, String target,
			ArrayList<String> similars) ;


	abstract public void printRecommendeds(String recOutPath,
			String target, ArrayList<Recommendation> resultRecs);

	abstract public void printSet( String path, Set<String> targets);

	abstract public void printOverallEvalResult(String resultPath, 
			DrugRecommenderParameters parameters,
			EvaluationResult overallEvalRes);

	abstract public void printEvalResult(String evalResultPath,
			HashMap<Item, EvaluationResult> evalResultMap);

	abstract public void printOverallEvalResult(String resultPath, EvaluationResult overallEvalRes);

	abstract public void printDataTsv(String path, String[] diseaseNames, 
			Map<String, Integer[]> binaryMatrix) ;
}
