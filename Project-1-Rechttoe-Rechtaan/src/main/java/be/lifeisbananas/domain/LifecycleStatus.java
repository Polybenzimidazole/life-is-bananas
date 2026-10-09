package be.lifeisbananas.domain;

/**
 * De levenscyclus die een receptuur en een product doorlopen.
 * <p>
 * De volgorde is bindend: er mag telkens maar één stap vooruit gezet worden.
 * Een receptuur gaat dus niet rechtstreeks van IN_DEVELOPMENT naar APPROVED.
 */
public enum LifecycleStatus {

	IN_DEVELOPMENT,
	TESTED,
	APPROVED,
	IN_PRODUCTION;

	/** Mag er van deze status naar {@code doel} overgegaan worden? */
	public boolean mayChangeTo(LifecycleStatus doel) {
		return doel != null && doel.ordinal() == this.ordinal() + 1;
	}

	/** De eerstvolgende status, of leeg als dit de laatste is. */
	public LifecycleStatus next() {
		return this == IN_PRODUCTION ? null : values()[ordinal() + 1];
	}
}
