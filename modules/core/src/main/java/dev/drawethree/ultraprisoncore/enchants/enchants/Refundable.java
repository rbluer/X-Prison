package dev.drawethree.ultraprisoncore.enchants.enchants;

public interface Refundable {

	boolean isRefundEnabled();

	int getRefundGuiSlot();

	double getRefundPercentage();
}
