package tk.bridgersilk.asyncwc;

public enum GenerationMode {
	MINIMAL,
	DECREASED,
	ALL;

	public static GenerationMode from(String s) {
		return switch (s.toLowerCase()) {
			case "minimal" -> MINIMAL;
			case "decreased" -> DECREASED;
			case "all" -> ALL;
			default -> MINIMAL;
		};
	}
}
