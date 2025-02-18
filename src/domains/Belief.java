package domains;

public interface Belief<value> {
	public value getMin(int player);
	public value getMax(int player);

}
