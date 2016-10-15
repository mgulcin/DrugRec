package eval;

public class EvaluationResult {
	Double tp;
	Double fp;
	Double tn;
	Double fn;
	
	Double precision;
	Double recall;
	Double f1;
	
	public EvaluationResult(Double tp, Double fp, Double tn, Double fn) {
		super();
		this.tp = tp;
		this.fp = fp;
		this.tn = tn;
		this.fn = fn;
		this.precision = precision(tp,fp);
		this.recall = recall(tp,fn);
		this.f1 = f1(tp,fp,fn);
	}
	

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(tp);
		builder.append(", ");
		builder.append(fp);
		builder.append(", ");
		builder.append(tn);
		builder.append(", ");
		builder.append(fn);
		builder.append(", ");
		builder.append(precision);
		builder.append(", ");
		builder.append(recall);
		builder.append(", ");
		builder.append(f1);
		return builder.toString();
	}





	public Double getTp() {
		return tp;
	}

	public void setTp(Double tp) {
		this.tp = tp;
	}

	public Double getFp() {
		return fp;
	}

	public void setFp(Double fp) {
		this.fp = fp;
	}

	public Double getTn() {
		return tn;
	}

	public void setTn(Double tn) {
		this.tn = tn;
	}

	public Double getFn() {
		return fn;
	}

	public void setFn(Double fn) {
		this.fn = fn;
	}
	
	public Double getPrecision() {
		return precision;
	}

	public void setPrecision(Double precision) {
		this.precision = precision;
	}

	public Double getRecall() {
		return recall;
	}

	public void setRecall(Double recall) {
		this.recall = recall;
	}

	public Double getF1() {
		return f1;
	}

	public void setF1(Double f1) {
		this.f1 = f1;
	}

	public Double precision(Double tp, Double fp){
		return tp/(tp+fp);
	}

	public Double recall(Double tp, Double fn){
		return tp/(tp+fn);
	}

	public Double accuracy(Double tp,Double fp, Double tn, Double fn){
		return (tp+tn)/(tp+tn+fp+fn);
	}

	public Double f1(Double tp, Double fp, Double fn){
		Double precision = precision(tp,fp);
		Double recall = recall(tp,fn);
		return (2*precision*recall)/(precision+recall);
	}
}
