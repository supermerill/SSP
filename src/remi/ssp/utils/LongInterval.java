package remi.ssp.utils;

public class LongInterval{
	public long min;
	public long max;
	public LongInterval(long min, long max) {
		super();
		this.min = min;
		this.max = max;
	}
	public LongInterval set(LongInterval other){
		min=other.min; max=other.max; return this;
	}
	public LongInterval set(long min, long max){ this.min=min; this.max=max; return this; }
	public LongInterval setMin(long min){ this.min=min; return this; }
	public LongInterval setMax(long max){ this.max=max; return this; }
	
	public long minmax(long value){ return Math.max(Math.min(value, max), min); }
	
}