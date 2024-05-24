package cn.javaex.mybatisjj.util;

public class StartupBannerUtils {

	private static final String BANNER =
            "  __  __       ____        _   _      _  _ \n" +
            " |  \\/  |_   _| __ )  __ _| |_(_)___ (_)(_)\n" +
            " | |\\/| | | | |  _ \\ / _` | __| / __|| || |\n" +
            " | |  | | |_| | |_) | (_| | |_| \\__ \\| || |\n" +
            " |_|  |_|\\__, |____/ \\__,_|\\__|_|___// |/ |\n" +
            "         |___/                      |_/__/  (v2.0.0)\n";
	
	public static void printBanner() {
		System.out.println(BANNER);
	}
	
}
