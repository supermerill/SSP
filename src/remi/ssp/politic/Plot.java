package remi.ssp.politic;

public class Plot {
	short x,y;
	//can be 5 if making a globe... be careful!
	public Plot[] around = new Plot[6]; // 0=  up, then direct direction
	Province province;
	byte positionInProvince; // 0= center, 1=  up, then direct direction
	
	public boolean isSea = false;

	public short getX() { return x; }
	public short getY() { return y; }
	public Plot[] getAround() { return around; }
	public Province getProvince() { return province; }
	public byte getPositionInProvince() { return positionInProvince; }
	public boolean isSea() { return isSea; }
	
}
