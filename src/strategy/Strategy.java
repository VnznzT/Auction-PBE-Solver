package strategy;

public interface Strategy<Value, Bid> {
	
	Bid getBid(Value v);
	
	Value getMaxValue();
	Value getMinValue();
}
