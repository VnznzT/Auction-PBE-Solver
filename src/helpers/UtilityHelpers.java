package helpers;

public final class UtilityHelpers {
	
		private UtilityHelpers() {}; 

	public static double absoluteLoss(double oldu, double newu) {
		return newu - oldu;
	}
	
	public static double relativeLoss(double oldu, double newu) {
						
						
				return newu / oldu - 1;
	}
	
	public static double loss(double oldu, double newu, boolean useAbsolute) {
		if (useAbsolute) {
			return UtilityHelpers.absoluteLoss(oldu, newu);
		} else {
			return UtilityHelpers.relativeLoss(oldu, newu);
		}
	}
}
