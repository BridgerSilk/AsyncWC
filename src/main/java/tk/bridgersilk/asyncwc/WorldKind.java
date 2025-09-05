package tk.bridgersilk.asyncwc;

public enum WorldKind {
	NORMAL,
	FLAT,
	VOID;

	public static WorldKind from(String s) {
		s = s.toLowerCase();
		return switch (s) {
			case "flat" -> FLAT;
			case "void" -> VOID;
			default -> NORMAL;
		};
	}
}