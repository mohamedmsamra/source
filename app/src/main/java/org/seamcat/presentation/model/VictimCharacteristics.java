package org.seamcat.presentation.model;


public class VictimCharacteristics {
	
	private double victimBandwidth;
	private double frequencyOffset; 
	private boolean showACLR;
	private double interfererBandwidth;
	
	public VictimCharacteristics(double victimBandwidth, double frequencyOffset, boolean showACLR, double interfererBandwidth) {
	   this.victimBandwidth = victimBandwidth;
	   this.frequencyOffset = frequencyOffset;
	   this.showACLR = showACLR;
	   this.interfererBandwidth = interfererBandwidth;
   }
	
   public double getVictimBandwidth() {
   	return victimBandwidth;
   }

	
   public void setVictimBandwidth(double victimBandwidth) {
   	this.victimBandwidth = victimBandwidth;
   }

	
   public double getFrequencyOffset() {
   	return frequencyOffset;
   }

	
   public void setFrequencyOffset(double frequencyOffset) {
   	this.frequencyOffset = frequencyOffset;
   }

	
   public boolean isShowACLR() {
   	return showACLR;
   }

	
   public void setShowACLR(boolean showACLR) {
   	this.showACLR = showACLR;
   }

	
   public double getInterfererBandwidth() {
   	return interfererBandwidth;
   }

	
   public void setInterfererBandwidth(double interfererBandwidth) {
   	this.interfererBandwidth = interfererBandwidth;
   }	
}
